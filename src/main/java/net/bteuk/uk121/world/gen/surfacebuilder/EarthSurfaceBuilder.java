package net.bteuk.uk121.world.gen.surfacebuilder;

import com.mojang.serialization.Codec;
import net.bteuk.uk121.Config;
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
    public void generate(Random random, Chunk chunk, Biome biome, int x, int z, int height, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, int i, long l, TernarySurfaceConfig surfaceConfig) {

        //Generates defaultBlock at the specified location x,height,z
        //chunk.setBlockState(new BlockPos(x, height, z), defaultBlock, false);

        //Generated defaultBlock at the specified height and everything below that.
        for (int h = yMin; h <= height; h++)
        {
            if (h == height)
            {
                chunk.setBlockState(new BlockPos(x, h, z), surfaceConfig.getTopMaterial(), false);
            } else {
                chunk.setBlockState(new BlockPos(x, h, z), defaultBlock, false);
            }
        }
    }
}
