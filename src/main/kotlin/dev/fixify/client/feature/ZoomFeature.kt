package dev.fixify.client.feature

import com.mojang.blaze3d.platform.InputConstants
import dev.fixify.client.FixifyConfig
import dev.fixify.client.FixifyKeybinds
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import kotlin.math.sign

object ZoomFeature {
	private const val MIN_FOV = 10
	private const val MAX_FOV = 110
	private const val FOV_STEP = 2
	private const val DEFAULT_KEY = "key.keyboard.c"

	private var applied = false
	private var savedFov = 70

	fun register() {
		ClientTickEvents.END_CLIENT_TICK.register { client -> tick(client) }
	}

	@JvmStatic
	fun isZooming(): Boolean {
		val client = Minecraft.getInstance()
		return FixifyFeatures.zoomEnabled &&
			client.screen == null &&
			client.isWindowActive &&
			isZoomKeyDown(client)
	}

	@JvmStatic
	fun adjustByScroll(scrollAmount: Double) {
		if (!FixifyFeatures.zoomScrollable || !isZooming() || scrollAmount == 0.0) {
			return
		}

		val nextFov = (FixifyFeatures.zoomFov - sign(scrollAmount).toInt() * FOV_STEP)
			.coerceIn(MIN_FOV, MAX_FOV)
		if (nextFov == FixifyFeatures.zoomFov) {
			return
		}

		FixifyFeatures.zoomFov = nextFov
		FixifyConfig.updateEntry("Visuals.Zoom.FOV") {
			it.value = nextFov.toString()
			it.sliderPercentage = (nextFov - MIN_FOV).toFloat() / (MAX_FOV - MIN_FOV)
		}
	}

	private fun tick(client: Minecraft) {
		if (isZooming()) {
			if (!applied) {
				savedFov = client.options.fov().get()
				applied = true
			}
			client.options.fov().set(FixifyFeatures.zoomFov.coerceIn(MIN_FOV, MAX_FOV))
		} else if (applied) {
			client.options.fov().set(savedFov)
			applied = false
		}
	}

	private fun isZoomKeyDown(client: Minecraft): Boolean {
		val entry = FixifyConfig.entry("Visuals.Zoom.Keybind")
		val keyName = entry?.keyName ?: FixifyKeybinds.inferKeyName(entry?.value.orEmpty()) ?: DEFAULT_KEY
		val key = runCatching { InputConstants.getKey(keyName) }.getOrNull() ?: return false
		return when (key.type) {
			InputConstants.Type.KEYSYM -> InputConstants.isKeyDown(client.window, key.value)
			InputConstants.Type.MOUSE -> GLFW.glfwGetMouseButton(client.window.handle(), key.value) == GLFW.GLFW_PRESS
			else -> false
		}
	}
}
