package dev.fixify.mixin;

import dev.fixify.client.feature.FullbrightFeature;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Lightmap.class)
public class LightmapMixin {
	@Inject(
		method = "getBrightness(Lnet/minecraft/world/level/dimension/DimensionType;I)F",
		at = @At("RETURN"),
		cancellable = true
	)
	private static void fixify$forceFullBrightness(
		DimensionType dimensionType,
		int lightLevel,
		CallbackInfoReturnable<Float> cir
	) {
		if (FullbrightFeature.isActive()) {
			cir.setReturnValue(1.0F);
		}
	}
}
