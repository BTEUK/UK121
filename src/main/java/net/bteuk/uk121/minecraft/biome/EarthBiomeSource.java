package net.bteuk.uk121.minecraft.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bteuk.uk121.mod.UK121;
import net.minecraft.util.dynamic.RegistryLookupCodec;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;

import java.util.Map;
import java.util.stream.Collectors;

public class EarthBiomeSource extends BiomeSource {

    //Creates the CODEC for EarthBiomeSource.
    public static final Codec<EarthBiomeSource> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(RegistryLookupCodec.of(Registry.BIOME_KEY).forGetter((biomeSource) ->
                    biomeSource.BIOME_REGISTRY), Codec.intRange(1, 20).fieldOf("biome_size").orElse(2).forGetter((biomeSource) ->
                    biomeSource.biomeSize), Codec.LONG.fieldOf("seed").stable().forGetter((biomeSource) ->
                    biomeSource.seed)).apply(instance, instance.stable(EarthBiomeSource::new)));

    private final Registry<Biome> BIOME_REGISTRY;
    public static Registry<Biome> LAYERS_BIOME_REGISTRY;
    private final long seed;
    private final int biomeSize;

    /*
    Constructor.
    Sets Registry<Biome> biomeRegistry with all existing biomes in the game.
    int BiomeSize and long seed are not applicable for this world generation type, however they are parameters nontheless.
     */
    public EarthBiomeSource(Registry<Biome> biomeRegistry, int biomeSize, long seed) {
        super(biomeRegistry.getEntries().stream()
                .filter(entry -> entry.getKey().getValue().getNamespace().equals(UK121.MOD_ID))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList()));
        this.BIOME_REGISTRY = biomeRegistry;
        EarthBiomeSource.LAYERS_BIOME_REGISTRY = biomeRegistry;
        this.biomeSize = biomeSize;
        this.seed = seed;
    }

    @Override
    protected Codec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long seed) {
        return new EarthBiomeSource(this.BIOME_REGISTRY, this.biomeSize, seed);
    }

    /*
    Selects the biome for a specific x,z coordinate, y is not taken into account.
    To add y as a parameter simply edit the method parameters.
    Any methods that call this method will also need to be adjusted then.
     */
    public Biome sample(Registry<Biome> dynamicBiomeRegistry, int x, int z) {

        //No biome selection process has been added currently, defaults to plains.
        RegistryKey<Biome> backupBiomeKey = BiomeKeys.PLAINS;
        return dynamicBiomeRegistry.get(backupBiomeKey);
    }

    //Overwrites the default biome generation.
    //Calls sample() to select the biome to return.
    @Override
    public Biome getBiomeForNoiseGen(int x, int y, int z) {
        return this.sample(this.BIOME_REGISTRY, x, z);
    }
}
