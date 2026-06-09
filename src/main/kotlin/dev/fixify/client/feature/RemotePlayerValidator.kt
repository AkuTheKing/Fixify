package dev.fixify.client.feature

import net.minecraft.client.Minecraft
import net.minecraft.client.player.RemotePlayer
import net.minecraft.world.entity.Entity
import java.util.UUID

internal object RemotePlayerValidator {
	private const val VALIDATION_DELAY_MS = 1250L
	private val validationStartedAt = mutableMapOf<UUID, Long>()

	fun isRealRemotePlayer(entity: Entity?): Boolean {
		val player = entity as? RemotePlayer ?: return false
		val connection = Minecraft.getInstance().connection ?: return false
		if (connection.getPlayerInfo(player.uuid) == null) {
			validationStartedAt.remove(player.uuid)
			return false
		}

		val now = System.currentTimeMillis()
		val firstSeenAt = validationStartedAt.putIfAbsent(player.uuid, now) ?: now
		return now - firstSeenAt >= VALIDATION_DELAY_MS
	}
}
