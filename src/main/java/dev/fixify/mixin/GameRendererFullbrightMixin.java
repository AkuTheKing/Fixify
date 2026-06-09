package dev.fixify.mixin;

import dev.fixify.client.feature.FullbrightFeature;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererFullbrightMixin {
	@Inject(method = "getBossOverlayWorldDarkening", at = @At("RETURN"), cancellable = true)
	private void fixify$disableWorldDarkening(float tickDelta, CallbackInfoReturnable<Float> cir) {
		if (FullbrightFeature.isActive()) {
			cir.setReturnValue(0.0F);
		}
	}
}
