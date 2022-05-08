package net.mossx.compactredstone.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions
import net.minecraft.block.Block
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.minecraft.world.RaycastContext.FluidHandling
import net.minecraft.world.TeleportTarget
import net.minecraft.world.World
import net.mossx.compactredstone.CompactRedstone
import net.mossx.compactredstone.block.CompactInput
import net.mossx.compactredstone.block.CompactInput.Companion.POWER
import net.mossx.compactredstone.block.CompactOutput
import net.mossx.compactredstone.block.CompactRedstoneBlock
import net.mossx.compactredstone.block.redstonecomponent.circuit
import net.mossx.compactredstone.block.redstonecomponent.component_side
import net.mossx.compactredstone.redstoneworld.RedstoneWorldChunkGenerator
import kotlin.math.max
import kotlin.math.min


object CompactRedstoneCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>, dedicated: Boolean) {
        dispatcher.register(CommandManager.literal("compactredstone")
                .executes { context -> run(context) })

        dispatcher.register(CommandManager.literal("compactredstone").then(CommandManager.literal("setinput")
            .then(CommandManager.argument("value", IntegerArgumentType.integer())
                .executes { context -> set_input(context, context.getArgument("value", Int::class.java))
            })))

        dispatcher.register(CommandManager.literal("compactredstone")
            .then(CommandManager.literal("create").executes { context -> run_create(context) }))

        dispatcher.register(CommandManager.literal("compactredstone")
            .then(CommandManager.literal("wand").executes { context -> run_wand(context) }))


        dispatcher.register(CommandManager.literal("compactredstone").then(CommandManager.literal("setside")
            .then(CommandManager.literal("front")
                .executes { context -> set_side(context, component_side.front)
                })))

        dispatcher.register(CommandManager.literal("compactredstone").then(CommandManager.literal("setside")
            .then(CommandManager.literal("right")
                .executes { context -> set_side(context, component_side.right)
                })))

        dispatcher.register(CommandManager.literal("compactredstone").then(CommandManager.literal("setside")
            .then(CommandManager.literal("left")
                .executes { context -> set_side(context, component_side.left)
                })))

        dispatcher.register(CommandManager.literal("compactredstone").then(CommandManager.literal("setside")
            .then(CommandManager.literal("back")
                .executes { context -> set_side(context, component_side.back)
                })))

        dispatcher.register(CommandManager.literal("compactredstone")
            .then(CommandManager.literal("save").then(CommandManager.argument("name", StringArgumentType.string())
                .executes {context -> run_save(context, context.getArgument("name", String::class.java)) })))



        dispatcher.register(CommandManager.literal("compactredstone")
            .then(CommandManager.literal("create").then(CommandManager.argument("width", IntegerArgumentType.integer())
            .then(CommandManager.argument("length", IntegerArgumentType.integer())
            .then(CommandManager.argument("height", IntegerArgumentType.integer()).executes { context -> run_create(context, context.getArgument("width", Int::class.java),
                    context.getArgument("length", Int::class.java), context.getArgument("height", Int::class.java)) })))))
    }

    fun run_wand(context: CommandContext<ServerCommandSource>): Int {

        return 0
    }

    fun raycast(world: World, player: PlayerEntity, fluidHandling: FluidHandling?): BlockHitResult? {
        // I think this converts from pitch and yaw to quaternions but idk i ripped it from Item.java
        val pitch = player.pitch
        val yaw = player.yaw
        val vec3d = player.eyePos
        val h = MathHelper.cos(-yaw * (Math.PI.toFloat() / 180) - Math.PI.toFloat())
        val i = MathHelper.sin(-yaw * (Math.PI.toFloat() / 180) - Math.PI.toFloat())
        val j = -MathHelper.cos(-pitch * (Math.PI.toFloat() / 180))
        val k = MathHelper.sin(-pitch * (Math.PI.toFloat() / 180))
        val l = i * j
        val n = h * j
        val d = 5.0
        val vec3d2 = vec3d.add(l.toDouble() * d, k.toDouble() * d, n.toDouble() * d)
        return world.raycast(RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.OUTLINE, fluidHandling, player))
    }

    private fun set_side(context: CommandContext<ServerCommandSource>, side: component_side): Int {
        val blockhit = raycast(context.source.world, context.source.player, FluidHandling.ANY)
        if (blockhit == null) {
            CompactRedstone.LOGGER.info("No block found")
            context.source.player.sendMessage(LiteralText("You must be looking at an input or output block to run this function"), false)
            return -1
        }
        val block_state = context.source.world.getBlockState(blockhit.blockPos)
        val block = block_state.block
        if (block_state.isOf(CompactRedstone.COMPACTOUTPUT) || block_state.isOf(CompactRedstone.COMPACTINPUT)) {
            CompactRedstone.LOGGER.info("Set block $block at ${blockhit.blockPos} to $side")
            context.source.world.setBlockState(blockhit.blockPos, block_state.with(circuit.BLOCK_SIDE, side))
            return 0
        }
        CompactRedstone.LOGGER.info("found block: $block")
        context.source.player.sendMessage(LiteralText("You must be looking at an input or output block to run this function"), false)
        return -1
    }

    @Throws(CommandSyntaxException::class)
    fun run(context: CommandContext<ServerCommandSource>): Int {
        context.source.player.sendMessage(LiteralText("Usage: /compactredstone"), false)
        context.source.player.sendMessage(LiteralText("create: "), false)
        context.source.player.sendMessage(LiteralText("create: <width> <length> <height>"), false)
        context.source.player.sendMessage(LiteralText("setinput: <value>"), false)
        return 0
    }

    @Throws(CommandSyntaxException::class)
    fun run_save(context: CommandContext<ServerCommandSource>, name: String): Int {
        if (!context.source.world.isClient) {
            val inv = context.source.player.inventory
            val circuit = circuit.create(context.source.world)
            CompactRedstone.LOGGER.info(circuit.toString())
            inv.setStack(inv.emptySlot, CompactRedstoneBlock.create(circuit, name))
            return 0
        }
        return -1
    }

    fun set_input(context: CommandContext<ServerCommandSource>, value: Int): Int {
        if (context.source.world != context.source.world.server.getWorld(CompactRedstone.REDSTONE_WORLD)) {
            context.source.player.sendMessage(LiteralText("This command must be run in the redstone world"), false)
            return -1
        }

        val power = max(min(value, 15), 0)

        for (pos in CompactInput.all_input_pos) {
            val state = context.source.world.getBlockState(pos)
            context.source.world.setBlockState(pos, state.with(POWER, power), Block.NOTIFY_ALL)
        }

        return 0
    }

    fun clear_spawn_chunks(redstoneWorld: ServerWorld, width: Int, length: Int, height: Int) {
        val rwcg: RedstoneWorldChunkGenerator = redstoneWorld.chunkManager.chunkGenerator as RedstoneWorldChunkGenerator

        rwcg.sizex = width
        rwcg.sizez = length
        rwcg.sizey = height

        rwcg.reset_floor(redstoneWorld)
        rwcg.reset_roof(redstoneWorld)
        rwcg.reset_walls(redstoneWorld)
    }

    @Throws(CommandSyntaxException::class)
    fun run_create(context: CommandContext<ServerCommandSource>, x: Int = -1, z: Int = -1, y: Int = -1): Int {
        val serverWorld = context.source.world
        if (!context.source.world.isClient) {
            var width = x
            if (width == -1) {
                width = 16
            }
            var length = z
            if (length == -1) {
                length = 16
            }
            var height = y
            if (height == -1) {
                height = 8
            }
            val redstoneWorld: ServerWorld? = serverWorld.server.getWorld(CompactRedstone.REDSTONE_WORLD)
            if (redstoneWorld == null) {
                context.source.player.sendMessage(LiteralText("Failed to find redstone world, was it registered?"), false)
                return -1
            }
            FabricDimensions.teleport(context.source.player, redstoneWorld, TeleportTarget(Vec3d(0.0,
                RedstoneWorldChunkGenerator.spawn_height.toDouble() + 1, 0.0), Vec3d.ZERO, 0F, 0F))


            clear_spawn_chunks(redstoneWorld, width, length, height)

            return 0
        }
        return -1
    }
}