package net.mossx.compactredstone.block

import net.minecraft.block.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.mossx.compactredstone.block.redstonecomponent.and_component
import net.mossx.compactredstone.block.redstonecomponent.circuit.Companion.find_connection

class And : AbstractRedstoneBlock(Settings.of(Material.STONE).solidBlock { _, _, _ -> false }) {

    init {
        defaultState = stateManager.defaultState.with(FACING, Direction.NORTH)
            .with(POWERED, false)
    }

    override fun isValidInput(state: BlockState): Boolean {
        return isRedstoneGate(state)
    }

    override fun convert_to_logic_component(world: World, pos: BlockPos, state: BlockState): and_component {
        val dir = state.get(FACING)
        val left_dir = dir.rotateYClockwise()
        val right_dir = dir.rotateYCounterclockwise()

        return and_component(listOf(find_connection(world, pos.offset(left_dir), left_dir), find_connection(world, pos.offset(right_dir), right_dir)))
    }

    override fun getPower(world: World, pos: BlockPos, state: BlockState): Int {
        return Math.min(getEastPower(world, pos, state), getWestPower(world, pos, state))
    }

    override fun getWeakRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return if (state.get(POWERED)) {
            if (state.get(FACING) == direction) 15 else 0
        } else {
            0
        }
    }

    override fun getStrongRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
       return if (state.get(POWERED)) {
            if (state.get(FACING) == direction) 15 else 0
        } else {
            0
        }
    }
}