package dev.fixify.client

enum class FixifyHudWidget(
	val displayName: String,
	val configPrefix: String,
) {
	PET("Pet Overlay", "SkyBlock.Pet Overlay"),
	PRESSURE("Pressure Display", "SkyBlock.Pressure Display"),
	DRILL_FUEL("Drill Fuel Meter", "SkyBlock.Drill Fuel Meter"),
	DUNGEON_SCORE("Dungeon Score Meter", "Dungeons.Dungeon Score Meter"),
}
