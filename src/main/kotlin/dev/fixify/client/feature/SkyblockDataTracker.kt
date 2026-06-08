package dev.fixify.client.feature

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.scores.DisplaySlot
import java.util.Locale

object SkyblockDataTracker {
	var inSkyblock: Boolean = false
		private set
	var inDungeon: Boolean = false
		private set
	var inWater: Boolean = false
		private set
	var pressure: Float = 0.0f
		private set
	var health: Float = 1.0f
		private set
	var dungeonScore: Float = 0.0f
		private set
	var fuelCurrent: Int = 0
		private set
	var fuelMaximum: Int = 3000
		private set
	var lastFuelSeenAt: Long = 0L
		private set

	var petActive: Boolean = false
		private set
	var petName: String = "Pet"
		private set
	var petLevel: Int = 1
		private set
	var petMaxLevel: Int = 100
		private set
	var petXp: Float = 0.0f
		private set
	var petRarity: String = "common"
		private set
	var petItem: ItemStack? = null
		private set
	var petHeldItem: ItemStack? = null
		private set
	var petLevelUpAt: Long = 0L
		private set

	private val pressurePattern = Regex("""Pressure:\s*\D*(\d+)%""", RegexOption.IGNORE_CASE)
	private val drillFuelPattern = Regex("""([\d,]+)/([\d,.]+[kKmM]?)\s+Drill Fuel""")
	private val dungeonScorePattern = Regex("""Cleared:\s*\d+%\s*\((\d+)\)""", RegexOption.IGNORE_CASE)
	private val petNamePattern = Regex("""\[Lvl\s+(\d+)(?:\s*->\s*\d+)?]\s*(.+)""", RegexOption.IGNORE_CASE)
	private val summonPattern = Regex("""You (summoned|despawned) your (.+?)!""", RegexOption.IGNORE_CASE)
	private val autopetPattern = Regex("""Autopet equipped your \[Lvl\s+(\d+)]\s+(.+?)!\s+VIEW RULE""", RegexOption.IGNORE_CASE)
	private val levelUpPattern = Regex("""Your (.+?) leveled up to level (\d+)!""", RegexOption.IGNORE_CASE)
	private val progressPattern = Regex("""Progress to.*?([\d.]+)%""", RegexOption.IGNORE_CASE)
	private val tabXpPattern = Regex("""\(([\d.]+)%\)""")
	private val petCache = LinkedHashMap<String, ItemStack>()

	private var ticks = 0
	private var lastTabReadAt = 0L

	fun register() {
		ClientReceiveMessageEvents.MODIFY_GAME.register { message, overlay ->
			if (!overlay) {
				message
			} else {
				readActionBar(message)
				cleanActionBar(message)
			}
		}
		ClientReceiveMessageEvents.GAME.register { message, overlay ->
			if (!overlay) {
				readGameMessage(message)
			}
		}
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			tick(client)
		}
	}

	fun shouldRender(): Boolean = !FixifyFeatures.onlyInSkyblock || inSkyblock

	fun fuelProgress(): Float {
		if (fuelMaximum <= 0) {
			return 0.0f
		}
		return (fuelCurrent.toFloat() / fuelMaximum).coerceIn(0.0f, 1.0f)
	}

	private fun tick(client: Minecraft) {
		val player = client.player ?: run {
			resetWorldState()
			return
		}

		health = if (player.maxHealth <= 0.0f) 1.0f else (player.health / player.maxHealth).coerceIn(0.0f, 1.0f)
		ticks++
		if (ticks % 10 == 0) {
			readScoreboard(client)
			inWater = player.level().getFluidState(player.blockPosition()).type == Fluids.WATER
			if (!inWater) {
				pressure = 0.0f
			}
			readPetMenu(client)
		}

		val now = System.currentTimeMillis()
		if (now - lastTabReadAt >= 2500L) {
			lastTabReadAt = now
			readPetTab(client)
		}
	}

	private fun resetWorldState() {
		inSkyblock = false
		inDungeon = false
		inWater = false
		pressure = 0.0f
		dungeonScore = 0.0f
		petActive = false
	}

	private fun readScoreboard(client: Minecraft) {
		val player = client.player ?: return
		val scoreboard = player.level().scoreboard
		val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR)
		val lines = ArrayList<String>()
		if (objective != null) {
			lines.add(objective.displayName.string)
			for (score in scoreboard.listPlayerScores(objective)) {
				val owner = score.owner()
				val team = scoreboard.getPlayersTeam(owner)
				val component = score.display()
					?: team?.getFormattedName(Component.literal(owner))
					?: score.ownerName()
				lines.add(component.string)
			}
		}

		inSkyblock = lines.any {
			it.contains("SKYBLOCK", ignoreCase = true) ||
				it.contains("The Catacombs", ignoreCase = true) ||
				it.contains("Purse:", ignoreCase = true) ||
				it.contains("Bits:", ignoreCase = true)
		}

		inDungeon = lines.any { it.contains("The Catacombs", ignoreCase = true) }
		dungeonScore = lines.firstNotNullOfOrNull { line ->
			dungeonScorePattern.find(line)?.groupValues?.getOrNull(1)?.toFloatOrNull()
		} ?: if (inDungeon) dungeonScore else 0.0f
	}

	private fun readActionBar(message: Component) {
		val content = ChatFormatting.stripFormatting(message.string).orEmpty()
		pressurePattern.find(content)?.groupValues?.getOrNull(1)?.toFloatOrNull()?.let {
			pressure = (it / 100.0f).coerceIn(0.0f, 1.0f)
		}
		drillFuelPattern.find(content.replace(",", ""))?.let { match ->
			fuelCurrent = match.groupValues[1].toIntOrNull() ?: fuelCurrent
			fuelMaximum = parseSuffix(match.groupValues[2]).coerceAtLeast(1)
			lastFuelSeenAt = System.currentTimeMillis()
		}
	}

	private fun cleanActionBar(message: Component): Component {
		if (!shouldRender() || (!FixifyFeatures.hidePressureInActionBar && !FixifyFeatures.hideDrillFuelInActionBar)) {
			return message
		}

		val parts = message.string.split(Regex("""\s{5,}""")).filterNot { part ->
			(FixifyFeatures.hidePressureInActionBar && part.contains("Pressure", ignoreCase = true)) ||
				(FixifyFeatures.hideDrillFuelInActionBar && part.contains("Drill Fuel", ignoreCase = true))
		}
		return Component.literal(parts.joinToString("     "))
	}

	private fun readGameMessage(message: Component) {
		val content = ChatFormatting.stripFormatting(message.string).orEmpty()
		if (content.contains("You fainted from pressure", ignoreCase = true)) {
			pressure = 0.0f
		}

		summonPattern.find(content)?.let { match ->
			if (match.groupValues[1].equals("despawned", ignoreCase = true)) {
				petActive = false
			} else {
				petActive = true
				setPetByName(match.groupValues[2], rarityFrom(message))
			}
		}

		autopetPattern.find(content)?.let { match ->
			petLevel = match.groupValues[1].toIntOrNull() ?: petLevel
			petName = normalizePetName(match.groupValues[2])
			petMaxLevel = petMaxLevel(petName)
			petRarity = rarityFrom(message)
			petActive = true
			setPetByName(petName, petRarity)
		}

		levelUpPattern.find(content)?.let { match ->
			val newLevel = match.groupValues[2].toIntOrNull() ?: return@let
			if (normalizePetName(match.groupValues[1]).contains(petName, ignoreCase = true) || petName.contains(match.groupValues[1], ignoreCase = true)) {
				petLevel = newLevel
				petXp = 0.0f
				petLevelUpAt = System.currentTimeMillis()
			}
		}
	}

	private fun readPetMenu(client: Minecraft) {
		val screen = client.screen as? AbstractContainerScreen<*> ?: return
		if (!screen.title.string.startsWith("Pets")) {
			return
		}

		for (slot in screen.menu.slots) {
			val stack = slot.item
			if (stack.isEmpty || stack.item != Items.PLAYER_HEAD) {
				continue
			}
			val name = stack.customName?.string ?: continue
			val normalized = normalizePetName(name)
			petCache[normalized.lowercase(Locale.ROOT)] = stack.copy()
			val lore = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).styledLines()
			if (lore.any { it.string.contains("Click to despawn", ignoreCase = true) }) {
				setPetFromStack(stack)
				petActive = true
			}
		}
	}

	private fun readPetTab(client: Minecraft) {
		val connection = client.connection ?: return
		val lines = connection.onlinePlayers.mapNotNull { it.tabListDisplayName }
		val petLine = lines.firstOrNull { petNamePattern.containsMatchIn(it.string) } ?: return
		val match = petNamePattern.find(petLine.string) ?: return
		val tabName = normalizePetName(match.groupValues[2])
		if (petName != "Pet" && !tabName.contains(petName, ignoreCase = true) && !petName.contains(tabName, ignoreCase = true)) {
			return
		}

		petLevel = match.groupValues[1].toIntOrNull() ?: petLevel
		petName = tabName
		petMaxLevel = petMaxLevel(tabName)
		petRarity = rarityFrom(petLine)
		petActive = true
		lines.firstNotNullOfOrNull { tabXpPattern.find(it.string)?.groupValues?.getOrNull(1)?.toFloatOrNull() }?.let {
			petXp = (it / 100.0f).coerceIn(0.0f, 1.0f)
		}
		setPetByName(tabName, petRarity)
	}

	private fun setPetByName(name: String, rarity: String) {
		val normalized = normalizePetName(name)
		petName = normalized
		petRarity = rarity.ifBlank { petRarity }
		petMaxLevel = petMaxLevel(normalized)
		val cached = petCache[normalized.lowercase(Locale.ROOT)]
			?: petCache.entries.firstOrNull {
				it.key.contains(normalized.lowercase(Locale.ROOT)) || normalized.lowercase(Locale.ROOT).contains(it.key)
			}?.value
		if (cached != null) {
			setPetFromStack(cached)
		}
	}

	private fun setPetFromStack(stack: ItemStack) {
		petItem = stack.copy()
		val name = stack.customName?.string.orEmpty()
		petNamePattern.find(name)?.let { match ->
			petLevel = match.groupValues[1].toIntOrNull() ?: petLevel
			petName = normalizePetName(match.groupValues[2])
		}
		petMaxLevel = petMaxLevel(petName)
		petRarity = rarityFrom(stack.customName)

		val lore = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).styledLines()
		for (line in lore) {
			val text = line.string
			progressPattern.find(text)?.groupValues?.getOrNull(1)?.toFloatOrNull()?.let {
				petXp = (it / 100.0f).coerceIn(0.0f, 1.0f)
			}
			if (text.contains("MAX LEVEL", ignoreCase = true)) {
				petLevel = petMaxLevel
				petXp = 1.0f
			}
			if (text.contains("Held Item:", ignoreCase = true)) {
				petHeldItem = heldItemFor(text.substringAfter("Held Item:").trim())
			}
		}
	}

	private fun heldItemFor(name: String): ItemStack {
		val item = when {
			name.contains("Exp Boost", ignoreCase = true) -> Items.EXPERIENCE_BOTTLE
			name.contains("Shelmet", ignoreCase = true) -> Items.TURTLE_HELMET
			name.contains("Crochet", ignoreCase = true) -> Items.STRING
			name.contains("Claw", ignoreCase = true) -> Items.PRISMARINE_SHARD
			name.contains("Textbook", ignoreCase = true) -> Items.BOOK
			name.isBlank() -> Items.AIR
			else -> Items.NETHER_STAR
		}
		return ItemStack(item)
	}

	private fun rarityFrom(component: Component?): String {
		val color = componentColors(component).firstOrNull {
			it in setOf("red", "aqua", "light_purple", "gold", "dark_purple", "blue", "green")
		}.orEmpty()
		return when (color) {
			"red" -> "special"
			"aqua" -> "divine"
			"light_purple" -> "mythic"
			"gold" -> "legendary"
			"dark_purple" -> "epic"
			"blue" -> "rare"
			"green" -> "uncommon"
			else -> "common"
		}
	}

	private fun componentColors(component: Component?): List<String> {
		if (component == null) {
			return emptyList()
		}
		val colors = ArrayList<String>()
		component.style.color?.serialize()?.let(colors::add)
		for (sibling in component.siblings) {
			colors.addAll(componentColors(sibling))
		}
		return colors
	}

	private fun normalizePetName(value: String): String {
		return value
			.replace(Regex("""\[Lvl\s+\d+(?:\s*->\s*\d+)?]\s*"""), "")
			.replace("\u2726", "")
			.replace("\u2B50", "")
			.trim()
	}

	private fun petMaxLevel(name: String): Int {
		return if (name.contains("Golden Dragon", ignoreCase = true) || name.contains("Jade Dragon", ignoreCase = true)) 200 else 100
	}

	private fun parseSuffix(value: String): Int {
		val normalized = value.replace(",", "").lowercase(Locale.ROOT)
		return when {
			normalized.endsWith("k") -> ((normalized.dropLast(1).toDoubleOrNull() ?: 0.0) * 1_000.0).toInt()
			normalized.endsWith("m") -> ((normalized.dropLast(1).toDoubleOrNull() ?: 0.0) * 1_000_000.0).toInt()
			else -> normalized.toDoubleOrNull()?.toInt() ?: 0
		}
	}
}
