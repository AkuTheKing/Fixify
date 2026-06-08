package com.mojang.blaze3d.opengl

import com.mojang.blaze3d.systems.GpuDevice
import dev.fixify.mixin.GpuDeviceAccessor

object FixifyGlBridge {
	fun directStateAccess(device: GpuDevice): DirectStateAccess? {
		val backend = (device as GpuDeviceAccessor).fixifyBackend()
		return if (backend is GlDevice) backend.directStateAccess() else null
	}
}
