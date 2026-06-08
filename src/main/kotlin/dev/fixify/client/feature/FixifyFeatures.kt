package dev.fixify.client.feature

import dev.fixify.client.FixifyConfig
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object FixifyFeatures {
	var onlyInSkyblock: Boolean = true

	var dungeonBreakerEnabled: Boolean = false
	var dungeonPreventMiningSecrets: Boolean = true
	var dungeonInstaMineWhenFatigue: Boolean = true
	var teammateHighlightEnabled: Boolean = false
	var etherwarpEnabled: Boolean = false
	var etherwarpShowGuess: Boolean = true
	var etherwarpShowFailed: Boolean = true
	var etherwarpRenderStyle: Int = 1
	var etherwarpUseServerPosition: Boolean = false
	var etherwarpFullBlock: Boolean = false
	var etherwarpDepth: Boolean = false
	var etherwarpLeftClickMode: Int = 2

	var petOverlayEnabled: Boolean = true
	var petOverlayType: Int = 0
	var petOverlayShowItem: Boolean = false
	var petOverlayInvert: Boolean = false
	var petOverlayFlip: Boolean = false
	var petOverlayAnchor: Int = 7
	var petOverlayScale: Float = 1.0f
	var petOverlayTheme: Int = 0
	var petOverlayIdlePulse: Boolean = true
	var petOverlayIdleHover: Boolean = true
	var petOverlayLevelUpAnimation: Boolean = true
	var petOverlayValueAnimation: Boolean = true
	var petOverlayRainbowLevel: Boolean = false
	var petOverlayRainbowXp: Boolean = false
	var petOverlayRainbowBackground: Boolean = false
	var petOverlayX: Int = 65
	var petOverlayY: Int = -40

	var pressureDisplayEnabled: Boolean = true
	var pressureDisplayShowAt: Float = 0.05f
	var pressureDisplayAnchor: Int = 7
	var pressureDisplayScale: Float = 1.0f
	var pressureDisplayTheme: Int = 0
	var pressureDisplayX: Int = -90
	var pressureDisplayY: Int = -55

	var drillFuelMeterEnabled: Boolean = true
	var drillFuelMeterAnchor: Int = 7
	var drillFuelMeterScale: Float = 1.0f
	var drillFuelMeterTheme: Int = 0
	var drillFuelMeterX: Int = -120
	var drillFuelMeterY: Int = -65

	var dungeonScoreMeterEnabled: Boolean = true
	var dungeonScoreMeterAnchor: Int = 7
	var dungeonScoreMeterScale: Float = 1.0f
	var dungeonScoreMeterTheme: Int = 0
	var dungeonScoreMeterGradientRotation: Float = 0.9f
	var dungeonScoreMeterX: Int = -160
	var dungeonScoreMeterY: Int = -50

	var lowHpIndicatorEnabled: Boolean = true
	var lowHpIndicatorHeartbeat: Boolean = true
	var lowHpIndicatorTransparency: Float = 0.4f

	var missingEnchantsEnabled: Boolean = true
	var compactPetLevelEnabled: Boolean = true
	var hidePressureInActionBar: Boolean = false
	var hideDrillFuelInActionBar: Boolean = false

	val etherwarpColor = ColorState(0x80FFAA00.toInt())
	val etherwarpFailColor = ColorState(0x80FF5555.toInt())
	val archerColor = ColorState(0xFFFFBC0A.toInt())
	val berserkerColor = ColorState(0xFF880015.toInt())
	val tankColor = ColorState(0xFF188037.toInt())
	val mageColor = ColorState(0xFF00A2E8.toInt())
	val healerColor = ColorState(0xFFFFAFCA.toInt())
	val petOverlayLevelColor = ColorState(0xFFFFFFFF.toInt())
	val petOverlayXpColor = ColorState(0xFF888888.toInt())
	val petOverlayBackgroundColor = ColorState(0xFF333333.toInt())
	val dungeonScoreGradientColor1 = ColorState(0xFF6F9896.toInt())
	val dungeonScoreGradientColor2 = ColorState(0xFFA6DB83.toInt())

	val etherwarpRenderStyles = arrayOf("Filled", "Outline", "Filled Outline")
	val etherwarpLeftClickModes = arrayOf("Off", "Left Click", "Left Click + Shift")
	val hudAnchors = arrayOf(
		"Top Left",
		"Middle Left",
		"Bottom Left",
		"Top Right",
		"Middle Right",
		"Bottom Right",
		"Top Middle",
		"Bottom Middle",
	)
	val petOverlayTypes = arrayOf("Bar", "Bar (alt)", "Circular", "Circular (alt)")
	val petOverlayThemes = arrayOf(
		"Pet Rarity",
		"Custom",
		"Special",
		"Divine",
		"Mythic",
		"Legendary",
		"Epic",
		"Rare",
		"Uncommon",
		"Common",
	)
	val pressureThemes = arrayOf("Nighttime", "Peach")
	val drillFuelThemes = arrayOf("Biofuel", "Mithril")
	val dungeonScoreThemes = arrayOf("Rank", "Gradient")

	private val roleColors = mapOf(
		'A' to archerColor,
		'B' to berserkerColor,
		'T' to tankColor,
		'M' to mageColor,
		'H' to healerColor,
	)

	fun colorForRole(role: Char): ColorState? = roleColors[role.uppercaseChar()]

	fun loadFromConfig() {
		FixifyConfig.entry("SkyBlock.Only in SkyBlock")?.enabled?.let { onlyInSkyblock = it }

		FixifyConfig.entry("Dungeons.DungeonBreaker")?.enabled?.let { dungeonBreakerEnabled = it }
		FixifyConfig.entry("Dungeons.DungeonBreaker.Prevent mining secrets")?.switchValue?.let { dungeonPreventMiningSecrets = it }
		FixifyConfig.entry("Dungeons.DungeonBreaker.Insta-mine when fatigue")?.switchValue?.let { dungeonInstaMineWhenFatigue = it }
		FixifyConfig.entry("Dungeons.Teammate Highlight")?.enabled?.let { teammateHighlightEnabled = it }
		FixifyConfig.entry("Dungeons.Etherwarp")?.enabled?.let { etherwarpEnabled = it }
		FixifyConfig.entry("Dungeons.Etherwarp.Show Guess")?.switchValue?.let { etherwarpShowGuess = it }
		FixifyConfig.entry("Dungeons.Etherwarp.Show when failed")?.switchValue?.let { etherwarpShowFailed = it }
		FixifyConfig.entry("Dungeons.Etherwarp.Render Style")?.selected?.let {
			etherwarpRenderStyle = it.coerceIn(0, etherwarpRenderStyles.lastIndex)
		}
		FixifyConfig.entry("Dungeons.Etherwarp.Use Server Position")?.switchValue?.let { etherwarpUseServerPosition = it }
		FixifyConfig.entry("Dungeons.Etherwarp.Full Block")?.switchValue?.let { etherwarpFullBlock = it }
		FixifyConfig.entry("Dungeons.Etherwarp.Depth")?.switchValue?.let { etherwarpDepth = it }
		FixifyConfig.entry("Dungeons.Etherwarp.Left Click Mode")?.selected?.let {
			etherwarpLeftClickMode = it.coerceIn(0, etherwarpLeftClickModes.lastIndex)
		}
		FixifyConfig.entry("Dungeons.Etherwarp.Color")?.color?.let { etherwarpColor.setArgb(it) }
		FixifyConfig.entry("Dungeons.Etherwarp.Fail Color")?.color?.let { etherwarpFailColor.setArgb(it) }
		FixifyConfig.entry("Dungeons.Teammate Highlight.Archer Color")?.color?.let { archerColor.setArgb(it) }
		FixifyConfig.entry("Dungeons.Teammate Highlight.Berserker Color")?.color?.let { berserkerColor.setArgb(it) }
		FixifyConfig.entry("Dungeons.Teammate Highlight.Tank Color")?.color?.let { tankColor.setArgb(it) }
		FixifyConfig.entry("Dungeons.Teammate Highlight.Mage Color")?.color?.let { mageColor.setArgb(it) }
		FixifyConfig.entry("Dungeons.Teammate Highlight.Healer Color")?.color?.let { healerColor.setArgb(it) }

		FixifyConfig.entry("SkyBlock.Pet Overlay")?.enabled?.let { petOverlayEnabled = it }
		petOverlayType = selected("SkyBlock.Pet Overlay.Type", petOverlayType, petOverlayTypes.lastIndex)
		petOverlayShowItem = switch("SkyBlock.Pet Overlay.Show Pet Item", petOverlayShowItem)
		petOverlayInvert = switch("SkyBlock.Pet Overlay.Invert Level/XP Color", petOverlayInvert)
		petOverlayFlip = switch("SkyBlock.Pet Overlay.Flip Icon Position", petOverlayFlip)
		petOverlayAnchor = selected("SkyBlock.Pet Overlay.Anchor", petOverlayAnchor, hudAnchors.lastIndex)
		petOverlayScale = slider("SkyBlock.Pet Overlay.Scale", 0.5f, 2.0f, petOverlayScale)
		petOverlayTheme = selected("SkyBlock.Pet Overlay.Theme", petOverlayTheme, petOverlayThemes.lastIndex)
		petOverlayIdlePulse = switch("SkyBlock.Pet Overlay.Idle Pulse", petOverlayIdlePulse)
		petOverlayIdleHover = switch("SkyBlock.Pet Overlay.Idle Hover", petOverlayIdleHover)
		petOverlayLevelUpAnimation = switch("SkyBlock.Pet Overlay.Level Up Animation", petOverlayLevelUpAnimation)
		petOverlayValueAnimation = switch("SkyBlock.Pet Overlay.Level/XP Animation", petOverlayValueAnimation)
		petOverlayRainbowLevel = switch("SkyBlock.Pet Overlay.Rainbow Level", petOverlayRainbowLevel)
		petOverlayRainbowXp = switch("SkyBlock.Pet Overlay.Rainbow XP", petOverlayRainbowXp)
		petOverlayRainbowBackground = switch("SkyBlock.Pet Overlay.Rainbow Background", petOverlayRainbowBackground)
		color("SkyBlock.Pet Overlay.Level Color", petOverlayLevelColor)
		color("SkyBlock.Pet Overlay.XP Color", petOverlayXpColor)
		color("SkyBlock.Pet Overlay.Background Color", petOverlayBackgroundColor)
		petOverlayX = intValue("SkyBlock.Pet Overlay.Hud X", petOverlayX)
		petOverlayY = intValue("SkyBlock.Pet Overlay.Hud Y", petOverlayY)

		FixifyConfig.entry("SkyBlock.Pressure Display")?.enabled?.let { pressureDisplayEnabled = it }
		pressureDisplayShowAt = slider("SkyBlock.Pressure Display.Show At", 0.01f, 0.99f, pressureDisplayShowAt)
		pressureDisplayAnchor = selected("SkyBlock.Pressure Display.Anchor", pressureDisplayAnchor, hudAnchors.lastIndex)
		pressureDisplayScale = slider("SkyBlock.Pressure Display.Scale", 0.5f, 2.0f, pressureDisplayScale)
		pressureDisplayTheme = selected("SkyBlock.Pressure Display.Theme", pressureDisplayTheme, pressureThemes.lastIndex)
		pressureDisplayX = intValue("SkyBlock.Pressure Display.Hud X", pressureDisplayX)
		pressureDisplayY = intValue("SkyBlock.Pressure Display.Hud Y", pressureDisplayY)

		FixifyConfig.entry("SkyBlock.Drill Fuel Meter")?.enabled?.let { drillFuelMeterEnabled = it }
		drillFuelMeterAnchor = selected("SkyBlock.Drill Fuel Meter.Anchor", drillFuelMeterAnchor, hudAnchors.lastIndex)
		drillFuelMeterScale = slider("SkyBlock.Drill Fuel Meter.Scale", 0.5f, 2.0f, drillFuelMeterScale)
		drillFuelMeterTheme = selected("SkyBlock.Drill Fuel Meter.Theme", drillFuelMeterTheme, drillFuelThemes.lastIndex)
		drillFuelMeterX = intValue("SkyBlock.Drill Fuel Meter.Hud X", drillFuelMeterX)
		drillFuelMeterY = intValue("SkyBlock.Drill Fuel Meter.Hud Y", drillFuelMeterY)

		FixifyConfig.entry("Dungeons.Dungeon Score Meter")?.enabled?.let { dungeonScoreMeterEnabled = it }
		dungeonScoreMeterAnchor = selected("Dungeons.Dungeon Score Meter.Anchor", dungeonScoreMeterAnchor, hudAnchors.lastIndex)
		dungeonScoreMeterScale = slider("Dungeons.Dungeon Score Meter.Scale", 0.5f, 2.0f, dungeonScoreMeterScale)
		dungeonScoreMeterTheme = selected("Dungeons.Dungeon Score Meter.Theme", dungeonScoreMeterTheme, dungeonScoreThemes.lastIndex)
		dungeonScoreMeterGradientRotation = slider(
			"Dungeons.Dungeon Score Meter.Gradient Rotation",
			0.0f,
			1.0f,
			dungeonScoreMeterGradientRotation,
		)
		color("Dungeons.Dungeon Score Meter.Gradient 1st Color", dungeonScoreGradientColor1)
		color("Dungeons.Dungeon Score Meter.Gradient 2nd Color", dungeonScoreGradientColor2)
		dungeonScoreMeterX = intValue("Dungeons.Dungeon Score Meter.Hud X", dungeonScoreMeterX)
		dungeonScoreMeterY = intValue("Dungeons.Dungeon Score Meter.Hud Y", dungeonScoreMeterY)

		FixifyConfig.entry("SkyBlock.Low HP Indicator")?.enabled?.let { lowHpIndicatorEnabled = it }
		lowHpIndicatorTransparency = slider(
			"SkyBlock.Low HP Indicator.Transparency",
			0.2f,
			1.0f,
			lowHpIndicatorTransparency,
		)
		lowHpIndicatorHeartbeat = switch("SkyBlock.Low HP Indicator.Pulse Animation", lowHpIndicatorHeartbeat)

		FixifyConfig.entry("SkyBlock.Missing Enchants")?.enabled?.let { missingEnchantsEnabled = it }
		FixifyConfig.entry("SkyBlock.Compact Pet Level")?.enabled?.let { compactPetLevelEnabled = it }
		FixifyConfig.entry("SkyBlock.Action Bar Cleanup")?.enabled?.let {
			if (!it) {
				hidePressureInActionBar = false
				hideDrillFuelInActionBar = false
			}
		}
		if (FixifyConfig.entry("SkyBlock.Action Bar Cleanup")?.enabled != false) {
			hidePressureInActionBar = switch("SkyBlock.Action Bar Cleanup.Hide Pressure", hidePressureInActionBar)
			hideDrillFuelInActionBar = switch("SkyBlock.Action Bar Cleanup.Hide Drill Fuel", hideDrillFuelInActionBar)
		}
	}

	fun saveHudLayout(prefix: String, x: Int, y: Int, scale: Float) {
		FixifyConfig.updateEntry("$prefix.Hud X") { it.value = x.toString() }
		FixifyConfig.updateEntry("$prefix.Hud Y") { it.value = y.toString() }
		val range = 2.0f - 0.5f
		FixifyConfig.updateEntry("$prefix.Scale") {
			it.value = "${(scale * 100.0f).toInt()}%"
			it.sliderPercentage = ((scale - 0.5f) / range).coerceIn(0.0f, 1.0f)
		}
		loadFromConfig()
	}

	private fun selected(key: String, default: Int, lastIndex: Int): Int {
		return FixifyConfig.entry(key)?.selected?.coerceIn(0, lastIndex) ?: default
	}

	private fun switch(key: String, default: Boolean): Boolean {
		return FixifyConfig.entry(key)?.switchValue ?: default
	}

	private fun slider(key: String, min: Float, max: Float, default: Float): Float {
		val percentage = FixifyConfig.entry(key)?.sliderPercentage ?: return default
		return min + (max - min) * percentage.coerceIn(0.0f, 1.0f)
	}

	private fun intValue(key: String, default: Int): Int {
		return FixifyConfig.entry(key)?.value?.toIntOrNull() ?: default
	}

	private fun color(key: String, state: ColorState) {
		FixifyConfig.entry(key)?.color?.let(state::setArgb)
	}

	class ColorState(defaultArgb: Int) {
		var hue: Float = 0.0f
			private set
		var saturation: Float = 0.0f
			private set
		var brightness: Float = 0.0f
			private set
		var alpha: Float = 1.0f
			private set

		val argb: Int
			get() {
				val rgb = hsbToRgb(hue, saturation, brightness)
				val a = (alpha.coerceIn(0.0f, 1.0f) * 255.0f).toInt().coerceIn(0, 255)
				return (a shl 24) or (rgb and 0x00FFFFFF)
			}

		init {
			setArgb(defaultArgb)
		}

		fun setArgb(value: Int) {
			val r = (value ushr 16) and 0xFF
			val g = (value ushr 8) and 0xFF
			val b = value and 0xFF
			val hsb = rgbToHsb(r, g, b)
			hue = hsb[0]
			saturation = hsb[1]
			brightness = hsb[2]
			alpha = ((value ushr 24) and 0xFF) / 255.0f
		}

		fun setSaturationBrightness(newSaturation: Float, newBrightness: Float) {
			saturation = newSaturation.coerceIn(0.0f, 1.0f)
			brightness = newBrightness.coerceIn(0.0f, 1.0f)
		}

		fun setHue(newHue: Float) {
			hue = newHue.coerceIn(0.0f, 1.0f)
		}

		fun setAlpha(newAlpha: Float) {
			alpha = newAlpha.coerceIn(0.0f, 1.0f)
		}

		fun hueArgb(): Int {
			return (0xFF shl 24) or (hsbToRgb(hue, 1.0f, 1.0f) and 0x00FFFFFF)
		}

		fun hex(includeAlpha: Boolean = true): String {
			val value = argb
			val r = (value ushr 16) and 0xFF
			val g = (value ushr 8) and 0xFF
			val b = value and 0xFF
			val a = (value ushr 24) and 0xFF
			return if (includeAlpha) {
				String.format(Locale.ROOT, "%02X%02X%02X%02X", r, g, b, a)
			} else {
				String.format(Locale.ROOT, "%02X%02X%02X", r, g, b)
			}
		}
	}

	private fun rgbToHsb(r: Int, g: Int, b: Int): FloatArray {
		val red = r / 255.0f
		val green = g / 255.0f
		val blue = b / 255.0f
		val max = max(red, max(green, blue))
		val min = min(red, min(green, blue))
		val delta = max - min
		val hue = when {
			delta == 0.0f -> 0.0f
			max == red -> ((green - blue) / delta) % 6.0f
			max == green -> (blue - red) / delta + 2.0f
			else -> (red - green) / delta + 4.0f
		}
		val normalizedHue = (hue / 6.0f).let { if (it < 0.0f) it + 1.0f else it }
		val saturation = if (max == 0.0f) 0.0f else delta / max
		return floatArrayOf(normalizedHue, saturation, max)
	}

	fun hsbToRgb(hue: Float, saturation: Float, brightness: Float): Int {
		val h = (hue.coerceIn(0.0f, 1.0f) * 6.0f).let { if (it == 6.0f) 0.0f else it }
		val s = saturation.coerceIn(0.0f, 1.0f)
		val v = brightness.coerceIn(0.0f, 1.0f)
		val sector = h.toInt()
		val fraction = h - sector
		val p = v * (1.0f - s)
		val q = v * (1.0f - s * fraction)
		val t = v * (1.0f - s * (1.0f - fraction))
		val (r, g, b) = when (sector) {
			0 -> Triple(v, t, p)
			1 -> Triple(q, v, p)
			2 -> Triple(p, v, t)
			3 -> Triple(p, q, v)
			4 -> Triple(t, p, v)
			else -> Triple(v, p, q)
		}
		return ((r * 255.0f).toInt().coerceIn(0, 255) shl 16) or
			((g * 255.0f).toInt().coerceIn(0, 255) shl 8) or
			(b * 255.0f).toInt().coerceIn(0, 255)
	}
}
