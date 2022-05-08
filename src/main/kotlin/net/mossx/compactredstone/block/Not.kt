package net.mossx.compactredstone.block

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.DustParticleEffect
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties.AXIS
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.mossx.compactredstone.CompactRedstone
import net.mossx.compactredstone.block.redstonecomponent.circuit.Companion.find_connection
import net.mossx.compactredstone.block.redstonecomponent.not_component
import net.mossx.compactredstone.block.redstonecomponent.nothing
import net.mossx.compactredstone.block.redstonecomponent.redstonecomponent
import java.util.*

class Not : AbstractRedstoneBlock(Settings.of(Material.STONE).solidBlock { _, _, _ -> false }) {

    init {
        defaultState = stateManager.defaultState.with(FACING, Direction.NORTH)
            .with(POWERED, false)
    }

    override fun isValidInput(state: BlockState): Boolean {
        return isRedstoneGate(state)
    }

    override fun getWeakRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return if (!state.get(POWERED)) {
            if (state.get(FACING) == direction) 15 else 0
        } else {
            0
        }
    }

    override fun getStrongRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
       return if (!state.get(POWERED)) {
            if (state.get(FACING) == direction) 15 else 0
        } else {
            0
        }
    }

    override fun convert_to_logic_component(world: World, pos: BlockPos, state: BlockState): redstonecomponent {
        return not_component(listOf(find_connection(world, pos.offset(state.get(FACING)), state.get(FACING))))
    }
}