package net.bteuk.uk121.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bteuk.uk121.UK121;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.DefaultBlockSource;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.surfacebuilder.ConfiguredSurfaceBuilder;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class EarthGenerator extends ChunkGenerator {

    protected final ChunkRandom random;
    protected final BlockState defaultBlock;
    protected final BlockState defaultFluid;

    //Structure config values
    private static final int iDistance = 0;
    private static final int iSpread = 0;
    private static final int iCount = 0;

    public static final Codec<EarthGenerator> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((EarthGenerator) -> {
            return EarthGenerator.biomeSource;
        })).apply(instance, instance.stable(EarthGenerator::new));
    });

    public EarthGenerator(BiomeSource biomeSource) {
        super(biomeSource, new StructuresConfig(Optional.of(ConfigSetup()), new HashMap<>()));
        random = new ChunkRandom(0);
        defaultBlock = Blocks.STONE.getDefaultState();
        defaultFluid = Blocks.WATER.getDefaultState();
    }

    private static StrongholdConfig ConfigSetup()
    {
        StrongholdConfig ourStructureConfig = new StrongholdConfig(iDistance, iSpread, iCount);
        return ourStructureConfig;
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public void buildSurface(ChunkRegion region, Chunk chunk) {
/*
        ChunkPos chunkPos = chunk.getPos();
        int cx = chunkPos.x;
        int cz = chunkPos.z;

        int x0 = chunkPos.getStartX();
        int z0 = chunkPos.getStartZ();

        int x;
        int z;

        for (int i = 0; i < 16; i++){
            for (int j = 0; j < 16; j++){
                x = x0 + i;
                z = z0 + j;

            }
        }*/
        UK121.LOGGER.info("buildSurface!");
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, StructureAccessor accessor, Chunk chunk) {

        CompletableFuture<Chunk> cfc = new CompletableFuture<>();
        cfc.complete(chunk);
        return cfc;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world) {
        UK121.LOGGER.info("getHeight!");
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world) {
        UK121.LOGGER.info("getColumnSample!");
        return new VerticalBlockSample(0, new BlockState[0]);
    }
}
