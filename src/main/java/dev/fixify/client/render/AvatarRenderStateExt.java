package dev.fixify.client.render;

public interface AvatarRenderStateExt {
	boolean fixify$isPlayerHiderHidden();

	void fixify$setPlayerHiderHidden(boolean hidden);

	boolean fixify$isPlayerHiderGhost();

	void fixify$setPlayerHiderGhost(boolean ghost);

	boolean fixify$shouldPlayerSizeScale();

	void fixify$setPlayerSizeScale(boolean shouldScale);
}
