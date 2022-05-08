package net.mossx.compactredstone.block

import net.minecraft.block.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.mossx.compactredstone.block.redstonecomponent.*

class Or : AbstractRedstoneBlock(Settings.of(Material.STONE).solidBlock { _, _, _ -> false }) {

    init {
        defaultState = stateManager.defaultState.with(FACING, Direction.NORTH)
            .with(POWERED, false)
    }

    override fun isValidInput(state: BlockState): Boolean {
        return isRedstoneGate(state)
    }

    override fun getPower(world: World, pos: BlockPos, state: BlockState): Int {
        return Math.max(getEastPower(world, pos, state), getWestPower(world, pos, state))
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

    override fun convert_to_logic_component(world: World, pos: BlockPos, state: BlockState): redstonecomponent {
        val dir = state.get(FACING)
        val left_dir = dir.rotateYClockwise()
        val right_dir = dir.rotateYCounterclockwise()

        return or_component(listOf(circuit.find_connection(world, pos.offset(left_dir), left_dir), circuit.find_connection(world, pos.offset(right_dir), right_dir)))
    }
}