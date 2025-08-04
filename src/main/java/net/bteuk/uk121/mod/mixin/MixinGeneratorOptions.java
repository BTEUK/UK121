package net.bteuk.uk121.mod.mixin;

import com.google.common.base.MoreObjects;
import net.bteuk.uk121.minecraft.biome.EarthGenerator;
import net.bteuk.uk121.minecraft.biome.EarthBiomeSource;
import net.bteuk.uk121.minecraft.biome.EarthPopulationSource;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Properties;
import java.util.Random;

@Mixin(GeneratorOptions.class)
public class MixinGeneratorOptions {
    @Inject(method = "fromProperties", at = @At("HEAD"), cancellable = true)
    private static void injectWorldTypes(DynamicRegistryManager dynamicRegistryManager, Properties properties, CallbackInfoReturnable<GeneratorOptions> cir) {

        // no server.properties file generated
        if (properties.get("level-type") == null) {
            return;
        }

        // check for our world type and return if so
        if (properties.get("level-type").toString().trim().toLowerCase().equals("earth")) {
            // get or generate seed
            String seed = (String) MoreObjects.firstNonNull(properties.get("level-seed"), "");
            long lSeed = new Random().nextLong();
            if (!seed.isEmpty()) {
                try {
                    long m = Long.parseLong(seed);
                    if (m != 0L) {
                        lSeed = m;
                    }
                } catch (NumberFormatException var14) {
                    lSeed = seed.hashCode();
                }
            }
            Registry<DimensionType> dimensionTypes = dynamicRegistryManager.get(Registry.DIMENSION_TYPE_KEY);
            Registry<Biome> biomes = dynamicRegistryManager.get(Registry.BIOME_KEY);
            Registry<ChunkGeneratorSettings> chunkGeneratorSettings = dynamicRegistryManager.get(Registry.CHUNK_GENERATOR_SETTINGS_KEY);
            SimpleRegistry<DimensionOptions> dimensionOptions = DimensionType.createDefaultDimensionOptions(dimensionTypes, biomes, chunkGeneratorSettings, lSeed);

            // return our chunk generator
            cir.setReturnValue(new GeneratorOptions(lSeed, false, false, GeneratorOptions.getRegistryWithReplacedOverworldGenerator(dimensionTypes, dimensionOptions, new EarthGenerator(new EarthBiomeSource(biomes,0, lSeed), new EarthPopulationSource(biomes, 0, lSeed)))));
        }
    }
}
