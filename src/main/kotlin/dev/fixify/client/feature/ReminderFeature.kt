package dev.fixify.client.feature

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.fixify.client.FixifyClient
import dev.fixify.client.render.FixifyNvgPipRenderer
import dev.fixify.client.render.FixifyNvgRenderer
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import net.minecraft.world.scores.DisplaySlot
import java.util.Locale
import kotlin.math.max

object ReminderFeature {
	private val dayPattern = Regex("""\b([A-Za-z][A-Za-z ]*?)\s+(\d{1,2})(?:st|nd|rd|th)\b""")

	@Volatile
	private var warningTitle = ""

	@Volatile
	private var warningSubtitle = ""

	@Volatile
	private var warningStartedAt = 0L

	@Volatile
	private var warningEndsAt = 0L

	private var ticks = 0
	private var currentDate = ""
	private var lastObservedDate = ""
	private var lastWarnedDate = ""

	fun register() {
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.HOTBAR,
			Identifier.fromNamespaceAndPath(FixifyClient.MOD_ID, "reminder"),
			::renderWarning,
		)
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			ticks++
			if (ticks % 20 == 0) {
				checkDate(client)
			}
		}
	}

	fun command(): LiteralArgumentBuilder<FabricClientCommandSource> {
		return LiteralArgumentBuilder.literal<FabricClientCommandSource>("reminder")
			.executes { context ->
				context.source.client.player?.sendSystemMessage(statusMessage())
				1
			}
			.then(
				LiteralArgumentBuilder.literal<FabricClientCommandSource>("status").executes { context ->
					context.source.client.player?.sendSystemMessage(statusMessage())
					1
				},
			)
			.then(
				LiteralArgumentBuilder.literal<FabricClientCommandSource>("test").executes { context ->
					val client = context.source.client
					showWarning("SKYBLOCK DAY", "Test warning")
					client.player?.sendSystemMessage(
						Component.literal("[Fixify] ").withStyle(ChatFormatting.LIGHT_PURPLE)
							.append(Component.literal("Triggered Reminder test.").withStyle(ChatFormatting.YELLOW)),
					)
					1
				},
			)
	}

	private fun checkDate(client: Minecraft) {
		if (!FixifyFeatures.reminderEnabled) {
			return
		}
		val date = findCurrentDate(client) ?: return
		currentDate = date.fullText
		if (date.fullText == lastObservedDate) {
			return
		}
		lastObservedDate = date.fullText

		val rule = FixifyFeatures.reminderRules.firstOrNull {
			it.enabled && date.day in parseDays(it.days) && it.command.isNotBlank()
		} ?: return
		if (date.fullText == lastWarnedDate) {
			return
		}

		lastWarnedDate = date.fullText
		showWarning("DAY ${date.day}", rule.name.ifBlank { date.fullText })
		sendReminderMessage(client, date, rule)
	}

	private fun findCurrentDate(client: Minecraft): SkyblockDate? {
		val level = client.level ?: return null
		val objective = level.scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return null
		for (score in level.scoreboard.listPlayerScores(objective)) {
			val owner = score.owner()
			val team = level.scoreboard.getPlayersTeam(owner)
			val component = score.display()
				?: team?.getFormattedName(Component.literal(owner))
				?: score.ownerName()
			parseDate(component.string)?.let { return it }
		}
		return null
	}

	internal fun parseDate(line: String): SkyblockDate? {
		val match = dayPattern.find(line.trim()) ?: return null
		val day = match.groupValues[2].toIntOrNull()?.takeIf { it in 1..31 } ?: return null
		return SkyblockDate(match.value.trim(), day)
	}

	internal fun parseDays(value: String): Set<Int> {
		val days = linkedSetOf<Int>()
		for (part in value.split(',')) {
			val trimmed = part.trim()
			if (trimmed.isEmpty()) {
				continue
			}
			val range = trimmed.split('-', limit = 2)
			if (range.size == 2) {
				val start = range[0].trim().toIntOrNull()
				val end = range[1].trim().toIntOrNull()
				if (start != null && end != null) {
					for (day in minOf(start, end)..maxOf(start, end)) {
						if (day in 1..31) {
							days.add(day)
						}
					}
				}
			} else {
				trimmed.toIntOrNull()?.takeIf { it in 1..31 }?.let(days::add)
			}
		}
		return days
	}

	private fun sendReminderMessage(
		client: Minecraft,
		date: SkyblockDate,
		rule: FixifyFeatures.ReminderRule,
	) {
		val player = client.player ?: return
		val prefix = Component.literal("[Fixify] ").withStyle(ChatFormatting.LIGHT_PURPLE)
		val message = prefix.append(
			Component.literal("${date.fullText}: ${rule.name.ifBlank { "Reminder" }} ")
				.withStyle(ChatFormatting.YELLOW),
		)
		if (FixifyFeatures.reminderChatButton) {
			val command = normalizeCommand(rule.command)
			val label = command.removePrefix("/").uppercase(Locale.ROOT)
			message.append(
				Component.literal("[$label]").withStyle(
					Style.EMPTY
						.withColor(ChatFormatting.GOLD)
						.withBold(true)
						.withClickEvent(ClickEvent.RunCommand(command))
						.withHoverEvent(HoverEvent.ShowText(Component.literal("Click to run $command"))),
				),
			)
		}
		player.sendSystemMessage(message)
	}

	private fun showWarning(title: String, subtitle: String) {
		val now = System.currentTimeMillis()
		warningTitle = title
		warningSubtitle = subtitle
		warningStartedAt = now
		warningEndsAt = now + FixifyFeatures.reminderWarningDuration * 1000L
	}

	private fun renderWarning(graphics: GuiGraphicsExtractor, ignored: net.minecraft.client.DeltaTracker) {
		val now = System.currentTimeMillis()
		if (!FixifyFeatures.reminderEnabled || now >= warningEndsAt) {
			return
		}
		val duration = (warningEndsAt - warningStartedAt).coerceAtLeast(1L)
		val remaining = ((warningEndsAt - now).toFloat() / duration).coerceIn(0.0f, 1.0f)
		val scale = FixifyFeatures.reminderWarningScale
		val width = graphics.guiWidth().toFloat()
		val height = graphics.guiHeight().toFloat()

		FixifyNvgPipRenderer.draw(graphics, 0, 0, graphics.guiWidth(), graphics.guiHeight()) {
			val titleWidth = FixifyNvgRenderer.textWidth(warningTitle, 14.0f)
			val subtitleWidth = FixifyNvgRenderer.textWidth(warningSubtitle, 9.0f)
			val panelWidth = max(132.0f, max(titleWidth, subtitleWidth) + 34.0f)
			val panelHeight = 48.0f
			val x = width / 2.0f - panelWidth * scale / 2.0f
			val y = height / 3.0f - panelHeight * scale / 2.0f

			FixifyNvgRenderer.push()
			FixifyNvgRenderer.translate(x, y)
			FixifyNvgRenderer.scale(scale, scale)
			FixifyNvgRenderer.roundedRect(0.0f, 0.0f, panelWidth, panelHeight, 8.0f, 0xE514141C.toInt())
			FixifyNvgRenderer.roundedOutline(0.5f, 0.5f, panelWidth - 1.0f, panelHeight - 1.0f, 8.0f, 1.0f, 0x88AAA4FF.toInt())
			FixifyNvgRenderer.gradientRect(
				5.0f,
				5.0f,
				3.0f,
				panelHeight - 10.0f,
				1.5f,
				0xFFAAA4FF.toInt(),
				0xFF6578FF.toInt(),
				true,
			)
			centerText(warningTitle, panelWidth / 2.0f, 8.0f, 14.0f, 0xFFFFFFFF.toInt())
			centerText(warningSubtitle, panelWidth / 2.0f, 27.0f, 9.0f, 0xFFCAC7D8.toInt())
			FixifyNvgRenderer.roundedRect(12.0f, panelHeight - 5.0f, panelWidth - 24.0f, 2.0f, 1.0f, 0x443A3946)
			FixifyNvgRenderer.gradientRect(
				12.0f,
				panelHeight - 5.0f,
				(panelWidth - 24.0f) * remaining,
				2.0f,
				1.0f,
				0xFFAAA4FF.toInt(),
				0xFF6578FF.toInt(),
				false,
			)
			FixifyNvgRenderer.pop()
		}
	}

	private fun centerText(text: String, centerX: Float, y: Float, size: Float, color: Int) {
		val textWidth = FixifyNvgRenderer.textWidth(text, size)
		FixifyNvgRenderer.text(text, centerX - textWidth / 2.0f, y, size, color)
	}

	private fun statusMessage(): Component {
		val activeRules = FixifyFeatures.reminderRules.count { it.enabled && it.command.isNotBlank() }
		return Component.literal("[Reminder Status]\n").withStyle(ChatFormatting.LIGHT_PURPLE)
			.append(
				Component.literal("Current Date: ${currentDate.ifBlank { "Unknown" }}\n")
					.withStyle(ChatFormatting.GRAY),
			)
			.append(
				Component.literal("Last Notified: ${lastWarnedDate.ifBlank { "None" }}\n")
					.withStyle(ChatFormatting.GRAY),
			)
			.append(Component.literal("Active Reminders: $activeRules").withStyle(ChatFormatting.GRAY))
	}

	private fun normalizeCommand(value: String): String {
		val command = value.trim()
		return if (command.startsWith('/')) command else "/$command"
	}

	internal data class SkyblockDate(val fullText: String, val day: Int)
}
