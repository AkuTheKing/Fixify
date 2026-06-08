package dev.fixify.client.feature

import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import java.util.Locale

object DungeonBreakerFeature {
	private val blacklistedBlocks = setOf(
		Blocks.BARRIER,
		Blocks.BEDROCK,
		Blocks.COMMAND_BLOCK,
		Blocks.CHAIN_COMMAND_BLOCK,
		Blocks.REPEATING_COMMAND_BLOCK,
		Blocks.PLAYER_HEAD,
		Blocks.PLAYER_WALL_HEAD,
		Blocks.SKELETON_SKULL,
		Blocks.SKELETON_WALL_SKULL,
		Blocks.WITHER_SKELETON_SKULL,
		Blocks.WITHER_SKELETON_WALL_SKULL,
		Blocks.TNT,
		Blocks.CHEST,
		Blocks.TRAPPED_CHEST,
		Blocks.END_PORTAL_FRAME,
		Blocks.END_PORTAL,
		Blocks.PISTON,
		Blocks.PISTON_HEAD,
		Blocks.STICKY_PISTON,
		Blocks.MOVING_PISTON,
		Blocks.LEVER,
		Blocks.STONE_BUTTON,
	)

	fun register() {
		AttackBlockCallback.EVENT.register { player, level, hand, pos, direction ->
			onAttackBlock(player, level, hand, pos, direction)
		}
	}

	private fun onAttackBlock(
		player: Player,
		level: Level,
		hand: InteractionHand,
		pos: BlockPos,
		@Suppress("UNUSED_PARAMETER") direction: Direction,
	): InteractionResult {
		if (!FixifyFeatures.dungeonBreakerEnabled || hand != InteractionHand.MAIN_HAND || !level.isClientSide) {
			return InteractionResult.PASS
		}

		if (!isDungeonBreaker(player.mainHandItem)) {
			return InteractionResult.PASS
		}

		val state = level.getBlockState(pos)
		val block = state.block
		if (FixifyFeatures.dungeonPreventMiningSecrets && block in blacklistedBlocks) {
			return InteractionResult.FAIL
		}

		if (
			FixifyFeatures.dungeonInstaMineWhenFatigue &&
			player.hasEffect(MobEffects.MINING_FATIGUE) &&
			block !in blacklistedBlocks
		) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)
			return InteractionResult.SUCCESS
		}

		return InteractionResult.PASS
	}

	private fun isDungeonBreaker(stack: ItemStack): Boolean {
		if (stack.isEmpty) {
			return false
		}

		val name = stack.hoverName.string.lowercase(Locale.ROOT).filter { it.isLetterOrDigit() }
		val itemName = stack.itemName.string.lowercase(Locale.ROOT).filter { it.isLetterOrDigit() }
		return "dungeonbreaker" in name || "dungeonbreaker" in itemName
	}
}
