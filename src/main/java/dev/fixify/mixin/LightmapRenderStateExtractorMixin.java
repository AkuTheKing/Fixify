package dev.fixify.mixin;

import dev.fixify.client.feature.FullbrightFeature;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public class LightmapRenderStateExtractorMixin {
	@Inject(
		method = "extract(Lnet/minecraft/client/renderer/state/LightmapRenderState;F)V",
		at = @At("TAIL")
	)
	private void fixify$applyFullbright(LightmapRenderState state, float tickDelta, CallbackInfo ci) {
		if (!FullbrightFeature.isActive()) {
			return;
		}

		Vector3f white = new Vector3f(1.0F, 1.0F, 1.0F);
		state.needsUpdate = true;
		state.blockFactor = 1.4F;
		state.blockLightTint = white;
		state.skyFactor = 1.0F;
		state.skyLightColor = white;
		state.ambientColor = white;
		state.brightness = 1.0F;
		state.darknessEffectScale = 0.0F;
		state.bossOverlayWorldDarkening = 0.0F;
	}
}
