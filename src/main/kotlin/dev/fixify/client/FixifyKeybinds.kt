package dev.fixify.client

import com.mojang.blaze3d.platform.InputConstants
import dev.fixify.client.feature.FixifyFeatures
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

object FixifyKeybinds {
	private val pressed = HashMap<String, Boolean>()
	private val moduleDefaults = mapOf(
		"Dungeons.Etherwarp" to false,
		"Dungeons.DungeonBreaker" to false,
		"Dungeons.Teammate Highlight" to false,
	)
	private val moduleNames = mapOf(
		"Dungeons.Etherwarp" to "Etherwarp",
		"Dungeons.DungeonBreaker" to "DungeonBreaker",
		"Dungeons.Teammate Highlight" to "Teammate Highlight",
	)
	private val defaultKeybinds = emptyMap<String, String>()

	fun tick(client: Minecraft) {
		val bindings = currentBindings()
		val allowToggle = client.screen == null
		for ((moduleKey, keyName) in bindings) {
			val down = isDown(client, keyName)
			val id = "$moduleKey:$keyName"
			if (allowToggle && down && pressed[id] != true) {
				toggleModule(client, moduleKey)
			}
			pressed[id] = down
		}

		val activeIds = bindings.mapTo(HashSet()) { "${it.key}:${it.value}" }
		pressed.keys.retainAll(activeIds)
	}

	private fun currentBindings(): Map<String, String> {
		val bindings = LinkedHashMap(defaultKeybinds)
		for ((entryKey, entry) in FixifyConfig.entriesSnapshot()) {
			if (!entryKey.endsWith(".Keybind")) {
				continue
			}
			val moduleKey = entryKey.removeSuffix(".Keybind")
			if (moduleKey !in moduleDefaults) {
				continue
			}
			val keyName = entry.keyName ?: inferKeyName(entry.value.orEmpty())
			if (keyName == null) {
				bindings.remove(moduleKey)
				continue
			}
			if (keyName.isNotBlank()) {
				bindings[moduleKey] = keyName
			}
		}
		return bindings
	}

	private fun isDown(client: Minecraft, keyName: String): Boolean {
		val key = runCatching { InputConstants.getKey(keyName) }.getOrNull() ?: return false
		return when (key.type) {
			InputConstants.Type.KEYSYM -> InputConstants.isKeyDown(client.window, key.value)
			InputConstants.Type.MOUSE -> GLFW.glfwGetMouseButton(client.window.handle(), key.value) == GLFW.GLFW_PRESS
			else -> false
		}
	}

	private fun toggleModule(client: Minecraft, moduleKey: String) {
		val current = FixifyConfig.entry(moduleKey)?.enabled ?: moduleDefaults[moduleKey] ?: false
		val enabled = !current
		FixifyConfig.updateEntry(moduleKey) {
			it.enabled = enabled
		}
		FixifyFeatures.loadFromConfig()
		showToggleMessage(client, moduleNames[moduleKey] ?: moduleKey.substringAfterLast('.'), enabled)
	}

	private fun showToggleMessage(client: Minecraft, moduleName: String, enabled: Boolean) {
		val message = Component.literal("Fixify")
			.withColor(FIXIFY_ACCENT)
			.append(Component.literal(" \u00BB ").withColor(MESSAGE_MUTED))
			.append(Component.literal(moduleName).withColor(MESSAGE_TEXT))
			.append(Component.literal(" "))
			.append(Component.literal(if (enabled) "enabled" else "disabled").withColor(if (enabled) ENABLED else DISABLED))
			.append(Component.literal(".").withColor(MESSAGE_TEXT))
		client.gui.chat.addClientSystemMessage(message)
	}

	fun inferKeyName(displayValue: String): String? {
		val display = displayValue.removeSuffix(" X").trim()
		if (display.isBlank() || display.equals("n/a", ignoreCase = true) || display.equals("none", ignoreCase = true)) {
			return null
		}
		if (display.length == 1 && display[0].isLetterOrDigit()) {
			return "key.keyboard.${display.lowercase()}"
		}
		return null
	}

	private const val FIXIFY_ACCENT = 0xAAA4FF
	private const val MESSAGE_MUTED = 0x8D8B9A
	private const val MESSAGE_TEXT = 0xE7E5F1
	private const val ENABLED = 0x55FF55
	private const val DISABLED = 0xFF5555
}
