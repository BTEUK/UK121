package net.bteuk.uk121;

import com.mojang.serialization.Codec;
import net.bteuk.uk121.item.ModItems;
import net.bteuk.uk121.mixin.GeneratorTypeAccessor;
import net.bteuk.uk121.world.gen.EarthGenerator;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static net.minecraft.world.biome.BuiltinBiomes.PLAINS;

public class UK121 implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LogManager.getLogger("uk121");

    public static final String MOD_ID = "uk121";

    private static final GeneratorType VOID = new GeneratorType("void") {
        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry,
                                                   Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
            FlatChunkGeneratorConfig config = new FlatChunkGeneratorConfig(
                    new StructuresConfig(Optional.empty(), Collections.emptyMap()), biomeRegistry);
            config.updateLayerBlocks();
            return new FlatChunkGenerator(config);
        }
    };

    private static final GeneratorType EARTH = new GeneratorType("earth") {
        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry,
                                                   Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry,
                                                   long seed) {
            return new EarthGenerator(new FixedBiomeSource(PLAINS), new StructuresConfig(false));
        }
    };

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        GeneratorTypeAccessor.getValues().add(EARTH);
        GeneratorTypeAccessor.getValues().add(VOID);

        LOGGER.info("Hello Fabric world!");
    }
}
