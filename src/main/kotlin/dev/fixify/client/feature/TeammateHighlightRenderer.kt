package dev.fixify.client.feature

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import dev.fixify.client.render.FixifyWorldRenderTypes
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.AABB
import java.util.Locale

object TeammateHighlightRenderer {
	private val rolePattern = Regex("""\[(A|B|T|M|H)]\s+([A-Za-z0-9_]{1,16})""")
	private val boxEdges = intArrayOf(
		0, 1, 1, 5, 5, 4, 4, 0,
		3, 2, 2, 6, 6, 7, 7, 3,
		0, 3, 1, 2, 5, 6, 4, 7,
	)

	fun register() {
		LevelRenderEvents.END_MAIN.register { context ->
			render(context)
		}
	}

	private fun render(context: LevelRenderContext) {
		if (!FixifyFeatures.teammateHighlightEnabled) {
			return
		}

		val client = Minecraft.getInstance()
		val level = client.level ?: return
		val localPlayer = client.player ?: return
		val roles = scoreboardRoles(level.scoreboard)
		if (roles.isEmpty()) {
			return
		}

		val poseStack = context.poseStack()
		val bufferSource = context.bufferSource()
		val camera = client.gameRenderer.getMainCamera().position()

		poseStack.pushPose()
		poseStack.translate(-camera.x, -camera.y, -camera.z)
		for (player in level.players()) {
			if (player == localPlayer || !player.isAlive) {
				continue
			}

			val role = roles[player.scoreboardName.lowercase(Locale.ROOT)] ?: continue
			val color = FixifyFeatures.colorForRole(role)?.argb ?: continue
			val box = player.boundingBox.inflate(0.08, 0.08, 0.08)
			drawFilledBox(poseStack, bufferSource, box, withAlpha(color, (((color ushr 24) and 0xFF) * 0.30f).toInt()))
			drawLineBox(poseStack, bufferSource, box, color)
		}
		poseStack.popPose()
		bufferSource.endBatch(FixifyWorldRenderTypes.filledEsp)
		bufferSource.endBatch(FixifyWorldRenderTypes.linesEsp)
	}

	private fun scoreboardRoles(scoreboard: net.minecraft.world.scores.Scoreboard): Map<String, Char> {
		val objective = scoreboard.getDisplayObjective(net.minecraft.world.scores.DisplaySlot.SIDEBAR) ?: return emptyMap()
		val roles = HashMap<String, Char>()
		for (score in scoreboard.listPlayerScores(objective)) {
			val team = scoreboard.getPlayersTeam(score.owner())
			val candidates = listOf(
				score.display()?.getString().orEmpty(),
				score.ownerName().getString(),
				score.owner(),
				team?.getFormattedName(Component.literal(score.owner()))?.getString().orEmpty(),
				"${team?.playerPrefix?.getString().orEmpty()}${score.owner()}${team?.playerSuffix?.getString().orEmpty()}",
			)
			for (candidate in candidates) {
				val match = rolePattern.find(candidate) ?: continue
				roles[match.groupValues[2].lowercase(Locale.ROOT)] = match.groupValues[1][0]
				break
			}
		}
		return roles
	}

	private fun drawFilledBox(poseStack: PoseStack, bufferSource: MultiBufferSource, box: AABB, color: Int) {
		val pose = poseStack.last()
		val buffer = bufferSource.getBuffer(FixifyWorldRenderTypes.filledEsp)
		val r = ((color ushr 16) and 0xFF) / 255.0f
		val g = ((color ushr 8) and 0xFF) / 255.0f
		val b = (color and 0xFF) / 255.0f
		val a = ((color ushr 24) and 0xFF) / 255.0f
		filledVertices(pose, buffer, box, r, g, b, a)
	}

	private fun drawLineBox(poseStack: PoseStack, bufferSource: MultiBufferSource, box: AABB, color: Int) {
		val pose = poseStack.last()
		val buffer = bufferSource.getBuffer(FixifyWorldRenderTypes.linesEsp)
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

	private fun withAlpha(color: Int, alpha: Int): Int {
		return (alpha.coerceIn(18, 160) shl 24) or (color and 0x00FFFFFF)
	}
}
