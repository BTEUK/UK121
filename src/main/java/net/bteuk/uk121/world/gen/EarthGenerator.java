package net.bteuk.uk121.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bteuk.uk121.UK121;
import net.minecraft.block.BlockState;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class EarthGenerator extends ChunkGenerator {

    protected final boolean customBool;

    public static final Codec<EarthGenerator> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source")
                                    .forGetter((generator) -> generator.biomeSource),
                            Codec.BOOL.fieldOf("custom_bool")
                                    .forGetter((generator) -> generator.customBool)
                    )
                    .apply(instance, instance.stable(EarthGenerator::new))
    );

    public EarthGenerator(BiomeSource biomeSource, boolean customBool) {
        super(biomeSource, new StructuresConfig(Optional.of(new StrongholdConfig(0,0,0)), new HashMap<>()));
        this.customBool = customBool;
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
