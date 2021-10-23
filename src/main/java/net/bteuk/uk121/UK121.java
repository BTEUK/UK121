package net.bteuk.uk121;

import net.bteuk.uk121.commands.Tpll;
import net.bteuk.uk121.mixin.GeneratorTypeAccessor;
import net.bteuk.uk121.world.gen.EarthGenerator;
import net.bteuk.uk121.world.gen.biome.EarthBiomeSource;
import net.bteuk.uk121.world.gen.biome.EarthPopulationSource;
import net.bteuk.uk121.world.gen.biome.EmptyBiome;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.MessageArgumentType.getMessage;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class UK121 implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LogManager.getLogger("uk121");

    public static final String MOD_ID = "uk121";

    //Title Screen image.
    public static final Identifier TITLE_SCREEN = new Identifier("uk121:textures/gui/title/background/2.png");

    //Setup empty biome
    public static final RegistryKey<Biome> EMPTY_KEY = RegistryKey.of(Registry.BIOME_KEY, new Identifier(MOD_ID, "empty"));
    public static final Biome EMPTY = EmptyBiome.EMPTY;

    private Config config;
    public static Config CONFIG;

    //Adds the "void" generator type - used for testing
    private static final GeneratorType VOID = new GeneratorType("void")
    {
        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry,
                                                   Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
            FlatChunkGeneratorConfig config = new FlatChunkGeneratorConfig(
                    new StructuresConfig(Optional.empty(), Collections.emptyMap()), biomeRegistry);
            config.updateLayerBlocks();
            return new FlatChunkGenerator(config);
        }
    };

    //Adds the "Earth" generator type, used for generating the earth
    private static final GeneratorType EARTH = new GeneratorType("earth") {
        //Returns a new EarthGenerator class
        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry,
                                                   Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry,
                                                   long seed) {

            //Initiates a new biome source object. Fields: biomes, biome size, seed
            EarthBiomeSource earthBiomeSource = new EarthBiomeSource(biomeRegistry, 0, 0);
            //Initiates a new biome population object. Fields: biomes, biome size, seed
            EarthPopulationSource earthPopulationSource = new EarthPopulationSource(biomeRegistry, 0, 0);

            //Returns a new EarthGenerator object, parsing the biome source in
            return new EarthGenerator(earthPopulationSource, earthBiomeSource);
        }
    };

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        //Adds the generator type to the accessor
        GeneratorTypeAccessor.getValues().add(EARTH);
        //Register custom empty biome
        Registry.register(BuiltinRegistries.BIOME, EMPTY_KEY.getValue(), EMPTY);
        //Register Biome Source for biome and population
        Registry.register(Registry.BIOME_SOURCE, new Identifier(MOD_ID, "earth_population_source"), EarthPopulationSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, new Identifier(MOD_ID, "earth_biome_source"), EarthBiomeSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, id("chunkgenerator"), EarthGenerator.CODEC);

        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            Tpll.register(dispatcher);
        }));


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
