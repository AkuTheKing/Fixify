package dev.fixify.client

import com.mojang.blaze3d.platform.InputConstants
import dev.fixify.client.feature.FixifyFeatures
import dev.fixify.client.render.FixifyNvgPipRenderer
import dev.fixify.client.render.FixifyNvgRenderer
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.StringUtil
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sqrt

class FixifyMenuScreen : Screen(Component.literal("Fixify")) {
	private val clickTargets = ArrayList<ClickTarget>()
	private val shapeDraws = ArrayList<ShapeDraw>()
	private val textDraws = ArrayList<TextDraw>()
	private val columnDrawOrder = ArrayList<CategoryColumn>()
	private val openedAt = System.currentTimeMillis()

	private var closing = false
	private var closingAt = 0L
	private var layoutX = 0.0f
	private var layoutY = 0.0f
	private var layoutScale = 1.0f
	private var hoveredEntry: Entry? = null
	private var draggedColumn: CategoryColumn? = null
	private var draggedSlider: Entry? = null
	private var draggedColor: ColorDrag? = null
	private var dragOffsetX = 0.0f
	private var dragOffsetY = 0.0f
	private var currentTextClip: ClipRect? = null
	private var currentTargetClip: ClipRect? = null
	private var currentDrawColumn: CategoryColumn? = null
	private var searchQuery = ""
	private var searchFocused = false
	private var listeningKeybind: Entry? = null
	private var focusedColorHex: Entry? = null
	private var focusedTextEntry: Entry? = null

	private val columns = arrayListOf(
		CategoryColumn(
			"Dungeons",
			arrayOf(
				Entry.module("Dungeon Score Meter", FixifyFeatures.dungeonScoreMeterEnabled),
				Entry.mode("Anchor", "", FixifyFeatures.hudAnchors, FixifyFeatures.dungeonScoreMeterAnchor),
				Entry.slider("Scale", FixifyFeatures.dungeonScoreMeterScale * 100.0, 50.0, 200.0, 5.0, "%", 0),
				Entry.action("Open HUD Editor") {
					minecraft.setScreen(FixifyHudEditorScreen(this, FixifyHudWidget.DUNGEON_SCORE))
				},
				Entry.mode("Theme", "", FixifyFeatures.dungeonScoreThemes, FixifyFeatures.dungeonScoreMeterTheme),
				Entry.color("Gradient 1st Color", FixifyFeatures.dungeonScoreGradientColor1),
				Entry.color("Gradient 2nd Color", FixifyFeatures.dungeonScoreGradientColor2),
				Entry.slider(
					"Gradient Rotation",
					FixifyFeatures.dungeonScoreMeterGradientRotation * 360.0,
					0.0,
					360.0,
					1.0,
					"\u00B0",
					0,
				),
				Entry.module("Etherwarp", FixifyFeatures.etherwarpEnabled),
				Entry.setting("Show Guess", "x", FixifyFeatures.etherwarpShowGuess),
				Entry.color("Color", FixifyFeatures.etherwarpColor),
				Entry.setting("Show when failed", "x", FixifyFeatures.etherwarpShowFailed),
				Entry.color("Fail Color", FixifyFeatures.etherwarpFailColor),
				Entry.mode("Render Style", "", FixifyFeatures.etherwarpRenderStyles, FixifyFeatures.etherwarpRenderStyle),
				Entry.setting("Use Server Position", "x", FixifyFeatures.etherwarpUseServerPosition),
				Entry.setting("Full Block", "x", FixifyFeatures.etherwarpFullBlock),
				Entry.setting("Depth", "x", FixifyFeatures.etherwarpDepth),
				Entry.mode("Left Click Mode", "", FixifyFeatures.etherwarpLeftClickModes, FixifyFeatures.etherwarpLeftClickMode),
				Entry.setting("Keybind", "n/a", null),
				Entry.module("DungeonBreaker", FixifyFeatures.dungeonBreakerEnabled),
				Entry.setting("Prevent mining secrets", "x", FixifyFeatures.dungeonPreventMiningSecrets),
				Entry.setting("Insta-mine when fatigue", "x", FixifyFeatures.dungeonInstaMineWhenFatigue),
				Entry.setting("Keybind", "n/a", null),
				Entry.module("Teammate Highlight", FixifyFeatures.teammateHighlightEnabled),
				Entry.setting("Keybind", "n/a", null),
				Entry.color("Archer Color", FixifyFeatures.archerColor),
				Entry.color("Berserker Color", FixifyFeatures.berserkerColor),
				Entry.color("Tank Color", FixifyFeatures.tankColor),
				Entry.color("Mage Color", FixifyFeatures.mageColor),
				Entry.color("Healer Color", FixifyFeatures.healerColor),
			),
			MENU_WIDTH / 2.0f - COLUMN_WIDTH - 12.0f,
		),
		CategoryColumn(
			"Visuals",
			arrayOf(
				Entry.module("Player Hider", FixifyFeatures.playerHiderEnabled),
				Entry.setting("Hide Players", "x", FixifyFeatures.playerHiderHidePlayers),
				Entry.slider("Distance", FixifyFeatures.playerHiderDistance.toDouble(), 0.5, 10.0, 0.1, "", 1),
				Entry.setting("Hide All", "x", FixifyFeatures.playerHiderHideAll),
				Entry.setting("Ghost Mode", "x", FixifyFeatures.playerHiderGhostMode),
				Entry.slider("Opacity", FixifyFeatures.playerHiderGhostOpacity * 100.0, 0.0, 100.0, 1.0, "%", 0),
				Entry.setting("Click Through Players", "x", FixifyFeatures.playerHiderClickThrough),
				Entry.module("Player Size", FixifyFeatures.playerSizeEnabled),
				Entry.setting("Scale All Players", "x", FixifyFeatures.playerSizeScaleAllPlayers),
				Entry.slider("X Scale", FixifyFeatures.playerSizeX.toDouble(), 0.1, 3.0, 0.1, "", 1),
				Entry.slider("Y Scale", FixifyFeatures.playerSizeY.toDouble(), -3.0, 3.0, 0.1, "", 1),
				Entry.slider("Z Scale", FixifyFeatures.playerSizeZ.toDouble(), 0.1, 3.0, 0.1, "", 1),
				Entry.module("Hit Color", FixifyFeatures.hitColorEnabled),
				Entry.color("Color", FixifyFeatures.hitColor),
				Entry.module("Fullbright", FixifyFeatures.fullbrightEnabled),
				Entry.module("Performance HUD", FixifyFeatures.performanceHudEnabled),
				Entry.mode(
					"Direction",
					"",
					FixifyFeatures.performanceHudDirections,
					FixifyFeatures.performanceHudDirection,
				),
				Entry.setting("Show FPS", "x", FixifyFeatures.performanceHudShowFps),
				Entry.setting("Show TPS", "x", FixifyFeatures.performanceHudShowTps),
				Entry.setting("Show Ping", "x", FixifyFeatures.performanceHudShowPing),
				Entry.mode("Anchor", "", FixifyFeatures.hudAnchors, FixifyFeatures.performanceHudAnchor),
				Entry.slider("Scale", FixifyFeatures.performanceHudScale * 100.0, 50.0, 200.0, 5.0, "%", 0),
				Entry.action("Open HUD Editor") {
					minecraft.setScreen(FixifyHudEditorScreen(this, FixifyHudWidget.PERFORMANCE))
				},
				Entry.color("Name Color", FixifyFeatures.performanceHudNameColor),
				Entry.color("Value Color", FixifyFeatures.performanceHudValueColor),
				Entry.module("Render Optimizer", FixifyFeatures.renderOptimizerEnabled),
				Entry.setting("Hide Falling Blocks", "x", FixifyFeatures.renderOptimizerHideFallingBlocks),
				Entry.setting("Hide Lightning", "x", FixifyFeatures.renderOptimizerHideLightning),
				Entry.setting("Hide Experience Orbs", "x", FixifyFeatures.renderOptimizerHideExperienceOrbs),
				Entry.setting("Hide Death Animation", "x", FixifyFeatures.renderOptimizerHideDeathAnimation),
				Entry.setting("Hide Dying Armor Stands", "x", FixifyFeatures.renderOptimizerHideDyingArmorStands),
				Entry.setting("Hide Explosion Particles", "x", FixifyFeatures.renderOptimizerHideExplosionParticles),
				Entry.setting("Hide Archer Passive", "x", FixifyFeatures.renderOptimizerHideArcherPassive),
				Entry.setting("Hide Healer Fairy", "x", FixifyFeatures.renderOptimizerHideHealerFairy),
				Entry.setting("Hide Soul Weaver", "x", FixifyFeatures.renderOptimizerHideSoulWeaver),
				Entry.setting("Hide Tentacle Head", "x", FixifyFeatures.renderOptimizerHideTentacleHead),
				Entry.setting("Hide Fire Overlay", "x", FixifyFeatures.renderOptimizerHideFireOverlay),
				Entry.module("Name Replace", FixifyFeatures.nameReplaceEnabled),
				Entry.text("Replacement", FixifyFeatures.nameReplacement, "Fixify"),
				Entry.color("Color", FixifyFeatures.nameReplaceColor),
				Entry.module("Zoom", FixifyFeatures.zoomEnabled),
				Entry.slider("FOV", FixifyFeatures.zoomFov.toDouble(), 10.0, 110.0, 2.0, "", 0),
				Entry.setting("Scrollable", "x", FixifyFeatures.zoomScrollable),
				Entry.setting("Keybind", "C", null),
				Entry.module("Pet Overlay", FixifyFeatures.petOverlayEnabled),
				Entry.mode("Type", "", FixifyFeatures.petOverlayTypes, FixifyFeatures.petOverlayType),
				Entry.setting("Show Pet Item", "x", FixifyFeatures.petOverlayShowItem),
				Entry.setting("Invert Level/XP Color", "x", FixifyFeatures.petOverlayInvert),
				Entry.setting("Flip Icon Position", "x", FixifyFeatures.petOverlayFlip),
				Entry.mode("Anchor", "", FixifyFeatures.hudAnchors, FixifyFeatures.petOverlayAnchor),
				Entry.slider("Scale", FixifyFeatures.petOverlayScale * 100.0, 50.0, 200.0, 5.0, "%", 0),
				Entry.action("Open HUD Editor") {
					minecraft.setScreen(FixifyHudEditorScreen(this, FixifyHudWidget.PET))
				},
				Entry.mode("Theme", "", FixifyFeatures.petOverlayThemes, FixifyFeatures.petOverlayTheme),
				Entry.color("Level Color", FixifyFeatures.petOverlayLevelColor),
				Entry.color("XP Color", FixifyFeatures.petOverlayXpColor),
				Entry.color("Background Color", FixifyFeatures.petOverlayBackgroundColor),
				Entry.setting("Idle Pulse", "x", FixifyFeatures.petOverlayIdlePulse),
				Entry.setting("Idle Hover", "x", FixifyFeatures.petOverlayIdleHover),
				Entry.setting("Level Up Animation", "x", FixifyFeatures.petOverlayLevelUpAnimation),
				Entry.setting("Level/XP Animation", "x", FixifyFeatures.petOverlayValueAnimation),
				Entry.setting("Rainbow Level", "x", FixifyFeatures.petOverlayRainbowLevel),
				Entry.setting("Rainbow XP", "x", FixifyFeatures.petOverlayRainbowXp),
				Entry.setting("Rainbow Background", "x", FixifyFeatures.petOverlayRainbowBackground),
				Entry.module("Pressure Display", FixifyFeatures.pressureDisplayEnabled),
				Entry.slider("Show At", FixifyFeatures.pressureDisplayShowAt * 100.0, 1.0, 99.0, 1.0, "%", 0),
				Entry.mode("Anchor", "", FixifyFeatures.hudAnchors, FixifyFeatures.pressureDisplayAnchor),
				Entry.slider("Scale", FixifyFeatures.pressureDisplayScale * 100.0, 50.0, 200.0, 5.0, "%", 0),
				Entry.action("Open HUD Editor") {
					minecraft.setScreen(FixifyHudEditorScreen(this, FixifyHudWidget.PRESSURE))
				},
				Entry.mode("Theme", "", FixifyFeatures.pressureThemes, FixifyFeatures.pressureDisplayTheme),
				Entry.module("Low HP Indicator", FixifyFeatures.lowHpIndicatorEnabled),
				Entry.slider("Transparency", FixifyFeatures.lowHpIndicatorTransparency * 100.0, 20.0, 100.0, 1.0, "%", 0),
				Entry.setting("Pulse Animation", "x", FixifyFeatures.lowHpIndicatorHeartbeat),
				Entry.module("Drill Fuel Meter", FixifyFeatures.drillFuelMeterEnabled),
				Entry.mode("Anchor", "", FixifyFeatures.hudAnchors, FixifyFeatures.drillFuelMeterAnchor),
				Entry.slider("Scale", FixifyFeatures.drillFuelMeterScale * 100.0, 50.0, 200.0, 5.0, "%", 0),
				Entry.action("Open HUD Editor") {
					minecraft.setScreen(FixifyHudEditorScreen(this, FixifyHudWidget.DRILL_FUEL))
				},
				Entry.mode("Theme", "", FixifyFeatures.drillFuelThemes, FixifyFeatures.drillFuelMeterTheme),
				Entry.module("Action Bar Cleanup", FixifyFeatures.actionBarCleanupEnabled),
				Entry.setting("Hide Pressure", "x", FixifyFeatures.hidePressureInActionBar),
				Entry.setting("Hide Drill Fuel", "x", FixifyFeatures.hideDrillFuelInActionBar),
			),
			MENU_WIDTH / 2.0f + 12.0f,
		),
		CategoryColumn(
			"Misc",
			arrayOf(
				Entry.module("Diana QoL", FixifyFeatures.dianaQolEnabled),
				Entry.module("Golden Fish CI", FixifyFeatures.goldenFishCiEnabled),
				Entry.module("Leap Frog", FixifyFeatures.leapFrogEnabled),
				Entry.module("Smart Term AC", FixifyFeatures.smartTermAcEnabled),
				Entry.module("Infinite Chat", FixifyFeatures.infiniteChatEnabled),
				*reminderEntries(),
				Entry.module("Missing Enchants", FixifyFeatures.missingEnchantsEnabled),
				Entry.module("Compact Pet Level", FixifyFeatures.compactPetLevelEnabled),
			),
			MENU_WIDTH / 2.0f + COLUMN_WIDTH + 36.0f,
		),
	)

	private fun reminderEntries(): Array<Entry> {
		val entries = ArrayList<Entry>()
		entries.add(Entry.module("Reminder", FixifyFeatures.reminderEnabled))
		entries.add(
			Entry.slider(
				"Warning Duration",
				FixifyFeatures.reminderWarningDuration.toDouble(),
				1.0,
				30.0,
				1.0,
				"s",
				0,
			),
		)
		entries.add(
			Entry.slider(
				"Warning Scale",
				FixifyFeatures.reminderWarningScale * 100.0,
				50.0,
				250.0,
				5.0,
				"%",
				0,
			),
		)
		entries.add(Entry.setting("Chat Command Button", "x", FixifyFeatures.reminderChatButton))
		for ((index, rule) in FixifyFeatures.reminderRules.withIndex()) {
			val number = index + 1
			entries.add(Entry.setting("Reminder $number Enabled", "x", rule.enabled))
			entries.add(Entry.text("Reminder $number Name", rule.name, "Reminder $number"))
			entries.add(Entry.text("Reminder $number Days", rule.days, "7, 14 or 29-31"))
			entries.add(Entry.text("Reminder $number Command", rule.command, "/warp forge"))
		}
		return entries.toTypedArray()
	}

	override fun tick() {
		if (closing && closeProgress() <= 0.0f) {
			minecraft.setScreen(null)
		}
	}

	override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
		val progress = animationProgress()
		val eased = easeOutQuint(progress)
		val overlayAlpha = (99.0f * eased).roundToInt()
		shapeDraws.clear()
		textDraws.clear()

		graphics.fillGradient(0, 0, width, height, alpha(OVERLAY_TOP, overlayAlpha), alpha(OVERLAY_BOTTOM, overlayAlpha))
		drawOverlayTexture(graphics, eased)

		val fit = min((width - 28) / MENU_WIDTH.toFloat(), (height - 36) / MENU_HEIGHT.toFloat())
		val targetScale = min(fit * 0.82f, MAX_MENU_SCALE)
		layoutScale = targetScale * (0.91f + 0.09f * eased)
		layoutX = (width - MENU_WIDTH * layoutScale) / 2.0f
		layoutY = (height - MENU_HEIGHT * layoutScale) / 2.0f + (1.0f - eased) * 22.0f

		val baseMouseX = (mouseX - layoutX) / layoutScale
		val baseMouseY = (mouseY - layoutY) / layoutScale
		updateDraggedColumn(baseMouseX, baseMouseY)
		draggedSlider?.updateSlider(baseMouseX)
		draggedColor?.update(baseMouseX, baseMouseY)
		hoveredEntry = if (draggedColumn == null) findHoveredEntry(baseMouseX, baseMouseY) else null

		val pose = graphics.pose()
		pose.pushMatrix()
		pose.translate(layoutX, layoutY)
		pose.scale(layoutScale)

		clickTargets.clear()
		columnDrawOrder.clear()
		for (column in columns) {
			if (column != draggedColumn) {
				columnDrawOrder.add(column)
				drawColumn(graphics, column)
			}
		}
		draggedColumn?.let {
			columnDrawOrder.add(it)
			drawColumn(graphics, it)
		}
		currentDrawColumn = null
		drawSearchBar(graphics, baseMouseX, baseMouseY)

		pose.popMatrix()
		submitTextLayer(graphics, eased, layoutX, layoutY, layoutScale)
	}

	override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
		if (closing) {
			return true
		}

		val baseX = ((event.x() - layoutX) / layoutScale).toFloat()
		val baseY = ((event.y() - layoutY) / layoutScale).toFloat()

		listeningKeybind?.let {
			it.setKeybind(InputConstants.Type.MOUSE.getOrCreate(event.button()))
			listeningKeybind = null
			it.persist()
			it.parentModule?.persist()
			return true
		}

		if (isSearchHovered(baseX, baseY)) {
			finishTextEditing()
			searchFocused = event.button() == InputConstants.MOUSE_BUTTON_LEFT
			focusedColorHex = null
			return true
		}
		searchFocused = false
		focusedColorHex = null

		for (column in columns.reversed()) {
			if (!column.isHeaderHovered(baseX, baseY)) {
				continue
			}

			if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
				finishTextEditing()
				promoteColumn(column)
				dragOffsetX = column.state.x - baseX
				dragOffsetY = column.state.y - baseY
				draggedColumn = column
			} else if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
				promoteColumn(column)
				column.state.toggleExtended()
				column.state.persist(column.title)
			}
			return true
		}

		if (event.button() != InputConstants.MOUSE_BUTTON_LEFT && event.button() != InputConstants.MOUSE_BUTTON_RIGHT) {
			return true
		}

		for (target in clickTargets.asReversed()) {
			if (target.contains(baseX, baseY)) {
				if (target.entry.type == EntryType.TEXT && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
					focusTextEntry(target.entry)
					return true
				}
				finishTextEditing()
				if (target.entry.isKeybind() && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
					listeningKeybind = target.entry
					return true
				}
				if (target.entry.type == EntryType.SLIDER && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
					draggedSlider = target.entry
					target.entry.updateSlider(baseX)
					return true
				}
				if (target.entry.type == EntryType.COLOR && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
					when (target.optionIndex) {
						COLOR_MAIN_TARGET,
						COLOR_HUE_TARGET,
						COLOR_ALPHA_TARGET,
						-> {
							draggedColor = ColorDrag(target.entry, target.optionIndex)
							draggedColor?.update(baseX, baseY)
							return true
						}

						COLOR_HEX_TARGET -> {
							focusedColorHex = target.entry
							target.entry.syncColorHex()
							return true
						}
					}
				}
				target.entry.click(target.optionIndex, event.button())
				return true
			}
		}

		finishTextEditing()
		return true
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
		if (closing) {
			return true
		}

		val baseX = ((mouseX - layoutX) / layoutScale).toFloat()
		val baseY = ((mouseY - layoutY) / layoutScale).toFloat()
		val amount = (verticalAmount.sign * SCROLL_STEP).toFloat()
		for (column in columns.reversed()) {
			if (column.isPanelHovered(baseX, baseY) && column.state.scroll(amount)) {
				return true
			}
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
	}

	override fun mouseReleased(event: MouseButtonEvent): Boolean {
		if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
			draggedColumn?.let { it.state.persist(it.title) }
			draggedSlider?.persist()
			draggedColor?.entry?.persist()
			draggedColumn = null
			draggedSlider = null
			draggedColor = null
		}
		return true
	}

	override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
		if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
			val baseX = ((event.x() - layoutX) / layoutScale).toFloat()
			val baseY = ((event.y() - layoutY) / layoutScale).toFloat()
			draggedSlider?.updateSlider(baseX)
			draggedColor?.update(baseX, baseY)
		}
		return true
	}

	override fun keyPressed(event: KeyEvent): Boolean {
		focusedTextEntry?.let {
			when (event.key()) {
				InputConstants.KEY_ESCAPE,
				InputConstants.KEY_RETURN,
				InputConstants.KEY_NUMPADENTER,
				-> finishTextEditing()

				InputConstants.KEY_BACKSPACE -> it.removeTextCharacter()
				InputConstants.KEY_DELETE -> it.clearText()
				else -> if (event.isPaste()) {
					it.appendText(minecraft.keyboardHandler.clipboard)
				}
			}
			return true
		}

		listeningKeybind?.let {
			when (event.key()) {
				InputConstants.KEY_ESCAPE,
				InputConstants.KEY_BACKSPACE,
				-> it.clearKeybind()

				InputConstants.KEY_RETURN,
				InputConstants.KEY_NUMPADENTER,
				-> Unit

				else -> it.setKeybind(InputConstants.getKey(event))
			}
			listeningKeybind = null
			it.persist()
			it.parentModule?.persist()
			return true
		}

		if (event.key() == InputConstants.KEY_RSHIFT) {
			beginClose()
			return true
		}

		focusedColorHex?.let {
			when (event.key()) {
				InputConstants.KEY_ESCAPE,
				InputConstants.KEY_RETURN,
				InputConstants.KEY_NUMPADENTER,
				-> focusedColorHex = null

				InputConstants.KEY_BACKSPACE -> it.removeColorHexChar()
				InputConstants.KEY_DELETE -> it.clearColorHex()
				else -> if (event.isPaste()) {
					it.appendColorHex(minecraft.keyboardHandler.clipboard)
				}
			}
			return true
		}

		if (searchFocused) {
			when (event.key()) {
				InputConstants.KEY_ESCAPE,
				InputConstants.KEY_RETURN,
				InputConstants.KEY_NUMPADENTER,
				-> searchFocused = false

				InputConstants.KEY_BACKSPACE -> if (searchQuery.isNotEmpty()) {
					setSearchQuery(searchQuery.dropLast(1))
				}

				InputConstants.KEY_DELETE -> setSearchQuery("")
				else -> if (event.isPaste()) {
					appendSearch(minecraft.keyboardHandler.clipboard)
				}
			}
			return true
		}

		if (event.key() == InputConstants.KEY_ESCAPE) {
			beginClose()
			return true
		}

		hoveredEntry?.let {
			if (it.type == EntryType.SLIDER) {
				val direction = when (event.key()) {
					InputConstants.KEY_RIGHT,
						InputConstants.KEY_EQUALS,
						-> 1

					InputConstants.KEY_LEFT,
						InputConstants.KEY_MINUS,
						-> -1

					else -> 0
				}
				if (direction != 0) {
					it.nudgeSlider(direction)
					it.persist()
					return true
				}
			}
		}
		return super.keyPressed(event)
	}

	override fun charTyped(event: CharacterEvent): Boolean {
		focusedTextEntry?.let {
			it.appendText(event.codepointAsString())
			return true
		}

		focusedColorHex?.let {
			it.appendColorHex(event.codepointAsString())
			return true
		}

		if (!searchFocused) {
			return super.charTyped(event)
		}
		appendSearch(event.codepointAsString())
		return true
	}

	override fun shouldCloseOnEsc(): Boolean = false

	override fun isPauseScreen(): Boolean = false

	fun isClosing(): Boolean = closing

	fun beginClose() {
		if (!closing) {
			finishTextEditing()
			closing = true
			closingAt = System.currentTimeMillis()
		}
	}

	private fun focusTextEntry(entry: Entry) {
		if (focusedTextEntry != entry) {
			finishTextEditing()
		}
		searchFocused = false
		focusedColorHex = null
		focusedTextEntry = entry
	}

	private fun finishTextEditing() {
		focusedTextEntry?.persist()
		focusedTextEntry = null
	}

	private fun animationProgress(): Float {
		return if (closing) {
			closeProgress()
		} else {
			clamp((System.currentTimeMillis() - openedAt) / OPEN_ANIMATION_MS.toFloat())
		}
	}

	private fun closeProgress(): Float {
		return 1.0f - clamp((System.currentTimeMillis() - closingAt) / CLOSE_ANIMATION_MS.toFloat())
	}

	private fun findHoveredEntry(mouseX: Float, mouseY: Float): Entry? {
		for (target in clickTargets.asReversed()) {
			if (target.contains(mouseX, mouseY)) {
				return target.entry
			}
		}
		return null
	}

	private fun updateDraggedColumn(mouseX: Float, mouseY: Float) {
		val column = draggedColumn ?: return
		column.state.x = mouseX + dragOffsetX
		column.state.y = mouseY + dragOffsetY
	}

	private fun promoteColumn(column: CategoryColumn) {
		columns.remove(column)
		columns.add(column)
	}

	private fun appendSearch(value: String) {
		setSearchQuery((searchQuery + StringUtil.filterText(value)).take(MAX_SEARCH_LENGTH))
	}

	private fun setSearchQuery(value: String) {
		if (value == searchQuery) {
			return
		}
		searchQuery = value
		for (column in columns) {
			column.state.scrollOffset = 0.0f
		}
	}

	private fun drawOverlayTexture(graphics: GuiGraphicsExtractor, alphaValue: Float) {
		val vignette = alpha(VIGNETTE, (59.0f * alphaValue).roundToInt())
		graphics.fillGradient(0, 0, width, 95, vignette, 0x00000000)
		graphics.fillGradient(0, height - 110, width, height, 0x00000000, vignette)
		graphics.fill(0, 0, 18, height, alpha(0x73000000, (48.0f * alphaValue).roundToInt()))
		graphics.fill(width - 18, 0, width, height, alpha(0x73000000, (48.0f * alphaValue).roundToInt()))
	}

	private fun drawColumn(graphics: GuiGraphicsExtractor, column: CategoryColumn) {
		currentDrawColumn = column
		val x = column.state.x.roundToInt()
		val y = column.state.y.roundToInt()
		val contentHeight = columnContentHeight(column)
		val targetHeight = columnTargetHeight(contentHeight)
		val visibleHeight = column.state.updateHeight(targetHeight)
		column.state.lastHeight = visibleHeight
		column.state.lastContentHeight = contentHeight
		column.state.coerceScroll()

		if (visibleHeight <= COLUMN_HEADER_HEIGHT) {
			roundedRect(graphics, x, y, COLUMN_WIDTH, COLUMN_HEADER_HEIGHT, 8, PANEL_BACKGROUND)
		} else if (visibleHeight >= COLUMN_HEIGHT) {
			graphics.enableScissor(x, y, x + COLUMN_WIDTH, y + visibleHeight)
			texture(graphics, PANEL_TEXTURE, x, y, COLUMN_WIDTH, COLUMN_HEIGHT)
			graphics.disableScissor()
		} else {
			roundedRect(graphics, x, y, COLUMN_WIDTH, visibleHeight, 8, PANEL_BACKGROUND)
		}
		graphics.fill(x + 1, y + 38, x + COLUMN_WIDTH - 1, y + 39, PANEL_EDGE)
		val titleWidth = nvgTextWidth(column.title, TITLE_TEXT_SIZE).roundToInt()
		text(graphics, column.title, x + (COLUMN_WIDTH - titleWidth) / 2, y + 13, TEXT, TITLE_TEXT_SIZE)

		if (visibleHeight <= COLUMN_BODY_TOP) {
			currentDrawColumn = null
			return
		}

		val bodyClip = ClipRect(x, y + COLUMN_HEADER_HEIGHT, COLUMN_WIDTH, visibleHeight - COLUMN_HEADER_HEIGHT)
		currentTextClip = bodyClip
		currentTargetClip = bodyClip
		graphics.enableScissor(bodyClip.x, bodyClip.y, bodyClip.right, bodyClip.bottom)
		drawColumnEntries(graphics, column, x + 10, y + COLUMN_BODY_TOP + column.state.scrollOffset.roundToInt(), bodyClip)
		graphics.disableScissor()
		currentTextClip = null
		currentTargetClip = null
		currentDrawColumn = null
	}

	private fun columnTargetHeight(contentHeight: Int): Int {
		return min(COLUMN_HEIGHT, contentHeight).coerceAtLeast(COLUMN_HEADER_HEIGHT)
	}

	private fun columnContentHeight(column: CategoryColumn): Int {
		val content = if (searchQuery.isBlank()) {
			measureFullEntries(column)
		} else {
			column.entries
				.filter { it.type == EntryType.MODULE && it.label.contains(searchQuery, ignoreCase = true) }
				.sumOf { entryHeight(it) }
		}
		if (content == 0) {
			return COLUMN_HEADER_HEIGHT
		}
		return COLUMN_BODY_TOP + content + COLUMN_BOTTOM_PADDING
	}

	private fun measureFullEntries(column: CategoryColumn): Int {
		var height = 0
		var i = 0
		while (i < column.entries.size) {
			val entry = column.entries[i]
			if (entry.type != EntryType.MODULE) {
				height += entryHeight(entry)
				i++
				continue
			}

			height += entryHeight(entry)
			val childHeight = moduleChildHeight(column, i)
			if (childHeight > 0) {
				height += (childHeight * entry.moduleExtension()).roundToInt()
			}
			i = nextModuleIndex(column, i + 1)
		}
		return height
	}

	private fun drawColumnEntries(graphics: GuiGraphicsExtractor, column: CategoryColumn, x: Int, startY: Int, bodyClip: ClipRect) {
		if (searchQuery.isNotBlank()) {
			var y = startY
			for (entry in column.entries) {
				if (entry.type == EntryType.MODULE && entry.label.contains(searchQuery, ignoreCase = true)) {
					y += drawEntry(graphics, entry, x, y)
				}
			}
			return
		}

		var y = startY
		var i = 0
		while (i < column.entries.size) {
			val entry = column.entries[i]
			if (entry.type != EntryType.MODULE) {
				y += drawEntry(graphics, entry, x, y)
				i++
				continue
			}

			y += drawEntry(graphics, entry, x, y)
			val nextModule = nextModuleIndex(column, i + 1)
			val childFullHeight = moduleChildHeight(column, i)
			val childVisibleHeight = (childFullHeight * entry.moduleExtension()).roundToInt()
			if (childVisibleHeight > 0) {
				val childY = y
				val childClip = bodyClip.intersection(ClipRect(x - 10, childY, COLUMN_WIDTH, childVisibleHeight))
				if (childClip != null) {
					val previousTextClip = currentTextClip
					val previousTargetClip = currentTargetClip
					currentTextClip = childClip
					currentTargetClip = childClip
					graphics.enableScissor(childClip.x, childClip.y, childClip.right, childClip.bottom)
					smoothRoundedRect(x, childY, ROW_WIDTH, childVisibleHeight, MODULE_CONFIG_RADIUS, MODULE_CONFIG_BACKGROUND)
					var drawY = childY
					for (childIndex in i + 1 until nextModule) {
						drawY += drawEntry(graphics, column.entries[childIndex], x, drawY)
					}
					graphics.disableScissor()
					currentTextClip = previousTextClip
					currentTargetClip = previousTargetClip
				}
				y = childY + childVisibleHeight
			}
			i = nextModule
		}
	}

	private fun nextModuleIndex(column: CategoryColumn, startIndex: Int): Int {
		var index = startIndex
		while (index < column.entries.size && column.entries[index].type != EntryType.MODULE) {
			index++
		}
		return index
	}

	private fun moduleChildHeight(column: CategoryColumn, moduleIndex: Int): Int {
		var height = 0
		val nextModule = nextModuleIndex(column, moduleIndex + 1)
		for (i in moduleIndex + 1 until nextModule) {
			height += entryHeight(column.entries[i])
		}
		return height
	}

	private fun drawSearchBar(graphics: GuiGraphicsExtractor, mouseX: Float, mouseY: Float) {
		val hovered = isSearchHovered(mouseX, mouseY)
		val border = if (searchFocused) ACCENT else if (hovered) TEXT_DIM else DETAIL
		smoothRoundedRect(SEARCH_X, SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT, 9, border)
		smoothRoundedRect(SEARCH_X + 2, SEARCH_Y + 2, SEARCH_WIDTH - 4, SEARCH_HEIGHT - 4, 7, PANEL_BACKGROUND)

		val value = if (searchQuery.isEmpty()) SEARCH_PLACEHOLDER else searchQuery
		val color = if (searchQuery.isEmpty()) TEXT_DIM else TEXT
		val textWidth = nvgTextWidth(value, SEARCH_TEXT_SIZE)
		val textX = SEARCH_X + (SEARCH_WIDTH - textWidth).roundToInt() / 2
		text(graphics, value, textX, SEARCH_Y + 10, color, SEARCH_TEXT_SIZE)

		if (searchFocused && (System.currentTimeMillis() / 250L) % 2L == 0L) {
			val caretX = textX + textWidth.roundToInt() + 2
			smoothRoundedRect(caretX, SEARCH_Y + 9, 1, SEARCH_HEIGHT - 18, 0, ACCENT)
		}
	}

	private fun isSearchHovered(mouseX: Float, mouseY: Float): Boolean {
		return mouseX >= SEARCH_X &&
			mouseX <= SEARCH_X + SEARCH_WIDTH &&
			mouseY >= SEARCH_Y &&
			mouseY <= SEARCH_Y + SEARCH_HEIGHT
	}

	private fun drawEntry(graphics: GuiGraphicsExtractor, entry: Entry, x: Int, y: Int): Int {
		when (entry.type) {
			EntryType.MODULE -> {
				drawModule(graphics, entry, x, y)
				addTarget(entry, x, y, ROW_WIDTH, MODULE_ROW_HEIGHT, -1)
			}

			EntryType.SETTING -> {
				drawSetting(graphics, entry, x, y)
				if (entry.switchValue != null) {
					addTarget(entry, x + ROW_WIDTH - TOGGLE_WIDTH - 3, y + 5, TOGGLE_WIDTH, TOGGLE_HEIGHT, -1)
				} else if (entry.isKeybind()) {
					val bounds = keybindBounds(entry, x, y)
					addTarget(entry, bounds.x, bounds.y, bounds.width, bounds.height, -1)
				}
			}

			EntryType.SLIDER -> {
				drawSlider(graphics, entry, x, y)
				addTarget(entry, x + SLIDER_HORIZONTAL_INSET, y + SLIDER_TRACK_Y - 5, SLIDER_TRACK_WIDTH, SLIDER_CLICK_HEIGHT, -1)
			}

			EntryType.TEXT -> {
				drawTextEntry(graphics, entry, x, y)
				addTarget(entry, x + 2, y + TEXT_INPUT_Y, ROW_WIDTH - 4, TEXT_INPUT_HEIGHT, -1)
			}

			EntryType.TAB -> {
				centeredText(graphics, entry.label, x + 78, y + 1, ACCENT)
			}

			EntryType.MODE -> {
				drawMode(graphics, entry, x, y)
			}

			EntryType.COLOR -> {
				drawColor(graphics, entry, x, y)
			}

			EntryType.TAGS -> {
				drawTags(graphics, entry, x, y)
			}

			EntryType.SMALL_TEXT -> {
				text(graphics, entry.label, x + 26, y, TEXT_MUTED)
			}

			EntryType.DROPDOWN -> {
				drawDropdown(graphics, entry, x, y)
			}

			EntryType.ACTION -> {
				drawAction(graphics, entry, x, y)
				addTarget(entry, x, y, ROW_WIDTH, ACTION_ENTRY_HEIGHT, -1)
			}

			EntryType.SPACE -> Unit
		}
		return entryHeight(entry)
	}

	private fun entryHeight(entry: Entry): Int {
		return when (entry.type) {
			EntryType.MODULE -> MODULE_ENTRY_HEIGHT
			EntryType.SETTING -> SETTING_ENTRY_HEIGHT
			EntryType.SLIDER -> SLIDER_SETTING_HEIGHT
			EntryType.TEXT -> TEXT_ENTRY_HEIGHT
			EntryType.TAB -> 18
			EntryType.MODE -> modeHeight(entry)
			EntryType.COLOR -> colorHeight(entry)
			EntryType.TAGS -> 20
			EntryType.SMALL_TEXT -> 16
			EntryType.DROPDOWN -> 80
			EntryType.ACTION -> ACTION_ENTRY_HEIGHT
			EntryType.SPACE -> entry.height
		}
	}

	private fun drawAction(graphics: GuiGraphicsExtractor, entry: Entry, x: Int, y: Int) {
		val hovered = hoveredEntry == entry
		smoothRoundedRect(x + 2, y + 3, ROW_WIDTH - 4, ACTION_ENTRY_HEIGHT - 7, 6, if (hovered) ROW_ALT else DETAIL_DARK)
		smoothRoundedOutline(x + 2, y + 3, ROW_WIDTH - 4, ACTION_ENTRY_HEIGHT - 7, 6, 1.0f, if (hovered) WHITE else ACCENT)
		centeredText(graphics, entry.label, x + ROW_WIDTH / 2, y + 8, if (hovered) WHITE else TEXT)
	}

	private fun drawModule(graphics: GuiGraphicsExtractor, entry: Entry, x: Int, y: Int) {
		val hover = hoveredEntry == entry
		val label = fitTextToWidth(entry.label, ROW_WIDTH - if (entry.hasChildren) 32 else 18, TEXT_SIZE)
		if (entry.enabled) {
			texture(graphics, ROW_ACTIVE_TEXTURE, x, y, ROW_WIDTH, MODULE_ROW_HEIGHT)
			text(graphics, label, x + 12, y + 8, WHITE)
		} else {
			texture(graphics, if (hover) ROW_HOVER_TEXTURE else ROW_TEXTURE, x, y, ROW_WIDTH, MODULE_ROW_HEIGHT)
			text(graphics, label, x + 12, y + 8, if (hover) TEXT else TEXT_MUTED)
		}
		if (entry.hasChildren) {
			text(graphics, if (entry.settingsExpanded) "-" else "+", x + ROW_WIDTH - 17, y + 8, if (hover || entry.enabled) WHITE else TEXT_DIM)
		}
	}

	private fun drawSetting(graphics: GuiGraphicsExtractor, entry: Entry, x: Int, y: Int) {
		val switchValue = entry.switchValue
		val reserve = when {
			switchValue != null -> TOGGLE_WIDTH + 12
			entry.isKeybind() -> keybindBounds(entry, x, y).width + 12
			entry.value != "o" -> selectorWidth(entry.value) + 12
			else -> 0
		}
		text(graphics, fitTextToWidth(entry.label, ROW_WIDTH - reserve - 4, TEXT_SIZE), x + 2, y + 7, TEXT)
		if (switchValue != null) {
			drawToggle(graphics, entry, x + ROW_WIDTH - TOGGLE_WIDTH - 3, y + 5)
		} else if (entry.value == "o") {
			return
		} else if (entry.isKeybind()) {
			drawKeybind(graphics, entry, x, y)
		} else {
			drawSelector(graphics, entry.value, x, y)
		}
	}

	private fun drawSlider(graphics: GuiGraphicsExtractor, entry: Entry, x: Int, y: Int) {
		val valueWidth = nvgTextWidth(entry.value).roundToInt()
		text(graphics, fitTextToWidth(entry.label, ROW_WIDTH - valueWidth - 12, TEXT_SIZE), x + 2, y, TEXT)
		text(graphics, entry.value, x + ROW_WIDTH - valueWidth - 2, y, TEXT)
		val trackX = x + SLIDER_HORIZONTAL_INSET
		val trackY = y + SLIDER_TRACK_Y
		entry.sliderTrackX = trackX.toFloat()
		entry.sliderTrackWidth = SLIDER_TRACK_WIDTH.toFloat()
		smoothRoundedRect(trackX, trackY, SLIDER_TRACK_WIDTH, SLIDER_TRACK_HEIGHT, SLIDER_TRACK_RADIUS, DETAIL_DARK)
		val fillWidth = (SLIDER_TRACK_WIDTH * entry.sliderPercentage).roundToInt()
		if (fillWidth > 0) {
			smoothRoundedRect(trackX, trackY, max(SLIDER_TRACK_HEIGHT, fillWidth), SLIDER_TRACK_HEIGHT, SLIDER_TRACK_RADIUS, ACCENT)
		}
		smoothCircle(
			trackX + SLIDER_TRACK_WIDTH * entry.sliderPercentage,
			trackY + SLIDER_TRACK_HEIGHT / 2.0f,
			if (draggedSlider == entry || hoveredEntry == entry) SLIDER_KNOB_HOVER_RADIUS else SLIDER_KNOB_RADIUS,
			WHITE,
		)
	}

	private fun drawTextEntry(graphics: GuiGraphicsExtractor, entry: Entry, x: Int, y: Int) {
		val focused = focusedTextEntry == entry
		text(graphics, entry.label, x + 2, y, TEXT)
		val boxX = x + 2
		val boxY = y + TEXT_INPUT_Y
		val boxWidth = ROW_WIDTH - 4
		smoothRoundedRect(boxX, boxY, boxWidth, TEXT_INPUT_HEIGHT, TEXT_INPUT_RADIUS, DETAIL_DARK)
		smoothRoundedOutline(
			boxX,
			boxY,
			boxWidth,
			TEXT_INPUT_HEIGHT,
			TEXT_INPUT_RADIUS,
			if (focused) 1.5f else 1.0f,
			if (focused) WHITE else ACCENT,
		)
		val displayed = if (entry.value.isEmpty() && !focused) entry.placeholder else entry.value
		val color = if (entry.value.isEmpty() && !focused) TEXT_DIM else TEXT_MUTED
		val fitted = fitTextToWidth(displayed, boxWidth - 14, TEXT_INPUT_TEXT_SIZE)
		text(graphics, fitted, boxX + 7, boxY + 5, color, TEXT_INPUT_TEXT_SIZE)
		if (focused && (System.currentTimeMillis() / 250L) % 2L == 0L) {
			val caretX = boxX + 7 + nvgTextWidth(fitted, TEXT_INPUT_TEXT_SIZE).roundToInt() + 1
			smoothRoundedRect(caretX.coerceAtMost(boxX + boxWidth - 6), boxY + 4, 1, TEXT_INPUT_HEIGHT - 8, 0, WHITE)
		}
	}

	private fun drawMode(graphics: GuiGraphicsExtractor, entry: Entry, x: Int, y: Int) {
		val selected = entry.options!![entry.selected]
		val selectedWidth = nvgTextWidth(selected, SELECTOR_TEXT_SIZE)
		val pillWidth = selectorWidth(selected)
		val pillX = x + ROW_WIDTH - pillWidth - 3
		val pillY = y + (SELECTOR_BASE_HEIGHT - SELECTOR_HEIGHT) / 2
		text(graphics, fitTextToWidth(entry.label, pillX - x - 8, TEXT_SIZE), x + 2, y + 6, TEXT)
		smoothRoundedRect(pillX, pillY, pillWidth, SELECTOR_HEIGHT, SELECTOR_RADIUS, SELECTOR_PILL_BACKGROUND)
		smoothRoundedOutline(pillX, pillY, pillWidth, SELECTOR_HEIGHT, SELECTOR_RADIUS, 1.0f, ACCENT)
		text(graphics, selected, pillX + ((pillWidth - selectedWidth) / 2.0f).roundToInt(), pillY + 4, TEXT_MUTED, SELECTOR_TEXT_SIZE)
		addTarget(entry, x, y, ROW_WIDTH, SELECTOR_BASE_HEIGHT, MODE_TOGGLE_TARGET)

		val extension = entry.selectorExtension()
		val optionsHeight = (entry.options.size * SELECTOR_OPTION_HEIGHT * extension).roundToInt()
		if (optionsHeight <= 0) {
			return
		}

		val listX = x + SELECTOR_LIST_INSET
		val listY = y + SELECTOR_LIST_TOP
		val listWidth = ROW_WIDTH - SELECTOR_LIST_INSET * 2
		val previousTextClip = currentTextClip
		val previousTargetClip = currentTargetClip
		val listClip = previousTextClip?.intersection(ClipRect(listX, listY, listWidth, optionsHeight))
			?: ClipRect(listX, listY, listWidth, optionsHeight)
		currentTextClip = listClip
		currentTargetClip = listClip
		smoothRoundedRect(listX, listY, listWidth, optionsHeight, SELECTOR_RADIUS, SELECTOR_DROPDOWN_BACKGROUND)
		for (i in entry.options.indices) {
			val optionY = listY + i * SELECTOR_OPTION_HEIGHT
			val option = entry.options[i]
			if (i == entry.selected) {
				smoothRoundedRect(listX, optionY, listWidth, SELECTOR_OPTION_HEIGHT, SELECTOR_RADIUS, SELECTOR_SELECTED_BACKGROUND)
			}
			val optionWidth = nvgTextWidth(option, SELECTOR_TEXT_SIZE)
			text(
				graphics,
				option,
				listX + ((listWidth - optionWidth) / 2.0f).roundToInt(),
				optionY + 7,
				if (i == entry.selected) WHITE else TEXT,
				SELECTOR_TEXT_SIZE,
			)
			addTarget(entry, listX, optionY, listWidth, SELECTOR_OPTION_HEIGHT, i)
		}
		currentTextClip = previousTextClip
		currentTargetClip = previousTargetClip
	}

	private fun modeHeight(entry: Entry): Int {
		return SELECTOR_BASE_HEIGHT + (entry.options!!.size * SELECTOR_OPTION_HEIGHT * entry.selectorExtension()).roundToInt()
	}

	private fun drawColor(graphics: GuiGraphicsExtractor, entry: Entry, x: Int, y: Int) {
		val color = entry.colorState ?: return
		text(graphics, fitTextToWidth(entry.label, ROW_WIDTH - COLOR_SWATCH_WIDTH - 15, TEXT_SIZE), x + 2, y + 9, TEXT)
		val swatchX = x + ROW_WIDTH - COLOR_SWATCH_WIDTH - 5
		val swatchY = y + 6
		smoothRoundedRect(swatchX, swatchY, COLOR_SWATCH_WIDTH, COLOR_SWATCH_HEIGHT, COLOR_SWATCH_RADIUS, color.argb)
		smoothRoundedOutline(swatchX, swatchY, COLOR_SWATCH_WIDTH, COLOR_SWATCH_HEIGHT, COLOR_SWATCH_RADIUS, 1.25f, darken(color.argb, 0.55f))
		addTarget(entry, swatchX, swatchY, COLOR_SWATCH_WIDTH, COLOR_SWATCH_HEIGHT, COLOR_SWATCH_TARGET)

		val extension = entry.colorExtension()
		val visibleHeight = (COLOR_PICKER_HEIGHT * extension).roundToInt()
		if (visibleHeight <= 0) {
			return
		}

		val pickerY = y + COLOR_BASE_HEIGHT
		val previousTextClip = currentTextClip
		val previousTargetClip = currentTargetClip
		val pickerClip = previousTextClip?.intersection(ClipRect(x, pickerY, ROW_WIDTH, visibleHeight))
			?: ClipRect(x, pickerY, ROW_WIDTH, visibleHeight)
		currentTextClip = pickerClip
		currentTargetClip = pickerClip

		val areaX = x + COLOR_PICKER_INSET
		val areaY = pickerY + COLOR_AREA_TOP
		val areaWidth = ROW_WIDTH - COLOR_PICKER_INSET * 2
		smoothGradientRect(areaX, areaY, areaWidth, COLOR_AREA_HEIGHT, COLOR_PICKER_RADIUS, WHITE, color.hueArgb(), false)
		smoothGradientRect(areaX, areaY, areaWidth, COLOR_AREA_HEIGHT + 1, COLOR_PICKER_RADIUS, 0x00000000, 0xFF000000.toInt(), true)
		smoothRoundedOutline(areaX, areaY, areaWidth, COLOR_AREA_HEIGHT, COLOR_PICKER_RADIUS, 1.0f, DETAIL)
		entry.colorMainX = areaX.toFloat()
		entry.colorMainY = areaY.toFloat()
		entry.colorMainWidth = areaWidth.toFloat()
		entry.colorMainHeight = COLOR_AREA_HEIGHT.toFloat()
		addTarget(entry, areaX, areaY, areaWidth, COLOR_AREA_HEIGHT, COLOR_MAIN_TARGET)

		val sbX = areaX + color.saturation * areaWidth
		val sbY = areaY + (1.0f - color.brightness) * COLOR_AREA_HEIGHT
		drawColorPointer(sbX, sbY, color.argb)

		val hueY = areaY + COLOR_AREA_HEIGHT + COLOR_BAR_GAP
		drawHueBar(areaX, hueY, areaWidth, COLOR_BAR_HEIGHT)
		smoothRoundedOutline(areaX, hueY, areaWidth, COLOR_BAR_HEIGHT, COLOR_PICKER_RADIUS, 1.0f, DETAIL)
		entry.colorHueX = areaX.toFloat()
		entry.colorHueWidth = areaWidth.toFloat()
		addTarget(entry, areaX, hueY, areaWidth, COLOR_BAR_HEIGHT, COLOR_HUE_TARGET)
		drawColorPointer(areaX + color.hue * areaWidth, hueY + COLOR_BAR_HEIGHT / 2.0f, color.hueArgb())

		val alphaY = hueY + COLOR_BAR_HEIGHT + COLOR_ALPHA_GAP
		val opaqueColor = (0xFF shl 24) or (color.argb and 0x00FFFFFF)
		smoothGradientRect(areaX, alphaY, areaWidth, COLOR_BAR_HEIGHT, COLOR_PICKER_RADIUS, color.argb and 0x00FFFFFF, opaqueColor, false)
		smoothRoundedOutline(areaX, alphaY, areaWidth, COLOR_BAR_HEIGHT, COLOR_PICKER_RADIUS, 1.0f, DETAIL)
		entry.colorAlphaX = areaX.toFloat()
		entry.colorAlphaWidth = areaWidth.toFloat()
		addTarget(entry, areaX, alphaY, areaWidth, COLOR_BAR_HEIGHT, COLOR_ALPHA_TARGET)
		drawColorPointer(areaX + color.alpha * areaWidth, alphaY + COLOR_BAR_HEIGHT / 2.0f, WHITE)

		val hexY = alphaY + COLOR_BAR_HEIGHT + COLOR_HEX_GAP
		val hexX = x + (ROW_WIDTH - COLOR_HEX_WIDTH) / 2
		val focused = focusedColorHex == entry
		smoothRoundedRect(hexX, hexY, COLOR_HEX_WIDTH, COLOR_HEX_HEIGHT, COLOR_HEX_RADIUS, DETAIL_DARK)
		smoothRoundedOutline(hexX, hexY, COLOR_HEX_WIDTH, COLOR_HEX_HEIGHT, COLOR_HEX_RADIUS, if (focused) 1.5f else 1.0f, if (focused) WHITE else ACCENT)
		val hex = entry.colorHexText()
		val hexWidth = nvgTextWidth(hex, COLOR_HEX_TEXT_SIZE)
		text(graphics, hex, hexX + ((COLOR_HEX_WIDTH - hexWidth) / 2.0f).roundToInt(), hexY + 6, if (focused) WHITE else TEXT_MUTED, COLOR_HEX_TEXT_SIZE)
		addTarget(entry, hexX, hexY, COLOR_HEX_WIDTH, COLOR_HEX_HEIGHT, COLOR_HEX_TARGET)

		currentTextClip = previousTextClip
		currentTargetClip = previousTargetClip
	}

	private fun drawHueBar(x: Int, y: Int, width: Int, height: Int) {
		for (i in 0 until COLOR_HUE_STEPS) {
			val start = i / COLOR_HUE_STEPS.toFloat()
			val next = (i + 1) / COLOR_HUE_STEPS.toFloat()
			val segmentX = x + (width * start).roundToInt()
			val segmentRight = x + (width * next).roundToInt()
			val segmentWidth = max(1, segmentRight - segmentX + 1)
			val rgb = FixifyFeatures.hsbToRgb(start, 1.0f, 1.0f)
			smoothRoundedRect(segmentX, y, segmentWidth, height, 0, (0xFF shl 24) or rgb)
		}
	}

	private fun drawColorPointer(x: Float, y: Float, color: Int) {
		smoothCircle(x, y, COLOR_POINTER_OUTER_RADIUS, WHITE)
		smoothCircle(x, y, COLOR_POINTER_INNER_RADIUS, (0xFF shl 24) or (color and 0x00FFFFFF))
	}

	private fun colorHeight(entry: Entry): Int {
		return COLOR_BASE_HEIGHT + (COLOR_PICKER_HEIGHT * entry.colorExtension()).roundToInt()
	}

	private fun drawTags(graphics: GuiGraphicsExtractor, entry: Entry, x: Int, y: Int) {
		var tagX = x + 2
		for (i in entry.options!!.indices) {
			val option = entry.options[i]
			val w = nvgTextWidth(option).roundToInt() + 9
			roundedRect(graphics, tagX, y, w, 16, 3, if (i == entry.selected) DETAIL else ROW_ALT)
			text(graphics, option, tagX + 5, y + 4, if (i == entry.selected) TEXT_MUTED else TEXT_DIM)
			addTarget(entry, tagX, y, w, 16, i)
			tagX += w + 4
		}
	}

	private fun drawDropdown(graphics: GuiGraphicsExtractor, entry: Entry, x: Int, y: Int) {
		text(graphics, entry.label, x + 2, y, TEXT)
		text(graphics, entry.options!![entry.selected], x + 18, y + 18, TEXT)
		texture(graphics, DROPDOWN_TEXTURE, x + 10, y + 31, 132, 54)
		for (i in entry.options.indices) {
			val optionY = y + 37 + i * 16
			if (i == entry.selected) {
				graphics.fill(x + 10, optionY - 3, x + 142, optionY + 13, 0x7732323B)
			}
			text(graphics, entry.options[i], x + 18, optionY, if (i == entry.selected) TEXT else TEXT_MUTED)
			addTarget(entry, x + 10, optionY - 3, 132, 16, i)
		}
	}

	private fun drawToggle(graphics: GuiGraphicsExtractor, entry: Entry, x: Int, y: Int) {
		val progress = entry.switchProgress()
		smoothRoundedRect(x, y, TOGGLE_WIDTH, TOGGLE_HEIGHT, TOGGLE_RADIUS, mixColor(DETAIL_DARK, ACCENT, progress))
		smoothRoundedOutline(x, y, TOGGLE_WIDTH, TOGGLE_HEIGHT, TOGGLE_RADIUS, 1.25f, ACCENT)
		val knobTravel = TOGGLE_WIDTH - TOGGLE_KNOB_SIZE - TOGGLE_KNOB_INSET * 2
		val knobCenterX = x + TOGGLE_KNOB_INSET + TOGGLE_KNOB_SIZE / 2.0f + knobTravel * progress
		smoothCircle(knobCenterX, y + TOGGLE_HEIGHT / 2.0f, TOGGLE_KNOB_SIZE / 2.0f, WHITE)
	}

	private fun drawKeybind(graphics: GuiGraphicsExtractor, entry: Entry, x: Int, y: Int) {
		val displayValue = when {
			listeningKeybind == entry -> "..."
			entry.value == "n/a" -> "None"
			else -> entry.value.removeSuffix(" X")
		}
		val textWidth = nvgTextWidth(displayValue, KEYBIND_TEXT_SIZE)
		val bounds = keybindBounds(entry, x, y)
		smoothRoundedRect(bounds.x, bounds.y, bounds.width, bounds.height, KEYBIND_RADIUS, DETAIL_DARK)
		smoothRoundedOutline(
			bounds.x,
			bounds.y,
			bounds.width,
			bounds.height,
			KEYBIND_RADIUS,
			if (listeningKeybind == entry) 1.5f else 1.0f,
			if (listeningKeybind == entry) WHITE else ACCENT,
		)
		text(
			graphics,
			displayValue,
			bounds.x + ((bounds.width - textWidth) / 2.0f).roundToInt(),
			bounds.y + 4,
			if (listeningKeybind == entry) WHITE else TEXT_MUTED,
			KEYBIND_TEXT_SIZE,
		)
	}

	private fun keybindBounds(entry: Entry, x: Int, y: Int): ClipRect {
		val displayValue = when {
			listeningKeybind == entry -> "..."
			entry.value == "n/a" -> "None"
			else -> entry.value.removeSuffix(" X")
		}
		val width = max(KEYBIND_MIN_WIDTH, nvgTextWidth(displayValue, KEYBIND_TEXT_SIZE).roundToInt() + KEYBIND_HORIZONTAL_PADDING * 2)
		return ClipRect(x + ROW_WIDTH - width - 3, y + (SETTING_ENTRY_HEIGHT - KEYBIND_HEIGHT) / 2, width, KEYBIND_HEIGHT)
	}

	private fun drawSelector(graphics: GuiGraphicsExtractor, value: String, x: Int, y: Int) {
		val textWidth = nvgTextWidth(value, SELECTOR_TEXT_SIZE)
		val width = selectorWidth(value)
		val selectorX = x + ROW_WIDTH - width - 3
		smoothRoundedRect(selectorX, y + 1, width, SELECTOR_HEIGHT, SELECTOR_RADIUS, DETAIL_DARK)
		smoothRoundedOutline(selectorX, y + 1, width, SELECTOR_HEIGHT, SELECTOR_RADIUS, 1.0f, MODE_OUTLINE)
		text(
			graphics,
			value,
			selectorX + ((width - textWidth) / 2.0f).roundToInt(),
			y + 5,
			TEXT_MUTED,
			SELECTOR_TEXT_SIZE,
		)
	}

	private fun text(graphics: GuiGraphicsExtractor, value: String, x: Int, y: Int, color: Int, size: Float = TEXT_SIZE) {
		textDraws.add(TextDraw(value, x.toFloat(), y.toFloat(), color, size, false, currentTextClip, currentDrawColumn))
	}

	private fun centeredText(graphics: GuiGraphicsExtractor, value: String, x: Int, y: Int, color: Int, size: Float = TEXT_SIZE) {
		textDraws.add(TextDraw(value, x.toFloat(), y.toFloat(), color, size, true, currentTextClip, currentDrawColumn))
	}

	private fun smoothRoundedRect(x: Int, y: Int, width: Int, height: Int, radius: Int, color: Int) {
		shapeDraws.add(ShapeDraw(ShapeType.ROUNDED_RECT, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat(), 0.0f, color, color, false, currentTextClip, currentDrawColumn))
	}

	private fun smoothGradientRect(x: Int, y: Int, width: Int, height: Int, radius: Int, startColor: Int, endColor: Int, vertical: Boolean) {
		shapeDraws.add(ShapeDraw(ShapeType.GRADIENT_RECT, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat(), 0.0f, startColor, endColor, vertical, currentTextClip, currentDrawColumn))
	}

	private fun smoothRoundedOutline(x: Int, y: Int, width: Int, height: Int, radius: Int, thickness: Float, color: Int) {
		shapeDraws.add(ShapeDraw(ShapeType.ROUNDED_OUTLINE, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat(), thickness, color, color, false, currentTextClip, currentDrawColumn))
	}

	private fun smoothCircle(x: Float, y: Float, radius: Float, color: Int) {
		shapeDraws.add(ShapeDraw(ShapeType.CIRCLE, x, y, radius, 0.0f, 0.0f, 0.0f, color, color, false, currentTextClip, currentDrawColumn))
	}

	private fun submitTextLayer(
		graphics: GuiGraphicsExtractor,
		alphaValue: Float,
		x: Float,
		y: Float,
		scale: Float,
	) {
		val draws = ArrayList(textDraws)
		val shapes = ArrayList(shapeDraws)
		FixifyNvgPipRenderer.draw(graphics, 0, 0, width, height) {
			FixifyNvgRenderer.push()
			FixifyNvgRenderer.globalAlpha(alphaValue)
			fun renderShapes(values: List<ShapeDraw>) {
				for (shape in values) {
					for (clip in visibleClips(shape.owner, shape.clip)) {
						if (clip != null) {
							pushScreenScissor(x, y, scale, clip)
						}

						val shapeX = x + shape.x * scale
						val shapeY = y + shape.y * scale
						when (shape.type) {
							ShapeType.ROUNDED_RECT -> FixifyNvgRenderer.roundedRect(
								shapeX,
								shapeY,
								shape.width * scale,
								shape.height * scale,
								shape.radius * scale,
								shape.color,
							)

							ShapeType.ROUNDED_OUTLINE -> FixifyNvgRenderer.roundedOutline(
								shapeX,
								shapeY,
								shape.width * scale,
								shape.height * scale,
								shape.radius * scale,
								shape.thickness * scale,
								shape.color,
							)

							ShapeType.GRADIENT_RECT -> FixifyNvgRenderer.gradientRect(
								shapeX,
								shapeY,
								shape.width * scale,
								shape.height * scale,
								shape.radius * scale,
								shape.color,
								shape.color2,
								shape.vertical,
							)

							ShapeType.CIRCLE -> FixifyNvgRenderer.circle(
								shapeX,
								shapeY,
								shape.width * scale,
								shape.color,
							)
						}

						if (clip != null) {
							FixifyNvgRenderer.pop()
						}
					}
				}
			}

			fun renderTexts(values: List<TextDraw>) {
				for (draw in values) {
					for (clip in visibleClips(draw.owner, draw.clip)) {
						if (clip != null) {
							pushScreenScissor(x, y, scale, clip)
						}
						val textX = if (draw.centered) {
							draw.x - FixifyNvgRenderer.textWidth(draw.value, draw.size) / 2.0f
						} else {
							draw.x
						}
						FixifyNvgRenderer.text(
							draw.value,
							(x + textX * scale).roundToInt().toFloat(),
							(y + draw.y * scale).roundToInt().toFloat(),
							draw.size * scale,
							draw.color,
						)
						if (clip != null) {
							FixifyNvgRenderer.pop()
						}
					}
				}
			}

			renderShapes(shapes.filter { it.owner != null })
			renderTexts(draws.filter { it.owner != null })
			renderShapes(shapes.filter { it.owner == null })
			renderTexts(draws.filter { it.owner == null })
			FixifyNvgRenderer.pop()
		}
	}

	private fun pushScreenScissor(x: Float, y: Float, scale: Float, clip: ClipRect) {
		FixifyNvgRenderer.push()
		FixifyNvgRenderer.scissor(
			(x + clip.x * scale).roundToInt().toFloat(),
			(y + clip.y * scale).roundToInt().toFloat(),
			(clip.width * scale).roundToInt().toFloat(),
			(clip.height * scale).roundToInt().toFloat(),
		)
	}

	private fun visibleClips(owner: CategoryColumn?, requestedClip: ClipRect?): List<ClipRect?> {
		if (owner == null) {
			return listOf(requestedClip)
		}

		val ownerClip = ClipRect(
			owner.state.x.roundToInt(),
			owner.state.y.roundToInt(),
			COLUMN_WIDTH,
			owner.state.lastHeight,
		)
		val baseClip = requestedClip?.intersection(ownerClip) ?: ownerClip
		var clips = listOf(baseClip)
		val ownerIndex = columnDrawOrder.indexOf(owner)
		if (ownerIndex < 0) {
			return clips
		}

		for (i in ownerIndex + 1 until columnDrawOrder.size) {
			val above = columnDrawOrder[i]
			val cover = ClipRect(
				above.state.x.roundToInt(),
				above.state.y.roundToInt(),
				COLUMN_WIDTH,
				above.state.lastHeight,
			)
			clips = clips.flatMap { subtractClip(it, cover) }
			if (clips.isEmpty()) {
				break
			}
		}
		return clips
	}

	private fun subtractClip(source: ClipRect, cover: ClipRect): List<ClipRect> {
		val overlap = source.intersection(cover) ?: return listOf(source)
		val pieces = ArrayList<ClipRect>(4)
		if (overlap.y > source.y) {
			pieces.add(ClipRect(source.x, source.y, source.width, overlap.y - source.y))
		}
		if (overlap.bottom < source.bottom) {
			pieces.add(ClipRect(source.x, overlap.bottom, source.width, source.bottom - overlap.bottom))
		}
		if (overlap.x > source.x) {
			pieces.add(ClipRect(source.x, overlap.y, overlap.x - source.x, overlap.height))
		}
		if (overlap.right < source.right) {
			pieces.add(ClipRect(overlap.right, overlap.y, source.right - overlap.right, overlap.height))
		}
		return pieces
	}

	private fun nvgTextWidth(value: String, size: Float = TEXT_SIZE): Float = FixifyNvgRenderer.textWidth(value, size)

	private fun selectorWidth(value: String): Int {
		return max(SELECTOR_MIN_WIDTH, nvgTextWidth(value, SELECTOR_TEXT_SIZE).roundToInt() + SELECTOR_HORIZONTAL_PADDING * 2)
	}

	private fun fitTextToWidth(value: String, maxWidth: Int, size: Float): String {
		if (maxWidth <= 0 || nvgTextWidth(value, size) <= maxWidth) {
			return value
		}
		var end = value.length
		while (end > 0) {
			val candidate = value.take(end).trimEnd() + "..."
			if (nvgTextWidth(candidate, size) <= maxWidth) {
				return candidate
			}
			end--
		}
		return ""
	}

	private fun addTarget(entry: Entry, x: Int, y: Int, width: Int, height: Int, optionIndex: Int) {
		val clip = currentTargetClip
		if (clip == null) {
			clickTargets.add(ClickTarget(entry, optionIndex, x, y, width, height))
			return
		}

		val clippedX = max(x, clip.x)
		val clippedY = max(y, clip.y)
		val clippedRight = min(x + width, clip.right)
		val clippedBottom = min(y + height, clip.bottom)
		if (clippedRight > clippedX && clippedBottom > clippedY) {
			clickTargets.add(ClickTarget(entry, optionIndex, clippedX, clippedY, clippedRight - clippedX, clippedBottom - clippedY))
		}
	}

	private fun texture(graphics: GuiGraphicsExtractor, texture: Identifier, x: Int, y: Int, width: Int, height: Int) {
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			texture,
			x,
			y,
			0.0f,
			0.0f,
			width,
			height,
			width * TEXTURE_SCALE,
			height * TEXTURE_SCALE,
			width * TEXTURE_SCALE,
			height * TEXTURE_SCALE,
		)
	}

	private fun roundedRect(graphics: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, r: Int, color: Int) {
		if (r <= 0) {
			graphics.fill(x, y, x + w, y + h, color)
			return
		}
		for (row in 0 until h) {
			val inset = roundedInset(row, h, r)
			graphics.fill(x + inset, y + row, x + w - inset, y + row + 1, color)
		}
	}

	private fun drawRoundedOutline(
		graphics: GuiGraphicsExtractor,
		x: Int,
		y: Int,
		w: Int,
		h: Int,
		r: Int,
		thickness: Int,
		color: Int,
	) {
		roundedRect(graphics, x, y, w, h, r, color)
		roundedRect(graphics, x + thickness, y + thickness, w - thickness * 2, h - thickness * 2, max(0, r - thickness), DETAIL_DARK)
	}

	private fun roundedInset(row: Int, h: Int, r: Int): Int {
		val local = if (row < r) row else h - 1 - row
		if (local >= r) {
			return 0
		}
		val dy = r - local - 0.5
		return max(0, ceil(r - sqrt(max(0.0, r * r - dy * dy))).toInt())
	}

	private data class TextDraw(
		val value: String,
		val x: Float,
		val y: Float,
		val color: Int,
		val size: Float,
		val centered: Boolean,
		val clip: ClipRect?,
		val owner: CategoryColumn?,
	)

	private data class ShapeDraw(
		val type: ShapeType,
		val x: Float,
		val y: Float,
		val width: Float,
		val height: Float,
		val radius: Float,
		val thickness: Float,
		val color: Int,
		val color2: Int,
		val vertical: Boolean,
		val clip: ClipRect?,
		val owner: CategoryColumn?,
	)

	private enum class ShapeType {
		ROUNDED_RECT,
		ROUNDED_OUTLINE,
		GRADIENT_RECT,
		CIRCLE,
	}

	private data class ClipRect(val x: Int, val y: Int, val width: Int, val height: Int) {
		val right: Int get() = x + width
		val bottom: Int get() = y + height

		fun intersection(other: ClipRect): ClipRect? {
			val clippedX = max(x, other.x)
			val clippedY = max(y, other.y)
			val clippedRight = min(right, other.right)
			val clippedBottom = min(bottom, other.bottom)
			if (clippedRight <= clippedX || clippedBottom <= clippedY) {
				return null
			}
			return ClipRect(clippedX, clippedY, clippedRight - clippedX, clippedBottom - clippedY)
		}
	}

	private class CategoryColumn(
		val title: String,
		val entries: Array<Entry>,
		defaultX: Float,
	) {
		val state = COLUMN_STATES.getOrPut(title) {
			val saved = FixifyConfig.column(title)
			ColumnState(
				saved?.x ?: defaultX,
				saved?.y ?: COLUMN_TOP.toFloat(),
				saved?.extended ?: true,
			)
		}

		init {
			var currentModule: Entry? = null
			for (entry in entries) {
				if (entry.type == EntryType.MODULE) {
					currentModule = entry
					entry.bindStorage("$title.${entry.label}")
				} else if (entry.type != EntryType.SPACE) {
					currentModule?.hasChildren = true
					entry.parentModule = currentModule
					entry.bindStorage("$title.${currentModule?.label ?: "General"}.${entry.label.ifEmpty { entry.type.name }}")
				}
			}
		}

		fun isHeaderHovered(mouseX: Float, mouseY: Float): Boolean {
			return mouseX >= state.x &&
				mouseX <= state.x + COLUMN_WIDTH &&
				mouseY >= state.y &&
				mouseY <= state.y + COLUMN_HEADER_HEIGHT
		}

		fun isPanelHovered(mouseX: Float, mouseY: Float): Boolean {
			return state.extended &&
				mouseX >= state.x &&
				mouseX <= state.x + COLUMN_WIDTH &&
				mouseY >= state.y &&
				mouseY <= state.y + state.lastHeight
		}
	}

	private data class ColumnState(
		var x: Float,
		var y: Float,
		var extended: Boolean,
		var currentHeight: Float = if (extended) COLUMN_HEIGHT.toFloat() else COLUMN_HEADER_HEIGHT.toFloat(),
		var lastHeight: Int = if (extended) COLUMN_HEIGHT else COLUMN_HEADER_HEIGHT,
		var lastContentHeight: Int = COLUMN_HEIGHT,
		var scrollOffset: Float = 0.0f,
		var animationUpdatedAt: Long = System.currentTimeMillis(),
	) {
		fun toggleExtended() {
			extended = !extended
		}

		fun persist(title: String) {
			FixifyConfig.updateColumn(title) {
				it.x = x
				it.y = y
				it.extended = extended
			}
		}

		fun updateHeight(targetHeight: Int): Int {
			val now = System.currentTimeMillis()
			val elapsed = (now - animationUpdatedAt).coerceAtLeast(0L)
			animationUpdatedAt = now
			val target = if (extended) targetHeight.toFloat() else COLUMN_HEADER_HEIGHT.toFloat()
			val step = elapsed / COLLAPSE_ANIMATION_MS.toFloat()
			currentHeight += (target - currentHeight) * easeOutCubic(step.coerceIn(0.0f, 1.0f))
			if (kotlin.math.abs(target - currentHeight) < 0.5f) {
				currentHeight = target
			}
			return currentHeight.roundToInt().coerceIn(COLUMN_HEADER_HEIGHT, COLUMN_HEIGHT)
		}

		fun scroll(amount: Float): Boolean {
			if (!extended) {
				return false
			}
			val old = scrollOffset
			scrollOffset += amount
			coerceScroll()
			return old != scrollOffset
		}

		fun coerceScroll() {
			val minScroll = min(0.0f, lastHeight - lastContentHeight.toFloat())
			scrollOffset = scrollOffset.coerceIn(minScroll, 0.0f)
			if (lastContentHeight <= lastHeight) {
				scrollOffset = 0.0f
			}
		}
	}

	private data class ClickTarget(
		val entry: Entry,
		val optionIndex: Int,
		val x: Int,
		val y: Int,
		val width: Int,
		val height: Int,
	) {
		fun contains(mouseX: Float, mouseY: Float): Boolean {
			return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
		}
	}

	private data class ColorDrag(val entry: Entry, val section: Int) {
		fun update(mouseX: Float, mouseY: Float) {
			entry.updateColor(section, mouseX, mouseY)
		}
	}

	private enum class EntryType {
		MODULE,
		SETTING,
		SLIDER,
		TEXT,
		TAB,
		MODE,
		COLOR,
		TAGS,
		SMALL_TEXT,
		DROPDOWN,
		ACTION,
		SPACE,
	}

	private class Entry private constructor(
		val type: EntryType,
		val label: String,
		var value: String,
		val options: Array<String>?,
		var enabled: Boolean,
		var switchValue: Boolean?,
		var selected: Int,
		val amount: Int,
		val height: Int,
		val colorState: FixifyFeatures.ColorState? = null,
		var hasChildren: Boolean = false,
		var settingsExpanded: Boolean = false,
		var settingsProgress: Float = 0.0f,
		var settingsAnimationUpdatedAt: Long = System.currentTimeMillis(),
		var toggleProgress: Float = if (switchValue == true) 1.0f else 0.0f,
		var toggleAnimationUpdatedAt: Long = System.currentTimeMillis(),
		var selectorExpanded: Boolean = false,
		var selectorProgress: Float = 0.0f,
		var selectorAnimationUpdatedAt: Long = System.currentTimeMillis(),
		var sliderPercentage: Float = if (type == EntryType.SLIDER) {
			(amount / SLIDER_LEGACY_WIDTH.toFloat()).coerceIn(0.0f, 1.0f)
		} else {
			0.0f
		},
		var sliderTrackX: Float = 0.0f,
		var sliderTrackWidth: Float = 1.0f,
		var colorExpanded: Boolean = false,
		var colorProgress: Float = 0.0f,
		var colorAnimationUpdatedAt: Long = System.currentTimeMillis(),
		var colorMainX: Float = 0.0f,
		var colorMainY: Float = 0.0f,
		var colorMainWidth: Float = 1.0f,
		var colorMainHeight: Float = 1.0f,
		var colorHueX: Float = 0.0f,
		var colorHueWidth: Float = 1.0f,
		var colorAlphaX: Float = 0.0f,
		var colorAlphaWidth: Float = 1.0f,
		var colorHexBuffer: String = colorState?.hex(true) ?: "",
		var storageKey: String = "",
		var keyName: String? = if (type == EntryType.SETTING && label == "Keybind") FixifyKeybinds.inferKeyName(value) else null,
		var parentModule: Entry? = null,
		val action: (() -> Unit)? = null,
		var sliderMin: Double = 0.0,
		var sliderMax: Double = 100.0,
		var sliderStep: Double = 1.0,
		var sliderSuffix: String = "",
		var sliderDecimals: Int = 0,
		val placeholder: String = "",
	) {
		fun bindStorage(key: String) {
			storageKey = key
			val saved = FixifyConfig.entry(key) ?: return
			saved.enabled?.let { enabled = it }
			saved.settingsExpanded?.let { settingsExpanded = it }
			saved.switchValue?.let { if (switchValue != null) switchValue = it }
			saved.value?.let { value = it }
			if (isKeybind()) {
				keyName = saved.keyName ?: FixifyKeybinds.inferKeyName(value)
			}
			saved.selected?.let {
				if (options != null && options.isNotEmpty()) {
					selected = it.coerceIn(0, options.lastIndex)
				}
			}
			saved.sliderPercentage?.let {
				if (type == EntryType.SLIDER) {
					sliderPercentage = it.coerceIn(0.0f, 1.0f)
					updateSliderDisplay()
				}
			}
			saved.color?.let {
				colorState?.setArgb(it)
				syncColorHex()
			}
		}

		fun persist() {
			if (storageKey.isBlank()) {
				return
			}
			FixifyConfig.updateEntry(storageKey) {
				when (type) {
					EntryType.MODULE -> {
						it.enabled = enabled
						it.settingsExpanded = settingsExpanded
					}

					EntryType.SETTING -> {
						it.switchValue = switchValue
						it.value = value
						if (isKeybind()) {
							it.keyName = keyName
						}
					}

					EntryType.SLIDER -> {
						it.value = value
						it.sliderPercentage = sliderPercentage
					}

					EntryType.TEXT -> it.value = value

					EntryType.MODE,
					EntryType.TAGS,
					EntryType.DROPDOWN,
					-> it.selected = selected

					EntryType.COLOR -> it.color = colorState?.argb
					else -> Unit
				}
			}
			FixifyFeatures.loadFromConfig()
		}

		fun isKeybind(): Boolean = type == EntryType.SETTING && label == "Keybind"

		fun setKeybind(key: InputConstants.Key) {
			value = key.displayName.string
			keyName = key.name
		}

		fun clearKeybind() {
			value = "n/a"
			keyName = null
		}

		fun appendText(input: String) {
			val allowed = input.filter { !it.isISOControl() }
			if (allowed.isNotEmpty()) {
				value = (value + allowed).take(TEXT_INPUT_MAX_LENGTH)
			}
		}

		fun removeTextCharacter() {
			if (value.isNotEmpty()) {
				value = value.dropLast(1)
			}
		}

		fun clearText() {
			value = ""
		}

		fun click(optionIndex: Int, button: Int) {
			var changed = false
			if (type == EntryType.MODULE && button == InputConstants.MOUSE_BUTTON_LEFT) {
				enabled = !enabled
				changed = true
			} else if (type == EntryType.MODULE && button == InputConstants.MOUSE_BUTTON_RIGHT && hasChildren) {
				settingsExpanded = !settingsExpanded
				changed = true
			} else if (button == InputConstants.MOUSE_BUTTON_LEFT && type == EntryType.SETTING && switchValue != null) {
				switchProgress()
				switchValue = !switchValue!!
				changed = true
			} else if (type == EntryType.MODE && optionIndex == MODE_TOGGLE_TARGET) {
				if (button == InputConstants.MOUSE_BUTTON_LEFT) {
					selectorExpanded = !selectorExpanded
				} else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
					selected = (selected + 1) % options!!.size
					changed = true
				}
			} else if (button == InputConstants.MOUSE_BUTTON_LEFT && type == EntryType.MODE && optionIndex >= 0) {
				selected = optionIndex
				selectorExpanded = false
				changed = true
			} else if (button == InputConstants.MOUSE_BUTTON_LEFT && type == EntryType.COLOR && optionIndex == COLOR_SWATCH_TARGET) {
				colorExpanded = !colorExpanded
			} else if (button == InputConstants.MOUSE_BUTTON_LEFT && (type == EntryType.TAGS || type == EntryType.DROPDOWN) && optionIndex >= 0) {
				selected = optionIndex
				changed = true
			} else if (button == InputConstants.MOUSE_BUTTON_LEFT && type == EntryType.ACTION) {
				action?.invoke()
				return
			}
			if (changed) {
				persist()
			}
		}

		fun moduleExtension(): Float {
			if (type != EntryType.MODULE || !hasChildren) {
				return 0.0f
			}
			val now = System.currentTimeMillis()
			val elapsed = (now - settingsAnimationUpdatedAt).coerceAtLeast(0L)
			settingsAnimationUpdatedAt = now
			val change = elapsed / MODULE_COLLAPSE_ANIMATION_MS.toFloat()
			settingsProgress = if (settingsExpanded) {
				min(1.0f, settingsProgress + change)
			} else {
				max(0.0f, settingsProgress - change)
			}
			return easeInOutCubic(settingsProgress)
		}

		fun switchProgress(): Float {
			val target = switchValue ?: return 0.0f
			val now = System.currentTimeMillis()
			val elapsed = (now - toggleAnimationUpdatedAt).coerceAtLeast(0L)
			toggleAnimationUpdatedAt = now
			val change = elapsed / TOGGLE_ANIMATION_MS.toFloat()
			toggleProgress = if (target) {
				min(1.0f, toggleProgress + change)
			} else {
				max(0.0f, toggleProgress - change)
			}
			return easeInOutCubic(toggleProgress)
		}

		fun selectorExtension(): Float {
			if (type != EntryType.MODE) {
				return 0.0f
			}
			val now = System.currentTimeMillis()
			val elapsed = (now - selectorAnimationUpdatedAt).coerceAtLeast(0L)
			selectorAnimationUpdatedAt = now
			val change = elapsed / SELECTOR_ANIMATION_MS.toFloat()
			selectorProgress = if (selectorExpanded) {
				min(1.0f, selectorProgress + change)
			} else {
				max(0.0f, selectorProgress - change)
			}
			return easeInOutCubic(selectorProgress)
		}

		fun colorExtension(): Float {
			if (type != EntryType.COLOR) {
				return 0.0f
			}
			val now = System.currentTimeMillis()
			val elapsed = (now - colorAnimationUpdatedAt).coerceAtLeast(0L)
			colorAnimationUpdatedAt = now
			val change = elapsed / COLOR_ANIMATION_MS.toFloat()
			colorProgress = if (colorExpanded) {
				min(1.0f, colorProgress + change)
			} else {
				max(0.0f, colorProgress - change)
			}
			return easeInOutCubic(colorProgress)
		}

		fun updateColor(section: Int, mouseX: Float, mouseY: Float) {
			val color = colorState ?: return
			when (section) {
				COLOR_MAIN_TARGET -> {
					val saturation = ((mouseX - colorMainX) / colorMainWidth).coerceIn(0.0f, 1.0f)
					val brightness = (1.0f - ((mouseY - colorMainY) / colorMainHeight)).coerceIn(0.0f, 1.0f)
					color.setSaturationBrightness(saturation, brightness)
				}

				COLOR_HUE_TARGET -> color.setHue(((mouseX - colorHueX) / colorHueWidth).coerceIn(0.0f, 1.0f))
				COLOR_ALPHA_TARGET -> color.setAlpha(((mouseX - colorAlphaX) / colorAlphaWidth).coerceIn(0.0f, 1.0f))
			}
			syncColorHex()
		}

		fun syncColorHex() {
			colorHexBuffer = colorState?.hex(true) ?: colorHexBuffer
		}

		fun colorHexText(): String {
			if (colorHexBuffer.isBlank()) {
				syncColorHex()
			}
			return colorHexBuffer
		}

		fun appendColorHex(input: String) {
			val filtered = input.filter { it in '0'..'9' || it in 'A'..'F' || it in 'a'..'f' }
				.uppercase(Locale.ROOT)
			if (filtered.isEmpty()) {
				return
			}
			colorHexBuffer = (colorHexBuffer + filtered).take(COLOR_HEX_MAX_LENGTH)
			applyColorHex()
		}

		fun removeColorHexChar() {
			if (colorHexBuffer.isNotEmpty()) {
				colorHexBuffer = colorHexBuffer.dropLast(1)
				applyColorHex()
			}
		}

		fun clearColorHex() {
			colorHexBuffer = ""
		}

		private fun applyColorHex() {
			val color = colorState ?: return
			if (colorHexBuffer.length == 6 || colorHexBuffer.length == 8) {
				val rgba = colorHexBuffer.padEnd(8, 'F').toLongOrNull(16) ?: return
				val r = ((rgba ushr 24) and 0xFF).toInt()
				val g = ((rgba ushr 16) and 0xFF).toInt()
				val b = ((rgba ushr 8) and 0xFF).toInt()
				val a = (rgba and 0xFF).toInt()
				color.setArgb((a shl 24) or (r shl 16) or (g shl 8) or b)
				persist()
			}
		}

		fun updateSlider(mouseX: Float) {
			if (type != EntryType.SLIDER) {
				return
			}
			sliderPercentage = ((mouseX - sliderTrackX) / sliderTrackWidth).coerceIn(0.0f, 1.0f)
			updateSliderDisplay()
		}

		fun nudgeSlider(direction: Int) {
			if (type != EntryType.SLIDER) {
				return
			}
			sliderPercentage = (sliderPercentage + direction * SLIDER_KEY_STEP).coerceIn(0.0f, 1.0f)
			updateSliderDisplay()
		}

		private fun updateSliderDisplay() {
			val raw = sliderMin + (sliderMax - sliderMin) * sliderPercentage
			val stepped = if (sliderStep > 0.0) {
				kotlin.math.round(raw / sliderStep) * sliderStep
			} else {
				raw
			}.coerceIn(sliderMin, sliderMax)
			value = if (sliderDecimals <= 0) {
				"${stepped.roundToInt()}$sliderSuffix"
			} else {
				"${String.format(Locale.ROOT, "%.${sliderDecimals}f", stepped)}$sliderSuffix"
			}
		}

		companion object {
			fun module(label: String, enabled: Boolean): Entry {
				return Entry(EntryType.MODULE, label, "", null, enabled, null, -1, 0, 0)
			}

			fun setting(label: String, value: String, switchValue: Boolean?): Entry {
				return Entry(EntryType.SETTING, label, value, null, false, switchValue, -1, 0, 0)
			}

			fun slider(label: String, value: String, amount: Int): Entry {
				val initial = value.takeWhile { it.isDigit() || it == '.' || it == '-' }.toDoubleOrNull() ?: 0.0
				val suffix = value.dropWhile { it.isDigit() || it == '.' || it == '-' }
				val percentage = (amount / SLIDER_LEGACY_WIDTH.toFloat()).coerceIn(0.0f, 1.0f)
				val maximum = if (percentage > 0.0f) initial / percentage else 100.0
				return Entry(
					EntryType.SLIDER,
					label,
					value,
					null,
					false,
					null,
					-1,
					amount,
					0,
					sliderPercentage = percentage,
					sliderMin = 0.0,
					sliderMax = maximum,
					sliderSuffix = suffix,
					sliderDecimals = if (value.contains('.')) 1 else 0,
				)
			}

			fun slider(
				label: String,
				initial: Double,
				minimum: Double,
				maximum: Double,
				step: Double,
				suffix: String,
				decimals: Int,
			): Entry {
				val entry = Entry(
					EntryType.SLIDER,
					label,
					"",
					null,
					false,
					null,
					-1,
					0,
					0,
					sliderMin = minimum,
					sliderMax = maximum,
					sliderStep = step,
					sliderSuffix = suffix,
					sliderDecimals = decimals,
				)
				entry.sliderPercentage = if (maximum > minimum) {
					((initial - minimum) / (maximum - minimum)).toFloat().coerceIn(0.0f, 1.0f)
				} else {
					0.0f
				}
				entry.updateSliderDisplay()
				return entry
			}

			fun text(label: String, value: String, placeholder: String = ""): Entry {
				return Entry(
					EntryType.TEXT,
					label,
					value,
					null,
					false,
					null,
					-1,
					0,
					0,
					placeholder = placeholder,
				)
			}

			fun tab(label: String): Entry {
				return Entry(EntryType.TAB, label, "", null, false, null, -1, 0, 0)
			}

			fun mode(label: String, value: String, options: Array<String>, selected: Int): Entry {
				return Entry(EntryType.MODE, label, value, options, false, null, selected, 0, 0)
			}

			fun color(label: String, colorState: FixifyFeatures.ColorState): Entry {
				return Entry(EntryType.COLOR, label, "", null, false, null, -1, 0, 0, colorState)
			}

			fun tags(options: Array<String>, selected: Int): Entry {
				return Entry(EntryType.TAGS, "", "", options, false, null, selected, 0, 0)
			}

			fun smallText(label: String): Entry {
				return Entry(EntryType.SMALL_TEXT, label, "", null, false, null, -1, 0, 0)
			}

			fun dropdown(label: String, value: String, options: Array<String>, selected: Int): Entry {
				return Entry(EntryType.DROPDOWN, label, value, options, false, null, selected, 0, 0)
			}

			fun action(label: String, action: () -> Unit): Entry {
				return Entry(EntryType.ACTION, label, "", null, false, null, -1, 0, 0, action = action)
			}

			fun space(height: Int): Entry {
				return Entry(EntryType.SPACE, "", "", null, false, null, -1, 0, height)
			}
		}
	}

	companion object {
		private const val MENU_WIDTH = 1318
		private const val MENU_HEIGHT = 566
		private const val COLUMN_WIDTH = 208
		private const val COLUMN_TOP = 0
		private const val COLUMN_HEIGHT = 496
		private const val COLUMN_HEADER_HEIGHT = 39
		private const val COLUMN_BODY_TOP = 48
		private const val COLUMN_BOTTOM_PADDING = 10
		private const val ROW_WIDTH = 188
		private const val MODULE_ROW_HEIGHT = 28
		private const val MODULE_ENTRY_HEIGHT = 32
		private const val SETTING_ENTRY_HEIGHT = 26
		private const val TOGGLE_WIDTH = 28
		private const val TOGGLE_HEIGHT = 16
		private const val TOGGLE_RADIUS = 8
		private const val TOGGLE_KNOB_SIZE = 10
		private const val TOGGLE_KNOB_INSET = 3
		private const val KEYBIND_MIN_WIDTH = 32
		private const val KEYBIND_HEIGHT = 18
		private const val KEYBIND_RADIUS = 6
		private const val KEYBIND_HORIZONTAL_PADDING = 7
		private const val KEYBIND_TEXT_SIZE = 11.5f
		private const val SELECTOR_MIN_WIDTH = 42
		private const val SELECTOR_HEIGHT = 18
		private const val SELECTOR_RADIUS = 6
		private const val SELECTOR_HORIZONTAL_PADDING = 7
		private const val SELECTOR_TEXT_SIZE = 11.5f
		private const val SELECTOR_BASE_HEIGHT = 24
		private const val SELECTOR_LIST_TOP = 26
		private const val SELECTOR_LIST_INSET = 4
		private const val SELECTOR_OPTION_HEIGHT = 26
		private const val MODE_TOGGLE_TARGET = -2
		private const val COLOR_SWATCH_TARGET = -10
		private const val COLOR_MAIN_TARGET = -11
		private const val COLOR_HUE_TARGET = -12
		private const val COLOR_ALPHA_TARGET = -13
		private const val COLOR_HEX_TARGET = -14
		private const val COLOR_BASE_HEIGHT = 32
		private const val COLOR_PICKER_HEIGHT = 222
		private const val COLOR_PICKER_INSET = 6
		private const val COLOR_PICKER_RADIUS = 5
		private const val COLOR_AREA_TOP = 4
		private const val COLOR_AREA_HEIGHT = 112
		private const val COLOR_BAR_HEIGHT = 15
		private const val COLOR_BAR_GAP = 8
		private const val COLOR_ALPHA_GAP = 5
		private const val COLOR_HEX_GAP = 8
		private const val COLOR_HEX_WIDTH = 78
		private const val COLOR_HEX_HEIGHT = 24
		private const val COLOR_HEX_RADIUS = 4
		private const val COLOR_HEX_TEXT_SIZE = 11.5f
		private const val COLOR_HEX_MAX_LENGTH = 8
		private const val COLOR_SWATCH_WIDTH = 34
		private const val COLOR_SWATCH_HEIGHT = 20
		private const val COLOR_SWATCH_RADIUS = 5
		private const val COLOR_POINTER_OUTER_RADIUS = 8.0f
		private const val COLOR_POINTER_INNER_RADIUS = 6.5f
		private const val COLOR_HUE_STEPS = 72
		private const val SLIDER_SETTING_HEIGHT = 38
		private const val SLIDER_HORIZONTAL_INSET = 3
		private const val SLIDER_TRACK_Y = 24
		private const val SLIDER_TRACK_WIDTH = ROW_WIDTH - SLIDER_HORIZONTAL_INSET * 2
		private const val SLIDER_TRACK_HEIGHT = 6
		private const val SLIDER_TRACK_RADIUS = 3
		private const val SLIDER_CLICK_HEIGHT = 15
		private const val SLIDER_KNOB_RADIUS = 4.0f
		private const val SLIDER_KNOB_HOVER_RADIUS = 5.0f
		private const val SLIDER_LEGACY_WIDTH = 144
		private const val SLIDER_KEY_STEP = 0.01f
		private const val TEXT_ENTRY_HEIGHT = 44
		private const val TEXT_INPUT_Y = 17
		private const val TEXT_INPUT_HEIGHT = 22
		private const val TEXT_INPUT_RADIUS = 5
		private const val TEXT_INPUT_TEXT_SIZE = 11.5f
		private const val TEXT_INPUT_MAX_LENGTH = 64
		private const val ACTION_ENTRY_HEIGHT = 29
		private const val MODULE_CONFIG_RADIUS = 6
		private const val TEXTURE_SCALE = 4
		private const val TEXT_SIZE = 12.5f
		private const val TITLE_TEXT_SIZE = 14.0f
		private const val SEARCH_WIDTH = 280
		private const val SEARCH_HEIGHT = 36
		private const val SEARCH_X = (MENU_WIDTH - SEARCH_WIDTH) / 2
		private const val SEARCH_Y = 516
		private const val SEARCH_TEXT_SIZE = 13.5f
		private const val SEARCH_PLACEHOLDER = "Search modules..."
		private const val MAX_SEARCH_LENGTH = 32

		private const val OPEN_ANIMATION_MS = 52L
		private const val CLOSE_ANIMATION_MS = 115L
		private const val COLLAPSE_ANIMATION_MS = 95L
		private const val MODULE_COLLAPSE_ANIMATION_MS = 85L
		private const val TOGGLE_ANIMATION_MS = 90L
		private const val SELECTOR_ANIMATION_MS = 100L
		private const val COLOR_ANIMATION_MS = 100L
		private const val SCROLL_STEP = 16.0
		private const val MAX_MENU_SCALE = 0.78f

		private val OVERLAY_TOP = 0xA60A0B10.toInt()
		private val OVERLAY_BOTTOM = 0xE20B0C11.toInt()
		private val VIGNETTE = 0xA3000000.toInt()
		private val PANEL_BACKGROUND = 0xFF191920.toInt()
		private val MODULE_CONFIG_BACKGROUND = 0x4D4A4A5E
		private val PANEL_EDGE = 0x442B2A36
		private val ROW_ALT = 0xB817171F.toInt()
		private val DETAIL = 0xFF2A2A34.toInt()
		private val DETAIL_DARK = 0xFF202028.toInt()
		private val SELECTOR_PILL_BACKGROUND = 0xFF262631.toInt()
		private val SELECTOR_DROPDOWN_BACKGROUND = 0xF022222A.toInt()
		private val SELECTOR_SELECTED_BACKGROUND = 0xFFAAA4FF.toInt()
		private val MODE_OUTLINE = 0xFF3A3946.toInt()
		private val TEXT = 0xFFE7E5F1.toInt()
		private val TEXT_MUTED = 0xFFC7C5D1.toInt()
		private val TEXT_DIM = 0xFF8D8B9A.toInt()
		private val ACCENT = 0xFFAAA4FF.toInt()
		private val WHITE = 0xFFFFFFFF.toInt()

		private val PANEL_TEXTURE = texture("gui/panel.png")
		private val ROW_TEXTURE = texture("gui/row.png")
		private val ROW_HOVER_TEXTURE = texture("gui/row_hover.png")
		private val ROW_ACTIVE_TEXTURE = texture("gui/row_active.png")
		private val DROPDOWN_TEXTURE = texture("gui/dropdown.png")
		private val COLUMN_STATES = HashMap<String, ColumnState>()

		private fun texture(path: String): Identifier {
			return Identifier.fromNamespaceAndPath(FixifyClient.MOD_ID, "textures/$path")
		}

		private fun clamp(value: Float): Float = max(0.0f, min(1.0f, value))

		private fun easeOutQuint(value: Float): Float {
			val inverse = 1.0f - value
			return 1.0f - inverse * inverse * inverse * inverse * inverse
		}

		private fun easeInOutCubic(value: Float): Float {
			return if (value < 0.5f) {
				4.0f * value * value * value
			} else {
				val inverse = -2.0f * value + 2.0f
				1.0f - inverse * inverse * inverse / 2.0f
			}
		}

		private fun easeOutCubic(value: Float): Float {
			val inverse = 1.0f - value
			return 1.0f - inverse * inverse * inverse
		}

		private fun mixColor(from: Int, to: Int, progress: Float): Int {
			val amount = progress.coerceIn(0.0f, 1.0f)
			fun channel(shift: Int): Int {
				val start = (from ushr shift) and 0xFF
				val end = (to ushr shift) and 0xFF
				return (start + (end - start) * amount).roundToInt().coerceIn(0, 255)
			}
			return (channel(24) shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
		}

		private fun darken(color: Int, factor: Float): Int {
			val amount = factor.coerceIn(0.0f, 1.0f)
			val a = (color ushr 24) and 0xFF
			val r = (((color ushr 16) and 0xFF) * amount).roundToInt().coerceIn(0, 255)
			val g = (((color ushr 8) and 0xFF) * amount).roundToInt().coerceIn(0, 255)
			val b = ((color and 0xFF) * amount).roundToInt().coerceIn(0, 255)
			return (a shl 24) or (r shl 16) or (g shl 8) or b
		}

		private fun alpha(color: Int, alpha: Int): Int {
			return (alpha.coerceIn(0, 255) shl 24) or (color and 0x00FFFFFF)
		}
	}
}
