package net.mossx.compactredstone.block.redstonecomponent

import net.minecraft.block.Blocks
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.mossx.compactredstone.CompactRedstone
import net.mossx.compactredstone.block.AbstractRedstoneBlock
import net.mossx.compactredstone.block.CompactRedstoneBlock
import net.mossx.compactredstone.redstoneworld.RedstoneWorldChunkGenerator
import net.mossx.compactredstone.util.ComponentSideProperty

class circuit(var logic: List<output_component>) {

    constructor() : this(listOf())



    fun set_input_powers(side_powers: Array<Int>, component: redstonecomponent) {
        for (connection in component.connected_components) {
            if (connection is input_component) {
                CompactRedstone.LOGGER.info("setting input: $connection: ${side_powers[connection.block_side.index]}")
                connection.value = side_powers[connection.block_side.index]
            }
            else {
                set_input_powers(side_powers, connection)
            }
        }
    }

    fun execute(side_powers: Array<Int>, block: CompactRedstoneBlock) {
        for (output in logic) {
            set_input_powers(side_powers, output)
        }

        for (output in logic) {
            for (comp in output.connected_components) {
                val power = comp.get_value()
                CompactRedstone.LOGGER.info("setting output to ${output.block_side}: $power")
                output.set_value(power, block)
            }
        }
    }

    fun execute(side_powers: Array<Int>): Int {
        for (output in logic) {
            set_input_powers(side_powers, output)
        }

        for (output in logic) {
            for (comp in output.connected_components) {
                val power = comp.get_value()
                CompactRedstone.LOGGER.info("setting output to ${output.block_side}: $power")
                return power
            }
        }
        return 0
    }

    override fun toString(): String {
        var out = ""
        for (output in logic) {
            for (connection in output.connected_components) {
            }
            out += connection_string(output) + "\n"
        }
        return out
    }

    companion object {
        var print_tab_depth = ""

        val found_components = ArrayList<BlockPos>()

        val BLOCK_SIDE = ComponentSideProperty.of("block_side")

        fun connection_string(component: redstonecomponent): String {
            var out = ""
            print_tab_depth += "\t"
            for (connection in component.connected_components) {
                if (connection is input_component) {
                    out += "$component\n$print_tab_depth-> input"
                    continue
                }
                out += component.toString() + "\n" + print_tab_depth + "-> " + connection_string(connection)
            }
            // remove tab once done
            print_tab_depth.substring(0, print_tab_depth.length - 1)
            return out
        }

        fun find_ouputs(world: World): ArrayList<BlockPos> {
            if (world is ServerWorld) {
                val generator = world.chunkManager.chunkGenerator
                if (generator is RedstoneWorldChunkGenerator) {
                    val mutable = BlockPos.Mutable()
                    val outputs = ArrayList<BlockPos>()
                    for (x in -generator.sizex/2 .. generator.sizex/2) {
                        for (z in -generator.sizez/2 .. generator.sizez/2) {
                            for (y in 0.. generator.worldHeight) {
                                if (world.getBlockState(mutable.set(x, y, z)).isOf(CompactRedstone.COMPACTOUTPUT)) {
                                    CompactRedstone.LOGGER.info("output found")
                                    outputs.add(BlockPos(x, y, z))
                                }
                            }
                        }
                    }
                    return outputs
                }
            }
            return ArrayList()
        }

        fun create(world: World): circuit {
            val output_components: ArrayList<output_component> = ArrayList()
            val outputs = find_ouputs(world)
            for (output_pos in outputs) {
                val output_connections = ArrayList<redstonecomponent>()
                CompactRedstone.LOGGER.info("front")
                found_components.clear()
                val front_connection = find_connection(world, output_pos.north(), Direction.NORTH)
                CompactRedstone.LOGGER.info("right")
                found_components.clear()
                val right_connection = find_connection(world, output_pos.east(), Direction.EAST)
                CompactRedstone.LOGGER.info("back")
                found_components.clear()
                val back_connection = find_connection(world, output_pos.south(), Direction.SOUTH)
                CompactRedstone.LOGGER.info("left")
                found_components.clear()
                val left_connection = find_connection(world, output_pos.west(), Direction.WEST)

                if (front_connection !is nothing) {
                    output_connections.add(front_connection)
                }
                if (right_connection !is nothing) {
                    output_connections.add(right_connection)
                }
                if (back_connection !is nothing) {
                    output_connections.add(back_connection)
                }
                if (left_connection !is nothing) {
                    output_connections.add(left_connection)
                }


                if (!output_connections.isEmpty()) {
                    output_components.add(
                        output_component(
                            listOf(or_component(output_connections)),
                            world.getBlockState(output_pos).get(BLOCK_SIDE)
                        )
                    )
                }
            }
            return circuit(output_components)
        }

        fun find_connection(world: World, pos: BlockPos, direction: Direction): redstonecomponent {
            val state = world.getBlockState(pos)
            val block = state.block

            CompactRedstone.LOGGER.info("Block: $block")

            if (found_components.contains(pos)) {
                CompactRedstone.LOGGER.info("Component Already found")
                return nothing()
            }

            // TODO: Add repeater support
            if (state.isOf(Blocks.REDSTONE_WIRE)) {
                val next_state = world.getBlockState(pos.offset(direction))

                val right_dir = direction.rotateYClockwise()
                val right_state = world.getBlockState(pos.offset(right_dir))
                val left_dir = direction.rotateYCounterclockwise()
                val left_state = world.getBlockState(pos.offset(left_dir))

                val right_exists = !right_state.isOf(Blocks.AIR)
                val left_exists = !left_state.isOf(Blocks.AIR)
                val next_exists = !next_state.isOf(Blocks.AIR)

                // TODO: Make these into lists and make a loop to find the ones that exist

                if (!right_exists && !left_exists && !next_exists) {
                    return nothing()
                }

                found_components.add(pos)

                if (right_exists && left_exists && next_exists) {
                    CompactRedstone.LOGGER.info("rln")
                    return or_component(listOf(
                        find_connection(world, pos.offset(right_dir), right_dir),
                        find_connection(world, pos.offset(left_dir), left_dir),
                        find_connection(world, pos.offset(direction), direction)))
                }

                if (left_exists && next_exists) {
                    CompactRedstone.LOGGER.info("ln")
                    return or_component(listOf(
                        find_connection(world, pos.offset(left_dir), left_dir),
                        find_connection(world, pos.offset(direction), direction)))
                }

                if (right_exists && next_exists) {
                    CompactRedstone.LOGGER.info("rn")
                    return or_component(listOf(
                        find_connection(world, pos.offset(right_dir), right_dir),
                        find_connection(world, pos.offset(direction), direction)))
                }

                if (right_exists && left_exists) {
                    CompactRedstone.LOGGER.info("rl")
                    return or_component(listOf(
                        find_connection(world, pos.offset(right_dir), right_dir),
                        find_connection(world, pos.offset(left_dir), left_dir)))
                }

                if (right_exists) {
                    CompactRedstone.LOGGER.info("r")
                    CompactRedstone.LOGGER.info(right_state.block.toString())
                    return find_connection(world, pos.offset(right_dir), right_dir)
                }

                if (left_exists) {
                    CompactRedstone.LOGGER.info("l")
                    return find_connection(world, pos.offset(left_dir), left_dir)
                }

                if (next_exists) {
                    CompactRedstone.LOGGER.info("n")
                    return find_connection(world, pos.offset(direction), direction)
                }
            }
            else if (block is AbstractRedstoneBlock) {
                found_components.add(pos)
                CompactRedstone.LOGGER.info("fc: $found_components")
                return block.convert_to_logic_component(world, pos, state)
            }
            else if (state.isOf(Blocks.AIR)) {
                return nothing()
            }
            else {
                TODO("TODO: Redstone component")
            }
            return nothing()
        }
    }
}