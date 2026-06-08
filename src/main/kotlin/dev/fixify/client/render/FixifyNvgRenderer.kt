package dev.fixify.client.render

import dev.fixify.client.FixifyClient
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT
import org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP
import org.lwjgl.nanovg.NanoVG.NVG_CW
import org.lwjgl.nanovg.NanoVG.NVG_ROUND
import org.lwjgl.nanovg.NanoVG.nvgArc
import org.lwjgl.nanovg.NanoVG.nvgBeginFrame
import org.lwjgl.nanovg.NanoVG.nvgBeginPath
import org.lwjgl.nanovg.NanoVG.nvgCircle
import org.lwjgl.nanovg.NanoVG.nvgCreateFontMem
import org.lwjgl.nanovg.NanoVG.nvgEndFrame
import org.lwjgl.nanovg.NanoVG.nvgFill
import org.lwjgl.nanovg.NanoVG.nvgFillColor
import org.lwjgl.nanovg.NanoVG.nvgFillPaint
import org.lwjgl.nanovg.NanoVG.nvgFontFaceId
import org.lwjgl.nanovg.NanoVG.nvgFontSize
import org.lwjgl.nanovg.NanoVG.nvgGlobalAlpha
import org.lwjgl.nanovg.NanoVG.nvgLinearGradient
import org.lwjgl.nanovg.NanoVG.nvgLineCap
import org.lwjgl.nanovg.NanoVG.nvgLineTo
import org.lwjgl.nanovg.NanoVG.nvgMoveTo
import org.lwjgl.nanovg.NanoVG.nvgRestore
import org.lwjgl.nanovg.NanoVG.nvgRGBA
import org.lwjgl.nanovg.NanoVG.nvgRoundedRect
import org.lwjgl.nanovg.NanoVG.nvgSave
import org.lwjgl.nanovg.NanoVG.nvgScale
import org.lwjgl.nanovg.NanoVG.nvgScissor
import org.lwjgl.nanovg.NanoVG.nvgText
import org.lwjgl.nanovg.NanoVG.nvgTextAlign
import org.lwjgl.nanovg.NanoVG.nvgTextBounds
import org.lwjgl.nanovg.NanoVG.nvgTranslate
import org.lwjgl.nanovg.NanoVG.nvgStroke
import org.lwjgl.nanovg.NanoVG.nvgStrokeColor
import org.lwjgl.nanovg.NanoVG.nvgStrokeWidth
import org.lwjgl.nanovg.NanoVGGL3.NVG_ANTIALIAS
import org.lwjgl.nanovg.NanoVGGL3.NVG_STENCIL_STROKES
import org.lwjgl.nanovg.NanoVGGL3.nvgCreate
import java.nio.ByteBuffer

object FixifyNvgRenderer {
	private val minecraft: Minecraft
		get() = Minecraft.getInstance()

	private val color: NVGColor = NVGColor.malloc()
	private val secondColor: NVGColor = NVGColor.malloc()
	private val paint: NVGPaint = NVGPaint.malloc()
	private val fontBounds = FloatArray(4)
	private val fontMap = HashMap<FixifyFont, NvgFont>()
	private var drawing = false
	private val vg = nvgCreate(NVG_ANTIALIAS or NVG_STENCIL_STROKES)

	val defaultFont: FixifyFont by lazy {
		val id = Identifier.fromNamespaceAndPath(FixifyClient.MOD_ID, "font.ttf")
		FixifyFont("Fixify", minecraft.resourceManager.getResource(id).orElseThrow().open())
	}

	init {
		require(vg != -1L) { "Failed to initialize NanoVG" }
	}

	fun devicePixelRatio(): Float {
		val window = minecraft.window
		return if (window.guiScaledWidth == 0) 1.0f else window.width / window.guiScaledWidth.toFloat()
	}

	fun beginFrame(width: Float, height: Float) {
		check(!drawing) { "[FixifyNvgRenderer] Already drawing, but beginFrame was called" }
		val dpr = devicePixelRatio()
		nvgBeginFrame(vg, width / dpr, height / dpr, dpr)
		nvgTextAlign(vg, NVG_ALIGN_LEFT or NVG_ALIGN_TOP)
		drawing = true
	}

	fun endFrame() {
		check(drawing) { "[FixifyNvgRenderer] Not drawing, but endFrame was called" }
		nvgEndFrame(vg)
		drawing = false
	}

	fun push() = nvgSave(vg)

	fun pop() = nvgRestore(vg)

	fun translate(x: Float, y: Float) = nvgTranslate(vg, x, y)

	fun scale(x: Float, y: Float) = nvgScale(vg, x, y)

	fun scissor(x: Float, y: Float, width: Float, height: Float) = nvgScissor(vg, x, y, width, height)

	fun globalAlpha(amount: Float) = nvgGlobalAlpha(vg, amount.coerceIn(0.0f, 1.0f))

	fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int) {
		nvgBeginPath(vg)
		nvgRoundedRect(vg, x, y, width, height, radius)
		setColor(color)
		nvgFillColor(vg, this.color)
		nvgFill(vg)
	}

	fun gradientRect(
		x: Float,
		y: Float,
		width: Float,
		height: Float,
		radius: Float,
		startColor: Int,
		endColor: Int,
		vertical: Boolean,
	) {
		nvgBeginPath(vg)
		nvgRoundedRect(vg, x, y, width, height, radius)
		setColor(startColor, color)
		setColor(endColor, secondColor)
		if (vertical) {
			nvgLinearGradient(vg, x, y, x, y + height, color, secondColor, paint)
		} else {
			nvgLinearGradient(vg, x, y, x + width, y, color, secondColor, paint)
		}
		nvgFillPaint(vg, paint)
		nvgFill(vg)
	}

	fun roundedOutline(x: Float, y: Float, width: Float, height: Float, radius: Float, thickness: Float, color: Int) {
		nvgBeginPath(vg)
		nvgRoundedRect(vg, x, y, width, height, radius)
		setColor(color)
		nvgStrokeColor(vg, this.color)
		nvgStrokeWidth(vg, thickness)
		nvgStroke(vg)
	}

	fun circle(x: Float, y: Float, radius: Float, color: Int) {
		nvgBeginPath(vg)
		nvgCircle(vg, x, y, radius)
		setColor(color)
		nvgFillColor(vg, this.color)
		nvgFill(vg)
	}

	fun arc(
		x: Float,
		y: Float,
		radius: Float,
		startAngle: Float,
		endAngle: Float,
		thickness: Float,
		color: Int,
	) {
		nvgBeginPath(vg)
		nvgArc(vg, x, y, radius, startAngle, endAngle, NVG_CW)
		setColor(color)
		nvgStrokeColor(vg, this.color)
		nvgStrokeWidth(vg, thickness)
		nvgLineCap(vg, NVG_ROUND)
		nvgStroke(vg)
	}

	fun line(x1: Float, y1: Float, x2: Float, y2: Float, thickness: Float, color: Int) {
		nvgBeginPath(vg)
		nvgMoveTo(vg, x1, y1)
		nvgLineTo(vg, x2, y2)
		setColor(color)
		nvgStrokeColor(vg, this.color)
		nvgStrokeWidth(vg, thickness)
		nvgLineCap(vg, NVG_ROUND)
		nvgStroke(vg)
	}

	fun text(text: String, x: Float, y: Float, size: Float, color: Int, font: FixifyFont = defaultFont) {
		nvgFontSize(vg, size)
		nvgFontFaceId(vg, fontId(font))
		setColor(color)
		nvgFillColor(vg, this.color)
		nvgText(vg, x, y + 0.5f, text)
	}

	fun textWidth(text: String, size: Float, font: FixifyFont = defaultFont): Float {
		nvgFontSize(vg, size)
		nvgFontFaceId(vg, fontId(font))
		return nvgTextBounds(vg, 0.0f, 0.0f, text, fontBounds)
	}

	private fun fontId(font: FixifyFont): Int {
		return fontMap.getOrPut(font) {
			val buffer = font.buffer()
			val id = nvgCreateFontMem(vg, font.name, buffer, false)
			require(id != -1) { "Failed to load Fixify NanoVG font: ${font.name}" }
			NvgFont(id, buffer)
		}.id
	}

	private fun setColor(value: Int) {
		setColor(value, color)
	}

	private fun setColor(value: Int, target: NVGColor) {
		nvgRGBA(
			((value ushr 16) and 0xFF).toByte(),
			((value ushr 8) and 0xFF).toByte(),
			(value and 0xFF).toByte(),
			((value ushr 24) and 0xFF).toByte(),
			target,
		)
	}

	private data class NvgFont(val id: Int, val buffer: ByteBuffer)
}
