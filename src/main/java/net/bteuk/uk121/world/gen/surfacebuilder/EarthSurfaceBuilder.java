package net.bteuk.uk121.world.gen.surfacebuilder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bteuk.uk121.UK121;
import net.bteuk.uk121.world.gen.EarthGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
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
        //chunk.setBlockState(new BlockPos(x, height, z), defaultBlock, false);

        //Generated defaultBlock at the specified height and everything below that.
        for (int h = UK121.YMIN; h <= height; h++){
            chunk.setBlockState(new BlockPos(x, h, z), defaultBlock, false);
        }
    }
}
