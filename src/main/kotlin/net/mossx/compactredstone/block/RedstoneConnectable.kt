package net.mossx.compactredstone.block

import net.minecraft.block.BlockState
import net.minecraft.util.math.Direction

interface RedstoneConnectable {

    /**
     * Return whether a redstone wire should connect to this block based on direction and state
     */
    fun connectsTo(state: BlockState, direction: Direction): Boolean

}