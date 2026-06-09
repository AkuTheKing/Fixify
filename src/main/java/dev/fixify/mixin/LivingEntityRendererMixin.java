package dev.fixify.mixin;

import dev.fixify.client.feature.PlayerHiderFeature;
import dev.fixify.client.render.AvatarRenderStateExt;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
	@ModifyConstant(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		constant = @Constant(intValue = 654311423)
	)
	private int fixify$playerGhostOpacity(int original, LivingEntityRenderState renderState) {
		if (
			renderState instanceof AvatarRenderStateExt state &&
			state.fixify$isPlayerHiderGhost()
		) {
			return PlayerHiderFeature.ghostColor();
		}
		return original;
	}
}
