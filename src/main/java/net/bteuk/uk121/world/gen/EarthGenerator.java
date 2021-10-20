package net.bteuk.uk121.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bteuk.uk121.UK121;
import net.bteuk.uk121.world.gen.surfacebuilder.BlockAPICall;
import net.bteuk.uk121.world.gen.surfacebuilder.EarthSurfaceBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class EarthGenerator extends ChunkGenerator {

    protected final Random random;
    protected final BlockState grassBlock;
    protected final BlockState dirtBlock;
    protected final BlockState stoneBlock;
    protected final BlockState defaultFluid;

    //Structure config values
    private static final int iDistance = 0;
    private static final int iSpread = 0;
    private static final int iCount = 0;

    private final BiomeSource populationSource;
    private final BiomeSource biomeSource;

    public static final Codec<EarthGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            BiomeSource.CODEC.fieldOf("earth_population_source").forGetter((EarthGenerator) -> EarthGenerator.populationSource),
            BiomeSource.CODEC.fieldOf("earth_biome_source").forGetter((EarthGenerator) -> EarthGenerator.biomeSource))
            .apply(instance, instance.stable(EarthGenerator::new)));

    public EarthGenerator(BiomeSource populationSource, BiomeSource biomeSource) {
        super(populationSource, biomeSource, new StructuresConfig(Optional.of(StrongholdConfigSetup()), new HashMap<>()), 0);
        //Random based on world seed. Not really applicable to this world type but added nontheless.
        random = new ChunkRandom(0);
        //Set the 3 default blocks.
        grassBlock = Blocks.GRASS_BLOCK.getDefaultState();
        dirtBlock = Blocks.DIRT.getDefaultState();
        stoneBlock = Blocks.STONE.getDefaultState();
        //Default fluid set to water.
        defaultFluid = Blocks.WATER.getDefaultState();
        this.populationSource = populationSource;
        this.biomeSource = biomeSource;
    }

    private static StrongholdConfig StrongholdConfigSetup()
    {
        StrongholdConfig ourStrongholdConfig = new StrongholdConfig(iDistance, iSpread, iCount);
        return ourStrongholdConfig;
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public void buildSurface(ChunkRegion region, Chunk chunk) {

        //Get the location of the chunk
        ChunkPos chunkPos = chunk.getPos();
        //Get the chunk x and z
        int cx = chunkPos.x;
        int cz = chunkPos.z;

        //Get the corner of the chunk in x and z.
        int x0 = chunkPos.getStartX();
        int z0 = chunkPos.getStartZ();

        int x;
        int z;

        //Create an array from 0 to 255
        int[] elev = new int[16*16];
        for (int p = 0; p < 16*16; p++){
            elev[p] = p;
        }

        //Basic surface config, to be edited later.
        TernarySurfaceConfig config = new TernarySurfaceConfig(grassBlock, dirtBlock, stoneBlock);
        //Create surfaceBuilder, which is where the blocks are actually generated.
        EarthSurfaceBuilder surfaceBuilder = new EarthSurfaceBuilder(config.CODEC);

        /*
        //Iterate through each x,z of Chunk
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                x = x0 + i;
                z = z0 + j;

                //Generate a block at x,z with height elev[i+j*16] for testing purposes.
                surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(x, 1, z), x, z, elev[i+j*16], 0.0, defaultBlock, defaultFluid, 63, 0, 0, config);
            }
        }
        */

        //Used to store the height value fetched from the API call
        int iHeight;

        //For each x of chunk
        for (int i = 0; i < 16; i++)
        {
            //Updates the actual x coordinate
            x = x0 + i;

            //For each z of each x
            for (int j = 0; j < 16; j++)
            {
                //Updates the actual z coordinate
                z = z0 + j;

                //Gets the height of a particular block
                //iHeight = BlockAPICall.getHeightforXZ(x, z);

                //This value is purely for testing purposes, until the height generation is complete.
                iHeight = 0;

                //Generate a block at x,z with the correct height fetched from the api call.
                surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(x, 1, z), x, z, iHeight, 0.0, stoneBlock, defaultFluid, UK121.SEALEVEL, 0, 0, config);
            }
        }
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, StructureAccessor accessor, Chunk chunk) {

        CompletableFuture<Chunk> cfc = new CompletableFuture<>();
        cfc.complete(chunk);
        return cfc;
    }



    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world) {
        UK121.LOGGER.info("getHeight!");
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world) {
        UK121.LOGGER.info("getColumnSample!");
        return new VerticalBlockSample(0, new BlockState[0]);
    }
}
