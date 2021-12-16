package net.bteuk.uk121.world.gen.elevation;

import net.bteuk.uk121.world.gen.surfacebuilder.APIService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ElevationTile {

    public String name;

    public int tileX, tileZ;

    public double[] coordMin, coordMax;
    public boolean accessed;

    public double lonRange;
    public double latRange;

    private int pixel1;
    private int pixel2;

    private String fileName;
    private File file;
    private BufferedImage pngTile;

    private int zoom;

    public ElevationTile(String name, int x, int y, int zoom) {
        this.name = name;

        this.tileX = x;
        this.tileZ = y;

        this.zoom = zoom;

        coordMin = new double[]{tile2lon(x, zoom), tile2lat(y + 1, zoom)};
        coordMax = new double[]{tile2lon(x + 1, zoom), tile2lat(y, zoom)};

        lonRange = coordMax[0]-coordMin[0];
        latRange = coordMax[1]-coordMin[1];

        accessed = true;

        fileName = APIService.downloadImage("https://s3.amazonaws.com/elevation-tiles-prod/terrarium/" + zoom + "/" + x + "/" + y + ".png", x, y, zoom);
        file = new File(fileName);
        try {
            pngTile = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Convert tile to longitude.
    private double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    //Convert tile to latitude.
    private double tile2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    //If the coordinate is in this tile, return true.
    public boolean contains(double lon, double lat) {
        if (coordMax[0] >= lon && coordMin[0] <= lon && coordMax[1] >= lat && coordMin[1] <= lat) {return true;} else {return false;}
    }

    //Get the height at a given coordinate.
    public double getHeight(double lon, double lat) {

        pixel1 = (int) (((lon-coordMin[0]) / lonRange) * 256);
        pixel2 = (int) (((coordMax[1]-lat) / latRange) * 256);

        if (pixel1 > 255 || pixel2 > 255 || pixel1 < 0 || pixel2 < 0) {
            return 0;
        }

        return getHeight(pixel1, pixel2);
    }

    //Gets the pixel at the given coordinate.
    public int[] getPixel(double lon, double lat) {
        pixel1 = (int) (((lon-coordMin[0]) / lonRange) * 256);
        pixel2 = (int) (((coordMax[1]-lat) / latRange) * 256);

        if (pixel1 > 255 || pixel2 > 255 || pixel1 < 0 || pixel2 < 0) {
            return null;
        } else {
            return new int[]{pixel1,pixel2};
        }
    }

    //Get longitude at a specific pixel.
    public double getLon(double pixelX) {
        return pixelX/256*lonRange + coordMin[0];
    }

    //Get latitude at a specific pixel.
    public double getLat(double pixelZ) {
        return coordMax[1] - pixelZ/256*latRange;
    }

    //Get the height at a given pixel.
    public double getHeight(int pixelX, int pixelZ) {

        try {
            int rgb = pngTile.getRGB(pixelX, pixelZ);
            double r = (rgb >> 16) & 0xff;
            double g = (rgb >> 8) & 0xff;
            double b = rgb & 0xff;
            return ((r * 256 + g + b / 256) - 32768);
        }
        catch (Exception e)
        {
            System.out.println("Error TING");
            return -30;
        }
    }
}

