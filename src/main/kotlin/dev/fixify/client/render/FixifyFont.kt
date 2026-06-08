package dev.fixify.client.render

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FixifyFont(
	val name: String,
	inputStream: InputStream,
) {
	private val cachedBytes = inputStream.use { it.readBytes() }

	fun buffer(): ByteBuffer {
		return ByteBuffer.allocateDirect(cachedBytes.size)
			.order(ByteOrder.nativeOrder())
			.put(cachedBytes)
			.flip() as ByteBuffer
	}

	override fun hashCode(): Int = name.hashCode()

	override fun equals(other: Any?): Boolean = other is FixifyFont && name == other.name
}
