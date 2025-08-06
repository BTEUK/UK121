package net.bteuk.uk121.data.surfacedecoration;

import net.bteuk.uk121.mod.TerraConstants;
import net.bteuk.uk121.util.geometry.BlockVector3;
import net.bteuk.uk121.util.geometry.BoundingBox;
import net.bteuk.uk121.util.geometry.Line;
import net.bteuk.uk121.Projections.ModifiedAirocean;
import net.bteuk.uk121.data.surfacedecoration.geojson.TileGrid;
import net.bteuk.uk121.util.geometry.Point;
import net.bteuk.uk121.data.surfacedecoration.overpassapi.Node;
import net.bteuk.uk121.data.surfacedecoration.overpassapi.Object;
import net.bteuk.uk121.data.surfacedecoration.overpassapi.Tag;
import net.bteuk.uk121.data.surfacedecoration.overpassapi.Way;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Identifies what each block in a 16x16 chunk represents
 */
public class BlockUse
{
    public UseType[][] grid;
    private ArrayList<Object> objects;
    private ArrayList<Way> ways;
    private BoundingBox bbox;
    private int[] blockMins;
    private ModifiedAirocean projection;

    public BlockUse(BoundingBox bbox, int[] blockMins, ModifiedAirocean projection)
    {
        grid = new UseType[48][48];
    //    objects = new ArrayList<Object>();
        ways = new ArrayList<Way>();
        this.bbox = bbox;
        this.blockMins = blockMins;
        this.projection = projection;
    }

    public BlockUse(UseType useType)
    {
        grid = new UseType[48][48];
        clearVoid(useType);
    }

    public static void main(String[] args)
    {
        ModifiedAirocean projection = new ModifiedAirocean();

        ChunkPos chunkPos = new ChunkPos(173696, -338396);

        int x0 = chunkPos.getStartX();
        int z0 = chunkPos.getStartZ();
        int x1 = chunkPos.getEndX();
        int z1 = chunkPos.getEndZ();

        int X0 = x0;
        int X1 = x1;
        int Z0 = z0;
        int Z1 = z1;

        double[] corner1 = projection.toGeo(X0, Z0);
        double[] corner2 = projection.toGeo(X1, Z1);

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

//        BoundingBox bbox = new BoundingBox(51.43757, 51.43829, 0.38353, 0.38454);
//        int[] blockMins = {0, 0};
//        BlockUse BU = new BlockUse(bbox , blockMins, TerraConstants.projection);
//
        BU.fillGrid(false);

     //   BU.display();
    }

    public UseType[][] getGrid()
    {
        return grid;
    }

    public void fillGrid(boolean bAlternative)
    {
    //    Calendar cal = Calendar.getInstance();
     //   Date time = cal.getTime();
    //    long lTime1 = time.getTime();

 //       ways = GetOSM.entry(bbox, bAlternative);

        Calendar cal = Calendar.getInstance();
        Date time = cal.getTime();
        long lTimeGetTileInfo1 = time.getTime();

        TileGrid tileGrid = new TileGrid(bbox);
        tileGrid.getInfo();

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeGetTileInfo2 = time.getTime();

        System.out.println("1.1.1 Time to get info from Json files: "+(lTimeGetTileInfo2-lTimeGetTileInfo1)+" ms");


        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeGetData1 = time.getTime();

        ways = tileGrid.readInfoToWays();

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeGetData2 = time.getTime();

        System.out.println("1.1.2 Time to read info into ways: "+(lTimeGetData2-lTimeGetData1)+" ms");

        //   cal = Calendar.getInstance();
    //    time = cal.getTime();
     //   long lTime2 = time.getTime();

    /*    if (bAlternative)
            System.out.println("Got "+ways.size() +" ways, alt api: " + (lTime2 - lTime1) + "ms");
        else
            System.out.println("Got "+ways.size() +" ways: " + (lTime2 - lTime1) + "ms");

     */

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeToInterpretData1 = time.getTime();

        int i;
        final int iWays = ways.size();
        //Goes through each "way" in the data
        for (i = 0 ; i < iWays ; i++)
        {
            //Imports the tags and nodes of the way
            Way way = ways.get(i);
            ArrayList<Tag> tags = way.getTags();
            ArrayList<Node> nodes = way.getNodes();

        //    System.out.println("ID: "+way.getId());

            UseType useType = UseType.Land;

            boolean bHighway = false;

            boolean bUseGathered = false;

            //Goes through each tag, looking for features which need to be generated
            for (Tag tag: tags)
            {
                //Checks tag keys
                switch (tag.key)
                {
                    case "highway":
                        bHighway = true;

                        switch (tag.value)
                        {
                            case "motorway":
                                useType = UseType.Motorway;
                                break;
                            case "trunk":
                            case "primary":
                                useType = UseType.Primary;
                                break;
                            case "secondary":
                                useType = UseType.Secondary;
                                break;
                            case "track":
                                useType = UseType.Track;
                                break;
                            case "footway":
                            case "cycleway":
                            case "bridleway":
                            case "path":
                            case "steps":
                                useType = UseType.Footway;
                                break;
                            case "tertiary":
                            default:
                                useType = UseType.Tertiary;
                                break;
                        }
                        bUseGathered = true;
                        break;
                    case "building":
                        useType = UseType.BuildingOutline;
                        break;

                    case "water":
                        useType = UseType.Water;
                    case "natural":
                        if (tag.value == "coastline")
                        {
                            useType = UseType.Water;
                        }
                        break;

                    case "railway":
                        useType = UseType.Railway;
                        break;
                }

                if (bUseGathered)
                    break;
            }

            //Deal with building, highway or water polygon
            if (bHighway || useType == UseType.BuildingOutline || useType == UseType.Water || useType == UseType.Railway)
            {
                final int iNodes = nodes.size();
                //Stores the block coordinates of each of the nodes
                int[][] iNodeBlocks = new int[iNodes][2];
                int iCount = 0;

                Point[] Polygon = new Point[iNodes];

                //Goes through each node and adds it to the node blocks array
                for (Node node : nodes)
                {
                    double[] MCcoords = projection.fromGeo(node.longitude, node.latitude);

                    iNodeBlocks[iCount][0] = (int) (MCcoords[0] - blockMins[0])/1;
                    iNodeBlocks[iCount][1] = (int) (MCcoords[1] - blockMins[1])/1;
                  //        System.out.println("The blocks of the downloaded node:");
                  //         System.out.println(iNodeBlocks[iCount][1]);
                    if (iNodeBlocks[iCount][0] >= 0 && iNodeBlocks[iCount][0] < 48 && iNodeBlocks[iCount][1]>= 0 && iNodeBlocks[iCount][1] < 48)
                    {
                        grid[iNodeBlocks[iCount][0]][iNodeBlocks[iCount][1]] = useType;
                    }

                    if (useType == UseType.Water && iCount < iNodes-1)
                    {
                        Point point = new Point((int) (MCcoords[0] - blockMins[0])/1, (int) (MCcoords[1] - blockMins[1])/1);
                        Polygon[iCount] = point;
                    }
                    iCount++;
                }

                //Go through each node
            /*    for (int j = 0 ; j < iCount - 1 ; j++)
                {
                    nextNode(iNodeBlocks, j, iCount, 0, 0, useType);
                }
            */
                //Go through each node
                Line line = new Line();
                for (int j = 1 ; j < iCount ; j++)
                {
                    ArrayList<BlockVector3> vset =  line.drawLine(iNodeBlocks[j - 1][0], iNodeBlocks[j - 1][1], iNodeBlocks[j][0], iNodeBlocks[j][1]);
                    final int iSetSize = vset.size();

                    for (int k = 0 ; k < iSetSize ; k++)
                    {
                        int iBestBlockX = vset.get(k).getBlockX();
                        int iBestBlockZ = vset.get(k).getBlockZ();

                        if (iBestBlockX >= 0 && iBestBlockX < 48 && iBestBlockZ>= 0 && iBestBlockZ < 48)
                        {
                            grid[iBestBlockX][iBestBlockZ] = useType;
                        }
                    }
                }

                if (useType == UseType.Water)
                {
                    //Creates pool of water from polygon
                    for (int k = 16; k < 32; k++)
                    {
                        for (int l = 16; l < 32; l++)
                        {
                            Point p = new Point(k, l);
                            if (Point.isInside(Polygon, iNodes-1, p))
                            {
                                grid[k][l] = UseType.Water;
                            }
                        }
                    }
                }
            }
        }
        //Fills the rest of the grid with land
        clearRemaining();

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeToInterpretData2 = time.getTime();

        System.out.println("1.1.3 Time to plot data: "+(lTimeToInterpretData2-lTimeToInterpretData1)+" ms");
    }

    private void nextNode(int[][] iNodeBlocks, int j, int iCount, int xOffset, int zOffset, UseType useType)
    {
        int iDistanceToNext;
        int iXComp, iZComp;

        int iShortestDistanceToNext = 999999999;
        int iBestBlockX = iNodeBlocks[j][0];
        int iBestBlockZ = iNodeBlocks[j][1];

        boolean bEnd = false;

        int xNewOffset = 0;
        int zNewOffset = 0;

        //Test every block around that node to test the closest block to the next node
        for (int x = xOffset - 1 ; x <= xOffset + 1 ; x++)
        {
            for (int z = zOffset - 1 ; z <= zOffset + 1 ; z++)
            {
                //Skip the node itself
                if (x == 0 && z == 0)
                    continue;

                //If this neighbouring block is the block of node 2, then that's it, we need to do no more
                if (iNodeBlocks[j][0]+x == iNodeBlocks[(j+1)][0] && iNodeBlocks[j][1]+z == iNodeBlocks[(j+1)][1])
                {
                    bEnd = true; //This is where the recursion ends. Once it reaches the next block
                    break;
                }

                iXComp = (iNodeBlocks[j][0]+x)-(iNodeBlocks[(j+1)][0]);
                iZComp = (iNodeBlocks[j][1]+z)-(iNodeBlocks[(j+1)][1]);
                iDistanceToNext = iXComp*iXComp + iZComp*iZComp;

                if (iDistanceToNext < iShortestDistanceToNext)
                {
                    iShortestDistanceToNext = iDistanceToNext;
                    //Best block in chunk
                    iBestBlockX = (iNodeBlocks[j][0]+x);
                    iBestBlockZ = (iNodeBlocks[j][1]+z);

                    //Offset from the node
                    xNewOffset = x;
                    zNewOffset = z;
                }
            }
            if (bEnd)
                break;
        }

        if (bEnd)
        {
            return;
        }
        else
        {
            if (iBestBlockX >= 0 && iBestBlockX < 48 && iBestBlockZ>= 0 && iBestBlockZ < 48)
            {
                grid[iBestBlockX][iBestBlockZ] = useType;
            }

            //Always then goes to the next node
            nextNode(iNodeBlocks, j, iCount, xNewOffset, zNewOffset, useType);

            //  else return;
        }
    }

    private void clearRemaining()
    {
        int i, j, k, l;

        float fThickness;

        for (i = 0 ; i < 48 ; i++)
        {
            for (j = 0 ; j < 48 ; j++)
            {
                if (grid[i][j] == null)
                {
                    grid[i][j] = UseType.Land;
                }

                switch (grid[i][j])
                {
                    case Motorway:
                        fThickness = 9;
                        for (k = -10 ; k < 10 ; k++)
                        {
                            for (l = -10; l < 10 ; l++)
                            {
                                if (l == 0 && k == 0)
                                    continue;
                                //If a block is 4 blocks distance from a road node, set its value to road derived
                                if ((i+k)<48 && (j+l)<48 && (i+k)>=0 && (j+l)>=0 && (k*k + l*l) < fThickness*fThickness)
                                {
                                    if (grid[i+k][j+l] != UseType.Motorway)
                                    {
                                        grid[i+k][j+l] = UseType.MotorwayDerived;
                                    }
                                }
                            }
                        }
                        break;

                    case Primary:
                        fThickness = 7;
                        for (k = -8 ; k < 8 ; k++)
                        {
                            for (l = -8; l < 8 ; l++)
                            {
                                if (l == 0 && k == 0)
                                    continue;
                                //If a block is 4 blocks distance from a road node, set its value to road derived
                                if ((i+k)<48 && (j+l)<48 && (i+k)>=0 && (j+l)>=0 && (k*k + l*l) < fThickness*fThickness)
                                {
                                    if (grid[i+k][j+l] != UseType.Primary)
                                    {
                                        grid[i+k][j+l] = UseType.PrimaryDerived;
                                    }
                                }
                            }
                        }
                        break;

                    case Secondary:
                        fThickness = 6;
                        for (k = -7 ; k < 7 ; k++)
                        {
                            for (l = -7; l < 7 ; l++)
                            {
                                if (l == 0 && k == 0)
                                    continue;
                                //If a block is 4 blocks distance from a road node, set its value to road derived
                                if ((i+k)<48 && (j+l)<48 && (i+k)>=0 && (j+l)>=0 && (k*k + l*l) < fThickness*fThickness)
                                {
                                    if (grid[i+k][j+l] != UseType.Secondary)
                                    {
                                        grid[i+k][j+l] = UseType.SecondaryDerived;
                                    }
                                }
                            }
                        }
                        break;

                    case Tertiary:
                        fThickness = 4;
                        for (k = -5 ; k < 5 ; k++)
                        {
                            for (l = -5; l < 5 ; l++)
                            {
                                if (l == 0 && k == 0)
                                    continue;
                                //If a block is 4 blocks distance from a road node, set its value to road derived
                                if ((i+k)<48 && (j+l)<48 && (i+k)>=0 && (j+l)>=0 && (k*k + l*l) < fThickness*fThickness)
                                {
                                    if (grid[i+k][j+l] != UseType.Tertiary)
                                    {
                                        grid[i+k][j+l] = UseType.TertiaryDerived;
                                    }
                                }
                            }
                        }
                        break;

                    case Footway:
                        fThickness = 1;
                        for (k = -2 ; k < 2 ; k++)
                        {
                            for (l = -2; l < 2 ; l++)
                            {
                                if (l == 0 && k == 0)
                                    continue;
                                //If a block is 4 blocks distance from a road node, set its value to road derived
                                if ((i+k)<48 && (j+l)<48 && (i+k)>=0 && (j+l)>=0 && (k*k + l*l) < fThickness*fThickness)
                                {
                                    if (grid[i+k][j+l] != UseType.Footway)
                                    {
                                        grid[i+k][j+l] = UseType.FootwayDerived;
                                    }
                                }
                            }
                        }
                        break;

                    case Track:
                        fThickness = 3;
                        for (k = -4 ; k < 4 ; k++)
                        {
                            for (l = -4; l < 4 ; l++)
                            {
                                if (l == 0 && k == 0)
                                    continue;
                                //If a block is 4 blocks distance from a road node, set its value to road derived
                                if ((i+k)<48 && (j+l)<48 && (i+k)>=0 && (j+l)>=0 && (k*k + l*l) < fThickness*fThickness)
                                {
                                    if (grid[i+k][j+l] != UseType.Track)
                                    {
                                        grid[i+k][j+l] = UseType.TrackDerived;
                                    }
                                }
                            }
                        }
                        break;
                    case Railway:
                        //Get direction of railway in degrees
                        int iDirection = 39;

                        if (i > 33 || i < 15 || j > 33 || j < 15)
                        {
                            break;
                        }

                        if (iDirection <= 90 || iDirection > 270)
                        {
                            grid[i+1][j] = UseType.xAlignedRail;
                            grid[i-1][j] = UseType.xAlignedRail;
                        }
                        else
                        {
                            grid[i][j+1] = UseType.yAlignedRail;
                            grid[i][j-1] = UseType.yAlignedRail;
                        }

                        if ((i+j)%2 == 0)
                        {
                            grid[i][j] = UseType.railwaySleeper;
                        }
                        break;
                }
            }
        }
    }

    private void clearVoid(UseType useType)
    {
        int i, j;
        for (i = 0 ; i < 48 ; i++)
        {
            for (j = 0 ; j < 48 ; j++)
            {
                grid[i][j] = useType;
            }
        }
    }

}
