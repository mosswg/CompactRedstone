package net.mossx.compactredstone.redstoneworld

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.structure.StructureSet
import net.minecraft.util.dynamic.RegistryOps
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryEntryList
import net.minecraft.world.ChunkRegion
import net.minecraft.world.HeightLimitView
import net.minecraft.world.Heightmap
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.biome.source.BiomeAccess
import net.minecraft.world.biome.source.FixedBiomeSource
import net.minecraft.world.biome.source.util.MultiNoiseUtil
import net.minecraft.world.biome.source.util.MultiNoiseUtil.MultiNoiseSampler
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.StructureAccessor
import net.minecraft.world.gen.chunk.Blender
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.chunk.VerticalBlockSample
import net.mossx.compactredstone.CompactRedstone
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.BiFunction
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class RedstoneWorldChunkGenerator(registry: Registry<StructureSet?>?, private val biomeRegistry: Registry<Biome>) : ChunkGenerator(
    registry,
    Optional.of(RegistryEntryList.of(emptyList())),
    FixedBiomeSource(biomeRegistry.getOrCreateEntry(BiomeKeys.THE_VOID))
) {

    companion object {
        const val spawn_height = 10
        const val roof_size = 10
        val CODEC: Codec<RedstoneWorldChunkGenerator> =
            RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<RedstoneWorldChunkGenerator> ->
                method_41042(instance).and(
                    RegistryOps.createRegistryCodec(Registry.BIOME_KEY)
                        .forGetter { generator: RedstoneWorldChunkGenerator -> generator.biomeRegistry })
                    .apply(instance, instance.stable( BiFunction{ registry: Registry<StructureSet?>?, biomeRegistry: Registry<Biome> -> RedstoneWorldChunkGenerator(registry, biomeRegistry) } ))
            }
    }

    override fun getCodec(): Codec<RedstoneWorldChunkGenerator> {
        return CODEC
    }

    var sizex = 16
    var sizez = 16
    var sizey = 8
    var block_type: Block = Blocks.GRAY_CONCRETE

    override fun withSeed(seed: Long): ChunkGenerator {
        return this
    }

    override fun getMultiNoiseSampler(): MultiNoiseSampler? {
        // Mirror what Vanilla does in the debug chunk generator
        return MultiNoiseUtil.method_40443()
    }

    override fun carve(
        chunkRegion: ChunkRegion?,
        l: Long,
        biomeAccess: BiomeAccess?,
        structureAccessor: StructureAccessor?,
        chunk: Chunk?,
        carver: GenerationStep.Carver?
    ) {
    }

    override fun buildSurface(region: ChunkRegion?, structureAccessor: StructureAccessor?, chunk: Chunk?) {}

    override fun populateEntities(region: ChunkRegion?) {}

    override fun getWorldHeight(): Int {
        return sizey + spawn_height + roof_size
    }

    fun reset_roof(world: World) {
        val mutable = BlockPos.Mutable()
        for (j in worldHeight downTo  worldHeight- roof_size) {
            for (l in -sizez .. sizez) {
                for (k in -sizex .. sizex) {
                    world.setBlockState(mutable.set(k, j, l), block_type.defaultState)
                }
            }
        }
    }

    fun reset_floor(world: World) {
        val mutable = BlockPos.Mutable()
        for (j in 0 .. spawn_height) {
            for (l in -sizez .. sizez) {
                for (k in -sizex .. sizex) {
                    world.setBlockState(mutable.set(k, j, l), block_type.defaultState)
                }
            }
        }
    }

    fun reset_walls(world: World) {
        val mutable = BlockPos.Mutable()

        for (j in spawn_height .. worldHeight - roof_size) {
            for (l in -sizez/2..sizez/2) {
                for (k in -sizex/2 .. sizex/2) {
                    world.setBlockState(mutable.set(k, j, l), Blocks.AIR.defaultState)
                }
            }
            for (l in -sizez..-sizez/2) {
                for (k in -sizex .. sizex) {
                    world.setBlockState(mutable.set(k, j, l), block_type.defaultState)
                }
            }
            for (l in sizez/2..sizez) {
                for (k in -sizex .. sizex) {
                    world.setBlockState(mutable.set(k, j, l), block_type.defaultState)
                }
            }

            for (k in -sizex..-sizex/2) {
                for (l in -sizez .. sizez) {
                    world.setBlockState(mutable.set(k, j, l), block_type.defaultState)
                }
            }
            for (k in sizex/2..sizex) {
                for (l in -sizez .. sizez) {
                    world.setBlockState(mutable.set(k, j, l), block_type.defaultState)
                }
            }
        }
    }


    fun create_walls(chunk: Chunk) {

        val mutable = BlockPos.Mutable()

        val chunk_offset_x = chunk.pos.x * 16
        val chunk_offset_z = chunk.pos.z * 16
        val chunk_edge_x = chunk.pos.x * 16 + 16
        val chunk_edge_z = chunk.pos.z * 16 + 16

        val num_filled_blocks_x = min(max(max(abs(chunk_edge_x) - sizex/2, abs(chunk_offset_x) - sizex/2), 0), 15)
        val num_filled_blocks_z = min(max(max(abs(chunk_edge_z) - sizez/2, abs(chunk_offset_z) - sizez/2), 0), 15)

        // Create the walls if there should be walls
        if (num_filled_blocks_x != 0 || num_filled_blocks_z != 0) {
            for (j in spawn_height .. worldHeight - roof_size) {
                if (chunk.pos.x < 0) {
                    for (k in 0..num_filled_blocks_x) {
                        for (l in 0..15) {
                            chunk.setBlockState(mutable.set(k, j, l), block_type.defaultState, false)
                        }
                    }
                }
                else {
                    for (k in 15 downTo 15-num_filled_blocks_x) {
                        for (l in 0..15) {
                            chunk.setBlockState(mutable.set(k, j, l), block_type.defaultState, false)
                        }
                    }
                }
                if (chunk.pos.z < 0) {
                    for (l in 0..num_filled_blocks_z) {
                        for (k in 0..15) {
                            chunk.setBlockState(mutable.set(k, j, l), block_type.defaultState, false)
                        }
                    }
                }
                else {
                    for (l in 15 downTo 15-num_filled_blocks_z) {
                        for (k in 0..15) {
                            chunk.setBlockState(mutable.set(k, j, l), block_type.defaultState, false)
                        }
                    }
                }
            }
        }
    }

    override fun populateNoise(
        ex: Executor?,
        bl: Blender?,
        sa: StructureAccessor?,
        chunk: Chunk
    ): CompletableFuture<Chunk>? {
        val mutable = BlockPos.Mutable()

        // Create the roof
        for (j in 0..spawn_height) {
            for (k in 0..15) {
                for (l in 0..15) {
                    chunk.setBlockState(mutable.set(k, j, l), block_type.defaultState, false)
                }
            }
        }
        // Create the floor
        for (j in this.worldHeight - 10..this.worldHeight) {
            for (k in 0..15) {
                for (l in 0..15) {
                    chunk.setBlockState(mutable.set(k, j, l), block_type.defaultState, false)
                }
            }
        }

        create_walls(chunk)
        return CompletableFuture.completedFuture(chunk)
    }

    override fun getSeaLevel(): Int {
        return 64
    }

    override fun getMinimumY(): Int {
        return 0
    }

    override fun getHeight(x: Int, z: Int, heightmapType: Heightmap.Type?, heightLimitView: HeightLimitView?): Int {
        return 0
    }

    override fun getColumnSample(x: Int, z: Int, heightLimitView: HeightLimitView?): VerticalBlockSample {
        return VerticalBlockSample(0, arrayOfNulls(0))
    }

    override fun getDebugHudText(list: List<String?>?, blockPos: BlockPos?) {}
}