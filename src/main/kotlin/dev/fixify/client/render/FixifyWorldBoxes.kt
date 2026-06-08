package dev.fixify.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.world.phys.AABB

object FixifyWorldBoxes {
	private val boxEdges = intArrayOf(
		0, 1, 1, 5, 5, 4, 4, 0,
		3, 2, 2, 6, 6, 7, 7, 3,
		0, 3, 1, 2, 5, 6, 4, 7,
	)

	fun draw(
		poseStack: PoseStack,
		bufferSource: MultiBufferSource.BufferSource,
		box: AABB,
		color: Int,
		style: Int,
		throughWalls: Boolean,
	) {
		val filledType = if (throughWalls) FixifyWorldRenderTypes.filledEsp else FixifyWorldRenderTypes.filledWorld
		val lineType = if (throughWalls) FixifyWorldRenderTypes.linesEsp else FixifyWorldRenderTypes.linesWorld
		val drawFilled = style == STYLE_FILLED || style == STYLE_FILLED_OUTLINE
		val drawOutline = style == STYLE_OUTLINE || style == STYLE_FILLED_OUTLINE

		if (drawFilled) {
			drawFilledBox(poseStack, bufferSource, filledType, box, color)
		}
		if (drawOutline) {
			drawLineBox(poseStack, bufferSource, lineType, box, outlineColor(color))
		}
		if (drawFilled) {
			bufferSource.endBatch(filledType)
		}
		if (drawOutline) {
			bufferSource.endBatch(lineType)
		}
	}

	private fun drawFilledBox(
		poseStack: PoseStack,
		bufferSource: MultiBufferSource,
		renderType: RenderType,
		box: AABB,
		color: Int,
	) {
		val pose = poseStack.last()
		val buffer = bufferSource.getBuffer(renderType)
		val r = ((color ushr 16) and 0xFF) / 255.0f
		val g = ((color ushr 8) and 0xFF) / 255.0f
		val b = (color and 0xFF) / 255.0f
		val a = ((color ushr 24) and 0xFF) / 255.0f
		filledVertices(pose, buffer, box, r, g, b, a)
	}

	private fun drawLineBox(
		poseStack: PoseStack,
		bufferSource: MultiBufferSource,
		renderType: RenderType,
		box: AABB,
		color: Int,
	) {
		val pose = poseStack.last()
		val buffer = bufferSource.getBuffer(renderType)
		val r = ((color ushr 16) and 0xFF) / 255.0f
		val g = ((color ushr 8) and 0xFF) / 255.0f
		val b = (color and 0xFF) / 255.0f
		val a = ((color ushr 24) and 0xFF) / 255.0f
		lineVertices(pose, buffer, box, r, g, b, a)
	}

	private fun lineVertices(
		pose: PoseStack.Pose,
		buffer: VertexConsumer,
		box: AABB,
		r: Float,
		g: Float,
		b: Float,
		a: Float,
	) {
		val x0 = box.minX.toFloat()
		val y0 = box.minY.toFloat()
		val z0 = box.minZ.toFloat()
		val x1 = box.maxX.toFloat()
		val y1 = box.maxY.toFloat()
		val z1 = box.maxZ.toFloat()
		val corners = floatArrayOf(
			x0, y0, z0,
			x1, y0, z0,
			x1, y1, z0,
			x0, y1, z0,
			x0, y0, z1,
			x1, y0, z1,
			x1, y1, z1,
			x0, y1, z1,
		)

		for (i in boxEdges.indices step 2) {
			val start = boxEdges[i] * 3
			val end = boxEdges[i + 1] * 3
			val sx = corners[start]
			val sy = corners[start + 1]
			val sz = corners[start + 2]
			val ex = corners[end]
			val ey = corners[end + 1]
			val ez = corners[end + 2]
			buffer.addVertex(pose, sx, sy, sz).setColor(r, g, b, a).setNormal(pose, ex - sx, ey - sy, ez - sz)
			buffer.addVertex(pose, ex, ey, ez).setColor(r, g, b, a).setNormal(pose, ex - sx, ey - sy, ez - sz)
		}
	}

	private fun filledVertices(
		pose: PoseStack.Pose,
		buffer: VertexConsumer,
		box: AABB,
		r: Float,
		g: Float,
		b: Float,
		a: Float,
	) {
		val matrix = pose.pose()
		val x0 = box.minX.toFloat()
		val y0 = box.minY.toFloat()
		val z0 = box.minZ.toFloat()
		val x1 = box.maxX.toFloat()
		val y1 = box.maxY.toFloat()
		val z1 = box.maxZ.toFloat()

		fun vertex(x: Float, y: Float, z: Float) {
			buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a)
		}

		vertex(x0, y0, z0)
		vertex(x0, y0, z1)
		vertex(x0, y1, z1)
		vertex(x0, y1, z0)

		vertex(x1, y0, z1)
		vertex(x1, y0, z0)
		vertex(x1, y1, z0)
		vertex(x1, y1, z1)

		vertex(x0, y0, z0)
		vertex(x0, y1, z0)
		vertex(x1, y1, z0)
		vertex(x1, y0, z0)

		vertex(x1, y0, z1)
		vertex(x1, y1, z1)
		vertex(x0, y1, z1)
		vertex(x0, y0, z1)

		vertex(x0, y0, z0)
		vertex(x1, y0, z0)
		vertex(x1, y0, z1)
		vertex(x0, y0, z1)

		vertex(x0, y1, z1)
		vertex(x1, y1, z1)
		vertex(x1, y1, z0)
		vertex(x0, y1, z0)
	}

	private fun outlineColor(color: Int): Int {
		val alpha = ((color ushr 24) and 0xFF).coerceAtLeast(190)
		return (alpha shl 24) or (color and 0x00FFFFFF)
	}

	const val STYLE_FILLED = 0
	const val STYLE_OUTLINE = 1
	const val STYLE_FILLED_OUTLINE = 2
}
