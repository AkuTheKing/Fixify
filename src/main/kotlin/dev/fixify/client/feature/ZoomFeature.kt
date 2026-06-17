package dev.fixify.client.feature

import com.mojang.blaze3d.platform.InputConstants
import dev.fixify.client.FixifyConfig
import dev.fixify.client.FixifyKeybinds
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt
import kotlin.math.sign

object ZoomFeature {
	private const val MIN_INTENSITY = 1
	private const val MAX_INTENSITY = 10
	private const val MIN_ZOOM_FOV = 1
	private const val MAX_ZOOM_FOV = 50
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

		val nextIntensity = (FixifyFeatures.zoomIntensity + sign(scrollAmount).toInt())
			.coerceIn(MIN_INTENSITY, MAX_INTENSITY)
		if (nextIntensity == FixifyFeatures.zoomIntensity) {
			return
		}

		FixifyFeatures.zoomIntensity = nextIntensity
		FixifyConfig.updateEntry("Visuals.Zoom.Intensity") {
			it.value = nextIntensity.toString()
			it.sliderPercentage = (nextIntensity - MIN_INTENSITY).toFloat() / (MAX_INTENSITY - MIN_INTENSITY)
		}
	}

	private fun tick(client: Minecraft) {
		if (isZooming()) {
			if (!applied) {
				savedFov = client.options.fov().get()
				applied = true
			}
			client.options.fov().set(fovForIntensity(FixifyFeatures.zoomIntensity))
		} else if (applied) {
			client.options.fov().set(savedFov)
			applied = false
		}
	}

	private fun fovForIntensity(intensity: Int): Int {
		val clamped = intensity.coerceIn(MIN_INTENSITY, MAX_INTENSITY)
		val fov = MAX_ZOOM_FOV - (clamped - MIN_INTENSITY) * (MAX_ZOOM_FOV - MIN_ZOOM_FOV).toDouble() /
			(MAX_INTENSITY - MIN_INTENSITY)
		return fov.roundToInt().coerceIn(MIN_ZOOM_FOV, MAX_ZOOM_FOV)
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
