package net.bteuk.uk121.world.gen.surfacedecoration;

/**
 * Identifies what each block in a 16x16 chunk represents
 */

import net.bteuk.uk121.TerraConstants;
import net.bteuk.uk121.world.gen.Projections.ModifiedAirocean;
import net.bteuk.uk121.world.gen.surfacedecoration.overpassapi.*;
import net.bteuk.uk121.world.gen.surfacedecoration.overpassapi.Object;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

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
        BoundingBox bbox = new BoundingBox(51.43757, 51.43829, 0.38353, 0.38454);
        int[] blockMins = {0, 0};
        BlockUse BU = new BlockUse(bbox , blockMins, TerraConstants.projection);

        BU.fillGrid(false);

     //   BU.display();
    }

    public UseType[][] getGrid()
    {
        return grid;
    }

    public void display()
    {
        for (int i = 16 ; i < 32 ; i++)
        {
            for (int j = 16 ; j < 32 ; j++)
            {
                if (grid[i][j] == UseType.Land)
                {
                    System.out.print(" L ");
                }
                if (grid[i][j] == UseType.Road)
                    System.out.print(" R ");

            }
            System.out.println();
        }
        System.out.println("------------------------------------------------");
    }

    public void fillGrid(boolean bAlternative)
    {
        //Returning an array list of objects wasn't working

            Calendar cal = Calendar.getInstance();
            Date time = cal.getTime();
            long lTime1 = time.getTime();

            ways = GetOSM.entry(bbox, bAlternative);

            cal = Calendar.getInstance();
            time = cal.getTime();
            long lTime2 = time.getTime();

            if (bAlternative)
                System.out.println("Getting ways, alt api: " + (lTime2 - lTime1) + "ms");
            else
                System.out.println("Getting ways: " + (lTime2 - lTime1) + "ms");


        boolean bHighway;
        int i;

        //Goes through each "way" in the data
        for (i = 0 ; i < ways.size() ; i++)
        {
            //Imports the tags and nodes of the way
            Way way = ways.get(i);
            ArrayList<Tag> tags = way.getTags();
            ArrayList<Node> nodes = way.getNodes();

            UseType useType = UseType.Land;

            for (Tag tag: tags)
            {
          //      System.out.println(tag.key);

                //Checks tag keys for highway and if found, stop searching tags and deal with the way as a road
                if (tag.key.equals("highway"))
                {
                    useType = UseType.Road;
                    break;
                }
                else if (tag.key.equals("building"))
                {
                    useType = UseType.BuildingOutline;
                }
            }

            //Deal with building or highway
            if (useType == UseType.Road || useType == UseType.BuildingOutline)
            {
                //Stores the block coordinates of each of the nodes
                int[][] iNodeBlocks = new int[nodes.size()][2];
                int iCount = 0;

                //Goes through each node and adds it to the node blocks array
                for (Node node : nodes)
                {
                    double[] coords = projection.fromGeo(node.longitude, node.latitude);

                    iNodeBlocks[iCount][0] = (int) Math.round(coords[0] - blockMins[0]);
                    iNodeBlocks[iCount][1] = (int) Math.round(coords[1] - blockMins[1]);
                    //      System.out.println("The blocks of the downloaded node:");
                    //      System.out.println(iNodeBlocks[iCount][0]);
                    //       System.out.println(iNodeBlocks[iCount][1]);
                    iCount++;
                }

                //Go through each node
             /*   for (int j = 0 ; j < iCount - 1 ; j++)
                {
                    nextNode(iNodeBlocks, j, iCount, 0, 0, useType);
                }
              */
                //Go through each node
                Line line = new Line();
                for (int j = 1 ; j < iCount ; j++)
                {
                    ArrayList<BlockVector3> vset =  line.drawLine(iNodeBlocks[j - 1][0], iNodeBlocks[j - 1][1], iNodeBlocks[j][0], iNodeBlocks[j][1]);
                    for (int k = 0 ; k < vset.size() ; k++)
                    {
                        int iBestBlockX = vset.get(k).getBlockX();
                        int iBestBlockZ = vset.get(k).getBlockZ();

                        if (iBestBlockX >= 0 && iBestBlockX < 48 && iBestBlockZ>= 0 && iBestBlockZ < 48)
                        {
                            grid[iBestBlockX][iBestBlockZ] = useType;
                        }
                    }
                }
            }
        }
        //Fills the rest of the grid with land
        clearRemaining();
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
        for (i = 0 ; i < 48 ; i++)
        {
            for (j = 0 ; j < 48 ; j++)
            {
                if (grid[i][j] == null)
                {
                    grid[i][j] = UseType.Land;
                }
                else if (grid[i][j].equals(UseType.Road))
                {
                    for (k = 0 ; k < 5 ; k++)
                    {
                        for (l = 0; l < 5 ; l++)
                        {
                            //If a block is 4 blocks distance from a road node, set its value to road derived
                            if ((i+k)<48 && (j+l)<48 && (k*k + l*l) < 20)
                            {
                                if (grid[i+k][j+l] != UseType.Road)
                                {
                                    grid[i+k][j+l] = UseType.RoadDerived;
                                }
                            }
                        }
                    }
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
