package dev.fixify.mixin;

import dev.fixify.client.feature.LeapFrogFeature;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class LeapFrogParticleMixin {
	@Inject(method = "handleParticleEvent", at = @At("TAIL"))
	private void fixify$handleFishingParticle(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
		LeapFrogFeature.handleParticle(packet);
	}
}
