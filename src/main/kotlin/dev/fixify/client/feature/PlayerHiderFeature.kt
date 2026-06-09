package dev.fixify.client.feature

import net.minecraft.world.entity.Avatar
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

object PlayerHiderFeature {
	@JvmStatic
	fun shouldHide(localPlayer: Player?, otherPlayer: Avatar, x: Double, y: Double, z: Double): Boolean {
		if (
			!isActive() ||
			!FixifyFeatures.playerHiderHidePlayers ||
			localPlayer == null ||
			otherPlayer === localPlayer ||
			!RemotePlayerValidator.isRealRemotePlayer(otherPlayer)
		) {
			return false
		}

		return FixifyFeatures.playerHiderHideAll ||
			localPlayer.distanceToSqr(x, y, z) <= hideDistanceSquared()
	}

	@JvmStatic
	fun shouldRenderAsGhost(localPlayer: Player?, otherPlayer: Avatar, x: Double, y: Double, z: Double): Boolean {
		return FixifyFeatures.playerHiderGhostMode && shouldHide(localPlayer, otherPlayer, x, y, z)
	}

	@JvmStatic
	fun shouldClickThrough(localPlayer: Player?, entity: Entity?): Boolean {
		return isActive() &&
			FixifyFeatures.playerHiderClickThrough &&
			localPlayer != null &&
			entity !== localPlayer &&
			RemotePlayerValidator.isRealRemotePlayer(entity)
	}

	@JvmStatic
	fun ghostColor(): Int {
		val alpha = (FixifyFeatures.playerHiderGhostOpacity.coerceIn(0.0f, 1.0f) * 255.0f).toInt()
		return (alpha shl 24) or 0x00FFFFFF
	}

	private fun isActive(): Boolean {
		return FixifyFeatures.playerHiderEnabled && SkyblockDataTracker.shouldRender()
	}

	private fun hideDistanceSquared(): Double {
		val distance = FixifyFeatures.playerHiderDistance.toDouble()
		return distance * distance
	}
}
