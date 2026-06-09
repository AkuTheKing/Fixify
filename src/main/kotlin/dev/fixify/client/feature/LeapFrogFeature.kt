package dev.fixify.client.feature

import dev.fixify.mixin.KeyMappingAccessor
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.world.item.Items
import kotlin.math.sqrt

object LeapFrogFeature {
	private const val TRIGGER_DISTANCE = 1.2
	private const val DISTANCE_EPSILON = 0.08
	private const val MIN_WAKE_DISTANCE = 0.2
	private const val MAX_WAKE_DISTANCE = 8.5
	private const val SIMULATED_JUMP_HOLD_TICKS = 2

	private var currentBobberId = -1
	private var jumpedBobberId = -1
	private var simulatedJumpReleaseTicks = 0

	fun register() {
		ClientTickEvents.START_CLIENT_TICK.register { client -> tick(client) }
		ClientTickEvents.END_CLIENT_TICK.register { client -> releaseSimulatedJump(client) }
	}

	@JvmStatic
	fun handleParticle(packet: ClientboundLevelParticlesPacket) {
		if (
			!isActive() ||
			packet.count != 0 ||
			packet.particle.type !== ParticleTypes.FISHING
		) {
			return
		}

		val client = Minecraft.getInstance()
		if (client.screen != null || !client.isWindowActive) {
			return
		}

		val player = client.player ?: return
		val bobber = player.fishing ?: return
		if (!isHoldingFishingRod(player)) {
			resetBobberState()
			return
		}

		updateBobberState(bobber.id)
		if (jumpedBobberId == bobber.id) {
			return
		}

		val distanceToWake = horizontalDistance(packet.x, packet.z, bobber.x, bobber.z)
		if (distanceToWake !in MIN_WAKE_DISTANCE..MAX_WAKE_DISTANCE) {
			return
		}

		if (distanceToWake <= TRIGGER_DISTANCE + DISTANCE_EPSILON && pressJumpOnce(client, player)) {
			jumpedBobberId = bobber.id
		}
	}

	private fun tick(client: Minecraft) {
		val player = client.player
		val bobber = player?.fishing
		if (!isActive() || player == null || bobber == null || !isHoldingFishingRod(player)) {
			resetBobberState()
			return
		}
		updateBobberState(bobber.id)
	}

	private fun updateBobberState(bobberId: Int) {
		if (currentBobberId != bobberId) {
			currentBobberId = bobberId
			jumpedBobberId = -1
		}
	}

	private fun resetBobberState() {
		currentBobberId = -1
		jumpedBobberId = -1
	}

	private fun pressJumpOnce(client: Minecraft, player: LocalPlayer): Boolean {
		if (!player.onGround() || player.isPassenger || player.abilities.flying || player.isFallFlying) {
			return false
		}

		val jumpKey = client.options.keyJump
		if (!jumpKey.isDown) {
			jumpKey.isDown = true
			simulatedJumpReleaseTicks = SIMULATED_JUMP_HOLD_TICKS
		}
		val accessor = jumpKey as KeyMappingAccessor
		accessor.fixifySetClickCount(accessor.fixifyClickCount() + 1)
		return true
	}

	private fun releaseSimulatedJump(client: Minecraft) {
		if (simulatedJumpReleaseTicks <= 0) {
			return
		}
		simulatedJumpReleaseTicks--
		if (simulatedJumpReleaseTicks == 0) {
			client.options.keyJump.isDown = false
		}
	}

	private fun isHoldingFishingRod(player: LocalPlayer): Boolean {
		return player.mainHandItem.item === Items.FISHING_ROD ||
			player.offhandItem.item === Items.FISHING_ROD
	}

	private fun isActive(): Boolean {
		return FixifyFeatures.leapFrogEnabled && SkyblockDataTracker.shouldRender()
	}

	private fun horizontalDistance(x1: Double, z1: Double, x2: Double, z2: Double): Double {
		val dx = x1 - x2
		val dz = z1 - z2
		return sqrt(dx * dx + dz * dz)
	}
}
