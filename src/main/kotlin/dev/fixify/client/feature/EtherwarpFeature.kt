package dev.fixify.client.feature

import dev.fixify.client.render.FixifyWorldBoxes
import dev.fixify.mixin.KeyMappingAccessor
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.AirBlock
import net.minecraft.world.level.block.BubbleColumnBlock
import net.minecraft.world.level.block.BushBlock
import net.minecraft.world.level.block.ButtonBlock
import net.minecraft.world.level.block.CarpetBlock
import net.minecraft.world.level.block.ComparatorBlock
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.DryVegetationBlock
import net.minecraft.world.level.block.FireBlock
import net.minecraft.world.level.block.FlowerBlock
import net.minecraft.world.level.block.FlowerPotBlock
import net.minecraft.world.level.block.GrowingPlantBlock
import net.minecraft.world.level.block.LadderBlock
import net.minecraft.world.level.block.LeverBlock
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.MushroomBlock
import net.minecraft.world.level.block.NetherPortalBlock
import net.minecraft.world.level.block.NetherWartBlock
import net.minecraft.world.level.block.RailBlock
import net.minecraft.world.level.block.RedStoneWireBlock
import net.minecraft.world.level.block.RedstoneTorchBlock
import net.minecraft.world.level.block.RepeaterBlock
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.block.SeagrassBlock
import net.minecraft.world.level.block.SkullBlock
import net.minecraft.world.level.block.SmallDripleafBlock
import net.minecraft.world.level.block.SnowLayerBlock
import net.minecraft.world.level.block.StemBlock
import net.minecraft.world.level.block.SugarCaneBlock
import net.minecraft.world.level.block.TallFlowerBlock
import net.minecraft.world.level.block.TallGrassBlock
import net.minecraft.world.level.block.TallSeagrassBlock
import net.minecraft.world.level.block.TorchBlock
import net.minecraft.world.level.block.TripWireBlock
import net.minecraft.world.level.block.TripWireHookBlock
import net.minecraft.world.level.block.VineBlock
import net.minecraft.world.level.block.WallSkullBlock
import net.minecraft.world.level.block.WebBlock
import net.minecraft.world.level.block.WoolCarpetBlock
import net.minecraft.world.level.block.piston.PistonHeadBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sign

object EtherwarpFeature {
	private val shiftDelays = intArrayOf(2, 3, 4)
	private var ticksLeft = 0
	private var shiftHeld = false

	private val validFeetBlocks = setOf(
		ButtonBlock::class.java,
		CarpetBlock::class.java,
		SkullBlock::class.java,
		WallSkullBlock::class.java,
		LadderBlock::class.java,
		SaplingBlock::class.java,
		FlowerBlock::class.java,
		StemBlock::class.java,
		CropBlock::class.java,
		RailBlock::class.java,
		SnowLayerBlock::class.java,
		BubbleColumnBlock::class.java,
		TripWireBlock::class.java,
		TripWireHookBlock::class.java,
		FireBlock::class.java,
		AirBlock::class.java,
		TorchBlock::class.java,
		FlowerPotBlock::class.java,
		TallFlowerBlock::class.java,
		TallGrassBlock::class.java,
		BushBlock::class.java,
		SeagrassBlock::class.java,
		TallSeagrassBlock::class.java,
		SugarCaneBlock::class.java,
		LiquidBlock::class.java,
		VineBlock::class.java,
		MushroomBlock::class.java,
		GrowingPlantBlock::class.java,
		PistonHeadBlock::class.java,
		WoolCarpetBlock::class.java,
		WebBlock::class.java,
		DryVegetationBlock::class.java,
		SmallDripleafBlock::class.java,
		LeverBlock::class.java,
		NetherWartBlock::class.java,
		NetherPortalBlock::class.java,
		RedStoneWireBlock::class.java,
		ComparatorBlock::class.java,
		RedstoneTorchBlock::class.java,
		RepeaterBlock::class.java,
	)

	fun register() {
		ClientPreAttackCallback.EVENT.register { client, player, clickCount ->
			handleLeftClick(client, player.mainHandItem, clickCount)
		}
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			tickShift(client)
		}
		LevelRenderEvents.END_MAIN.register { context ->
			render(context)
		}
	}

	private fun handleLeftClick(client: Minecraft, stack: ItemStack, clickCount: Int): Boolean {
		val leftClickMode = FixifyFeatures.etherwarpLeftClickMode
		if (!FixifyFeatures.etherwarpEnabled || leftClickMode == LEFT_CLICK_NONE || client.screen != null) {
			return false
		}

		if (stack.etherwarpData() == null) {
			return false
		}

		val player = client.player ?: return false
		val autoShift = leftClickMode == LEFT_CLICK_SHIFT
		if (!player.isCrouching && !autoShift) {
			return false
		}

		if (clickCount <= 0) {
			return player.isCrouching || ticksLeft > 0 || autoShift
		}

		if (!player.isCrouching) {
			if (ticksLeft == 0) {
				setShift(client, true)
				ticksLeft = shiftDelays.random()
			}
			return true
		}

		fakeRightClick(client)
		return true
	}

	private fun tickShift(client: Minecraft) {
		if (!FixifyFeatures.etherwarpEnabled || FixifyFeatures.etherwarpLeftClickMode != LEFT_CLICK_SHIFT) {
			ticksLeft = 0
			if (shiftHeld) {
				setShift(client, false)
			}
			return
		}

		if (ticksLeft <= 0) {
			return
		}

		ticksLeft--
		when (ticksLeft) {
			1 -> if (client.screen == null) {
				fakeRightClick(client)
			}

			0 -> setShift(client, false)
		}
	}

	private fun fakeRightClick(client: Minecraft) {
		val key = (client.options.keyUse as KeyMappingAccessor).fixifyBoundKey()
		KeyMapping.set(key, true)
		KeyMapping.click(key)
		KeyMapping.set(key, false)
		client.player?.swing(InteractionHand.MAIN_HAND)
	}

	private fun setShift(client: Minecraft, down: Boolean) {
		val key = (client.options.keyShift as KeyMappingAccessor).fixifyBoundKey()
		KeyMapping.set(key, down)
		shiftHeld = down
	}

	private fun render(context: LevelRenderContext) {
		if (!FixifyFeatures.etherwarpEnabled || !FixifyFeatures.etherwarpShowGuess) {
			return
		}

		val client = Minecraft.getInstance()
		if (client.screen != null) {
			return
		}

		val player = client.player ?: return
		val level = client.level ?: return
		val etherData = player.mainHandItem.etherwarpData() ?: return
		if (!player.isShiftKeyDown && etherData.itemId() != "ETHERWARP_CONDUIT") {
			return
		}

		val startPosition = if (FixifyFeatures.etherwarpUseServerPosition) player.oldPosition() else player.position()
		val tunedDistance = 57.0 + etherData.getIntOr("tuned_transmission", 0)
		val etherPos = getEtherPos(level, startPosition, player.getLookAngle(), player.isCrouching, tunedDistance)
		if (!etherPos.succeeded && !FixifyFeatures.etherwarpShowFailed) {
			return
		}

		val pos = etherPos.pos ?: return
		val state = etherPos.state ?: level.getBlockState(pos)
		val box = if (FixifyFeatures.etherwarpFullBlock) {
			AABB(pos)
		} else {
			state.getShape(level, pos).singleEncompassing()
				.takeIf { !it.isEmpty }
				?.bounds()
				?.move(pos) ?: AABB(pos)
		}

		val color = if (etherPos.succeeded) FixifyFeatures.etherwarpColor.argb else FixifyFeatures.etherwarpFailColor.argb
		val camera = client.gameRenderer.getMainCamera().position()
		val poseStack = context.poseStack()
		poseStack.pushPose()
		poseStack.translate(-camera.x, -camera.y, -camera.z)
		FixifyWorldBoxes.draw(
			poseStack,
			context.bufferSource(),
			box,
			color,
			FixifyFeatures.etherwarpRenderStyle,
			FixifyFeatures.etherwarpDepth,
		)
		poseStack.popPose()
	}

	private fun getEtherPos(
		level: Level,
		position: Vec3,
		look: Vec3,
		crouching: Boolean,
		distance: Double,
	): EtherPos {
		val eyeHeight = if (crouching) 1.54 else 1.62
		val start = position.add(0.0, eyeHeight, 0.0)
		val end = start.add(look.scale(distance))
		return traverseVoxels(level, start, end)
	}

	private fun traverseVoxels(level: Level, start: Vec3, end: Vec3): EtherPos {
		var x = floor(start.x).toInt()
		var y = floor(start.y).toInt()
		var z = floor(start.z).toInt()
		val endX = floor(end.x).toInt()
		val endY = floor(end.y).toInt()
		val endZ = floor(end.z).toInt()

		val dirX = end.x - start.x
		val dirY = end.y - start.y
		val dirZ = end.z - start.z
		val stepX = sign(dirX).toInt()
		val stepY = sign(dirY).toInt()
		val stepZ = sign(dirZ).toInt()
		val invDirX = if (dirX != 0.0) 1.0 / dirX else Double.MAX_VALUE
		val invDirY = if (dirY != 0.0) 1.0 / dirY else Double.MAX_VALUE
		val invDirZ = if (dirZ != 0.0) 1.0 / dirZ else Double.MAX_VALUE
		val tDeltaX = abs(invDirX * stepX)
		val tDeltaY = abs(invDirY * stepY)
		val tDeltaZ = abs(invDirZ * stepZ)
		var tMaxX = abs((x + max(stepX, 0) - start.x) * invDirX)
		var tMaxY = abs((y + max(stepY, 0) - start.y) * invDirY)
		var tMaxZ = abs((z + max(stepZ, 0) - start.z) * invDirZ)

		repeat(1000) {
			val pos = BlockPos(x, y, z)
			val state = level.getBlockState(pos)
			if (!isEtherwarpPassable(state)) {
				if (!isEtherwarpPassable(level.getBlockState(pos.offset(0, 1, 0)))) {
					return EtherPos(false, pos, state)
				}
				if (!isEtherwarpPassable(level.getBlockState(pos.offset(0, 2, 0)))) {
					return EtherPos(false, pos, state)
				}
				return EtherPos(true, pos, state)
			}

			if (x == endX && y == endY && z == endZ) {
				return EtherPos.NONE
			}

			when {
				tMaxX <= tMaxY && tMaxX <= tMaxZ -> {
					tMaxX += tDeltaX
					x += stepX
				}

				tMaxY <= tMaxZ -> {
					tMaxY += tDeltaY
					y += stepY
				}

				else -> {
					tMaxZ += tDeltaZ
					z += stepZ
				}
			}
		}

		return EtherPos.NONE
	}

	private fun isEtherwarpPassable(state: BlockState): Boolean {
		val block = state.block
		return state.isAir || validFeetBlocks.any { it.isInstance(block) }
	}

	private fun ItemStack.etherwarpData(): CompoundTag? {
		if (isEmpty) {
			return null
		}

		val data = getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
		val itemId = data.itemId()
		val name = hoverName.string.uppercase()
		return data.takeIf {
			it.getBooleanOr("ethermerge", false) ||
				it.getIntOr("ethermerge", 0) == 1 ||
				itemId == "ETHERWARP_CONDUIT" ||
				name.contains("ETHERWARP")
		}
	}

	private fun CompoundTag.itemId(): String = getStringOr("id", "")

	private data class EtherPos(
		val succeeded: Boolean,
		val pos: BlockPos?,
		val state: BlockState?,
	) {
		companion object {
			val NONE = EtherPos(false, null, null)
		}
	}

	private const val LEFT_CLICK_NONE = 0
	private const val LEFT_CLICK_SHIFT = 2
}
