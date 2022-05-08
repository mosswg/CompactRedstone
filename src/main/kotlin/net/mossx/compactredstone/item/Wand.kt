package net.mossx.compactredstone.item

import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.LiteralText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.mossx.compactredstone.CompactRedstone
import net.mossx.compactredstone.block.CompactInput.Companion.POWER
import net.mossx.compactredstone.block.redstonecomponent.circuit.Companion.BLOCK_SIDE

class Wand(settings: Settings) : Item(settings) {

    override fun canMine(state: BlockState, world: World, pos: BlockPos, miner: PlayerEntity): Boolean {
        if (!world.isClient) {
            if (state.isOf(CompactRedstone.COMPACTINPUT)) {
                var power = state.get(POWER)
                if (power == 15) {
                    power = -1
                }
                power++
                miner.sendMessage(LiteralText("Set Input to ${power}"), true)
                world.setBlockState(pos, state.with(POWER, power))
            }
        }
        return false
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        val pos = context.blockPos
        val state = world.getBlockState(pos)
        if (state.isOf(CompactRedstone.COMPACTINPUT)) {
            val rotated_side = state.get(BLOCK_SIDE).rotate()
            context.player?.sendMessage(LiteralText("Rotating Input to $rotated_side"), true)
            world.setBlockState(pos, state.with(BLOCK_SIDE, rotated_side))
        }
        else if (state.isOf(CompactRedstone.COMPACTOUTPUT)) {
            val rotated_side = state.get(BLOCK_SIDE).rotate()
            context.player?.sendMessage(LiteralText("Rotating Output to $rotated_side"), true)
            world.setBlockState(pos, state.with(BLOCK_SIDE, rotated_side))
        }
        return ActionResult.SUCCESS
    }
}