package dev.fixify.mixin;

import dev.fixify.client.feature.DianaQolFeature;
import dev.fixify.client.feature.PlayerHiderFeature;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LocalPlayer.class, priority = 2000)
public abstract class LocalPlayerTargetingMixin {
	@Inject(method = "raycastHitResult", at = @At("RETURN"), cancellable = true)
	private void fixify$retargetIgnoredHits(
		float tickDelta,
		Entity cameraEntity,
		CallbackInfoReturnable<HitResult> cir
	) {
		LocalPlayer player = (LocalPlayer)(Object)this;
		HitResult hitResult = cir.getReturnValue();
		if (!fixify$shouldRetarget(player, hitResult)) {
			return;
		}
		cir.setReturnValue(fixify$findAlternateHit(player, cameraEntity, tickDelta));
	}

	private static boolean fixify$shouldRetarget(LocalPlayer player, HitResult hitResult) {
		if (hitResult instanceof EntityHitResult entityHitResult) {
			Entity entity = entityHitResult.getEntity();
			return fixify$isDyingLivingEntity(entity) ||
				PlayerHiderFeature.shouldClickThrough(player, entity);
		}
		if (hitResult instanceof BlockHitResult blockHitResult) {
			BlockState state = player.level().getBlockState(blockHitResult.getBlockPos());
			return DianaQolFeature.shouldIgnore(state);
		}
		return false;
	}

	private static HitResult fixify$findAlternateHit(LocalPlayer player, Entity cameraEntity, float tickDelta) {
		double blockRange = player.blockInteractionRange();
		double entityRange = player.entityInteractionRange();
		AttackRange attackRange = player.getActiveItem().get(DataComponents.ATTACK_RANGE);
		if (attackRange != null) {
			entityRange = Math.max(entityRange, attackRange.effectiveMaxRange(cameraEntity));
		}

		Vec3 start = cameraEntity.getEyePosition(tickDelta);
		Vec3 look = cameraEntity.getViewVector(tickDelta);
		Vec3 blockEnd = start.add(look.scale(blockRange));
		Vec3 entityEnd = start.add(look.scale(entityRange));
		BlockHitResult blockHit = fixify$raycastIgnoringDianaBlocks(player, cameraEntity, start, blockEnd, look);

		double maxEntityDistanceSquared = entityRange * entityRange;
		if (blockHit.getType() != HitResult.Type.MISS) {
			maxEntityDistanceSquared = start.distanceToSqr(blockHit.getLocation());
		}

		AABB searchBox = cameraEntity.getBoundingBox()
			.expandTowards(look.scale(entityRange))
			.inflate(1.0);
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
			cameraEntity,
			start,
			entityEnd,
			searchBox,
			entity -> EntitySelector.CAN_BE_PICKED.test(entity) &&
				!fixify$isDyingLivingEntity(entity) &&
				!PlayerHiderFeature.shouldClickThrough(player, entity),
			maxEntityDistanceSquared
		);
		return entityHit != null ? entityHit : blockHit;
	}

	private static BlockHitResult fixify$raycastIgnoringDianaBlocks(
		LocalPlayer player,
		Entity cameraEntity,
		Vec3 start,
		Vec3 end,
		Vec3 look
	) {
		Vec3 currentStart = start;
		for (int i = 0; i < 32; i++) {
			BlockHitResult hit = player.level().clip(new ClipContext(
				currentStart,
				end,
				ClipContext.Block.OUTLINE,
				ClipContext.Fluid.NONE,
				cameraEntity
			));
			if (hit.getType() == HitResult.Type.MISS) {
				return hit;
			}

			BlockState state = player.level().getBlockState(hit.getBlockPos());
			if (!DianaQolFeature.shouldIgnore(state)) {
				return hit;
			}

			currentStart = fixify$stepPastIgnoredHit(hit, look);
			if (currentStart.distanceToSqr(end) <= 0.0001) {
				break;
			}
		}

		return BlockHitResult.miss(
			end,
			Direction.getApproximateNearest(look),
			BlockPos.containing(end)
		);
	}

	private static Vec3 fixify$stepPastIgnoredHit(BlockHitResult hit, Vec3 look) {
		Vec3 next = hit.getLocation().add(look.scale(0.05));
		BlockPos hitPos = hit.getBlockPos();
		for (int i = 0; i < 8 && BlockPos.containing(next).equals(hitPos); i++) {
			next = next.add(look.scale(0.05));
		}
		return next;
	}

	private static boolean fixify$isDyingLivingEntity(Entity entity) {
		return entity instanceof LivingEntity living &&
			(living.isDeadOrDying() || living.getHealth() <= 0.0F || living.deathTime > 0);
	}
}
