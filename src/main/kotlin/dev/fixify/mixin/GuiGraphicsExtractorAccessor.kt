package dev.fixify.mixin

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.state.gui.GuiRenderState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(GuiGraphicsExtractor::class)
interface GuiGraphicsExtractorAccessor {
	@Accessor("guiRenderState")
	fun fixifyGuiRenderState(): GuiRenderState
}
