package dev.fixify.client

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.blaze3d.platform.InputConstants
import dev.fixify.client.feature.AutoUpdater
import dev.fixify.client.feature.DungeonBreakerFeature
import dev.fixify.client.feature.EtherwarpFeature
import dev.fixify.client.feature.FixifyFeatures
import dev.fixify.client.feature.GoldenFishCiFeature
import dev.fixify.client.feature.LeapFrogFeature
import dev.fixify.client.feature.PerformanceMetricsFeature
import dev.fixify.client.feature.ReminderFeature
import dev.fixify.client.feature.SkyblockDataTracker
import dev.fixify.client.feature.SkyblockHudRenderer
import dev.fixify.client.feature.SkyblockTooltipFeatures
import dev.fixify.client.feature.SmartTermAcFeature
import dev.fixify.client.feature.TeammateHighlightRenderer
import dev.fixify.client.feature.ZoomFeature
import dev.fixify.client.render.FixifyNvgPipRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

class FixifyClient : ClientModInitializer {
	override fun onInitializeClient() {
		FixifyConfig.load()
		FixifyConfig.migrateEntryPrefixes(
			"Misc.Etherwarp" to "Dungeons.Etherwarp",
			"QoL.Day Reminder" to "QoL.Reminder",
			"QoL.Player Hider" to "Visuals.Player Hider",
			"QoL.Player Size" to "Visuals.Player Size",
			"QoL.Hit Color" to "Visuals.Hit Color",
			"QoL.Fullbright" to "Visuals.Fullbright",
			"QoL.Performance HUD" to "Visuals.Performance HUD",
			"QoL.Render Optimizer" to "Visuals.Render Optimizer",
			"QoL.Custom Name Replacer" to "Visuals.Name Replace",
			"QoL.Zoom" to "Visuals.Zoom",
			"SkyBlock.Pet Overlay" to "Visuals.Pet Overlay",
			"SkyBlock.Pressure Display" to "Visuals.Pressure Display",
			"SkyBlock.Low HP Indicator" to "Visuals.Low HP Indicator",
			"SkyBlock.Drill Fuel Meter" to "Visuals.Drill Fuel Meter",
			"SkyBlock.Action Bar Cleanup" to "Visuals.Action Bar Cleanup",
			"QoL.Diana QoL" to "Misc.Diana QoL",
			"QoL.Golden Fish CI" to "Misc.Golden Fish CI",
			"QoL.Leap Frog" to "Misc.Leap Frog",
			"QoL.Smart Term AC" to "Misc.Smart Term AC",
			"QoL.Infinite Chat" to "Misc.Infinite Chat",
			"QoL.Reminder" to "Misc.Reminder",
			"SkyBlock.Missing Enchants" to "Misc.Missing Enchants",
			"SkyBlock.Compact Pet Level" to "Misc.Compact Pet Level",
		)
		FixifyConfig.removeEntryPrefix("SkyBlock.Only in SkyBlock")
		FixifyConfig.removeEntryPrefix("Visuals.Name Replace.Original Name")
		FixifyConfig.migrateColumn("SkyBlock", "Visuals")
		FixifyConfig.migrateColumn("QoL", "Misc")
		FixifyConfig.migrateEtherwarpLeftClickMode()
		FixifyConfig.migrateZoomIntensity()
		FixifyFeatures.loadFromConfig()
		AutoUpdater.init()

		PictureInPictureRendererRegistry.register { context ->
			FixifyNvgPipRenderer(context.bufferSource())
		}
		DungeonBreakerFeature.register()
		EtherwarpFeature.register()
		TeammateHighlightRenderer.register()
		SkyblockDataTracker.register()
		SkyblockHudRenderer.register()
		SkyblockTooltipFeatures.register()
		GoldenFishCiFeature.register()
		LeapFrogFeature.register()
		PerformanceMetricsFeature.register()
		ReminderFeature.register()
		SmartTermAcFeature.register()
		ZoomFeature.register()

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
					)
					.then(autoCommand())
					.then(ReminderFeature.command()),
			)
		}

		ClientLifecycleEvents.CLIENT_STOPPING.register { _ ->
			AutoUpdater.onClientStopping()
		}

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			val menuKeyDown = InputConstants.isKeyDown(client.window, InputConstants.KEY_RSHIFT)
			if (menuKeyDown && !wasMenuKeyDown) {
				toggleMenu(client)
			}
			wasMenuKeyDown = menuKeyDown
			FixifyKeybinds.tick(client)
			AutoUpdater.onClientTick(client)
		}
	}

	companion object {
		const val MOD_ID: String = "fixify"

		private var wasMenuKeyDown: Boolean = false

		fun openMenu(client: Minecraft = Minecraft.getInstance()) {
			client.setScreen(FixifyMenuScreen())
		}

		private fun autoCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
			return LiteralArgumentBuilder.literal<FabricClientCommandSource>("auto")
				.executes { context ->
					sendUpdaterStatus(context.source.client)
					1
				}
				.then(
					LiteralArgumentBuilder.literal<FabricClientCommandSource>("status").executes { context ->
						sendUpdaterStatus(context.source.client)
						1
					},
				)
				.then(
					LiteralArgumentBuilder.literal<FabricClientCommandSource>("on").executes { context ->
						AutoUpdater.setEnabledState(true)
						AutoUpdater.checkForUpdatesAsync(true)
						context.source.client.player?.sendSystemMessage(updaterMessage("Auto updater enabled. Checking for updates..."))
						1
					},
				)
				.then(
					LiteralArgumentBuilder.literal<FabricClientCommandSource>("off").executes { context ->
						AutoUpdater.setEnabledState(false)
						context.source.client.player?.sendSystemMessage(updaterMessage("Auto updater disabled."))
						1
					},
				)
				.then(
					LiteralArgumentBuilder.literal<FabricClientCommandSource>("check").executes { context ->
						AutoUpdater.checkForUpdatesAsync(true)
						context.source.client.player?.sendSystemMessage(updaterMessage(AutoUpdater.getStatusLine()))
						1
					},
				)
		}

		private fun sendUpdaterStatus(client: Minecraft) {
			client.player?.sendSystemMessage(updaterMessage(AutoUpdater.getStatusLine()))
		}

		private fun updaterMessage(message: String): Component {
			return Component.literal("[Fixify] ").withStyle(ChatFormatting.LIGHT_PURPLE)
				.append(Component.literal(message).withStyle(ChatFormatting.YELLOW))
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
