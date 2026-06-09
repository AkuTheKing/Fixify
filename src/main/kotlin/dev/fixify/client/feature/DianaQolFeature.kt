package dev.fixify.client.feature

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.state.BlockState

object DianaQolFeature {
	private val ignoredBlockIds = setOf(
		"minecraft:short_grass",
		"minecraft:tall_grass",
		"minecraft:fern",
		"minecraft:large_fern",
		"minecraft:dead_bush",
		"minecraft:bush",
		"minecraft:red_tulip",
		"minecraft:azure_bluet",
		"minecraft:rose",
	)

	@JvmStatic
	fun isActive(): Boolean {
		return FixifyFeatures.dianaQolEnabled && SkyblockDataTracker.shouldRender()
	}

	@JvmStatic
	fun shouldIgnore(state: BlockState): Boolean {
		return isActive() && BuiltInRegistries.BLOCK.getKey(state.block).toString() in ignoredBlockIds
	}
}
