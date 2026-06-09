package dev.fixify.client.feature

import dev.fixify.client.FixifyClient
import dev.fixify.client.FixifyHudWidget
import dev.fixify.client.render.FixifyNvgPipRenderer
import dev.fixify.client.render.FixifyNvgRenderer
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.resources.Identifier
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object SkyblockHudRenderer {
	data class Bounds(val x: Float, val y: Float, val width: Float, val height: Float)

	private var animatedPressure = 0.0f
	private var animatedFuel = 0.0f
	private var animatedScore = 0.0f
	private var animatedPetXp = 0.0f
	private var animatedPetLevel = 0.0f

	fun register() {
		HudElementRegistry.attachElementBefore(
			VanillaHudElements.CROSSHAIR,
			Identifier.fromNamespaceAndPath(FixifyClient.MOD_ID, "low_hp_indicator"),
		::extractLowHealth,
		)
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.HOTBAR,
			Identifier.fromNamespaceAndPath(FixifyClient.MOD_ID, "skyblock_widgets"),
			::extractWidgets,
		)
	}

	fun renderEditor(graphics: GuiGraphicsExtractor, widget: FixifyHudWidget) {
		renderWidgets(graphics, widget)
		val bounds = bounds(widget, graphics.guiWidth(), graphics.guiHeight())
		graphics.outline(
			bounds.x.toInt() - 2,
			bounds.y.toInt() - 2,
			bounds.width.toInt() + 4,
			bounds.height.toInt() + 4,
			0x88FFFFFF.toInt(),
		)
	}

	fun bounds(widget: FixifyHudWidget, screenWidth: Int, screenHeight: Int): Bounds {
		val (width, height) = baseSize(widget)
		val scale = scale(widget)
		val scaledWidth = width * scale
		val scaledHeight = height * scale
		val (anchorX, anchorY) = anchoredPosition(
			anchor(widget),
			screenWidth.toFloat(),
			screenHeight.toFloat(),
			scaledWidth,
			scaledHeight,
		)
		return Bounds(
			anchorX + offsetX(widget),
			anchorY + offsetY(widget),
			scaledWidth,
			scaledHeight,
		)
	}

	fun updateLayout(widget: FixifyHudWidget, x: Int, y: Int, scale: Float) {
		when (widget) {
			FixifyHudWidget.PET -> {
				FixifyFeatures.petOverlayX = x
				FixifyFeatures.petOverlayY = y
				FixifyFeatures.petOverlayScale = scale
			}

			FixifyHudWidget.PRESSURE -> {
				FixifyFeatures.pressureDisplayX = x
				FixifyFeatures.pressureDisplayY = y
				FixifyFeatures.pressureDisplayScale = scale
			}

			FixifyHudWidget.DRILL_FUEL -> {
				FixifyFeatures.drillFuelMeterX = x
				FixifyFeatures.drillFuelMeterY = y
				FixifyFeatures.drillFuelMeterScale = scale
			}

			FixifyHudWidget.DUNGEON_SCORE -> {
				FixifyFeatures.dungeonScoreMeterX = x
				FixifyFeatures.dungeonScoreMeterY = y
				FixifyFeatures.dungeonScoreMeterScale = scale
			}

			FixifyHudWidget.PERFORMANCE -> {
				FixifyFeatures.performanceHudX = x
				FixifyFeatures.performanceHudY = y
				FixifyFeatures.performanceHudScale = scale
			}
		}
	}

	fun currentOffset(widget: FixifyHudWidget): Pair<Int, Int> = offsetX(widget).toInt() to offsetY(widget).toInt()

	fun currentScale(widget: FixifyHudWidget): Float = scale(widget)

	fun resetLayout(widget: FixifyHudWidget) {
		when (widget) {
			FixifyHudWidget.PET -> updateLayout(widget, 65, -40, 1.0f)
			FixifyHudWidget.PRESSURE -> updateLayout(widget, -90, -55, 1.0f)
			FixifyHudWidget.DRILL_FUEL -> updateLayout(widget, -120, -65, 1.0f)
			FixifyHudWidget.DUNGEON_SCORE -> updateLayout(widget, -160, -50, 1.0f)
			FixifyHudWidget.PERFORMANCE -> updateLayout(widget, 8, 8, 1.0f)
		}
	}

	fun persistLayout(widget: FixifyHudWidget) {
		val (x, y) = currentOffset(widget)
		FixifyFeatures.saveHudLayout(widget.configPrefix, x, y, currentScale(widget))
	}

	private fun extractLowHealth(graphics: GuiGraphicsExtractor, ignored: net.minecraft.client.DeltaTracker) {
		if (!FixifyFeatures.lowHpIndicatorEnabled || !SkyblockDataTracker.shouldRender()) {
			return
		}
		val health = SkyblockDataTracker.health.coerceIn(0.0f, 1.0f)
		if (health >= 0.5f) {
			return
		}

		val danger = ((0.5f - health) / 0.5f).coerceIn(0.0f, 1.0f)
		val pulse = if (FixifyFeatures.lowHpIndicatorHeartbeat) {
			val progress = (System.currentTimeMillis() % 1000L) / 1000.0f
			1.0f - progress * progress
		} else {
			1.0f
		}
		val alpha = (170.0f * FixifyFeatures.lowHpIndicatorTransparency * danger * (1.0f + pulse * 0.5f))
			.toInt()
			.coerceIn(0, 255)
		val strong = withAlpha(0xFF0000, alpha)
		val weak = withAlpha(0xFF0000, alpha / 4)
		val width = graphics.guiWidth().toFloat()
		val height = graphics.guiHeight().toFloat()
		val edge = (width.coerceAtMost(height) * 0.24f).coerceAtLeast(48.0f)

		FixifyNvgPipRenderer.draw(graphics, 0, 0, graphics.guiWidth(), graphics.guiHeight()) {
			FixifyNvgRenderer.gradientRect(0.0f, 0.0f, edge, height, 0.0f, strong, weak, false)
			FixifyNvgRenderer.gradientRect(width - edge, 0.0f, edge, height, 0.0f, weak, strong, false)
			FixifyNvgRenderer.gradientRect(0.0f, 0.0f, width, edge, 0.0f, strong, weak, true)
			FixifyNvgRenderer.gradientRect(0.0f, height - edge, width, edge, 0.0f, weak, strong, true)
		}
	}

	private fun extractWidgets(graphics: GuiGraphicsExtractor, ignored: net.minecraft.client.DeltaTracker) {
		renderWidgets(graphics, null)
	}

	private fun renderWidgets(graphics: GuiGraphicsExtractor, previewWidget: FixifyHudWidget?) {
		val preview = previewWidget != null
		val renderSkyblockWidgets = preview || SkyblockDataTracker.shouldRender()
		val renderPerformance = previewWidget == FixifyHudWidget.PERFORMANCE ||
			(!preview && FixifyFeatures.performanceHudEnabled)
		if (!renderSkyblockWidgets && !renderPerformance) {
			return
		}

		val now = System.currentTimeMillis()
		val visible = ArrayList<FixifyHudWidget>(5)
		if (
			(previewWidget == FixifyHudWidget.PET) ||
			(!preview && renderSkyblockWidgets && FixifyFeatures.petOverlayEnabled && SkyblockDataTracker.petActive)
		) {
			visible.add(FixifyHudWidget.PET)
		}
		if (
			(previewWidget == FixifyHudWidget.PRESSURE) ||
			(!preview && renderSkyblockWidgets && FixifyFeatures.pressureDisplayEnabled && SkyblockDataTracker.inWater &&
				SkyblockDataTracker.pressure >= FixifyFeatures.pressureDisplayShowAt)
		) {
			visible.add(FixifyHudWidget.PRESSURE)
		}
		if (
			(previewWidget == FixifyHudWidget.DRILL_FUEL) ||
			(!preview && renderSkyblockWidgets && FixifyFeatures.drillFuelMeterEnabled &&
				now - SkyblockDataTracker.lastFuelSeenAt < 1400L)
		) {
			visible.add(FixifyHudWidget.DRILL_FUEL)
		}
		if (
			(previewWidget == FixifyHudWidget.DUNGEON_SCORE) ||
			(!preview && renderSkyblockWidgets && FixifyFeatures.dungeonScoreMeterEnabled && SkyblockDataTracker.inDungeon)
		) {
			visible.add(FixifyHudWidget.DUNGEON_SCORE)
		}
		if (renderPerformance) {
			visible.add(FixifyHudWidget.PERFORMANCE)
		}
		if (visible.isEmpty()) {
			return
		}

		updateAnimations(preview)
		FixifyNvgPipRenderer.draw(graphics, 0, 0, graphics.guiWidth(), graphics.guiHeight()) {
			for (widget in visible) {
				val bounds = bounds(widget, graphics.guiWidth(), graphics.guiHeight())
				FixifyNvgRenderer.push()
				FixifyNvgRenderer.translate(bounds.x, bounds.y)
				FixifyNvgRenderer.scale(scale(widget), scale(widget))
				when (widget) {
					FixifyHudWidget.PET -> drawPet(preview)
					FixifyHudWidget.PRESSURE -> drawPressure(preview)
					FixifyHudWidget.DRILL_FUEL -> drawFuel(preview)
					FixifyHudWidget.DUNGEON_SCORE -> drawDungeonScore(preview)
					FixifyHudWidget.PERFORMANCE -> drawPerformance()
				}
				FixifyNvgRenderer.pop()
			}
		}

		if (FixifyHudWidget.PET in visible) {
			drawPetItems(graphics, preview)
		}
	}

	private fun updateAnimations(preview: Boolean) {
		val pressure = if (preview) 0.64f else SkyblockDataTracker.pressure
		val fuel = if (preview) 0.72f else SkyblockDataTracker.fuelProgress()
		val score = if (preview) 286.0f else SkyblockDataTracker.dungeonScore
		val petXp = if (preview) 0.63f else SkyblockDataTracker.petXp
		val petLevel = if (preview) 0.78f else {
			SkyblockDataTracker.petLevel.toFloat() / SkyblockDataTracker.petMaxLevel.coerceAtLeast(1)
		}
		animatedPressure = lerp(animatedPressure, pressure)
		animatedFuel = lerp(animatedFuel, fuel)
		animatedScore = lerp(animatedScore, score)
		if (FixifyFeatures.petOverlayValueAnimation) {
			animatedPetXp = lerp(animatedPetXp, petXp)
			animatedPetLevel = lerp(animatedPetLevel, petLevel)
		} else {
			animatedPetXp = petXp
			animatedPetLevel = petLevel
		}
	}

	private fun drawPet(preview: Boolean) {
		val type = FixifyFeatures.petOverlayType
		val circular = type >= 2
		val alt = type % 2 == 1
		val colors = petColors()
		var levelColor = colors.first
		var xpColor = colors.second
		val background = if (FixifyFeatures.petOverlayRainbowBackground) rainbow(0.66f) else colors.third
		if (FixifyFeatures.petOverlayInvert) {
			val swap = levelColor
			levelColor = xpColor
			xpColor = swap
		}
		if (FixifyFeatures.petOverlayRainbowLevel) {
			levelColor = rainbow(0.0f)
		}
		if (FixifyFeatures.petOverlayRainbowXp) {
			xpColor = rainbow(0.33f)
		}

		val hover = if (FixifyFeatures.petOverlayIdleHover) {
			(sin(System.currentTimeMillis() / 2700.0 * PI * 2.0) * 0.7).toFloat()
		} else {
			0.0f
		}
		FixifyNvgRenderer.translate(0.0f, hover)

		if (circular) {
			val cx = 22.0f
			val cy = 22.0f
			if (FixifyFeatures.petOverlayIdlePulse) {
				val pulse = ((System.currentTimeMillis() % 1700L) / 1700.0f)
				FixifyNvgRenderer.arc(cx, cy, 15.0f + pulse * 4.0f, 0.0f, (PI * 2.0).toFloat(), 1.0f, withAlpha(levelColor, ((1.0f - pulse) * 90).toInt()))
			}
			FixifyNvgRenderer.circle(cx, cy, 14.0f, background)
			FixifyNvgRenderer.arc(cx, cy, 12.5f, -PI.toFloat() / 2.0f, -PI.toFloat() / 2.0f + animatedPetLevel * PI.toFloat() * 2.0f, if (alt) 2.0f else 3.0f, levelColor)
			FixifyNvgRenderer.arc(cx, cy, 9.0f, -PI.toFloat() / 2.0f, -PI.toFloat() / 2.0f + animatedPetXp * PI.toFloat() * 2.0f, if (alt) 3.0f else 6.0f, xpColor)
			centerText(petLevelText(preview), 22.0f, 47.0f, 8.0f, levelColor)
			centerText(petXpText(preview), 22.0f, 56.0f, 7.0f, xpColor)
		} else {
			val iconOnRight = FixifyFeatures.petOverlayFlip
			val barX = if (iconOnRight) 0.0f else 23.0f
			if (FixifyFeatures.petOverlayIdlePulse) {
				val pulse = ((System.currentTimeMillis() % 1700L) / 1700.0f)
				FixifyNvgRenderer.roundedOutline(barX - pulse * 3.0f, 15.0f - pulse * 2.0f, 52.0f + pulse * 6.0f, 10.0f + pulse * 4.0f, 6.0f, 1.0f, withAlpha(levelColor, ((1.0f - pulse) * 100).toInt()))
			}
			FixifyNvgRenderer.roundedRect(barX, 16.0f, 52.0f, 9.0f, if (alt) 3.0f else 5.0f, background)
			FixifyNvgRenderer.roundedRect(barX, 16.0f, (52.0f * animatedPetLevel).coerceAtLeast(8.0f), 9.0f, if (alt) 3.0f else 5.0f, levelColor)
			FixifyNvgRenderer.roundedRect(barX + 2.0f, 18.0f, (48.0f * animatedPetXp).coerceAtLeast(2.0f), 5.0f, 2.0f, xpColor)
			centerText(petLevelText(preview), barX + 26.0f, 1.0f, 8.5f, levelColor)
			centerText(petXpText(preview), barX + 26.0f, 27.0f, 8.0f, xpColor)
		}
	}

	private fun drawPressure(preview: Boolean) {
		val colors = if (FixifyFeatures.pressureDisplayTheme == 1) {
			Triple(0xFFFFDAB9.toInt(), 0xFFF0A080.toInt(), 0xFFAC5F4A.toInt())
		} else {
			Triple(0xFFAFAFAF.toInt(), 0xFF3D3D41.toInt(), 0xFF1D1D21.toInt())
		}
		val cx = 17.0f
		val cy = 27.0f
		FixifyNvgRenderer.circle(cx, cy, 14.0f, colors.second)
		FixifyNvgRenderer.circle(cx, cy, 12.5f, colors.third)
		val start = Math.toRadians(135.0).toFloat()
		val sweep = Math.toRadians(270.0).toFloat()
		for (i in 0..8) {
			val angle = start + sweep * i / 8.0f
			val x1 = cx + cos(angle) * 8.0f
			val y1 = cy + sin(angle) * 8.0f
			val x2 = cx + cos(angle) * 11.0f
			val y2 = cy + sin(angle) * 11.0f
			FixifyNvgRenderer.line(x1, y1, x2, y2, if (i == 8) 1.2f else 0.7f, if (i == 8) 0xFF993333.toInt() else withAlpha(colors.second, 180))
		}
		val needle = start + sweep * animatedPressure
		FixifyNvgRenderer.line(cx, cy, cx + cos(needle) * 10.0f, cy + sin(needle) * 10.0f, 1.4f, colors.first)
		FixifyNvgRenderer.circle(cx, cy, 2.0f, colors.second)
		centerText("${((if (preview) 0.64f else SkyblockDataTracker.pressure) * 100).toInt()}%", cx, 1.0f, 9.0f, 0xFFFFFFFF.toInt())
	}

	private fun drawFuel(preview: Boolean) {
		val colors = if (FixifyFeatures.drillFuelMeterTheme == 1) {
			Triple(0xFFA7BFEF.toInt(), 0xFF354143.toInt(), 0xFF152527.toInt())
		} else {
			Triple(0xFF77FF77.toInt(), 0xFF224A22.toInt(), 0xFF102210.toInt())
		}
		FixifyNvgRenderer.roundedRect(1.0f, 11.0f, 22.0f, 42.0f, 6.0f, colors.third)
		FixifyNvgRenderer.roundedRect(3.0f, 13.0f, 18.0f, 38.0f, 4.0f, colors.second)
		val fillHeight = 38.0f * animatedFuel
		if (fillHeight > 0.0f) {
			FixifyNvgRenderer.roundedRect(3.0f, 51.0f - fillHeight, 18.0f, fillHeight, 4.0f, withAlpha(colors.first, 220))
			val wave = sin(System.currentTimeMillis() / 350.0).toFloat() * 1.5f
			FixifyNvgRenderer.line(4.0f, 51.0f - fillHeight + wave, 20.0f, 51.0f - fillHeight - wave, 1.0f, colors.first)
		}
		val value = if (preview) 72.0f else animatedFuel * 100.0f
		centerText(if (value >= 10.0f) "%.0f%%".format(value) else "%.1f%%".format(value), 12.0f, 0.0f, 8.0f, colors.first)
	}

	private fun drawDungeonScore(preview: Boolean) {
		val score = if (preview && animatedScore < 1.0f) 286.0f else animatedScore.coerceIn(0.0f, 305.0f)
		val (label, rankColor) = scoreRank(score)
		val cx = 20.0f
		val cy = 20.0f
		FixifyNvgRenderer.circle(cx, cy, 19.0f, 0xA0000000.toInt())
		val slices = floatArrayOf(99.0f, 60.0f, 70.0f, 40.4f, 29.6f, 6.0f)
		val colors = intArrayOf(0xFFFC0000.toInt(), 0xFF3F3FFD.toInt(), 0xFF7FCC19.toInt(), 0xFF7F3FB2.toInt(), 0xFFBEBE22.toInt(), 0xFFF9ED4C.toInt())
		var start = -PI.toFloat() / 2.0f
		for (i in slices.indices) {
			val sweep = slices[i] / 305.0f * PI.toFloat() * 2.0f
			FixifyNvgRenderer.arc(cx, cy, 15.0f, start + 0.01f, start + sweep - 0.01f, 1.7f, colors[i])
			start += sweep
		}
		val progressColor = if (FixifyFeatures.dungeonScoreMeterTheme == 1) {
			mixColor(
				FixifyFeatures.dungeonScoreGradientColor1.argb,
				FixifyFeatures.dungeonScoreGradientColor2.argb,
				((score / 305.0f + FixifyFeatures.dungeonScoreMeterGradientRotation) % 1.0f),
			)
		} else {
			rankColor
		}
		FixifyNvgRenderer.arc(
			cx,
			cy,
			17.0f,
			-PI.toFloat() / 2.0f,
			-PI.toFloat() / 2.0f + score / 305.0f * PI.toFloat() * 2.0f,
			3.0f,
			progressColor,
		)
		centerText(label, cx, 8.0f, 10.0f, rankColor)
		centerText("%.0f".format(score), cx, 21.0f, 8.0f, 0xCFFFFFFF.toInt())
	}

	private fun drawPerformance() {
		val metrics = performanceMetrics()
		if (metrics.isEmpty()) {
			return
		}
		val (width, height) = baseSize(FixifyHudWidget.PERFORMANCE)
		val nameColor = FixifyFeatures.performanceHudNameColor.argb
		val valueColor = FixifyFeatures.performanceHudValueColor.argb
		FixifyNvgRenderer.roundedRect(0.0f, 0.0f, width, height, 6.0f, 0xD914141C.toInt())
		FixifyNvgRenderer.roundedOutline(0.5f, 0.5f, width - 1.0f, height - 1.0f, 6.0f, 1.0f, 0x553E3D4A)
		FixifyNvgRenderer.gradientRect(
			4.0f,
			3.0f,
			width - 8.0f,
			2.0f,
			1.0f,
			0xFFAAA4FF.toInt(),
			0xFF6A7CFF.toInt(),
			false,
		)

		if (FixifyFeatures.performanceHudDirection == 0) {
			val cellWidth = width / metrics.size
			for ((index, metric) in metrics.withIndex()) {
				val center = cellWidth * index + cellWidth / 2.0f
				centerText(metric.first, center, 8.0f, 7.0f, nameColor)
				centerText(metric.second, center, 16.0f, 10.0f, valueColor)
				if (index > 0) {
					FixifyNvgRenderer.line(cellWidth * index, 9.0f, cellWidth * index, height - 5.0f, 1.0f, 0x332F2E39)
				}
			}
		} else {
			for ((index, metric) in metrics.withIndex()) {
				val y = 9.0f + index * 15.0f
				FixifyNvgRenderer.text(metric.first, 8.0f, y, 8.0f, nameColor)
				val valueWidth = FixifyNvgRenderer.textWidth(metric.second, 9.0f)
				FixifyNvgRenderer.text(metric.second, width - valueWidth - 8.0f, y - 0.5f, 9.0f, valueColor)
			}
		}
	}

	private fun performanceMetrics(): List<Pair<String, String>> {
		val metrics = ArrayList<Pair<String, String>>(3)
		if (FixifyFeatures.performanceHudShowTps) {
			metrics.add("TPS" to String.format(java.util.Locale.ROOT, "%.1f", PerformanceMetricsFeature.averageTps))
		}
		if (FixifyFeatures.performanceHudShowFps) {
			metrics.add("FPS" to PerformanceMetricsFeature.fps().toString())
		}
		if (FixifyFeatures.performanceHudShowPing) {
			metrics.add("PING" to "${PerformanceMetricsFeature.averagePing} ms")
		}
		return metrics
	}

	private fun drawPetItems(graphics: GuiGraphicsExtractor, preview: Boolean) {
		val bounds = bounds(FixifyHudWidget.PET, graphics.guiWidth(), graphics.guiHeight())
		val type = FixifyFeatures.petOverlayType
		val circular = type >= 2
		val iconX = if (circular) 14.0f else if (FixifyFeatures.petOverlayFlip) 58.0f else 3.0f
		val iconY = if (circular) 14.0f else 11.0f
		val trackedPet = SkyblockDataTracker.petItem
		val stack = if (preview || trackedPet == null || trackedPet.isEmpty) ItemStack(Items.PLAYER_HEAD) else trackedPet
		drawScaledItem(graphics, stack, bounds.x + iconX * scale(FixifyHudWidget.PET), bounds.y + iconY * scale(FixifyHudWidget.PET), scale(FixifyHudWidget.PET))
		if (FixifyFeatures.petOverlayShowItem) {
			val trackedHeldItem = SkyblockDataTracker.petHeldItem
			val held = if (preview || trackedHeldItem == null || trackedHeldItem.isEmpty) ItemStack(Items.NETHER_STAR) else trackedHeldItem
			drawScaledItem(
				graphics,
				held,
				bounds.x + (iconX + 10.0f) * scale(FixifyHudWidget.PET),
				bounds.y + (iconY - 5.0f) * scale(FixifyHudWidget.PET),
				scale(FixifyHudWidget.PET) * 0.55f,
			)
		}
	}

	private fun drawScaledItem(graphics: GuiGraphicsExtractor, stack: ItemStack, x: Float, y: Float, scale: Float) {
		graphics.pose().pushMatrix()
		graphics.pose().translate(x, y)
		graphics.pose().scale(scale, scale)
		graphics.item(stack, 0, 0)
		graphics.pose().popMatrix()
	}

	private fun petLevelText(preview: Boolean): String {
		val level = if (preview) 78 else SkyblockDataTracker.petLevel
		val maximum = if (preview) 100 else SkyblockDataTracker.petMaxLevel
		return if (level >= maximum) "LV MAX" else "Lvl $level"
	}

	private fun petXpText(preview: Boolean): String {
		val xp = if (preview) 0.63f else animatedPetXp
		val levelUpProgress = if (
			FixifyFeatures.petOverlayLevelUpAnimation &&
			System.currentTimeMillis() - SkyblockDataTracker.petLevelUpAt < 2200L
		) {
			1.0f
		} else {
			0.0f
		}
		return if (levelUpProgress > 0.0f) "LV UP" else if (xp >= 0.1f) "%.1f%%".format(xp * 100.0f) else "%.2f%%".format(xp * 100.0f)
	}

	private fun petColors(): Triple<Int, Int, Int> {
		if (FixifyFeatures.petOverlayTheme == 1) {
			return Triple(
				FixifyFeatures.petOverlayLevelColor.argb,
				FixifyFeatures.petOverlayXpColor.argb,
				FixifyFeatures.petOverlayBackgroundColor.argb,
			)
		}
		val rarity = when (FixifyFeatures.petOverlayTheme) {
			2 -> "special"
			3 -> "divine"
			4 -> "mythic"
			5 -> "legendary"
			6 -> "epic"
			7 -> "rare"
			8 -> "uncommon"
			9 -> "common"
			else -> SkyblockDataTracker.petRarity
		}
		return when (rarity) {
			"special" -> Triple(0xFFAA2121.toInt(), 0xFFFF3232.toInt(), 0xFF771515.toInt())
			"divine" -> Triple(0xFF085599.toInt(), 0xFF11AADD.toInt(), 0xFF053366.toInt())
			"mythic" -> Triple(0xFF772269.toInt(), 0xFFFF55FF.toInt(), 0xFF511144.toInt())
			"legendary" -> Triple(0xFFFFB700.toInt(), 0xFFFFCA00.toInt(), 0xFF603500.toInt())
			"epic" -> Triple(0xFF5701B7.toInt(), 0xFFAA32D9.toInt(), 0xFF240153.toInt())
			"rare" -> Triple(0xFF3232A3.toInt(), 0xFF5252F3.toInt(), 0xFF111164.toInt())
			"uncommon" -> Triple(0xFF158A15.toInt(), 0xFF54FD54.toInt(), 0xFF0B440B.toInt())
			else -> Triple(0xFF9A9A9A.toInt(), 0xFFFFFFFF.toInt(), 0xFF363636.toInt())
		}
	}

	private fun scoreRank(score: Float): Pair<String, Int> {
		return when {
			score >= 299.0f -> "S+" to 0xFFF9ED4C.toInt()
			score >= 269.4f -> "S" to 0xFFBEBE22.toInt()
			score >= 229.0f -> "A" to 0xFF7F3FB2.toInt()
			score >= 159.0f -> "B" to 0xFF7FCC19.toInt()
			score >= 99.0f -> "C" to 0xFF3F3FFD.toInt()
			else -> "D" to 0xFFFC0000.toInt()
		}
	}

	private fun centerText(text: String, centerX: Float, y: Float, size: Float, color: Int) {
		val width = FixifyNvgRenderer.textWidth(text, size)
		FixifyNvgRenderer.text(text, centerX - width / 2.0f, y, size, color)
	}

	private fun baseSize(widget: FixifyHudWidget): Pair<Float, Float> {
		return when (widget) {
			FixifyHudWidget.PET -> if (FixifyFeatures.petOverlayType >= 2) 44.0f to 65.0f else 75.0f to 38.0f
			FixifyHudWidget.PRESSURE -> 34.0f to 44.0f
			FixifyHudWidget.DRILL_FUEL -> 24.0f to 54.0f
			FixifyHudWidget.DUNGEON_SCORE -> 40.0f to 40.0f
			FixifyHudWidget.PERFORMANCE -> {
				val count = performanceMetrics().size.coerceAtLeast(1)
				if (FixifyFeatures.performanceHudDirection == 0) {
					(58.0f * count) to 31.0f
				} else {
					88.0f to (13.0f + count * 15.0f)
				}
			}
		}
	}

	private fun anchor(widget: FixifyHudWidget): Int = when (widget) {
		FixifyHudWidget.PET -> FixifyFeatures.petOverlayAnchor
		FixifyHudWidget.PRESSURE -> FixifyFeatures.pressureDisplayAnchor
		FixifyHudWidget.DRILL_FUEL -> FixifyFeatures.drillFuelMeterAnchor
		FixifyHudWidget.DUNGEON_SCORE -> FixifyFeatures.dungeonScoreMeterAnchor
		FixifyHudWidget.PERFORMANCE -> FixifyFeatures.performanceHudAnchor
	}

	private fun scale(widget: FixifyHudWidget): Float = when (widget) {
		FixifyHudWidget.PET -> FixifyFeatures.petOverlayScale
		FixifyHudWidget.PRESSURE -> FixifyFeatures.pressureDisplayScale
		FixifyHudWidget.DRILL_FUEL -> FixifyFeatures.drillFuelMeterScale
		FixifyHudWidget.DUNGEON_SCORE -> FixifyFeatures.dungeonScoreMeterScale
		FixifyHudWidget.PERFORMANCE -> FixifyFeatures.performanceHudScale
	}

	private fun offsetX(widget: FixifyHudWidget): Float = when (widget) {
		FixifyHudWidget.PET -> FixifyFeatures.petOverlayX.toFloat()
		FixifyHudWidget.PRESSURE -> FixifyFeatures.pressureDisplayX.toFloat()
		FixifyHudWidget.DRILL_FUEL -> FixifyFeatures.drillFuelMeterX.toFloat()
		FixifyHudWidget.DUNGEON_SCORE -> FixifyFeatures.dungeonScoreMeterX.toFloat()
		FixifyHudWidget.PERFORMANCE -> FixifyFeatures.performanceHudX.toFloat()
	}

	private fun offsetY(widget: FixifyHudWidget): Float = when (widget) {
		FixifyHudWidget.PET -> FixifyFeatures.petOverlayY.toFloat()
		FixifyHudWidget.PRESSURE -> FixifyFeatures.pressureDisplayY.toFloat()
		FixifyHudWidget.DRILL_FUEL -> FixifyFeatures.drillFuelMeterY.toFloat()
		FixifyHudWidget.DUNGEON_SCORE -> FixifyFeatures.dungeonScoreMeterY.toFloat()
		FixifyHudWidget.PERFORMANCE -> FixifyFeatures.performanceHudY.toFloat()
	}

	private fun anchoredPosition(
		anchor: Int,
		screenWidth: Float,
		screenHeight: Float,
		width: Float,
		height: Float,
	): Pair<Float, Float> {
		val margin = 4.0f
		return when (anchor.coerceIn(0, FixifyFeatures.hudAnchors.lastIndex)) {
			0 -> margin to margin
			1 -> margin to (screenHeight - height) / 2.0f
			2 -> margin to screenHeight - height - margin
			3 -> screenWidth - width - margin to margin
			4 -> screenWidth - width - margin to (screenHeight - height) / 2.0f
			5 -> screenWidth - width - margin to screenHeight - height - margin
			6 -> (screenWidth - width) / 2.0f to margin
			else -> (screenWidth - width) / 2.0f to screenHeight - height - margin
		}
	}

	private fun lerp(current: Float, target: Float): Float = current + (target - current) * 0.09f

	private fun rainbow(offset: Float): Int {
		val hue = ((System.currentTimeMillis() % 5000L) / 5000.0f + offset) % 1.0f
		return 0xFF000000.toInt() or FixifyFeatures.hsbToRgb(hue, 0.82f, 1.0f)
	}

	private fun withAlpha(color: Int, alpha: Int): Int {
		return (alpha.coerceIn(0, 255) shl 24) or (color and 0x00FFFFFF)
	}

	private fun mixColor(first: Int, second: Int, amount: Float): Int {
		return ARGB.srgbLerp(amount.coerceIn(0.0f, 1.0f), first, second)
	}
}
