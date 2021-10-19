package net.bteuk.uk121;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.bteuk.uk121.mixin.GeneratorTypeAccessor;
import net.bteuk.uk121.world.gen.EarthGenerator;
import net.bteuk.uk121.world.gen.biome.EarthBiomeSource;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.OverworldBiomes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.gen.chunk.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static net.minecraft.world.biome.BiomeKeys.FOREST;
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

            DynamicRegistryManager registry = DynamicRegistryManager.create();
            Registry<Biome> biomes = registry.get(Registry.BIOME_KEY);

            return new EarthGenerator(new EarthBiomeSource(biomes, 0, 0));
        }
    };

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        GeneratorTypeAccessor.getValues().add(EARTH);
        Registry.register(Registry.BIOME_SOURCE, new Identifier(MOD_ID, "earth_biome_source"), EarthBiomeSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, id("chunkgenerator"), EarthGenerator.CODEC);

        //GeneratorTypeAccessor.getValues().add(VOID);

        LOGGER.info("UK121 Initialised!");
    }

    public Identifier id(String... path){
        return new Identifier(MOD_ID, String.join(".", path));
    }

    /**
     * Earth's circumference around the equator, in meters.
     */
    public static final double EARTH_CIRCUMFERENCE = 40075017;

    /**
     * Earth's circumference around the poles, in meters.
     */
    public static final double EARTH_POLAR_CIRCUMFERENCE = 40008000;

    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
}
