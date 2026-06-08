package dev.fixify.client.render

import com.mojang.blaze3d.opengl.GlConst
import com.mojang.blaze3d.opengl.FixifyGlBridge
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.opengl.GlTexture
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import dev.fixify.mixin.GuiGraphicsExtractorAccessor
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState
import org.lwjgl.opengl.GL33C

class FixifyNvgPipRenderer(
	vertexConsumers: MultiBufferSource.BufferSource,
) : PictureInPictureRenderer<FixifyNvgPipRenderer.NvgRenderState>(vertexConsumers) {
	override fun renderToTexture(state: NvgRenderState, poseStack: PoseStack) {
		val colorTexture = RenderSystem.outputColorTextureOverride ?: return
		val bufferManager = checkNotNull(FixifyGlBridge.directStateAccess(RenderSystem.getDevice())) {
			"Fixify NanoVG renderer could not access Minecraft's OpenGL backend"
		}
		val depthTexture = (RenderSystem.outputDepthTextureOverride?.texture() as? GlTexture) ?: return
		val width = colorTexture.getWidth(0)
		val height = colorTexture.getHeight(0)

		(colorTexture.texture() as? GlTexture)?.getFbo(bufferManager, depthTexture)?.apply {
			GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this)
			GlStateManager._viewport(0, 0, width, height)
		}

		GL33C.glBindSampler(0, 0)
		FixifyNvgRenderer.beginFrame(width.toFloat(), height.toFloat())
		state.renderContent()
		FixifyNvgRenderer.endFrame()

		GlStateManager._disableDepthTest()
		GlStateManager._disableCull()
		GlStateManager._enableBlend()
		GlStateManager._blendFuncSeparate(770, 771, 1, 0)
	}

	override fun getTranslateY(height: Int, windowScaleFactor: Int): Float = height / 2.0f

	override fun getRenderStateClass(): Class<NvgRenderState> = NvgRenderState::class.java

	override fun getTextureLabel(): String = "fixify_nvg_renderer"

	data class NvgRenderState(
		private val x: Int,
		private val y: Int,
		private val width: Int,
		private val height: Int,
		private val scissor: ScreenRectangle?,
		private val bounds: ScreenRectangle,
		val renderContent: () -> Unit,
	) : PictureInPictureRenderState {
		override fun scale(): Float = 1.0f
		override fun x0(): Int = x
		override fun y0(): Int = y
		override fun x1(): Int = x + width
		override fun y1(): Int = y + height
		override fun scissorArea(): ScreenRectangle? = scissor
		override fun bounds(): ScreenRectangle = bounds
	}

	companion object {
		fun draw(
			graphics: GuiGraphicsExtractor,
			x: Int,
			y: Int,
			width: Int,
			height: Int,
			renderContent: () -> Unit,
		) {
			val bounds = ScreenRectangle(x, y, width, height)
			val state = NvgRenderState(x, y, width, height, null, bounds, renderContent)
			(graphics as GuiGraphicsExtractorAccessor).fixifyGuiRenderState().addPicturesInPictureState(state)
		}
	}
}
