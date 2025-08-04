package net.bteuk.uk121.minecraft.biome;

import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.surfacebuilder.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

public class EmptyBiome {

    private static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> SURFACE_BUILDER = SurfaceBuilder.DEFAULT
            .withConfig(new TernarySurfaceConfig(
                    Blocks.GRASS_BLOCK.getDefaultState(),
                    Blocks.DIRT.getDefaultState(),
                    Blocks.STONE.getDefaultState()));

    public static final Biome EMPTY = createBiome();

    private static Biome createBiome(){
        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();

        GenerationSettings.Builder generationSettings = new GenerationSettings.Builder();
        generationSettings.surfaceBuilder(SURFACE_BUILDER);

        return (new Biome.Builder())
                .precipitation(Biome.Precipitation.NONE)
                .category(Biome.Category.NONE)
                .depth(0.125F)
                .scale(0.05F)
                .temperature(0.8F)
                .downfall(0.4F)
                .effects((new BiomeEffects.Builder())
                        .waterColor(0x3f76e4)
                        .waterFogColor(0x050533)
                        .fogColor(0xc0d8ff)
                        .skyColor(0x77adff)
                        .build())
                .spawnSettings(spawnSettings.build())
                .generationSettings(generationSettings.build())
                .build();
    }

}
