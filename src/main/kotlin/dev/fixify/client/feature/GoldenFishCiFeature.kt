package dev.fixify.client.feature

import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items

object GoldenFishCiFeature {
	fun register() {
		UseEntityCallback.EVENT.register { player, level, hand, _, _ ->
			if (
				!level.isClientSide ||
				!FixifyFeatures.goldenFishCiEnabled ||
				!SkyblockDataTracker.shouldRender() ||
				!isHoldingFishingRod(player)
			) {
				return@register InteractionResult.PASS
			}

			val client = Minecraft.getInstance()
			if (client.player !== player) {
				return@register InteractionResult.PASS
			}

			val useHand = rodHand(player, hand) ?: hand
			val result = client.gameMode?.useItem(player, useHand) ?: InteractionResult.PASS
			if (result.consumesAction()) {
				player.swing(useHand)
			}
			InteractionResult.FAIL
		}
	}

	private fun isHoldingFishingRod(player: Player): Boolean {
		return InteractionHand.entries.any { player.getItemInHand(it).item === Items.FISHING_ROD }
	}

	private fun rodHand(player: Player, preferredHand: InteractionHand): InteractionHand? {
		if (player.getItemInHand(preferredHand).item === Items.FISHING_ROD) {
			return preferredHand
		}
		return InteractionHand.entries.firstOrNull { player.getItemInHand(it).item === Items.FISHING_ROD }
	}
}
