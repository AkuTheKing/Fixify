package dev.fixify.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import dev.fixify.client.feature.HitColorFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OverlayTexture.class)
public class OverlayTextureMixin {
	@Shadow
	@Final
	private DynamicTexture texture;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void fixify$installHitColorUpdater(CallbackInfo ci) {
		HitColorFeature.installUpdateCallback(
			() -> Minecraft.getInstance().execute(this::fixify$updateHitColor)
		);
		HitColorFeature.refresh();
	}

	@Unique
	private void fixify$updateHitColor() {
		NativeImage pixels = this.texture.getPixels();
		if (pixels == null) {
			return;
		}

		int color = HitColorFeature.overlayColor();
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 16; x++) {
				pixels.setPixel(x, y, color);
			}
		}
		this.texture.upload();
	}
}
