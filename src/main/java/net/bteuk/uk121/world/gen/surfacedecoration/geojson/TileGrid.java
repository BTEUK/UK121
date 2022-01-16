package net.bteuk.uk121.world.gen.surfacedecoration.geojson;

import net.bteuk.uk121.world.gen.surfacedecoration.BoundingBox;
import net.bteuk.uk121.world.gen.surfacedecoration.overpassapi.Node;
import net.bteuk.uk121.world.gen.surfacedecoration.overpassapi.Tag;
import net.bteuk.uk121.world.gen.surfacedecoration.overpassapi.Way;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TileGrid
{
    private Tile[] tileGrid;

    public static void main(String[] args)
    {
      //  TileGrid grid = new TileGrid(51.438209316908136, 0.38381567219754);
    }

    public TileGrid(BoundingBox bbox)
    {
        int negXTile = (int) (bbox.minX()*64);
        int negYTile = (int) (bbox.minZ()*64);
        int posXTile = (int) (bbox.maxX()*64);
        int posYTile = (int) (bbox.maxZ()*64);

        reset(negXTile, negYTile, posXTile, posYTile);
    }

    public TileGrid(int x, int z)
    {
        reset(x , z, x, z);
    }

    private void reset(int negXTile, int negYTile, int posXTile, int posYTile)
    {
        //Initiates every tile which needs to be got
        int iCount = 0;
        int i, j;


        if (negXTile != posXTile)
        {
            if (negYTile != posYTile)
            {
                tileGrid = new Tile[4];

                tileGrid[0] = new Tile(negXTile, negYTile);
                tileGrid[1] = new Tile(negXTile+1, negYTile);
                tileGrid[2] = new Tile(negXTile, negYTile+1);
                tileGrid[3] = new Tile(negXTile+1, negYTile+1);
            }
            else
            {
                tileGrid = new Tile[2];

                tileGrid[0] = new Tile(negXTile, negYTile);
                tileGrid[1] = new Tile(negXTile+1, negYTile);
            }
        }
        else
        {
            if (negYTile != posYTile)
            {
                tileGrid = new Tile[2];

                tileGrid[0] = new Tile(negXTile, negYTile);
                tileGrid[1] = new Tile(negXTile, negYTile+1);
            }
            else
            {
                tileGrid = new Tile[1];
                tileGrid[0] = new Tile(negXTile, negYTile);
            }
        }
    }

    public void getInfo()
    {
        int iCount;

        Calendar cal;
        Date time;

        for (iCount = 0 ; iCount < tileGrid.length ; iCount++)
        {
            tileGrid[iCount].getInfo(iCount);
        }
    }

    public ArrayList<Way> readInfoToWays()
    {
        ArrayList<Way> ways = new ArrayList<>();
        int iCount;
        int iInfos;

        Way way;
        Node node;
        Tag tag;

        for (iCount = 0 ; iCount < tileGrid.length ; iCount++)
        {
            for (iInfos = 0 ; iInfos < tileGrid[iCount].infos.length ; iInfos++)
            {
                if (tileGrid[iCount].infos[iInfos].type.equals("Feature"))
                {
                    //Stores the reference
                  //  long ref = sanatise(tileGrid[iCount].infos[iInfos].id);

                    //Road, path, building
                    if (tileGrid[iCount].infos[iInfos].geometry.type.equals("LineString") || tileGrid[iCount].infos[iInfos].geometry.type.equals("Polygon"))
                    {
                        //Creates way
                        way = new Way();
                        way.setId(tileGrid[iCount].infos[iInfos].id);

                        //Adds nodes to way
                        for (int i = 0; i < tileGrid[iCount].infos[iInfos].geometry.coordinates[0].length; i++)
                        {
                            //All in form lat, long
                            node = new Node(0, tileGrid[iCount].infos[iInfos].geometry.coordinates[0][i][1], tileGrid[iCount].infos[iInfos].geometry.coordinates[0][i][0]);
                            way.getNodes().add(node);
                        }

                        if (tileGrid[iCount].infos[iInfos].properties == null)
                            tileGrid[iCount].infos[iInfos].properties = new String[0][0];

                        int iProperties = tileGrid[iCount].infos[iInfos].properties.length;

                        //Adds the tags to the way
                        for (int i = 0; i < iProperties ; i++)
                        {
                            tag = new Tag(tileGrid[iCount].infos[iInfos].properties[i][0], tileGrid[iCount].infos[iInfos].properties[i][1]);
                            way.getTags().add(tag);
                        }
                        ways.add(way);
                    } //Ends if road, path or building
                } //End if type is a feature
            } //For every piece of data in the tile
        } //For every tile

        return ways;
    }

    private long sanatise(String dirty)
    {
        String clean;
        int index = dirty.indexOf('/');

        clean = dirty.substring(index+1);

        return Long.parseLong(clean);
    }
}

