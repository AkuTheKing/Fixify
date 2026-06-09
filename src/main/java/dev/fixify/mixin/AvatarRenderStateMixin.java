package dev.fixify.mixin;

import dev.fixify.client.render.AvatarRenderStateExt;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements AvatarRenderStateExt {
	@Unique
	private boolean fixify$playerHiderHidden;

	@Unique
	private boolean fixify$playerHiderGhost;

	@Unique
	private boolean fixify$playerSizeScale;

	@Override
	public boolean fixify$isPlayerHiderHidden() {
		return this.fixify$playerHiderHidden;
	}

	@Override
	public void fixify$setPlayerHiderHidden(boolean hidden) {
		this.fixify$playerHiderHidden = hidden;
	}

	@Override
	public boolean fixify$isPlayerHiderGhost() {
		return this.fixify$playerHiderGhost;
	}

	@Override
	public void fixify$setPlayerHiderGhost(boolean ghost) {
		this.fixify$playerHiderGhost = ghost;
	}

	@Override
	public boolean fixify$shouldPlayerSizeScale() {
		return this.fixify$playerSizeScale;
	}

	@Override
	public void fixify$setPlayerSizeScale(boolean shouldScale) {
		this.fixify$playerSizeScale = shouldScale;
	}
}
