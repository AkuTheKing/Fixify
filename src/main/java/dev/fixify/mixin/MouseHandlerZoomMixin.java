package dev.fixify.mixin;

import dev.fixify.client.feature.ZoomFeature;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerZoomMixin {
	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void fixify$adjustZoom(long window, double horizontalOffset, double verticalOffset, CallbackInfo ci) {
		if (ZoomFeature.isZooming()) {
			ZoomFeature.adjustByScroll(verticalOffset);
			ci.cancel();
		}
	}
}
