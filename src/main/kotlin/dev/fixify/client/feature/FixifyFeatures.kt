package dev.fixify.client.feature

import dev.fixify.client.FixifyConfig
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object FixifyFeatures {
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

	var playerHiderEnabled: Boolean = false
	var playerHiderHidePlayers: Boolean = false
	var playerHiderHideAll: Boolean = false
	var playerHiderGhostMode: Boolean = false
	var playerHiderClickThrough: Boolean = false
	var playerHiderDistance: Float = 1.5f
	var playerHiderGhostOpacity: Float = 0.15f
	var playerSizeEnabled: Boolean = false
	var playerSizeScaleAllPlayers: Boolean = true
	var playerSizeX: Float = 1.0f
	var playerSizeY: Float = 1.0f
	var playerSizeZ: Float = 1.0f
	var dianaQolEnabled: Boolean = false
	var goldenFishCiEnabled: Boolean = false
	var leapFrogEnabled: Boolean = false
	var smartTermAcEnabled: Boolean = false
	var hitColorEnabled: Boolean = true
	var infiniteChatEnabled: Boolean = false
	var fullbrightEnabled: Boolean = false
	var zoomEnabled: Boolean = true
	var zoomIntensity: Int = 5
	var zoomScrollable: Boolean = true
	var performanceHudEnabled: Boolean = false
	var performanceHudDirection: Int = 0
	var performanceHudShowFps: Boolean = true
	var performanceHudShowTps: Boolean = true
	var performanceHudShowPing: Boolean = true
	var performanceHudAnchor: Int = 0
	var performanceHudScale: Float = 1.0f
	var performanceHudX: Int = 8
	var performanceHudY: Int = 8
	var renderOptimizerEnabled: Boolean = false
	var renderOptimizerHideFallingBlocks: Boolean = true
	var renderOptimizerHideLightning: Boolean = true
	var renderOptimizerHideExperienceOrbs: Boolean = true
	var renderOptimizerHideDeathAnimation: Boolean = true
	var renderOptimizerHideDyingArmorStands: Boolean = false
	var renderOptimizerHideExplosionParticles: Boolean = false
	var renderOptimizerHideArcherPassive: Boolean = true
	var renderOptimizerHideHealerFairy: Boolean = true
	var renderOptimizerHideSoulWeaver: Boolean = true
	var renderOptimizerHideTentacleHead: Boolean = true
	var renderOptimizerHideFireOverlay: Boolean = true
	var nameReplaceEnabled: Boolean = false
	var nameReplacement: String = "Fixify"
	var reminderEnabled: Boolean = true
	var reminderWarningDuration: Int = 5
	var reminderWarningScale: Float = 1.0f
	var reminderChatButton: Boolean = true

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
	var actionBarCleanupEnabled: Boolean = true
	var hidePressureInActionBar: Boolean = false
	var hideDrillFuelInActionBar: Boolean = false

	val etherwarpColor = ColorState(0x80FFAA00.toInt())
	val etherwarpFailColor = ColorState(0x80FF5555.toInt())
	val archerColor = ColorState(0xFFFFBC0A.toInt())
	val berserkerColor = ColorState(0xFF880015.toInt())
	val tankColor = ColorState(0xFF188037.toInt())
	val mageColor = ColorState(0xFF00A2E8.toInt())
	val healerColor = ColorState(0xFFFFAFCA.toInt())
	val hitColor = ColorState(0xB2FF0000.toInt())
	val performanceHudNameColor = ColorState(0xFFAAA4FF.toInt())
	val performanceHudValueColor = ColorState(0xFFFFFFFF.toInt())
	val nameReplaceColor = ColorState(0xFFAAA4FF.toInt())
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
	val performanceHudDirections = arrayOf("Horizontal", "Vertical")
	val reminderRules = mutableListOf(
		ReminderRule(true, "Forge Reminders", "7, 14, 21, 28", "/warp forge"),
		ReminderRule(false, "Spooky Festival", "29-31", "/calendar"),
		ReminderRule(false, "Reminder 3", "", ""),
		ReminderRule(false, "Reminder 4", "", ""),
		ReminderRule(false, "Reminder 5", "", ""),
	)

	private val roleColors = mapOf(
		'A' to archerColor,
		'B' to berserkerColor,
		'T' to tankColor,
		'M' to mageColor,
		'H' to healerColor,
	)

	fun colorForRole(role: Char): ColorState? = roleColors[role.uppercaseChar()]

	fun loadFromConfig() {
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

		FixifyConfig.entry("Visuals.Player Hider")?.enabled?.let { playerHiderEnabled = it }
		playerHiderHidePlayers = switch("Visuals.Player Hider.Hide Players", playerHiderHidePlayers)
		playerHiderHideAll = switch("Visuals.Player Hider.Hide All", playerHiderHideAll)
		playerHiderGhostMode = switch("Visuals.Player Hider.Ghost Mode", playerHiderGhostMode)
		playerHiderClickThrough = switch("Visuals.Player Hider.Click Through Players", playerHiderClickThrough)
		playerHiderDistance = slider("Visuals.Player Hider.Distance", 0.5f, 10.0f, playerHiderDistance)
		playerHiderGhostOpacity = slider("Visuals.Player Hider.Opacity", 0.0f, 1.0f, playerHiderGhostOpacity)

		FixifyConfig.entry("Visuals.Player Size")?.enabled?.let { playerSizeEnabled = it }
		playerSizeScaleAllPlayers = switch("Visuals.Player Size.Scale All Players", playerSizeScaleAllPlayers)
		playerSizeX = slider("Visuals.Player Size.X Scale", 0.1f, 3.0f, playerSizeX)
		playerSizeY = slider("Visuals.Player Size.Y Scale", -3.0f, 3.0f, playerSizeY)
		playerSizeZ = slider("Visuals.Player Size.Z Scale", 0.1f, 3.0f, playerSizeZ)

		FixifyConfig.entry("Misc.Diana QoL")?.enabled?.let { dianaQolEnabled = it }
		FixifyConfig.entry("Misc.Golden Fish CI")?.enabled?.let { goldenFishCiEnabled = it }
		FixifyConfig.entry("Misc.Leap Frog")?.enabled?.let { leapFrogEnabled = it }
		FixifyConfig.entry("Misc.Smart Term AC")?.enabled?.let { smartTermAcEnabled = it }
		FixifyConfig.entry("Visuals.Hit Color")?.enabled?.let { hitColorEnabled = it }
		color("Visuals.Hit Color.Color", hitColor)
		FixifyConfig.entry("Misc.Infinite Chat")?.enabled?.let { infiniteChatEnabled = it }
		FixifyConfig.entry("Visuals.Fullbright")?.enabled?.let { fullbrightEnabled = it }
		FixifyConfig.entry("Visuals.Zoom")?.enabled?.let { zoomEnabled = it }
		zoomIntensity = slider("Visuals.Zoom.Intensity", 1.0f, 10.0f, zoomIntensity.toFloat()).roundToInt().coerceIn(1, 10)
		zoomScrollable = switch("Visuals.Zoom.Scrollable", zoomScrollable)
		FixifyConfig.entry("Visuals.Performance HUD")?.enabled?.let { performanceHudEnabled = it }
		performanceHudDirection = selected(
			"Visuals.Performance HUD.Direction",
			performanceHudDirection,
			performanceHudDirections.lastIndex,
		)
		performanceHudShowFps = switch("Visuals.Performance HUD.Show FPS", performanceHudShowFps)
		performanceHudShowTps = switch("Visuals.Performance HUD.Show TPS", performanceHudShowTps)
		performanceHudShowPing = switch("Visuals.Performance HUD.Show Ping", performanceHudShowPing)
		performanceHudAnchor = selected("Visuals.Performance HUD.Anchor", performanceHudAnchor, hudAnchors.lastIndex)
		performanceHudScale = slider("Visuals.Performance HUD.Scale", 0.5f, 2.0f, performanceHudScale)
		color("Visuals.Performance HUD.Name Color", performanceHudNameColor)
		color("Visuals.Performance HUD.Value Color", performanceHudValueColor)
		performanceHudX = intValue("Visuals.Performance HUD.Hud X", performanceHudX)
		performanceHudY = intValue("Visuals.Performance HUD.Hud Y", performanceHudY)

		FixifyConfig.entry("Visuals.Render Optimizer")?.enabled?.let { renderOptimizerEnabled = it }
		renderOptimizerHideFallingBlocks = switch(
			"Visuals.Render Optimizer.Hide Falling Blocks",
			renderOptimizerHideFallingBlocks,
		)
		renderOptimizerHideLightning = switch("Visuals.Render Optimizer.Hide Lightning", renderOptimizerHideLightning)
		renderOptimizerHideExperienceOrbs = switch(
			"Visuals.Render Optimizer.Hide Experience Orbs",
			renderOptimizerHideExperienceOrbs,
		)
		renderOptimizerHideDeathAnimation = switch(
			"Visuals.Render Optimizer.Hide Death Animation",
			renderOptimizerHideDeathAnimation,
		)
		renderOptimizerHideDyingArmorStands = switch(
			"Visuals.Render Optimizer.Hide Dying Armor Stands",
			renderOptimizerHideDyingArmorStands,
		)
		renderOptimizerHideExplosionParticles = switch(
			"Visuals.Render Optimizer.Hide Explosion Particles",
			renderOptimizerHideExplosionParticles,
		)
		renderOptimizerHideArcherPassive = switch(
			"Visuals.Render Optimizer.Hide Archer Passive",
			renderOptimizerHideArcherPassive,
		)
		renderOptimizerHideHealerFairy = switch(
			"Visuals.Render Optimizer.Hide Healer Fairy",
			renderOptimizerHideHealerFairy,
		)
		renderOptimizerHideSoulWeaver = switch(
			"Visuals.Render Optimizer.Hide Soul Weaver",
			renderOptimizerHideSoulWeaver,
		)
		renderOptimizerHideTentacleHead = switch(
			"Visuals.Render Optimizer.Hide Tentacle Head",
			renderOptimizerHideTentacleHead,
		)
		renderOptimizerHideFireOverlay = switch(
			"Visuals.Render Optimizer.Hide Fire Overlay",
			renderOptimizerHideFireOverlay,
		)

		FixifyConfig.entry("Visuals.Name Replace")?.enabled?.let { nameReplaceEnabled = it }
		nameReplacement = stringValue("Visuals.Name Replace.Replacement", nameReplacement)
		color("Visuals.Name Replace.Color", nameReplaceColor)

		FixifyConfig.entry("Misc.Reminder")?.enabled?.let { reminderEnabled = it }
		reminderWarningDuration = slider(
			"Misc.Reminder.Warning Duration",
			1.0f,
			30.0f,
			reminderWarningDuration.toFloat(),
		).roundToInt()
		reminderWarningScale = slider(
			"Misc.Reminder.Warning Scale",
			0.5f,
			2.5f,
			reminderWarningScale,
		)
		reminderChatButton = switch("Misc.Reminder.Chat Command Button", reminderChatButton)
		for ((index, rule) in reminderRules.withIndex()) {
			val number = index + 1
			rule.enabled = switch("Misc.Reminder.Reminder $number Enabled", rule.enabled)
			rule.name = stringValue("Misc.Reminder.Reminder $number Name", rule.name)
			rule.days = stringValue("Misc.Reminder.Reminder $number Days", rule.days)
			rule.command = stringValue("Misc.Reminder.Reminder $number Command", rule.command)
		}
		HitColorFeature.refresh()
		NameReplaceFeature.refresh()

		FixifyConfig.entry("Visuals.Pet Overlay")?.enabled?.let { petOverlayEnabled = it }
		petOverlayType = selected("Visuals.Pet Overlay.Type", petOverlayType, petOverlayTypes.lastIndex)
		petOverlayShowItem = switch("Visuals.Pet Overlay.Show Pet Item", petOverlayShowItem)
		petOverlayInvert = switch("Visuals.Pet Overlay.Invert Level/XP Color", petOverlayInvert)
		petOverlayFlip = switch("Visuals.Pet Overlay.Flip Icon Position", petOverlayFlip)
		petOverlayAnchor = selected("Visuals.Pet Overlay.Anchor", petOverlayAnchor, hudAnchors.lastIndex)
		petOverlayScale = slider("Visuals.Pet Overlay.Scale", 0.5f, 2.0f, petOverlayScale)
		petOverlayTheme = selected("Visuals.Pet Overlay.Theme", petOverlayTheme, petOverlayThemes.lastIndex)
		petOverlayIdlePulse = switch("Visuals.Pet Overlay.Idle Pulse", petOverlayIdlePulse)
		petOverlayIdleHover = switch("Visuals.Pet Overlay.Idle Hover", petOverlayIdleHover)
		petOverlayLevelUpAnimation = switch("Visuals.Pet Overlay.Level Up Animation", petOverlayLevelUpAnimation)
		petOverlayValueAnimation = switch("Visuals.Pet Overlay.Level/XP Animation", petOverlayValueAnimation)
		petOverlayRainbowLevel = switch("Visuals.Pet Overlay.Rainbow Level", petOverlayRainbowLevel)
		petOverlayRainbowXp = switch("Visuals.Pet Overlay.Rainbow XP", petOverlayRainbowXp)
		petOverlayRainbowBackground = switch("Visuals.Pet Overlay.Rainbow Background", petOverlayRainbowBackground)
		color("Visuals.Pet Overlay.Level Color", petOverlayLevelColor)
		color("Visuals.Pet Overlay.XP Color", petOverlayXpColor)
		color("Visuals.Pet Overlay.Background Color", petOverlayBackgroundColor)
		petOverlayX = intValue("Visuals.Pet Overlay.Hud X", petOverlayX)
		petOverlayY = intValue("Visuals.Pet Overlay.Hud Y", petOverlayY)

		FixifyConfig.entry("Visuals.Pressure Display")?.enabled?.let { pressureDisplayEnabled = it }
		pressureDisplayShowAt = slider("Visuals.Pressure Display.Show At", 0.01f, 0.99f, pressureDisplayShowAt)
		pressureDisplayAnchor = selected("Visuals.Pressure Display.Anchor", pressureDisplayAnchor, hudAnchors.lastIndex)
		pressureDisplayScale = slider("Visuals.Pressure Display.Scale", 0.5f, 2.0f, pressureDisplayScale)
		pressureDisplayTheme = selected("Visuals.Pressure Display.Theme", pressureDisplayTheme, pressureThemes.lastIndex)
		pressureDisplayX = intValue("Visuals.Pressure Display.Hud X", pressureDisplayX)
		pressureDisplayY = intValue("Visuals.Pressure Display.Hud Y", pressureDisplayY)

		FixifyConfig.entry("Visuals.Drill Fuel Meter")?.enabled?.let { drillFuelMeterEnabled = it }
		drillFuelMeterAnchor = selected("Visuals.Drill Fuel Meter.Anchor", drillFuelMeterAnchor, hudAnchors.lastIndex)
		drillFuelMeterScale = slider("Visuals.Drill Fuel Meter.Scale", 0.5f, 2.0f, drillFuelMeterScale)
		drillFuelMeterTheme = selected("Visuals.Drill Fuel Meter.Theme", drillFuelMeterTheme, drillFuelThemes.lastIndex)
		drillFuelMeterX = intValue("Visuals.Drill Fuel Meter.Hud X", drillFuelMeterX)
		drillFuelMeterY = intValue("Visuals.Drill Fuel Meter.Hud Y", drillFuelMeterY)

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

		FixifyConfig.entry("Visuals.Low HP Indicator")?.enabled?.let { lowHpIndicatorEnabled = it }
		lowHpIndicatorTransparency = slider(
			"Visuals.Low HP Indicator.Transparency",
			0.2f,
			1.0f,
			lowHpIndicatorTransparency,
		)
		lowHpIndicatorHeartbeat = switch("Visuals.Low HP Indicator.Pulse Animation", lowHpIndicatorHeartbeat)

		FixifyConfig.entry("Misc.Missing Enchants")?.enabled?.let { missingEnchantsEnabled = it }
		FixifyConfig.entry("Misc.Compact Pet Level")?.enabled?.let { compactPetLevelEnabled = it }
		FixifyConfig.entry("Visuals.Action Bar Cleanup")?.enabled?.let { actionBarCleanupEnabled = it }
		hidePressureInActionBar = switch("Visuals.Action Bar Cleanup.Hide Pressure", hidePressureInActionBar)
		hideDrillFuelInActionBar = switch("Visuals.Action Bar Cleanup.Hide Drill Fuel", hideDrillFuelInActionBar)
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

	private fun stringValue(key: String, default: String): String {
		return FixifyConfig.entry(key)?.value ?: default
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

	data class ReminderRule(
		var enabled: Boolean,
		var name: String,
		var days: String,
		var command: String,
	)

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
