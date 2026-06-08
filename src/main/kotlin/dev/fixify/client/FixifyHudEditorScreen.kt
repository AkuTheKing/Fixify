package dev.fixify.client

import com.mojang.blaze3d.platform.InputConstants
import dev.fixify.client.feature.SkyblockHudRenderer
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class FixifyHudEditorScreen(
	private val parent: Screen?,
	initialWidget: FixifyHudWidget = FixifyHudWidget.PET,
) : Screen(Component.literal("Fixify HUD Editor")) {
	private var selectedIndex = FixifyHudWidget.entries.indexOf(initialWidget).coerceAtLeast(0)
	private var dragging = false
	private var dragOffsetX = 0.0f
	private var dragOffsetY = 0.0f

	private val selected: FixifyHudWidget
		get() = FixifyHudWidget.entries[selectedIndex]

	override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
		graphics.fillGradient(0, 0, width, height, 0xD00A0B10.toInt(), 0xED11121A.toInt())
		drawHotbarPlaceholder(graphics)
		SkyblockHudRenderer.renderEditor(graphics, selected)

		val centerX = width / 2
		val centerY = height / 2
		val line = font.lineHeight + 3
		graphics.centeredText(font, "Left-click and drag the highlighted element.", centerX, centerY - line * 4, 0xFFFFFFFF.toInt())
		graphics.centeredText(font, "Right or middle-click it to reset.", centerX, centerY - line * 3, 0xFFD0CEDA.toInt())
		graphics.centeredText(font, "Scroll to scale. Press A/D to switch elements.", centerX, centerY - line * 2, 0xFFD0CEDA.toInt())
		graphics.centeredText(font, "Currently changing: ${selected.displayName}", centerX, centerY, 0xFFAAA4FF.toInt())
		val (offsetX, offsetY) = SkyblockHudRenderer.currentOffset(selected)
		val scale = SkyblockHudRenderer.currentScale(selected)
		graphics.centeredText(
			font,
			"Offset: [$offsetX, $offsetY]  Scale: ${(scale * 100.0f).toInt()}%",
			centerX,
			centerY + line,
			0xFFFFFFFF.toInt(),
		)
	}

	override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
		val bounds = SkyblockHudRenderer.bounds(selected, width, height)
		if (!contains(bounds, event.x(), event.y())) {
			return true
		}
		if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
			dragging = true
			dragOffsetX = event.x().toFloat() - bounds.x
			dragOffsetY = event.y().toFloat() - bounds.y
		} else {
			SkyblockHudRenderer.resetLayout(selected)
			SkyblockHudRenderer.persistLayout(selected)
		}
		return true
	}

	override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
		if (!dragging || event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
			return true
		}
		moveSelected(event.x().toFloat(), event.y().toFloat())
		return true
	}

	override fun mouseReleased(event: MouseButtonEvent): Boolean {
		if (dragging && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
			moveSelected(event.x().toFloat(), event.y().toFloat())
			dragging = false
			SkyblockHudRenderer.persistLayout(selected)
		}
		return true
	}

	override fun mouseScrolled(
		mouseX: Double,
		mouseY: Double,
		horizontalAmount: Double,
		verticalAmount: Double,
	): Boolean {
		val current = SkyblockHudRenderer.currentScale(selected)
		val next = (current + verticalAmount.toFloat() * 0.05f).coerceIn(0.5f, 2.0f)
		val (x, y) = SkyblockHudRenderer.currentOffset(selected)
		SkyblockHudRenderer.updateLayout(selected, x, y, next)
		SkyblockHudRenderer.persistLayout(selected)
		return true
	}

	override fun keyPressed(event: KeyEvent): Boolean {
		when (event.key()) {
			InputConstants.KEY_A,
			InputConstants.KEY_LEFT,
			-> {
				selectedIndex = (selectedIndex - 1 + FixifyHudWidget.entries.size) % FixifyHudWidget.entries.size
				dragging = false
				return true
			}

			InputConstants.KEY_D,
			InputConstants.KEY_RIGHT,
			-> {
				selectedIndex = (selectedIndex + 1) % FixifyHudWidget.entries.size
				dragging = false
				return true
			}
		}
		return super.keyPressed(event)
	}

	override fun onClose() {
		for (widget in FixifyHudWidget.entries) {
			SkyblockHudRenderer.persistLayout(widget)
		}
		minecraft.setScreen(parent)
	}

	override fun isPauseScreen(): Boolean = false

	private fun moveSelected(mouseX: Float, mouseY: Float) {
		val currentBounds = SkyblockHudRenderer.bounds(selected, width, height)
		val (currentX, currentY) = SkyblockHudRenderer.currentOffset(selected)
		val anchorX = currentBounds.x - currentX
		val anchorY = currentBounds.y - currentY
		val nextX = (mouseX - dragOffsetX - anchorX).toInt()
		val nextY = (mouseY - dragOffsetY - anchorY).toInt()
		SkyblockHudRenderer.updateLayout(selected, nextX, nextY, SkyblockHudRenderer.currentScale(selected))
	}

	private fun contains(bounds: SkyblockHudRenderer.Bounds, x: Double, y: Double): Boolean {
		return x >= bounds.x && x <= bounds.x + bounds.width && y >= bounds.y && y <= bounds.y + bounds.height
	}

	private fun drawHotbarPlaceholder(graphics: GuiGraphicsExtractor) {
		val hotbarWidth = 182
		val hotbarHeight = 22
		val x = width / 2 - hotbarWidth / 2
		val y = height - hotbarHeight
		graphics.fill(x, y, x + hotbarWidth, y + hotbarHeight, 0x65000000)
		graphics.centeredText(font, "Hotbar", width / 2, y + 7, 0x99FFFFFF.toInt())
	}
}
