package dev.fixify.mixin;

import dev.fixify.client.feature.InfiniteChatFeature;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
	@Inject(method = "clearMessages(Z)V", at = @At("HEAD"), cancellable = true)
	private void fixify$keepChatHistory(boolean clearSent, CallbackInfo ci) {
		if (InfiniteChatFeature.isActive()) {
			ci.cancel();
		}
	}

	@ModifyConstant(method = "*", constant = @Constant(intValue = 100), require = 0)
	private int fixify$expandChatLimit(int original) {
		return InfiniteChatFeature.isActive() ? InfiniteChatFeature.CHAT_LIMIT : original;
	}
}
