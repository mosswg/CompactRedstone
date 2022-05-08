package net.mossx.compactredstone.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.mossx.compactredstone.block.redstonecomponent.circuit
import net.mossx.compactredstone.block.redstonecomponent.component_side
import net.mossx.compactredstone.block.redstonecomponent.nothing
import net.mossx.compactredstone.block.redstonecomponent.redstonecomponent

open class CompactOutput : AbstractRedstoneBlock(Settings.of(Material.STONE).solidBlock { _, _, _ -> false }) {

    init {
        defaultState = stateManager.defaultState.with(FACING, Direction.NORTH)
            .with(POWERED, false).with(circuit.BLOCK_SIDE, component_side.front)
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(FACING, POWERED, circuit.BLOCK_SIDE)
    }

    override fun isValidInput(state: BlockState): Boolean {
        return isRedstoneGate(state)
    }

    // This shouldn't ever be called
    override fun convert_to_logic_component(world: World, pos: BlockPos, state: BlockState): redstonecomponent {
        return nothing()
    }

    override fun getPower(world: World, pos: BlockPos, state: BlockState): Int {
        return 0
    }

    override fun getWeakRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return 0
    }

    override fun onBreak(world: World?, pos: BlockPos?, state: BlockState?, player: PlayerEntity?) {
        super.onBreak(world, pos, state, player)
    }

    override fun getStrongRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return 0
    }
}
