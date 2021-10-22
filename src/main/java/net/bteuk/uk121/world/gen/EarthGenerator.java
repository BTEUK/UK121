package net.bteuk.uk121.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bteuk.uk121.Config;
import net.bteuk.uk121.ConfigVariables;
import net.bteuk.uk121.UK121;
import net.bteuk.uk121.world.gen.surfacebuilder.APIService;
import net.bteuk.uk121.world.gen.surfacebuilder.BlockAPICall;
import net.bteuk.uk121.world.gen.surfacebuilder.EarthSurfaceBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
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

    private int seaLevel;

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


        Config config = new Config();
        config.load();
        seaLevel = config.seaLevel;

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
        int x1 = chunkPos.getEndX();
        int z1 = chunkPos.getEndZ();

        int x;
        int z;

        //Basic surface config, to be edited later.
        TernarySurfaceConfig config = new TernarySurfaceConfig(grassBlock, dirtBlock, stoneBlock);
        //Create surfaceBuilder, which is where the blocks are actually generated.
        EarthSurfaceBuilder surfaceBuilder = new EarthSurfaceBuilder(config.CODEC);

        /*

        //Create an array from 0 to 255
        int[] elev = new int[16*16];
        for (int p = 0; p < 16*16; p++){
            elev[p] = p;
        }



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

        //Test all 4 corners of chunk. If they lie in the same tile, standardise tile.

        //Stores whether or not the height data can be received all from 1 tile
        boolean bAllInSameTile = true;

        //Gets the tile for each corner of the chunk
        int[] Corner1 = BlockAPICall.getTile(x0, z0);
        int[] Corner3 = BlockAPICall.getTile(x1, z1);;

        //If two opposite corners aren't in the same tile, declare a difference
        if (Corner1[0] != Corner3[0] || Corner1[1] != Corner3[1])
        {
            bAllInSameTile = false;
        }
        else //If two of them are in the same tile, it doesn't confirm the whole chunk is in the correct tile.
        {
            int[] Corner2 = BlockAPICall.getTile(x0, z1);;
            int[] Corner4 = BlockAPICall.getTile(x1, z0);;

            if (Corner2[0] != Corner4[0] || Corner2[1] != Corner4[1])
            {
                bAllInSameTile = false;
            }
        }

        String URL;
        BlockAPICall ourTile = null;

        //Downloads the required tile if they are all the same
        if (bAllInSameTile)
        {
            ourTile = new BlockAPICall(Corner1[0],  Corner1[1], 15);
            ourTile.loadPicture();
        }

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

                //This value is purely for testing purposes, until the height generation is complete.
                iHeight = 0;

                //Gets the height of a particular block
                if (bAllInSameTile)
                    iHeight = ourTile.getHeightForXZ(x, z, iHeight);
                else
                {
                    iHeight = BlockAPICall.getTileAndHeightForXZ(x, z, iHeight);
                }
                //Generate a block at x,z with the correct height fetched from the api call.
                surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(x, 1, z), x, z, iHeight, 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, config);
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
