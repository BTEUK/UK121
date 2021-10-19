package net.bteuk.uk121.world.gen.surfacebuilder;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

import java.util.Random;

public class EarthSurfaceBuilder extends SurfaceBuilder<TernarySurfaceConfig> {

    public EarthSurfaceBuilder(Codec<TernarySurfaceConfig> codec){
        super(codec);
    }

    @Override
    public void generate(Random random, Chunk chunk, Biome biome, int x, int z, int height, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, int i, long l, TernarySurfaceConfig surfaceConfig) {

        //Generates defaultBlock at the specified location x,height,z
        chunk.setBlockState(new BlockPos(x, height, z), defaultBlock, false);
    }
}
