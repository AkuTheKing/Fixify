package dev.fixify.client

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.blaze3d.platform.InputConstants
import dev.fixify.client.feature.DungeonBreakerFeature
import dev.fixify.client.feature.EtherwarpFeature
import dev.fixify.client.feature.FixifyFeatures
import dev.fixify.client.feature.SkyblockDataTracker
import dev.fixify.client.feature.SkyblockHudRenderer
import dev.fixify.client.feature.SkyblockTooltipFeatures
import dev.fixify.client.feature.TeammateHighlightRenderer
import dev.fixify.client.render.FixifyNvgPipRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry
import net.minecraft.client.Minecraft

class FixifyClient : ClientModInitializer {
	override fun onInitializeClient() {
		FixifyConfig.load()
		FixifyConfig.migrateEntryPrefix("Misc.Etherwarp", "Dungeons.Etherwarp")
		FixifyConfig.migrateEtherwarpLeftClickMode()
		FixifyFeatures.loadFromConfig()

		PictureInPictureRendererRegistry.register { context ->
			FixifyNvgPipRenderer(context.bufferSource())
		}
		DungeonBreakerFeature.register()
		EtherwarpFeature.register()
		TeammateHighlightRenderer.register()
		SkyblockDataTracker.register()
		SkyblockHudRenderer.register()
		SkyblockTooltipFeatures.register()

		ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
			dispatcher.register(
				LiteralArgumentBuilder.literal<FabricClientCommandSource>("fixify")
					.executes { context ->
						context.source.client.execute {
							openMenu(context.source.client)
						}
						1
					}
					.then(
						LiteralArgumentBuilder.literal<FabricClientCommandSource>("hudedit").executes { context ->
							context.source.client.execute {
								context.source.client.setScreen(FixifyHudEditorScreen(null))
							}
							1
						},
					),
			)
		}

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			val menuKeyDown = InputConstants.isKeyDown(client.window, InputConstants.KEY_RSHIFT)
			if (menuKeyDown && !wasMenuKeyDown) {
				toggleMenu(client)
			}
			wasMenuKeyDown = menuKeyDown
			FixifyKeybinds.tick(client)
		}
	}

	companion object {
		const val MOD_ID: String = "fixify"

		private var wasMenuKeyDown: Boolean = false

		fun openMenu(client: Minecraft = Minecraft.getInstance()) {
			client.setScreen(FixifyMenuScreen())
		}

		private fun toggleMenu(client: Minecraft) {
			val screen = client.screen
			if (screen is FixifyMenuScreen) {
				screen.beginClose()
			} else {
				openMenu(client)
			}
		}
	}
}
