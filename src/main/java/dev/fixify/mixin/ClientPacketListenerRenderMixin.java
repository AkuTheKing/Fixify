package dev.fixify.mixin;

import dev.fixify.client.feature.PerformanceMetricsFeature;
import dev.fixify.client.feature.RenderOptimizerFeature;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerRenderMixin {
	@Inject(method = "handleAddEntity", at = @At("HEAD"), cancellable = true)
	private void fixify$hideEntitySpawns(ClientboundAddEntityPacket packet, CallbackInfo ci) {
		if (RenderOptimizerFeature.shouldHideSpawn(packet.getType())) {
			ci.cancel();
		}
	}

	@Inject(method = "handleParticleEvent", at = @At("HEAD"), cancellable = true)
	private void fixify$hideExplosionParticles(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
		if (RenderOptimizerFeature.shouldHideParticle(packet)) {
			ci.cancel();
		}
	}

	@Inject(method = "handleSetEntityData", at = @At("TAIL"))
	private void fixify$hideArcherPassive(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
		RenderOptimizerFeature.handleEntityData(packet);
	}

	@Inject(method = "handleSetEquipment", at = @At("TAIL"))
	private void fixify$hideDungeonCosmetics(ClientboundSetEquipmentPacket packet, CallbackInfo ci) {
		RenderOptimizerFeature.handleEquipment(packet);
	}

	@Inject(method = "handleSetTime", at = @At("TAIL"))
	private void fixify$trackServerTps(ClientboundSetTimePacket packet, CallbackInfo ci) {
		PerformanceMetricsFeature.handleTimeUpdate();
	}
}
