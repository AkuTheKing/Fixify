package dev.fixify.client.feature

object FullbrightFeature {
	@JvmStatic
	fun isActive(): Boolean = FixifyFeatures.fullbrightEnabled
}
