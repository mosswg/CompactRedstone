package net.mossx.compactredstone.block

import net.minecraft.block.*
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.mossx.compactredstone.CompactRedstone
import net.mossx.compactredstone.block.redstonecomponent.circuit
import net.mossx.compactredstone.block.redstonecomponent.component_side
import net.mossx.compactredstone.block.redstonecomponent.input_component
import net.mossx.compactredstone.block.redstonecomponent.redstonecomponent

open class CompactInput : AbstractRedstoneBlock(Settings.of(Material.STONE).solidBlock { _, _, _ -> false }) {

    init {
        defaultState = stateManager.defaultState.with(FACING, Direction.NORTH)
            .with(POWERED, false).with(POWER, 0).with(circuit.BLOCK_SIDE, component_side.back)
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(FACING, POWERED, POWER, circuit.BLOCK_SIDE)
    }

    override fun isValidInput(state: BlockState): Boolean {
        return isRedstoneGate(state)
    }

    override fun getPower(world: World, pos: BlockPos, state: BlockState): Int {
        return state.get(POWER)
    }

    override fun getWeakRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return state.get(POWER)
    }

    override fun getStrongRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return state.get(POWER)
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        super.onPlaced(world, pos, state, placer, itemStack)
        all_input_pos.add(pos)
    }

    companion object {
        var POWER: IntProperty? = null
        var all_input_pos = ArrayList<BlockPos>(0)

        init {
            POWER = Properties.POWER
        }
    }

    override fun convert_to_logic_component(world: World, pos: BlockPos, state: BlockState): redstonecomponent {
        CompactRedstone.LOGGER.info("Creating input with side: ${state.get(circuit.BLOCK_SIDE)}")
        return input_component(state.get(circuit.BLOCK_SIDE))
    }
}
