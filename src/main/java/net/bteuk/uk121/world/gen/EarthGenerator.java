package net.bteuk.uk121.world.gen;

import com.mojang.serialization.Codec;
import net.bteuk.uk121.UK121;
import net.minecraft.block.BlockState;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.chunk.VerticalBlockSample;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class EarthGenerator extends ChunkGenerator {

    public EarthGenerator(BiomeSource biomeSource, StructuresConfig structuresConfig) {
        super(biomeSource, structuresConfig);
        UK121.LOGGER.info("EarthGenerator!");
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
        UK121.LOGGER.info("buildSurface!");
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, StructureAccessor accessor, Chunk chunk) {

        UK121.LOGGER.info(executor.toString());
        UK121.LOGGER.info(accessor.toString());
        UK121.LOGGER.info(chunk.getPos().x);
        UK121.LOGGER.info("populateNoise!");
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
        return new VerticalBlockSample(1, new BlockState[0]);
    }
}
