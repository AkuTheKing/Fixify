package dev.fixify.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.fixify.client.feature.PlayerHiderFeature;
import dev.fixify.client.feature.PlayerSizeFeature;
import dev.fixify.client.render.AvatarRenderStateExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
		at = @At("TAIL")
	)
	private void fixify$prepareAvatarFeatures(
		Avatar renderedPlayer,
		AvatarRenderState renderState,
		float tickDelta,
		CallbackInfo ci
	) {
		AvatarRenderStateExt state = (AvatarRenderStateExt)renderState;
		state.fixify$setPlayerHiderHidden(false);
		state.fixify$setPlayerHiderGhost(false);

		boolean shouldScale = PlayerSizeFeature.shouldScale(renderedPlayer);
		state.fixify$setPlayerSizeScale(shouldScale);
		PlayerSizeFeature.adjustNameTag(renderState, shouldScale);

		if (!PlayerHiderFeature.shouldHide(
			Minecraft.getInstance().player,
			renderedPlayer,
			renderState.x,
			renderState.y,
			renderState.z
		)) {
			return;
		}

		boolean ghost = PlayerHiderFeature.shouldRenderAsGhost(
			Minecraft.getInstance().player,
			renderedPlayer,
			renderState.x,
			renderState.y,
			renderState.z
		);
		state.fixify$setPlayerHiderHidden(true);
		state.fixify$setPlayerHiderGhost(ghost);
		renderState.isInvisible = !ghost;
		renderState.isInvisibleToPlayer = !ghost;
		renderState.showCape = false;
		renderState.nameTag = null;
		renderState.scoreText = null;
	}

	@Inject(
		method = "scale(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
		at = @At("TAIL")
	)
	private void fixify$scalePlayerModel(AvatarRenderState renderState, PoseStack poseStack, CallbackInfo ci) {
		if (((AvatarRenderStateExt)renderState).fixify$shouldPlayerSizeScale()) {
			PlayerSizeFeature.applyScale(renderState, poseStack);
		}
	}

	@Inject(
		method = "shouldRenderLayers(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void fixify$hidePlayerLayers(
		AvatarRenderState renderState,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (((AvatarRenderStateExt)renderState).fixify$isPlayerHiderHidden()) {
			cir.setReturnValue(false);
		}
	}
}
