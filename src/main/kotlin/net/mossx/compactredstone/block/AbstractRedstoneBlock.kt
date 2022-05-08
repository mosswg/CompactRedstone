//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package net.mossx.compactredstone.block

import net.minecraft.block.*
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.TickPriority
import net.minecraft.world.World
import net.minecraft.world.WorldView
import net.mossx.compactredstone.block.redstonecomponent.redstonecomponent
import java.util.*

abstract class AbstractRedstoneBlock protected constructor(settings: Settings?) :
    HorizontalFacingBlock(settings) {
    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        return SHAPE
    }

    var delay = 0

    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        return true
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(FACING, POWERED)
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        if (!isLocked(world, pos, state)) {
            val bl = state.get(POWERED) as Boolean
            val bl2 = hasPower(world, pos, state)
            if (bl && !bl2) {
                world.setBlockState(pos, state.with(POWERED, false) as BlockState, 2)
            } else if (!bl) {
                world.setBlockState(pos, state.with(POWERED, true) as BlockState, 2)
                if (!bl2) {
                    world.createAndScheduleBlockTick(pos, this, getUpdateDelayInternal(), TickPriority.VERY_HIGH)
                }
            }
        }
    }

    override fun getStrongRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return state.getWeakRedstonePower(world, pos, direction)
    }

    override fun getWeakRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return if (!state.get(POWERED)) {
            0
        } else {
            if (state.get(FACING) == direction) getOutputLevel(world, pos, state) else 0
        }
    }

    override fun neighborUpdate(
        state: BlockState,
        world: World,
        pos: BlockPos,
        block: Block,
        fromPos: BlockPos,
        notify: Boolean
    ) {
        if (state.canPlaceAt(world, pos)) {
            updatePowered(world, pos, state)
        } else {
            val blockEntity = if (state.hasBlockEntity()) world.getBlockEntity(pos) else null
            dropStacks(state, world, pos, blockEntity)
            world.removeBlock(pos, false)
            val var8 = Direction.values()
            val var9 = var8.size
            for (var10 in 0 until var9) {
                val direction = var8[var10]
                world.updateNeighborsAlways(pos.offset(direction), this)
            }
        }
    }

    protected open fun updatePowered(world: World, pos: BlockPos, state: BlockState) {
        if (!isLocked(world, pos, state)) {
            val bl = state.get(POWERED) as Boolean
            val bl2 = hasPower(world, pos, state)
            if (bl != bl2 && !world.blockTickScheduler.isTicking(pos, this)) {
                var tickPriority = TickPriority.HIGH
                if (isTargetNotAligned(world, pos, state)) {
                    tickPriority = TickPriority.EXTREMELY_HIGH
                } else if (bl) {
                    tickPriority = TickPriority.VERY_HIGH
                }
                world.createAndScheduleBlockTick(pos, this, getUpdateDelayInternal(), tickPriority)
            }
        }
    }

    fun getUpdateDelayInternal(): Int {
        return delay
    }

    open fun isLocked(world: WorldView?, pos: BlockPos?, state: BlockState?): Boolean {
        return false
    }

    protected open fun hasPower(world: World, pos: BlockPos, state: BlockState): Boolean {
        return getPower(world, pos, state) > 0
    }

    protected open fun getNorthPower(world: World, pos: BlockPos, state: BlockState): Int {
        val direction = state.get(FACING) as Direction
        val blockPos = pos.offset(direction)
        val i = world.getEmittedRedstonePower(blockPos, direction)
        return if (i >= 15) {
            i
        } else {
            val blockState = world.getBlockState(blockPos)
            Math.max(
                i,
                (if (blockState.isOf(Blocks.REDSTONE_WIRE)) blockState.get(RedstoneWireBlock.POWER) as Int else 0)
            )
        }
    }

    protected open fun getEastPower(world: World, pos: BlockPos, state: BlockState): Int {
        val direction = state.get(FACING).rotateYClockwise() as Direction
        val blockPos = pos.offset(direction)
        val i = world.getEmittedRedstonePower(blockPos, direction)
        return if (i >= 15) {
            i
        } else {
            val blockState = world.getBlockState(blockPos)
            Math.max(
                i,
                (if (blockState.isOf(Blocks.REDSTONE_WIRE)) blockState.get(RedstoneWireBlock.POWER) as Int else 0)
            )
        }
    }

    protected open fun getSouthPower(world: World, pos: BlockPos, state: BlockState): Int {
        val direction = state.get(FACING).rotateYClockwise().rotateYClockwise() as Direction
        val blockPos = pos.offset(direction)
        val i = world.getEmittedRedstonePower(blockPos, direction)
        return if (i >= 15) {
            i
        } else {
            val blockState = world.getBlockState(blockPos)
            Math.max(
                i,
                (if (blockState.isOf(Blocks.REDSTONE_WIRE)) blockState.get(RedstoneWireBlock.POWER) as Int else 0)
            )
        }
    }

    protected open fun getWestPower(world: World, pos: BlockPos, state: BlockState): Int {
        val direction = state.get(FACING).rotateYCounterclockwise() as Direction
        val blockPos = pos.offset(direction)
        val i = world.getEmittedRedstonePower(blockPos, direction)
        return if (i >= 15) {
            i
        } else {
            val blockState = world.getBlockState(blockPos)
            Math.max(
                i,
                (if (blockState.isOf(Blocks.REDSTONE_WIRE)) blockState.get(RedstoneWireBlock.POWER) as Int else 0)
            )
        }
    }

    protected open fun getPower(world: World, pos: BlockPos, state: BlockState): Int {
        return getNorthPower(world, pos, state)
    }

    protected fun getMaxInputLevelSides(world: WorldView, pos: BlockPos, state: BlockState): Int {
        val direction = state.get(FACING) as Direction
        val direction2 = direction.rotateYClockwise()
        val direction3 = direction.rotateYCounterclockwise()
        return Math.max(
            getInputLevel(world, pos.offset(direction2), direction2),
            getInputLevel(world, pos.offset(direction3), direction3)
        )
    }

    protected fun getInputLevel(world: WorldView, pos: BlockPos?, dir: Direction?): Int {
        val blockState = world.getBlockState(pos)
        return if (isValidInput(blockState)) {
            if (blockState.isOf(Blocks.REDSTONE_BLOCK)) {
                15
            } else {
                if (blockState.isOf(Blocks.REDSTONE_WIRE)) blockState.get(RedstoneWireBlock.POWER) as Int else world.getStrongRedstonePower(
                    pos,
                    dir
                )
            }
        } else {
            0
        }
    }

    override fun emitsRedstonePower(state: BlockState): Boolean {
        return true
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(FACING, ctx.playerFacing.opposite) as BlockState
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        if (hasPower(world, pos, state)) {
            world.createAndScheduleBlockTick(pos, this, 1)
        }
    }

    override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, notify: Boolean) {
        updateTarget(world, pos, state)
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!moved && !state.isOf(newState.block)) {
            super.onStateReplaced(state, world, pos, newState, moved)
            updateTarget(world, pos, state)
        }
    }

    protected fun updateTarget(world: World, pos: BlockPos, state: BlockState) {
        val direction = state.get(FACING) as Direction
        val blockPos = pos.offset(direction.opposite)
        world.updateNeighbor(blockPos, this, pos)
        world.updateNeighborsExcept(blockPos, this, direction)
    }

    protected open fun isValidInput(state: BlockState): Boolean {
        return state.emitsRedstonePower()
    }

    protected open fun getOutputLevel(world: BlockView?, pos: BlockPos?, state: BlockState?): Int {
        return 15
    }

    fun isTargetNotAligned(world: BlockView, pos: BlockPos, state: BlockState): Boolean {
        val direction = (state.get(FACING) as Direction).opposite
        val blockState = world.getBlockState(pos.offset(direction))
        return isRedstoneGate(blockState) && blockState.get(FACING) != direction
    }

    fun connectsTo(state: BlockState, direction: Direction): Boolean {
        return state.get(FACING).axis === direction.axis
    }

    abstract fun convert_to_logic_component(world: World, pos: BlockPos, state: BlockState): redstonecomponent

    companion object {
        protected val SHAPE = createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
        var POWERED: BooleanProperty? = null
        fun isRedstoneGate(state: BlockState): Boolean {
            return state.block is AbstractRedstoneGateBlock || state.block is AbstractRedstoneBlock
        }

        init {
            POWERED = Properties.POWERED
        }
    }
}