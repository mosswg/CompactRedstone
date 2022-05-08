package net.mossx.compactredstone

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted
import net.minecraft.block.Block
import net.minecraft.item.*
import net.minecraft.server.MinecraftServer
import net.minecraft.stat.StatFormatter
import net.minecraft.stat.Stats
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.mossx.compactredstone.block.*
import net.mossx.compactredstone.command.CompactRedstoneCommand
import net.mossx.compactredstone.item.Wand
import net.mossx.compactredstone.redstoneworld.RedstoneWorldChunkGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory


@Suppress("UNUSED")
object CompactRedstone: ModInitializer {
    const val NAMESPACE = "compactredstone"

    val LOGGER: Logger = LoggerFactory.getLogger("CompactRedstone")

    var NOT = Not()
    var AND = And()
    var OR = Or()
    var COMPACTINPUT = CompactInput()
    var COMPACTOUTPUT = CompactOutput()
    var COMPACTREDSTONEBLOCK = CompactRedstoneBlock()

    val WAND = Wand(Item.Settings().maxCount(1).rarity(Rarity.EPIC)) as Item

    var mc: MinecraftServer? = null

    var created_blocks: Array<CompactRedstoneBlock> = emptyArray()

    var CR_GROUP = FabricItemGroupBuilder.create(
        Identifier(NAMESPACE, "gates")).icon { ItemStack(Items.BOWL) }.appendItems { stacks ->
        run {
            stacks.add(ItemStack(WAND))
            stacks.add(ItemStack(NOT))
            stacks.add(ItemStack(AND))
            stacks.add(ItemStack(OR))
            stacks.add(ItemStack(COMPACTINPUT))
            stacks.add(ItemStack(COMPACTOUTPUT))
        }
    }.build()
    val REDSTONE_WORLD = RegistryKey.of(Registry.WORLD_KEY, Identifier(NAMESPACE, "compactredstoneworld"))
    val SETTINGS = Item.Settings().group(CR_GROUP)

    private const val MOD_ID = "compact_redstone"
    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(ServerStarted { mc: MinecraftServer? ->
            CompactRedstone.mc = mc
        })

        // Register Command
        CommandRegistrationCallback.EVENT.register(CompactRedstoneCommand::register)

        register("not", NOT)
        register("and", AND)
        register("or", OR)
        register("compactoutput", COMPACTOUTPUT)
        register("compactinput", COMPACTINPUT)
        register("compactredstoneblock", COMPACTREDSTONEBLOCK)

        registerItem("crwand", WAND)

        // Register chunk gen
        Registry.register(Registry.CHUNK_GENERATOR, REDSTONE_WORLD.value, RedstoneWorldChunkGenerator.CODEC)
    }

    private fun registerBlock(name: String, block: Block) {
        Registry.register(Registry.BLOCK, Identifier(NAMESPACE, name), block)
    }

    private fun registerItem(name: String, item: Item) {
        Registry.register(Registry.ITEM, Identifier(NAMESPACE, name), item)
    }

    private fun register(name: String, block: Block) {
        registerBlock(name, block)
        registerItem(name, BlockItem(block, SETTINGS))
    }

    private fun registerStat(id: Identifier) {
        Registry.register(Registry.CUSTOM_STAT, id, id)
        Stats.CUSTOM.getOrCreateStat(id, StatFormatter.DEFAULT)
    }

}