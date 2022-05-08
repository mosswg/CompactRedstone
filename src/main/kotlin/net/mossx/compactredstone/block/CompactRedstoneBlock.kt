package net.mossx.compactredstone.block

import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtInt
import net.minecraft.text.LiteralText
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.mossx.compactredstone.CompactRedstone
import net.mossx.compactredstone.block.redstonecomponent.*

class CompactRedstoneBlock: AbstractRedstoneBlock(Settings.of(Material.STONE).solidBlock { _, _, _ -> false }) {
    var logic: circuit = circuit()

    val side_values: IntArray = IntArray(4)

    override fun getPower(world: World, pos: BlockPos, state: BlockState): Int {
        logic.execute(arrayOf(
            getSouthPower(world, pos, state),
            getEastPower(world, pos, state),
            getNorthPower(world, pos, state),
            getWestPower(world, pos, state)), this)

        return side_values[component_side.front.index]
    }


    override fun getStrongRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return state.getWeakRedstonePower(world, pos, direction)
    }

    fun direction_to_side(state: BlockState, direction: Direction): component_side {

        val modified_dir = direction_difference(direction, state.get(FACING))

        return when (modified_dir) {
            Direction.NORTH -> {
                component_side.front
            }
            Direction.EAST -> {
                component_side.right
            }
            Direction.SOUTH -> {
                component_side.back
            }
            Direction.WEST -> {
                component_side.left
            }
            else -> component_side.none
        }
    }

    fun direction_difference(direction: Direction, other: Direction): Direction {
        // Pls tell me there is a better way to do this
        return when (direction) {
            Direction.NORTH -> {
                other
            }
            Direction.SOUTH -> {
                other.opposite
            }
            Direction.WEST -> {
                other.rotateYClockwise()
            }
            Direction.EAST -> {
                other.rotateYCounterclockwise()
            }
            else -> other
        }
    }

    override fun getWeakRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return side_values[direction_to_side(state, direction).index]
    }

    override fun convert_to_logic_component(world: World, pos: BlockPos, state: BlockState): redstonecomponent {
        // TODO: Make this work for input and output sides
        return compact_redstone_component(this.logic, listOf(
            circuit.find_connection(
                world,
                pos.north(),
                Direction.NORTH
            )
        ))
    }

    fun set_power(value: Int, side: component_side) {
        side_values[side.index] = value
    }

    fun set_circuit(circuit: circuit) {
        this.logic = circuit
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        super.onPlaced(world, pos, state, placer, itemStack)

        val subnbt = itemStack.getOrCreateNbt().get("circuit")
        if (subnbt != null && subnbt is NbtInt) {
            this.logic = circuits[subnbt.intValue()]
        }
    }

    companion object {
        var circuits: ArrayList<circuit> = ArrayList()

        fun create(circuit: circuit, name: String): ItemStack {
            val block = CompactRedstone.COMPACTREDSTONEBLOCK
            circuits.add(circuit)
            val item = ItemStack(block)
            item.setCustomName(LiteralText(name))
            item.setSubNbt("circuit", NbtInt.of(circuits.size-1))
            return item
        }

    }
}