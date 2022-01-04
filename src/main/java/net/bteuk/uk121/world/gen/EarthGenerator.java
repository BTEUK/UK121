package net.bteuk.uk121.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bteuk.uk121.Config;
import net.bteuk.uk121.ConfigVariables;
import net.bteuk.uk121.UK121;
import net.bteuk.uk121.world.gen.Projections.ModifiedAirocean;
import net.bteuk.uk121.world.gen.elevation.ElevationManager;
import net.bteuk.uk121.world.gen.surfacedecoration.BlockUse;
import net.bteuk.uk121.world.gen.surfacedecoration.BoundingBox;
//import net.bteuk.uk121.world.gen.surfacedecoration.geojsonOld.Tile;
import net.bteuk.uk121.world.gen.surfacedecoration.UseType;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class EarthGenerator extends ChunkGenerator {

    public ModifiedAirocean projection;

    protected final Random random;
    protected final BlockState grassBlock;
    protected final BlockState dirtBlock;
    protected final BlockState stoneBlock;
    protected final BlockState wornRoadBlock;
    protected final BlockState normalRoadBlock;
    protected final BlockState goodRoadBlock;
    protected final BlockState footwayBlock;
    protected final BlockState defaultFluid;
    protected final BlockState buildingBlock;

    //Structure config values
    private static final int iDistance = 0;
    private static final int iSpread = 0;
    private static final int iCount = 0;

    private final BiomeSource populationSource;
    private final BiomeSource biomeSource;

    //Height api
    private ElevationManager elevationManager;
    private int[][] heights;
    private UseType[][] grid;
    protected boolean bStopped;

    TernarySurfaceConfig land;
    TernarySurfaceConfig seabed;
    TernarySurfaceConfig wornRoad;
    TernarySurfaceConfig normalRoad;
    TernarySurfaceConfig goodRoad;
    TernarySurfaceConfig footway;
    TernarySurfaceConfig building;
    EarthSurfaceBuilder surfaceBuilder;

    public static final Codec<EarthGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    BiomeSource.CODEC.fieldOf("earth_population_source").forGetter((EarthGenerator) -> EarthGenerator.populationSource),
                    BiomeSource.CODEC.fieldOf("earth_biome_source").forGetter((EarthGenerator) -> EarthGenerator.biomeSource))
            .apply(instance, instance.stable(EarthGenerator::new)));

    public EarthGenerator(BiomeSource populationSource, BiomeSource biomeSource) {
        super(populationSource, biomeSource, new StructuresConfig(Optional.of(StrongholdConfigSetup()), new HashMap<>()), 0);
        //Random based on world seed. Not really applicable to this world type but added nontheless.
        random = new ChunkRandom(0);

        //Set the 6 default blocks.
        grassBlock = Blocks.GRASS_BLOCK.getDefaultState();
        dirtBlock = Blocks.DIRT.getDefaultState();
        stoneBlock = Blocks.STONE.getDefaultState();

        normalRoadBlock = Blocks.GRAY_CONCRETE_POWDER.getDefaultState();
        wornRoadBlock = Blocks.CYAN_TERRACOTTA.getDefaultState();
        goodRoadBlock = Blocks.GRAY_CONCRETE_POWDER.getDefaultState();
        footwayBlock = Blocks.LIGHT_GRAY_CONCRETE_POWDER.getDefaultState();

        buildingBlock = Blocks.IRON_BLOCK.getDefaultState();

        //Default fluid set to water.
        defaultFluid = Blocks.WATER.getDefaultState();
        this.populationSource = populationSource;
        this.biomeSource = biomeSource;

        this.projection = new ModifiedAirocean();

        elevationManager = new ElevationManager(projection);

        Config config = new Config();
        config.load();

        //Basic surface config, to be edited later.
        land = new TernarySurfaceConfig(grassBlock, dirtBlock, stoneBlock);

        //Seabed surface
        seabed = new TernarySurfaceConfig(dirtBlock, dirtBlock, stoneBlock);

        //Good road
        goodRoad = new TernarySurfaceConfig(goodRoadBlock, dirtBlock, stoneBlock);

        //Normal road
        normalRoad = new TernarySurfaceConfig(normalRoadBlock, dirtBlock, stoneBlock);

        //Worn road
        wornRoad = new TernarySurfaceConfig(wornRoadBlock, dirtBlock, stoneBlock);

        //Footway
        footway = new TernarySurfaceConfig(footwayBlock, dirtBlock, stoneBlock);

        //Building surface
        building = new TernarySurfaceConfig(buildingBlock, dirtBlock, stoneBlock);

        //Create surfaceBuilder, which is where the blocks are actually generated.
        surfaceBuilder = new EarthSurfaceBuilder(land.CODEC);

    }

    private static StrongholdConfig StrongholdConfigSetup() {
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
    public void buildSurface(ChunkRegion region, Chunk chunk)
    {
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

        //Used to store the height value fetched from the API call
        int iNullIslandHeight = 0;

        if (isNullIsland(cx, cz)) {
            //For each x of chunk
            for (int i = 0; i < 16; i++)
            {
                //Updates the actual x coordinate
                x = x0 + i;

                //For each z of each x
                for (int j = 0; j < 16; j++) {
                    //Updates the actual z coordinate
                    z = z0 + j;

                    //Generate a block at x,z with the correct height fetched from the api call.
                    surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(x, 1, z), x, z, iNullIslandHeight, 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, land);
                }
            }
            return;
        }

        //All heights done before new thread created
        heights = elevationManager.getHeights(x0, x1, z0, z1);
      //  Thread bigThread = new Thread(() ->
      //  {


                int X0 = x0;
                int X1 = x1;
                int Z0 = z0;
                int Z1 = z1;
                Chunk chunk1 = chunk;

                double[] corner1 = projection.toGeo(X0, Z0);

                //If the chunk is not part of the projection, fill it with water

                boolean bVoid = false; //TESTING

                if (bVoid)
                {
                    BlockUse BU = new BlockUse(UseType.Land);
                    grid = BU.getGrid();
                }
                else if (Double.isNaN(corner1[0]))
                {
                    BlockUse BU = new BlockUse(UseType.Water);
                    grid = BU.getGrid();
                }
                else
                {
                    double[] corner2 = projection.toGeo(X1, Z1);
                    if (Double.isNaN(corner2[0]))
                    {
                        BlockUse BU = new BlockUse(UseType.Water);
                        grid = BU.getGrid();
                    }
                    else
                    {
                        //xMin, zMin, zMax, zMax
                        double[] geoCords = {min(corner1[1], corner2[1]), min(corner1[0], corner2[0]), max(corner1[1], corner2[1]), max(corner1[0], corner2[0])};

                        //Multiply the bbox by 3 on both sides
                        double xRange = geoCords[2] - geoCords[0];
                        geoCords[0] = geoCords[0] - Math.abs(xRange);
                        geoCords[2] = geoCords[2] + Math.abs(xRange);

                        double zRange = geoCords[3] - geoCords[1];
                        geoCords[1] = geoCords[1] - Math.abs(zRange);
                        geoCords[3] = geoCords[3] + Math.abs(zRange);

                        //Creates bounding box for use by the osm fetcher
                        BoundingBox bb = new BoundingBox(geoCords);
                        BlockUse BU = new BlockUse(bb, new int[]{X0 - 16, Z0 - 16}, projection);
                        BU.fillGrid(false);
                        grid = BU.getGrid();
                    }
                }
                buildChunk(X0, Z0, chunk1);

      //  }); //End big thread

     //   bigThread.start();

    //    //Hold up this thread until all the generation is finished, affectively meaning that only one chunk can be done at 1 time
        //This means that api spamming is stopped but the 60 second api lag issue no longer occurs
        //I believe it will also fix the slow rendering issue
    /*    while (bigThread.isAlive())
        {
            try
            {
                //Tested at 70 but is 80 for safety
                Thread.currentThread().sleep(0);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        // where the end comment was
        }

     */
    } //End build surface

    private void buildChunk(int x0, int z0, Chunk chunk)
    {
        //Store as local variables
        int X, Z;
        int[][] heights = this.heights;

        //For each x of chunk
        for (int i = 0; i < 16; i++) {
            //Updates the actual x coordinate
            X = x0 + i;

            //For each z of each x
            for (int j = 0; j < 16; j++) {
                //Updates the actual z coordinate
                Z = z0 + j;

                //Generate a block at X,z with the correct height fetched from the api call.

                if (heights[i][j] == -30) //Default value
                {
                    surfaceBuilder.generateWater(random, chunk, biomeSource.getBiomeForNoiseGen(X, 1, Z), X, Z, -30, 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, seabed);
                    continue;
                }
                switch (grid[i+16][j+16])
                {
                    case Land:
                    case Beach:
                        surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(X, 1, Z), X, Z, heights[i][j], 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, land);
                        break;

                    case Motorway:
                    case MotorwayDerived:
                    case Primary:
                    case PrimaryDerived:
                        surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(X, 1, Z), X, Z, heights[i][j], 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, goodRoad);
                        break;

                    case Secondary:
                    case SecondaryDerived:
                        surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(X, 1, Z), X, Z, heights[i][j], 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, normalRoad);
                        break;

                    case Tertiary:
                    case TertiaryDerived:
                    case Track:
                    case TrackDerived:
                        surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(X, 1, Z), X, Z, heights[i][j], 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, wornRoad);
                        break;

                    case Footway:
                    case FootwayDerived:
                        surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(X, 1, Z), X, Z, heights[i][j], 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, footway);
                        break;

                    case BuildingOutline:
                        surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(X, 1, Z), X, Z, heights[i][j], 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, building);
                        break;

                    case Water:
                        surfaceBuilder.generateWater(random, chunk, biomeSource.getBiomeForNoiseGen(X, 1, Z), X, Z, heights[i][j], 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, seabed);
                        break;
                }
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

    public static boolean isNullIsland(int chunkX, int chunkZ) {
        return max(chunkX ^ (chunkX >> 31), chunkZ ^ (chunkZ >> 31)) < 3;
    }
}
