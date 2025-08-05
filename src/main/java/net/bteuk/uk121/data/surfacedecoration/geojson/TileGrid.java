package net.bteuk.uk121.data.surfacedecoration.geojson;

import net.bteuk.uk121.util.geometry.BoundingBox;
import net.bteuk.uk121.data.surfacedecoration.overpassapi.Node;
import net.bteuk.uk121.data.surfacedecoration.overpassapi.Tag;
import net.bteuk.uk121.data.surfacedecoration.overpassapi.Way;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

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
            for (iInfos = 0 ; iInfos < tileGrid[iCount].infos.size() ; iInfos++)
            {
                if (tileGrid[iCount].infos.get(iInfos).type.equals("Feature"))
                {
                    //Stores the reference
                  //  long ref = sanatise(tileGrid[iCount].infos[iInfos].id);


                    //Road, path, building
                    String geometryType = tileGrid[iCount].infos.get(iInfos).geometry.type;
                    boolean bLineString = geometryType.equals("LineString");
                    boolean bPolygon = false;

                    if (!bLineString)
                        bPolygon = geometryType.equals("Polygon");

                    if (bLineString || bPolygon)
                    {
                        //Creates way
                        way = new Way();
                        way.setId(tileGrid[iCount].infos.get(iInfos).id);

                        double[][] coords;
                        coords = bLineString ? tileGrid[iCount].infos.get(iInfos).geometry.lineStringCoordinates :
                                tileGrid[iCount].infos.get(iInfos).geometry.polygonCoordinates.getFirst();

                        //Adds nodes to way
                        for (int i = 0; i < coords.length; i++)
                        {
                            //All in form lat, long
                            node = new Node(0, coords[i][1], coords[i][0]);
                            way.getNodes().add(node);
                        }

                        if (tileGrid[iCount].infos.get(iInfos).properties == null)
                            tileGrid[iCount].infos.get(iInfos).properties = new HashMap<>(0);

                        //Adds the tags to the way
                        for (HashMap.Entry<String, String> property : tileGrid[iCount].infos.get(iInfos).properties.entrySet())
                        {
                            tag = new Tag(property.getKey(), property.getValue());
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

