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

import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
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
    protected final BlockState roadBlock;
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


    public static final Codec<EarthGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    BiomeSource.CODEC.fieldOf("earth_population_source").forGetter((EarthGenerator) -> EarthGenerator.populationSource),
                    BiomeSource.CODEC.fieldOf("earth_biome_source").forGetter((EarthGenerator) -> EarthGenerator.biomeSource))
            .apply(instance, instance.stable(EarthGenerator::new)));

    public EarthGenerator(BiomeSource populationSource, BiomeSource biomeSource) {
        super(populationSource, biomeSource, new StructuresConfig(Optional.of(StrongholdConfigSetup()), new HashMap<>()), 0);
        //Random based on world seed. Not really applicable to this world type but added nontheless.
        random = new ChunkRandom(0);

        //Set the 4 default blocks.
        grassBlock = Blocks.GRASS_BLOCK.getDefaultState();
        dirtBlock = Blocks.DIRT.getDefaultState();
        stoneBlock = Blocks.STONE.getDefaultState();
        roadBlock = Blocks.GRAY_WOOL.getDefaultState();
        buildingBlock = Blocks.IRON_BLOCK.getDefaultState();

        //Default fluid set to water.
        defaultFluid = Blocks.WATER.getDefaultState();
        this.populationSource = populationSource;
        this.biomeSource = biomeSource;

        this.projection = new ModifiedAirocean();

        elevationManager = new ElevationManager(projection);

        Config config = new Config();
        config.load();

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
    public void buildSurface(ChunkRegion region, Chunk chunk){

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
        TernarySurfaceConfig land = new TernarySurfaceConfig(grassBlock, dirtBlock, stoneBlock);

        //Seabed surface
        TernarySurfaceConfig seabed = new TernarySurfaceConfig(dirtBlock, dirtBlock, stoneBlock);

        //Road surface
        TernarySurfaceConfig road = new TernarySurfaceConfig(roadBlock, dirtBlock, stoneBlock);

        //Building surface
        TernarySurfaceConfig building = new TernarySurfaceConfig(buildingBlock, dirtBlock, stoneBlock);

        //Create surfaceBuilder, which is where the blocks are actually generated.
        EarthSurfaceBuilder surfaceBuilder = new EarthSurfaceBuilder(land.CODEC);

        //Used to store the height value fetched from the API call
        int iNullIslandHeight = 0;

        if (isNullIsland(cx, cz)) {
            //For each x of chunk
            for (int i = 0; i < 16; i++) {
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

        double[] corner1 = projection.toGeo(x0,z0);

        UseType[][] grid;

        //If the chunk is not part of the projection, fill it with water

        boolean bVoid = true; //TESTING

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
            double[] corner2 = projection.toGeo(x1,z1);
            if (Double.isNaN(corner2[0]))
            {
                BlockUse BU = new BlockUse(UseType.Water);
                grid = BU.getGrid();
            }
            else
            {
                //xMin, zMin, zMax, zMax
                double[] geoCords = {Math.min(corner1[1], corner2[1]), Math.min(corner1[0], corner2[0]), Math.max(corner1[1], corner2[1]), Math.max(corner1[0], corner2[0])};

                //Multiply the bbox by 3 on both sides
                double xRange = geoCords[2]-geoCords[0];
                geoCords[0] = geoCords[0] - xRange;
                geoCords[2] = geoCords[2] + xRange;

                double zRange = geoCords[3]-geoCords[1];
                geoCords[1] = geoCords[1] - zRange;
                geoCords[3] = geoCords[3] + zRange;

                //Creates bounding box for use by the osm fetcher
                BoundingBox bb = new BoundingBox(geoCords);
                BlockUse BU = new BlockUse(bb, new int[]{x0, z0}, projection);
                BU.fillGrid();
                grid = BU.getGrid();
            }
        }

        //  Tile[] tiles = new Tile[positions.length];

    /*    for (int i = 0 ; i < positions.length ; i++)
        {
            System.out.println();
          //  tiles[i] = new Tile(positions[i].x, positions[i].z);
            System.out.println(positions[i].x +"/"+positions[i].z);
       //     tiles[i].getInfo();
        }
*/
        heights = elevationManager.getHeights(x0, x1, z0, z1);

        //For each x of chunk
        for (int i = 0; i < 16; i++) {
            //Updates the actual x coordinate
            x = x0 + i;

            //For each z of each x
            for (int j = 0; j < 16; j++) {
                //Updates the actual z coordinate
                z = z0 + j;

                //Generate a block at x,z with the correct height fetched from the api call.

                if (heights[i][j] == -30) //Default value
                {
                    surfaceBuilder.generateWater(random, chunk, biomeSource.getBiomeForNoiseGen(x, 1, z), x, z, -30, 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, seabed);
                    continue;
                }
                switch (grid[i+16][j+16])
                {
                    case Land:
                    case Beach:
                        surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(x, 1, z), x, z, heights[i][j], 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, land);
                        break;

                    case Road:
                    case RoadDerived:
                        surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(x, 1, z), x, z, heights[i][j], 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, road);
                        break;

                    case BuildingOutline:
                        surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(x, 1, z), x, z, heights[i][j], 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, building);
                        break;

                    case Water:
                        surfaceBuilder.generateWater(random, chunk, biomeSource.getBiomeForNoiseGen(x, 1, z), x, z, heights[i][j], 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, seabed);
                        break;
                }

            }
        }

        /*
        //Test all 4 corners of chunk. If they lie in the same tile, standardise tile.

        //Stores whether or not the height data can be received all from 1 tile
        boolean bAllInSameTile = true;

        //Gets the tile for each corner of the chunk
        int[] Corner1 = BlockAPICall.getTile(x0, z0);
        int[] Corner3 = BlockAPICall.getTile(x1, z1);

        //If two opposite corners aren't in the same tile, declare a difference
        if (Corner1[0] != Corner3[0] || Corner1[1] != Corner3[1]) {
            bAllInSameTile = false;
        } else //If two of them are in the same tile, it doesn't confirm the whole chunk is in the correct tile.
        {
            int[] Corner2 = BlockAPICall.getTile(x0, z1);
            int[] Corner4 = BlockAPICall.getTile(x1, z0);

            if (Corner2[0] != Corner4[0] || Corner2[1] != Corner4[1]) {
                bAllInSameTile = false;
            }
        }

        BlockAPICall ourTile = null;

        //Downloads the required tile if they are all the same
        if (bAllInSameTile) {
            ourTile = new BlockAPICall(Corner1[0], Corner1[1], 15, x0, z0);
            ourTile.loadPicture();
        } else {
            ourTile = new BlockAPICall(15, x0, z0);
        }

        //For each x of chunk
        for (int i = 0; i < 16; i++) {
            //Updates the actual x coordinate
            x = x0 + i;

            //For each z of each x
            for (int j = 0; j < 16; j++) {
                //Updates the actual z coordinate
                z = z0 + j;

                //Gets the height of a particular block
                if (bAllInSameTile) {
                    iHeight = ourTile.iHeights[i][j];
                } else {
                    iHeight = ourTile.getTileAndHeightForXZ(x, z);
                }

                //Generate a block at x,z with the correct height fetched from the api call.
                surfaceBuilder.generate(random, chunk, biomeSource.getBiomeForNoiseGen(x, 1, z), x, z, iHeight, 0.0, stoneBlock, defaultFluid, ConfigVariables.seaLevel, 0, 0, config);
            }
        }

         */
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
