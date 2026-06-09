package dev.fixify.client

enum class FixifyHudWidget(
	val displayName: String,
	val configPrefix: String,
) {
	PET("Pet Overlay", "Visuals.Pet Overlay"),
	PRESSURE("Pressure Display", "Visuals.Pressure Display"),
	DRILL_FUEL("Drill Fuel Meter", "Visuals.Drill Fuel Meter"),
	DUNGEON_SCORE("Dungeon Score Meter", "Dungeons.Dungeon Score Meter"),
	PERFORMANCE("Performance HUD", "Visuals.Performance HUD"),
}
