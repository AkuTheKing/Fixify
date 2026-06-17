package dev.fixify.client

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.reader
import kotlin.io.path.writeText
import kotlin.math.roundToInt

object FixifyConfig {
	private val gson = GsonBuilder().setPrettyPrinting().create()
	private val path: Path
		get() = FabricLoader.getInstance().configDir.resolve("fixify.json")

	private var data = ConfigData()
	private val entries: MutableMap<String, EntryData>
		get() {
			data.sanitize()
			return data.entries!!
		}
	private val columns: MutableMap<String, ColumnData>
		get() {
			data.sanitize()
			return data.columns!!
		}

	fun load() {
		val configPath = path
		data = if (configPath.exists()) {
			runCatching {
				configPath.reader().use { gson.fromJson(it, ConfigData::class.java) }
			}.getOrNull() ?: ConfigData()
		} else {
			ConfigData()
		}
		data.sanitize()
	}

	fun save() {
		val configPath = path
		configPath.parent?.createDirectories()
		configPath.writeText(gson.toJson(data))
	}

	fun entry(key: String): EntryData? = entries[key]

	fun entriesSnapshot(): Map<String, EntryData> = entries.toMap()

	fun migrateEntryPrefix(oldPrefix: String, newPrefix: String) {
		migrateEntryPrefixes(oldPrefix to newPrefix)
	}

	fun migrateEntryPrefixes(vararg migrations: Pair<String, String>) {
		var changed = false
		for ((oldPrefix, newPrefix) in migrations) {
			val oldEntries = entries.filterKeys { it == oldPrefix || it.startsWith("$oldPrefix.") }
			for ((oldKey, entry) in oldEntries) {
				val newKey = newPrefix + oldKey.removePrefix(oldPrefix)
				entries.putIfAbsent(newKey, entry)
				entries.remove(oldKey)
				changed = true
			}
		}
		if (changed) {
			save()
		}
	}

	fun removeEntryPrefix(prefix: String) {
		val removed = entries.keys.removeIf { it == prefix || it.startsWith("$prefix.") }
		if (removed) {
			save()
		}
	}

	fun migrateEtherwarpLeftClickMode() {
		val modeKey = "Dungeons.Etherwarp.Left Click Mode"
		val leftClickKey = "Dungeons.Etherwarp.Left click etherwarp"
		val autoShiftKey = "Dungeons.Etherwarp.Shift automatically"
		val leftClick = entries.remove(leftClickKey)?.switchValue
		val autoShift = entries.remove(autoShiftKey)?.switchValue
		if (leftClick == null && autoShift == null) {
			return
		}
		val mode = when {
			leftClick == false -> 0
			autoShift == true -> 2
			else -> 1
		}
		entries.getOrPut(modeKey) { EntryData() }.selected = mode
		save()
	}

	fun migrateZoomIntensity() {
		val oldEntry = entries["Visuals.Zoom.FOV"] ?: return
		if (entries.containsKey("Visuals.Zoom.Intensity")) {
			entries.remove("Visuals.Zoom.FOV")
			save()
			return
		}
		val oldFov = oldEntry.value?.toFloatOrNull()
			?: (10.0f + 100.0f * (oldEntry.sliderPercentage ?: 0.2f).coerceIn(0.0f, 1.0f))
		val intensity = (1.0f + (50.0f - oldFov.coerceIn(1.0f, 50.0f)) * 9.0f / 49.0f)
			.roundToInt()
			.coerceIn(1, 10)
		entries.remove("Visuals.Zoom.FOV")
		entries["Visuals.Zoom.Intensity"] = EntryData().apply {
			value = intensity.toString()
			sliderPercentage = (intensity - 1).toFloat() / 9.0f
		}
		save()
	}

	fun updateEntry(key: String, updater: (EntryData) -> Unit) {
		val entry = entries.getOrPut(key) { EntryData() }
		updater(entry)
		save()
	}

	fun column(key: String): ColumnData? = columns[key]

	fun migrateColumn(oldKey: String, newKey: String) {
		val oldColumn = columns.remove(oldKey) ?: return
		columns.putIfAbsent(newKey, oldColumn)
		save()
	}

	fun updateColumn(key: String, updater: (ColumnData) -> Unit) {
		val column = columns.getOrPut(key) { ColumnData() }
		updater(column)
		save()
	}

	fun getAutoUpdateEnabled(): Boolean = data.autoUpdateEnabled ?: true

	fun setAutoUpdateEnabled(enabled: Boolean) {
		data.autoUpdateEnabled = enabled
	}

	fun getAutoUpdateConsentGiven(): Boolean = data.autoUpdateConsentGiven ?: true

	fun setAutoUpdateConsentGiven(consentGiven: Boolean) {
		data.autoUpdateConsentGiven = consentGiven
	}

	class ConfigData {
		var entries: MutableMap<String, EntryData>? = linkedMapOf()
		var columns: MutableMap<String, ColumnData>? = linkedMapOf()
		var autoUpdateEnabled: Boolean? = true
		var autoUpdateConsentGiven: Boolean? = true

		fun sanitize() {
			if (entries == null) {
				entries = linkedMapOf()
			}
			if (columns == null) {
				columns = linkedMapOf()
			}
			if (autoUpdateEnabled == null) {
				autoUpdateEnabled = true
			}
			if (autoUpdateConsentGiven == null) {
				autoUpdateConsentGiven = true
			}
		}
	}

	class EntryData {
		var enabled: Boolean? = null
		var settingsExpanded: Boolean? = null
		var switchValue: Boolean? = null
		var value: String? = null
		var keyName: String? = null
		var selected: Int? = null
		var sliderPercentage: Float? = null
		var color: Int? = null
	}

	class ColumnData {
		var x: Float? = null
		var y: Float? = null
		var extended: Boolean? = null
	}
}
