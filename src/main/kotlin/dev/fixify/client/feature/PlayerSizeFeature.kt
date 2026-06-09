package dev.fixify.client.feature

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.world.entity.Avatar
import net.minecraft.world.phys.Vec3

object PlayerSizeFeature {
	private const val PLAYER_MODEL_RENDER_SCALE = 0.9375

	@JvmStatic
	fun shouldScale(renderedPlayer: Avatar): Boolean {
		if (!FixifyFeatures.playerSizeEnabled || !SkyblockDataTracker.shouldRender()) {
			return false
		}

		val localPlayer = Minecraft.getInstance().player ?: return false
		if (renderedPlayer === localPlayer) {
			return true
		}
		return FixifyFeatures.playerSizeScaleAllPlayers &&
			RemotePlayerValidator.isRealRemotePlayer(renderedPlayer)
	}

	@JvmStatic
	fun applyScale(renderState: AvatarRenderState, poseStack: PoseStack) {
		val x = FixifyFeatures.playerSizeX
		val y = FixifyFeatures.playerSizeY
		val z = FixifyFeatures.playerSizeZ
		if (x == 1.0f && y == 1.0f && z == 1.0f) {
			return
		}

		if (y < 0.0f) {
			poseStack.translate(0.0, -renderState.boundingBoxHeight / PLAYER_MODEL_RENDER_SCALE, 0.0)
		}
		poseStack.scale(x, y, z)
	}

	@JvmStatic
	fun adjustNameTag(renderState: AvatarRenderState, shouldScale: Boolean) {
		val yScale = FixifyFeatures.playerSizeY
		val attachment = renderState.nameTagAttachment
		if (!shouldScale || yScale <= 0.0f || yScale == 1.0f || attachment == null) {
			return
		}
		renderState.nameTagAttachment = Vec3(attachment.x, attachment.y * yScale, attachment.z)
	}
}
