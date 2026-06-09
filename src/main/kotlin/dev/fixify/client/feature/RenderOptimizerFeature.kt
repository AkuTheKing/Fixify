package dev.fixify.client.feature

import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object RenderOptimizerFeature {
	private const val TENTACLE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg1NzI3NzI0OSwKICAicHJvZmlsZUlkIiA6ICIxODA1Y2E2MmM0ZDI0M2NiOWQxYmY4YmM5N2E1YjgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJSdWxsZWQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdkODM2NzQ5MjZiODk3MTRlNmI1YTU1NDcwNTAxYzA0YjA2NmRkODdiZjZjMzM1Y2RkYzZlNjBhMWExYTVmNSIKICAgIH0KICB9Cn0="
	private const val HEALER_FAIRY_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTQ2MzA5MTA0NywKICAicHJvZmlsZUlkIiA6ICIyNjRkYzBlYjVlZGI0ZmI3OTgxNWIyZGY1NGY0OTgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJxdWludHVwbGV0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJlZWRjZmZjNmExMWEzODM0YTI4ODQ5Y2MzMTZhZjdhMjc1MmEzNzZkNTM2Y2Y4NDAzOWNmNzkxMDhiMTY3YWUiCiAgICB9CiAgfQp9"
	private const val SOUL_WEAVER_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NTk1ODAzNjI1NTMsInByb2ZpbGVJZCI6ImU3NmYwZDlhZjc4MjQyYzM5NDY2ZDY3MjE3MzBmNDUzIiwicHJvZmlsZU5hbWUiOiJLbGxscmFoIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yZjI0ZWQ2ODc1MzA0ZmE0YTFmMGM3ODViMmNiNmE2YTcyNTYzZTlmM2UyNGVhNTVlMTgxNzg0NTIxMTlhYTY2In19fQ=="

	@JvmStatic
	fun shouldHideSpawn(type: EntityType<*>): Boolean {
		if (!FixifyFeatures.renderOptimizerEnabled) {
			return false
		}
		return when (type) {
			EntityType.FALLING_BLOCK -> FixifyFeatures.renderOptimizerHideFallingBlocks
			EntityType.LIGHTNING_BOLT -> FixifyFeatures.renderOptimizerHideLightning
			EntityType.EXPERIENCE_ORB -> FixifyFeatures.renderOptimizerHideExperienceOrbs
			else -> false
		}
	}

	@JvmStatic
	fun shouldHideParticle(packet: ClientboundLevelParticlesPacket): Boolean {
		if (!FixifyFeatures.renderOptimizerEnabled || !FixifyFeatures.renderOptimizerHideExplosionParticles) {
			return false
		}
		val type = packet.particle.type
		return type === ParticleTypes.EXPLOSION || type === ParticleTypes.EXPLOSION_EMITTER
	}

	@JvmStatic
	fun handleEntityData(packet: ClientboundSetEntityDataPacket) {
		if (
			!FixifyFeatures.renderOptimizerEnabled ||
			!FixifyFeatures.renderOptimizerHideArcherPassive ||
			!SkyblockDataTracker.inDungeon
		) {
			return
		}
		val stack = packet.packedItems.firstOrNull { it.id == 8 }?.value as? ItemStack ?: return
		if (!stack.isEmpty && stack.item === Items.BONE_MEAL) {
			Minecraft.getInstance().level?.removeEntity(packet.id, Entity.RemovalReason.DISCARDED)
		}
	}

	@JvmStatic
	fun handleEquipment(packet: ClientboundSetEquipmentPacket) {
		if (!FixifyFeatures.renderOptimizerEnabled || !SkyblockDataTracker.inDungeon) {
			return
		}
		for (slot in packet.slots) {
			val stack = slot.second
			if (stack.isEmpty) {
				continue
			}
			val texture = texture(stack) ?: continue
			val shouldHide =
				(FixifyFeatures.renderOptimizerHideHealerFairy &&
					slot.first === EquipmentSlot.MAINHAND &&
					texture == HEALER_FAIRY_TEXTURE) ||
					(FixifyFeatures.renderOptimizerHideSoulWeaver &&
						slot.first === EquipmentSlot.HEAD &&
						texture == SOUL_WEAVER_TEXTURE) ||
					(FixifyFeatures.renderOptimizerHideTentacleHead &&
						slot.first === EquipmentSlot.HEAD &&
						texture == TENTACLE_TEXTURE)
			if (shouldHide) {
				Minecraft.getInstance().level?.removeEntity(packet.entity, Entity.RemovalReason.DISCARDED)
				return
			}
		}
	}

	@JvmStatic
	fun shouldHideEntity(entity: Entity): Boolean {
		if (!FixifyFeatures.renderOptimizerEnabled || !FixifyFeatures.renderOptimizerHideDeathAnimation) {
			return false
		}
		if (entity is LivingEntity && entity.isDeadOrDying) {
			return true
		}
		if (!FixifyFeatures.renderOptimizerHideDyingArmorStands || entity !is ArmorStand) {
			return false
		}
		val owner = entity.level().getEntity(entity.id - 1)
		return owner is LivingEntity && owner.isDeadOrDying
	}

	@JvmStatic
	fun shouldHideFireOverlay(): Boolean {
		return FixifyFeatures.renderOptimizerEnabled && FixifyFeatures.renderOptimizerHideFireOverlay
	}

	private fun texture(stack: ItemStack): String? {
		return stack.get(DataComponents.PROFILE)
			?.partialProfile()
			?.properties
			?.get("textures")
			?.firstOrNull()
			?.value
	}
}
