package dev.fixify.client.feature

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import java.util.ArrayDeque
import kotlin.math.roundToInt

object PerformanceMetricsFeature {
	@Volatile
	var averageTps: Float = 20.0f
		private set

	@Volatile
	var averagePing: Int = 0
		private set

	private val pingSamples = ArrayDeque<Int>()
	private var previousTimePacketAt = 0L
	private var nextPingSampleAt = 0L

	fun register() {
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			samplePing(client)
		}
		ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
			reset()
		}
	}

	@JvmStatic
	fun handleTimeUpdate() {
		val now = System.currentTimeMillis()
		if (previousTimePacketAt != 0L) {
			averageTps = (20_000.0f / (now - previousTimePacketAt + 1L)).coerceIn(0.0f, 20.0f)
		}
		previousTimePacketAt = now
	}

	fun fps(): Int = Minecraft.getInstance().fps

	private fun samplePing(client: Minecraft) {
		val now = System.currentTimeMillis()
		if (now < nextPingSampleAt) {
			return
		}
		nextPingSampleAt = now + 500L

		val player = client.player ?: return
		val ping = client.connection?.getPlayerInfo(player.uuid)?.latency?.coerceAtLeast(0) ?: return
		pingSamples.addLast(ping)
		while (pingSamples.size > 20) {
			pingSamples.removeFirst()
		}
		averagePing = pingSamples.average().roundToInt()
	}

	private fun reset() {
		averageTps = 20.0f
		averagePing = 0
		previousTimePacketAt = 0L
		nextPingSampleAt = 0L
		pingSamples.clear()
	}
}
