package dev.fixify.mixin;

import dev.fixify.client.feature.RenderOptimizerFeature;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererOptimizerMixin<T extends Entity> {
	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void fixify$hideDeathAnimations(
		T entity,
		Frustum frustum,
		double cameraX,
		double cameraY,
		double cameraZ,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (RenderOptimizerFeature.shouldHideEntity(entity)) {
			cir.setReturnValue(false);
		}
	}
}
