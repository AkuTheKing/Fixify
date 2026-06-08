package dev.fixify.client.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.CompareOp
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.rendertype.LayeringTransform
import net.minecraft.client.renderer.rendertype.OutputTarget
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.resources.Identifier

object FixifyWorldRenderTypes {
	private val filledEspPipeline = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(Identifier.fromNamespaceAndPath("fixify", "filled_esp"))
			.withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
			.withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
			.build(),
	)

	private val filledWorldPipeline = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(Identifier.fromNamespaceAndPath("fixify", "filled_world"))
			.withDepthStencilState(DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
			.withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
			.build(),
	)

	private val linesEspPipeline = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
			.withLocation(Identifier.fromNamespaceAndPath("fixify", "lines_esp"))
			.withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
			.withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
			.build(),
	)

	private val linesWorldPipeline = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
			.withLocation(Identifier.fromNamespaceAndPath("fixify", "lines_world"))
			.withDepthStencilState(DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
			.withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
			.build(),
	)

	val filledEsp: RenderType = RenderType.create(
		"fixify-filled-esp",
		RenderSetup.builder(filledEspPipeline)
			.bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
			.sortOnUpload()
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.MAIN_TARGET)
			.createRenderSetup(),
	)

	val filledWorld: RenderType = RenderType.create(
		"fixify-filled-world",
		RenderSetup.builder(filledWorldPipeline)
			.bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
			.sortOnUpload()
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.MAIN_TARGET)
			.createRenderSetup(),
	)

	val linesEsp: RenderType = RenderType.create(
		"fixify-lines-esp",
		RenderSetup.builder(linesEspPipeline)
			.bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.MAIN_TARGET)
			.createRenderSetup(),
	)

	val linesWorld: RenderType = RenderType.create(
		"fixify-lines-world",
		RenderSetup.builder(linesWorldPipeline)
			.bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.MAIN_TARGET)
			.createRenderSetup(),
	)
}
