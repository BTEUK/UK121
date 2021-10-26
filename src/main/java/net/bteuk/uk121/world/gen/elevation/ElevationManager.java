package net.bteuk.uk121.world.gen.elevation;

import net.bteuk.uk121.UK121;
import net.bteuk.uk121.world.gen.Projections.ModifiedAirocean;

import java.util.ArrayList;
import java.util.HashSet;

import static java.lang.Math.*;

public class ElevationManager {

    public ArrayList<ElevationTile> tiles;
    public ModifiedAirocean projection;
    public HashSet<ElevationTile> usedTiles;

    int[] tile1, tile2, tile3, tile4;
    double[] coord1, coord2, coord3, coord4;

    double lon, lat, steplon, steplat, rowlon, rowlat;

    int[][] heights;

    public ElevationManager() {

        tiles = new ArrayList<>();
        projection = new ModifiedAirocean();
        usedTiles = new HashSet<>();

    }

    public ElevationTile get(String tileName) {
        //If the elevationTile has already been downloaded return the tile.
        for (ElevationTile tile : tiles) {
            if (tile.name.equals(tileName)) {
                tile.accessed = true;
                return tile;
            }
        }
        //If not then return null;
        return null;
    }

    public boolean contains(String tileName) {
        //If the elevationTile has already been downloaded return true;
        for (ElevationTile tile : tiles) {
            if (tile.name.equals(tileName)) {
                return true;
            }
        }
        //If not then return false;
        return false;
    }

    public int[] getTile(final double lon, final double lat, final int zoom) {
        int xtile = (int)Math.floor( (lon + 180) / 360 * (1<<zoom) ) ;
        int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
        if (xtile < 0)
            xtile=0;
        if (xtile >= (1<<zoom))
            xtile=((1<<zoom)-1);
        if (ytile < 0)
            ytile=0;
        if (ytile >= (1<<zoom))
            ytile=((1<<zoom)-1);
        return(new int[]{xtile, ytile});
    }

    public int[][] getHeights(int xmin, int xmax, int zmin, int zmax) {

        //Zoom of the tiles, this will become a variable as other datasets are supported.
        int zoom = 15;

        //Get the min and max coordinates.
        coord1 = projection.toGeo(xmin+0.5,zmin+0.5);
        coord2 = projection.toGeo(xmax+0.5,zmax+0.5);

        //Get the coordinates of the other two coordinates to allow for stepsizes in the lat and lon direction for iteration.
        coord3 = projection.toGeo(xmin+0.5,zmax+0.5);
        coord4 = projection.toGeo(xmax+0.5,zmin+0.5);

        //Calculate the stepsize to iterate over lon/lat at an angle.
        steplon = (coord3[0] - coord1[0])/16;
        steplat = (coord3[1] - coord1[1])/16;
        rowlon = (coord4[0] - coord1[0])/16;
        rowlat = (coord4[1] - coord1[1])/16;

        //Get the tiles of the 4 extreme points.
        tile1 = getTile(coord1[0], coord1[1], zoom);
        tile2 = getTile(coord2[0], coord2[1], zoom);
        tile3 = getTile(coord3[0], coord3[1], zoom);
        tile4 = getTile(coord4[0], coord4[1], zoom);

        //Clear the list of tiles that will be used to get heights.
        usedTiles.clear();

        //Load the elevation tiles that are needed.
        //Since usedTiles is a set no duplicate entries can be added.
        usedTiles.add(loadTile(tile1, zoom));
        usedTiles.add(loadTile(tile2, zoom));
        usedTiles.add(loadTile(tile3, zoom));
        usedTiles.add(loadTile(tile4, zoom));

        //Create a new array of heights to be stored.
        heights = new int[16][16];

        //Get initial lon/lat
        lon = coord1[0];
        lat = coord1[1];

        //Iterate over each block in the chunk and increment the lon/lat accordingly.
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j<16; j++) {
                for (ElevationTile elevationTile : usedTiles) {
                    if (heights[i][j] == 0) {
                        heights[i][j] = elevationTile.getHeight(lon, lat);
                    }
                }
                lon += steplon;
                lat += steplat;
            }
            lon += (rowlon - (16 * steplon));
            lat += (rowlat - (16 * steplat));
        }

        return heights;
    }

    public ElevationTile loadTile(int[] tile, int zoom) {

        if (!(contains(zoom+"-"+tile[0]+"-"+tile[1]))) {
            ElevationTile elevationTile = new ElevationTile(zoom+"-"+tile[0]+"-"+tile[1], tile[0], tile[1], zoom);
            tiles.add(elevationTile);
            return (elevationTile);
        } else {
            return (get(zoom+"-"+tile[0]+"-"+tile[1]));
        }

    }



}
