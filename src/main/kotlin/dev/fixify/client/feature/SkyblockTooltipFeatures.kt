package dev.fixify.client.feature

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.TextColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Locale
import java.util.concurrent.CompletableFuture

object SkyblockTooltipFeatures {
	private const val ENCHANTS_URL =
		"https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/refs/heads/master/constants/enchants.json"

	@Volatile
	private var enchantData: JsonObject? = null

	private val petLevelPattern = Regex("""\[Lvl\s+(\d+)(?:\s*(?:->|\u2192)\s*(\d+))?]\s*(.+)""", RegexOption.IGNORE_CASE)
	private val itemRarityPattern = Regex(
		"""(?:VERY SPECIAL|SPECIAL|DIVINE|MYTHIC|LEGENDARY|EPIC|RARE|UNCOMMON|COMMON)\s+(.+)$""",
		RegexOption.IGNORE_CASE,
	)

	fun register() {
		loadEnchantData()
		ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
			if (!SkyblockDataTracker.shouldRender()) {
				return@register
			}
			if (FixifyFeatures.compactPetLevelEnabled) {
				runCatching { compactPetLevel(stack, lines) }
			}
			if (FixifyFeatures.missingEnchantsEnabled) {
				runCatching { appendMissingEnchants(stack, lines) }
			}
		}
	}

	private fun loadEnchantData() {
		CompletableFuture.runAsync {
			runCatching {
				val request = HttpRequest.newBuilder(URI.create(ENCHANTS_URL))
					.header("User-Agent", "Fixify/1.0")
					.GET()
					.build()
				val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
				if (response.statusCode() in 200..299) {
					enchantData = JsonParser.parseString(response.body()).asJsonObject
				}
			}
		}
	}

	private fun compactPetLevel(stack: ItemStack, lines: MutableList<Component>) {
		if (stack.item != Items.PLAYER_HEAD || lines.isEmpty()) {
			return
		}
		val customName = stack.customName ?: return
		val match = petLevelPattern.find(customName.string) ?: return
		val level = match.groupValues[1].toIntOrNull() ?: return
		val rangeEnd = match.groupValues[2].toIntOrNull()
		val petName = match.groupValues[3].trim()
		val maximum = if (
			petName.contains("Golden Dragon", ignoreCase = true) ||
			petName.contains("Jade Dragon", ignoreCase = true)
		) {
			200
		} else {
			100
		}

		val rarityColor = rarityColor(customName) ?: TextColor.fromLegacyFormat(ChatFormatting.WHITE)
		val levelColor = if (rangeEnd == null && level >= maximum) {
			rarityColor
		} else {
			TextColor.fromLegacyFormat(ChatFormatting.GRAY)
		}
		val compact = Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
		if (rangeEnd != null) {
			compact.append(colored(level.toString(), levelColor))
				.append(Component.literal(" \u2192 ").withStyle(ChatFormatting.DARK_GRAY))
				.append(colored(rangeEnd.toString(), levelColor))
		} else {
			compact.append(colored(level.toString(), levelColor))
		}
		compact.append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY))
			.append(colored(petName, rarityColor))
		lines[0] = compact
	}

	private fun appendMissingEnchants(stack: ItemStack, lines: MutableList<Component>) {
		val data = enchantData ?: return
		val customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
		val enchantments = customData.copyTag().getCompound("enchantments").orElse(null) ?: return
		val current = enchantments.keySet().mapTo(HashSet()) { it.lowercase(Locale.ROOT) }
		if (current.isEmpty()) {
			return
		}

		val itemType = itemType(lines) ?: return
		val enchantGroups = data.getAsJsonObject("enchants") ?: return
		val possible = enchantGroups.getAsJsonArray(itemType.uppercase(Locale.ROOT)) ?: return
		val pools = data.getAsJsonArray("enchant_pools")
		val missing = ArrayList<String>()
		val ultimate = ArrayList<String>()
		for (element in possible) {
			val enchant = element.asString.lowercase(Locale.ROOT)
			val conflict = pools?.any { pool ->
				val values = pool.asJsonArray.map { it.asString.lowercase(Locale.ROOT) }
				enchant in values && values.any(current::contains)
			} == true
			if (conflict || enchant in current || "one_for_all" in current) {
				continue
			}
			val display = titleCase(enchant.replace("pristine", "prismatic").replace('_', ' '))
			if (enchant.contains("ultimate")) {
				ultimate.add(display.replace("Ultimate ", ""))
			} else {
				missing.add(display)
			}
		}
		if (missing.isEmpty()) {
			return
		}
		if (ultimate.isNotEmpty()) {
			missing.add("Any Ultimate")
		}

		lines.add(Component.empty())
		lines.add(
			Component.literal(if (Minecraft.getInstance().hasShiftDown()) "\u2726 Missing enchantments:" else "\u2727 Missing enchantments:")
				.withColor(if (Minecraft.getInstance().hasShiftDown()) 0x55FFFF else 0x00AAAA),
		)
		if (!Minecraft.getInstance().hasShiftDown()) {
			lines.add(Component.literal("\u22D7 Press [SHIFT] to see").withStyle(ChatFormatting.GRAY))
			return
		}
		missing.chunked(3).forEach { group ->
			lines.add(Component.literal("\u22D7 ${group.joinToString(", ")}").withStyle(ChatFormatting.GRAY))
		}
	}

	private fun itemType(lines: List<Component>): String? {
		for (line in lines.asReversed()) {
			val clean = ChatFormatting.stripFormatting(line.string).orEmpty().trim()
			val match = itemRarityPattern.find(clean) ?: continue
			return match.groupValues[1]
				.lowercase(Locale.ROOT)
				.removePrefix("dungeon ")
				.trim()
				.takeIf { it.isNotEmpty() }
		}
		return null
	}

	private fun rarityColor(component: Component): TextColor? {
		component.style.color?.let {
			if (it.serialize() in RARITY_COLORS) {
				return it
			}
		}
		for (sibling in component.siblings) {
			rarityColor(sibling)?.let { return it }
		}
		return null
	}

	private fun colored(text: String, color: TextColor?): MutableComponent {
		val component = Component.literal(text)
		if (color != null) {
			component.withColor(color.value)
		}
		return component
	}

	private fun titleCase(value: String): String {
		return value.split(' ').joinToString(" ") { word ->
			word.replaceFirstChar { character ->
				if (character.isLowerCase()) character.titlecase(Locale.ROOT) else character.toString()
			}
		}
	}

	private val RARITY_COLORS = setOf("red", "aqua", "light_purple", "gold", "dark_purple", "blue", "green", "white")
}
