package dev.fixify.client.feature

import dev.fixify.mixin.KeyMappingAccessor
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import java.util.Locale
import java.util.Random

object SmartTermAcFeature {
	private const val TERMINATOR_ID = "TERMINATOR"
	private const val CLICKS_PER_SECOND = 7.0
	private val rendEnchantIds = setOf("rend", "ultimate_rend")
	private val random = Random()

	private var nextLeftClick = 0L

	fun register() {
		ClientTickEvents.START_CLIENT_TICK.register { client -> tick(client) }
	}

	private fun tick(client: Minecraft) {
		if (!shouldClick(client)) {
			nextLeftClick = 0L
			return
		}

		val now = System.currentTimeMillis()
		if (now < nextLeftClick) {
			return
		}

		val delay = (1000.0 / CLICKS_PER_SECOND).toLong()
		val randomOffset = (random.nextGaussian() * 60.0 - 30.0).toLong()
		nextLeftClick = now + (delay + randomOffset).coerceAtLeast(1L)

		val attackKey = client.options.keyAttack as KeyMappingAccessor
		attackKey.fixifySetClickCount(attackKey.fixifyClickCount() + 1)
	}

	private fun shouldClick(client: Minecraft): Boolean {
		if (
			!FixifyFeatures.smartTermAcEnabled ||
			!SkyblockDataTracker.shouldRender() ||
			client.screen != null ||
			!client.isWindowActive
		) {
			return false
		}

		val player = client.player ?: return false
		if (player.isUsingItem || !client.options.keyUse.isDown) {
			return false
		}

		val stack = player.mainHandItem
		return stack.skyblockId() == TERMINATOR_ID && !stack.hasRend()
	}

	private fun ItemStack.skyblockId(): String {
		if (isEmpty) {
			return ""
		}
		val tag = getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
		return tag.getString("id").orElse("").replace(':', '-')
	}

	private fun ItemStack.hasRend(): Boolean {
		if (isEmpty) {
			return false
		}
		val tag = getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
		val enchantments = tag.getCompound("enchantments").orElse(null) ?: return false
		return enchantments.keySet().any { id ->
			normalize(id) in rendEnchantIds && enchantments.getIntOr(id, 0) >= 1
		}
	}

	private fun normalize(value: String): String {
		return value.lowercase(Locale.US).replace(' ', '_').replace('-', '_')
	}
}
