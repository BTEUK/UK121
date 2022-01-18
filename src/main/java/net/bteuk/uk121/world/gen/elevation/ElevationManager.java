package net.bteuk.uk121.world.gen.elevation;

import net.flanagan.interpolation.BiCubicSpline;
import net.bteuk.uk121.UK121;
import net.bteuk.uk121.world.gen.Projections.ModifiedAirocean;

import java.util.ArrayList;
import java.util.HashSet;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ElevationManager {

    public ArrayList<ElevationTile> tiles;
    public ModifiedAirocean projection;
    public HashSet<ElevationTile> usedTiles;

    int[] tile1, tile2, tile3, tile4;
    int[] tile1Z10, tile2Z10, tile3Z10, tile4Z10;
    double[] coord1, coord2, coord3, coord4;

    double lon, lat, steplon, steplat, rowlon, rowlat;
    double lonRange, latRange;

    int[][] heights;

    public ElevationManager(ModifiedAirocean projection) {

        tiles = new ArrayList<>();
        this.projection = projection;
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
        int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        if (xtile < 0)
            xtile = 0;
        if (xtile >= (1 << zoom))
            xtile = ((1 << zoom) - 1);
        if (ytile < 0)
            ytile = 0;
        if (ytile >= (1 << zoom))
            ytile = ((1 << zoom) - 1);
        return (new int[]{xtile, ytile});
    }

    public int[][] getHeights(int xmin, int xmax, int zmin, int zmax) {


        double[] lons = null;
        double[] lats = null;
        double[][] elevs = null;

        int zoom = 15;

        //This will only work with zoom 15 for testing purposes.
        //There will need to be a check to see which zoom is used to determine the number of blocks offset need to be given

        //Get the coordinates for the 4 corners defined by the 2 coordinates.
        coord1 = projection.toGeo(xmin + 0.5, zmin + 0.5);
        coord2 = projection.toGeo(xmax + 0.5, zmax + 0.5);

        coord3 = projection.toGeo(xmin + 0.5, zmax + 0.5);
        coord4 = projection.toGeo(xmax + 0.5, zmin + 0.5);

        //Now get the minimum and maximum coordinates from these 4 corners.
        double[] coordMin = new double[]{
                min(min(coord1[0], coord2[0]), min(coord3[0], coord4[0])),
                min(min(coord1[1], coord2[1]), min(coord3[1], coord4[1])),
        };
        double[] coordMax = new double[]{
                max(max(coord1[0], coord2[0]), max(coord3[0], coord4[0])),
                max(max(coord1[1], coord2[1]), max(coord3[1], coord4[1])),
        };

        //Move the coordMin and coordMax by half the range between the two.
        //This will prevent issues are the corners.
        lonRange = coordMax[0] - coordMin[0];
        latRange = coordMax[1] - coordMin[1];

        double rangeModifier = Math.pow(2, (14 - zoom));

        coordMin[0] -= lonRange * rangeModifier;
        coordMin[1] -= latRange * rangeModifier;
        coordMax[0] += lonRange * rangeModifier;
        coordMax[1] += latRange * rangeModifier;

        //Get the tiles
        usedTiles.clear();
        usedTiles.add(loadTile(getTile(coordMin[0], coordMin[1], zoom), zoom));
        usedTiles.add(loadTile(getTile(coordMin[0], coordMax[1], zoom), zoom));
        usedTiles.add(loadTile(getTile(coordMax[0], coordMin[1], zoom), zoom));
        usedTiles.add(loadTile(getTile(coordMax[0], coordMax[1], zoom), zoom));

        int[] pixel1 = null, pixel2 = null;

        int tileX = 0, tileZ = 0;

        for (ElevationTile eTile : usedTiles) {
            if (eTile.contains(coordMin[0], coordMin[1])) {
                pixel1 = eTile.getPixel(coordMin[0], coordMin[1]);
                tileX = eTile.tileX;
            }
            if (eTile.contains(coordMax[0], coordMax[1])) {
                pixel2 = eTile.getPixel(coordMax[0], coordMax[1]);
                tileZ = eTile.tileZ;
            }
        }

        if (pixel1 == null || pixel2 == null) {
            return new int[16][16];
        }

        int[] pixelMin = new int[]{pixel1[0], pixel2[1]};
        int[] pixelMax = new int[]{pixel2[0], pixel1[1]};

        if (pixelMin[0] == pixelMax[0] || pixelMin[1] == pixelMax[1]) {
            return new int[16][16];
        }

        //Get the size of the arrays used to interpolate on.
        lons = new double[((((pixelMax[0] - pixelMin[0]) % 256) + 256) % 256)];
        lats = new double[((((pixelMax[1] - pixelMin[1]) % 256) + 256) % 256)];
        elevs = new double[lons.length][lats.length];

        if (lons.length < 3 || lats.length < 3) {
            return new int[16][16];
        }

        //UK121.LOGGER.info("Lons: " + lons.length + ", Lats: " + lats.length);
        //UK121.LOGGER.info("X: " + tileX + " Z: " + tileZ);

        //Iterate over all pixels and return the coordinates and height
        int m = pixelMin[0];
        int n = 256;

        for (int i = 0; i < lons.length; i++) {

            if (n < pixelMin[1]) { tileZ--;}
            n = pixelMin[1];

            for (ElevationTile eTile : usedTiles) {
                if (eTile.tileX == tileX && eTile.tileZ == tileZ) {
                    lons[i] = eTile.getLon(m);
                    break;
                }
            }

            for (int j = 0; j < lats.length; j++) {
                //Fill the lats on the fist iteration of i, this prevents repeating it.
                for (ElevationTile eTile : usedTiles) {
                    if (eTile.tileX == tileX && eTile.tileZ == tileZ) {
                        lats[j] = eTile.getLat(n);
                        elevs[i][j] = eTile.getHeight(m, n);
                        if (usedTiles.size() > 1) {
                        //    UK121.LOGGER.info("Coords: " + lons[i] + ", " + lats[j] + ", Elev: " + elevs[i][j] + " Pixel: " + m + ", " + n + " Tile: " + tileX + ", " + tileZ);
                        }
                        if (n == 255) {tileZ++;n = 0;} else {n++;}
                        break;
                    }
                }
            }
            if (m == 255) {tileX++;m = 0;} else {m++;}
        }

        /*
        Create a BiCubicSpline object which is used to get the interpolated elevations for each block in the chunk.
        The input values include the elevations are specific pixel coordinates on the elevation tile(s).
        Additionally at least 1 extra value is added at the edge of each chunk
        to ensure that the coordinate does not lie outside the allowed area.
         */

        if (lons[0] == 0.0 || lats[0] == 0.0) {
            UK121.LOGGER.info("Invalid coordinate 0.");
            return new int[16][16];
        } else if (lons[0] == lons[1] | lats[0] == lats[1]) {
            UK121.LOGGER.info("Invalid coordinates 0=1");
            return new int[16][16];
        } else if (lons[lons.length-2] == lons[lons.length-1] || lats[lats.length-2] == lats[lats.length-1]) {
            UK121.LOGGER.info("Invalid coordinates Max=Max-1");
            return new int[16][16];
        }

        BiCubicSpline bcs = new BiCubicSpline(lons, lats, elevs);

        //Calculate the stepsize to iterate over lon/lat at an angle.
        steplon = (coord3[0] - coord1[0]) / 15;
        steplat = (coord3[1] - coord1[1]) / 15;
        rowlon = (coord4[0] - coord1[0]) / 15;
        rowlat = (coord4[1] - coord1[1]) / 15;

        //Create a new array of heights to be stored.
        heights = new int[16][16];

        //Iterate over each block in the chunk and increment the lon/lat accordingly.
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {

                lon = coord1[0] + steplon * j + rowlon * i;
                lat = coord1[1] + steplat * j + rowlat * i;

                if (lon < lons[0] || lon > lons[lons.length - 1] || lat > lats[0] || lat < lats[lats.length - 1]) {
                    UK121.LOGGER.info("Invalid coordinates, outside range.");
                    return new int[16][16];
                }

                //UK121.LOGGER.info(bcs.interpolate(lon,lat));
                heights[i][j] = (int) Math.round(bcs.interpolate(lon, lat));
            }
        }

        return heights;

/*
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

        //Get the tiles of the 4 extreme points but zoom 10 for ocean
        tile1Z10 = getTile(coord1[0], coord1[1], 10);
        tile2Z10 = getTile(coord2[0], coord2[1], 10);
        tile3Z10 = getTile(coord3[0], coord3[1], 10);
        tile4Z10 = getTile(coord4[0], coord4[1], 10);

        //Clear the list of tiles that will be used to get heights.
        usedTiles.clear();

        //Load the elevation tiles that are needed.
        //Since usedTiles is a set no duplicate entries can be added.
        usedTiles.add(loadTile(tile1, zoom));
        usedTiles.add(loadTile(tile2, zoom));
        usedTiles.add(loadTile(tile3, zoom));
        usedTiles.add(loadTile(tile4, zoom));
        usedTiles.add(loadTile(tile1Z10, 10));
        usedTiles.add(loadTile(tile2Z10, 10));
        usedTiles.add(loadTile(tile3Z10, 10));
        usedTiles.add(loadTile(tile4Z10, 10));

        //Create a new array of heights to be stored.
        heights = new int[16][16];

        //Get initial lon/lat
        lon = coord1[0];
        lat = coord1[1];

        //Stores whether height data was located for each block and what zoom level it current is at
        int[] iHeightGot = {0}; //The value determines to which zoom level the height is of.
        // 0 means that no height has been found yet for any zoom level

        //Iterate over each block in the chunk and increment the lon/lat accordingly.
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j<16; j++) {
                iHeightGot[0] = 0;
                for (ElevationTile elevationTile : usedTiles) {
                    if (heights[i][j] == 0 || iHeightGot[0]!=15) { //If zoom level is not 15, keep going through the tiles to get a better one
                        heights[i][j] = elevationTile.getHeight(lon, lat, iHeightGot); //If height is found from this tile, iHeightGot[0] will update to the zoom of the tile
                    }
                    if (iHeightGot[0] == 0) //If data was not obtained from any of the tiles
                        heights[i][j] = -30;
                }
                lon += steplon;
                lat += steplat;
            }
            lon += (rowlon - (16 * steplon));
            lat += (rowlat - (16 * steplat));
        }

        return heights;
*/
    }

    public ElevationTile loadTile(int[] tile, int zoom)
    {

        if (!(contains(zoom + "-" + tile[0] + "-" + tile[1]))) {
            ElevationTile elevationTile = new ElevationTile(zoom + "-" + tile[0] + "-" + tile[1], tile[0], tile[1], zoom);
            tiles.add(elevationTile);
            return (elevationTile);
        } else {
            return (get(zoom + "-" + tile[0] + "-" + tile[1]));
        }

    }


}
