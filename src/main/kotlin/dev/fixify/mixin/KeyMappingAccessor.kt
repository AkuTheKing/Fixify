package dev.fixify.mixin

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(KeyMapping::class)
interface KeyMappingAccessor {
	@Accessor("key")
	fun fixifyBoundKey(): InputConstants.Key

	@Accessor("clickCount")
	fun fixifyClickCount(): Int

	@Accessor("clickCount")
	fun fixifySetClickCount(count: Int)
}
