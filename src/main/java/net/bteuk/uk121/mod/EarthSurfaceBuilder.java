package net.bteuk.uk121.mod;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

import java.util.Random;

public class EarthSurfaceBuilder extends SurfaceBuilder<TernarySurfaceConfig> {

    private int yMin;

    public EarthSurfaceBuilder(Codec<TernarySurfaceConfig> codec){
        super(codec);
        Config config = new Config();
        config.load();
        yMin = config.yMin;
    }

    @Override
    public void generate(Random random, Chunk chunk, Biome biome, int x, int z, int height, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, int i, long l, TernarySurfaceConfig surfaceConfig)
    {

        //Generates defaultBlock at the specified location x,height,z
        //chunk.setBlockState(new BlockPos(x, height, z), defaultBlock, false);

        //Generated defaultBlock at the specified height and everything below that.

        BlockState Top = surfaceConfig.getTopMaterial();

        for (int h = ConfigVariables.yMin; h <= height; h++)
        {
            if (h == height)
            {
                chunk.setBlockState(new BlockPos(x, h, z), Top, false);
            }
            else
            {
                chunk.setBlockState(new BlockPos(x, h, z), defaultBlock, false);
            }
        }
    }

    public void generateWater(Random random, Chunk chunk, Biome biome, int x, int z, int height, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, int i, long l, TernarySurfaceConfig surfaceConfig) {

        //Generates defaultBlock at the specified location x,height,z
        //chunk.setBlockState(new BlockPos(x, height, z), defaultBlock, false);

        //Generated defaultBlock at the specified height and everything below that.
        BlockState Top = surfaceConfig.getTopMaterial();

        for (int h = ConfigVariables.yMin; h <= height-1; h++)
        {
            if (h == height-1)
            {
                chunk.setBlockState(new BlockPos(x, h, z), Top, false);
            }
            else
            {
                chunk.setBlockState(new BlockPos(x, h, z), defaultBlock, false);
            }
        }

        chunk.setBlockState(new BlockPos(x, height, z), defaultFluid, false);

        for (int h = height+1 ; h <= seaLevel+1; h++)
        {
            chunk.setBlockState(new BlockPos(x, h, z), defaultFluid, false);
        }
    }


}

