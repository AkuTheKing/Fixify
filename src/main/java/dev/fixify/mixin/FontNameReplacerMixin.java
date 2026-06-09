package dev.fixify.mixin;

import dev.fixify.client.feature.NameReplaceFeature;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public class FontNameReplacerMixin {
	@ModifyVariable(
		method = "prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;",
		at = @At("HEAD"),
		argsOnly = true
	)
	private String fixify$replacePreparedString(String text) {
		return NameReplaceFeature.replaceStringIfNeeded(text);
	}

	@ModifyVariable(
		method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;",
		at = @At("HEAD"),
		argsOnly = true
	)
	private FormattedCharSequence fixify$replacePreparedSequence(FormattedCharSequence text) {
		return NameReplaceFeature.replaceSequenceIfNeeded(text);
	}

	@ModifyVariable(method = "width(Ljava/lang/String;)I", at = @At("HEAD"), argsOnly = true)
	private String fixify$replaceWidthString(String text) {
		return NameReplaceFeature.replaceStringIfNeeded(text);
	}

	@ModifyVariable(
		method = "width(Lnet/minecraft/network/chat/FormattedText;)I",
		at = @At("HEAD"),
		argsOnly = true
	)
	private FormattedText fixify$replaceWidthComponent(FormattedText text) {
		if (text instanceof Component component) {
			Component replacement = NameReplaceFeature.replaceComponentIfNeeded(component);
			if (replacement != null) {
				return replacement;
			}
		}
		return text;
	}

	@ModifyVariable(
		method = "width(Lnet/minecraft/util/FormattedCharSequence;)I",
		at = @At("HEAD"),
		argsOnly = true
	)
	private FormattedCharSequence fixify$replaceWidthSequence(FormattedCharSequence text) {
		return NameReplaceFeature.replaceSequenceIfNeeded(text);
	}
}
