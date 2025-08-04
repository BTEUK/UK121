package net.bteuk.uk121.mod;

import net.bteuk.uk121.minecraft.biome.EarthGenerator;
import net.bteuk.uk121.minecraft.biome.EarthBiomeSource;
import net.bteuk.uk121.minecraft.biome.EarthPopulationSource;
import net.bteuk.uk121.minecraft.biome.EmptyBiome;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UK121 implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LogManager.getLogger("uk121");

    public static final String directory = System.getProperty("user.dir") + "/uk121/";

    public static final String MOD_ID = "uk121";

    //Title Screen image.
    public static Identifier TITLE_SCREEN;

    //Setup empty biome
    public static RegistryKey<Biome> EMPTY_KEY;
    public static Biome EMPTY;

    private Config config;
    public static Config CONFIG;

    /*
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
     */

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        TITLE_SCREEN = new Identifier("uk121:textures/gui/title/background/2.png");
        EMPTY_KEY = RegistryKey.of(Registry.BIOME_KEY, new Identifier(MOD_ID, "empty"));
        EMPTY = EmptyBiome.EMPTY;

        //Register custom empty biome
        Registry.register(BuiltinRegistries.BIOME, EMPTY_KEY.getValue(), EMPTY);
        //Register Biome Source for biome and population
        Registry.register(Registry.BIOME_SOURCE, id("earth_population_source"), EarthPopulationSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, id("earth_biome_source"), EarthBiomeSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, id("earth"), EarthGenerator.CODEC);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
//            GeneratorType EARTH = new GeneratorType("earth") {
//                //Returns a new EarthGenerator class
//                protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry,
//                                                           Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry,
//                                                           long seed) {
//
//                    //Initiates a new biome source object. Fields: biomes, biome size, seed
//                    EarthBiomeSource earthBiomeSource = new EarthBiomeSource(biomeRegistry, 0, 0);
//                    //Initiates a new biome population object. Fields: biomes, biome size, seed
//                    EarthPopulationSource earthPopulationSource = new EarthPopulationSource(biomeRegistry, 0, 0);
//
//                    //Returns a new EarthGenerator object, parsing the biome source in
//                    return new EarthGenerator(earthPopulationSource, earthBiomeSource);
//                }
//            };
//            GeneratorTypeAccessor.getValues().add(EARTH);
        }

        /*
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            Tpll.register(dispatcher);
        }));
         */


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

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_ID+"] " + message);
    }
}
