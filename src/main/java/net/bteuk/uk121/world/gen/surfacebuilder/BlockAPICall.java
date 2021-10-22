package net.bteuk.uk121.world.gen.surfacebuilder;

import com.google.common.collect.BoundType;
import net.bteuk.uk121.world.gen.Projections.GeographicProjection;
import net.bteuk.uk121.world.gen.Projections.ModifiedAirocean;
import net.minecraft.client.util.PngFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class BlockAPICall {
    public static ModifiedAirocean projections = new ModifiedAirocean();

    public static double dLongitude;
    public static double dLatitude;
    public static int zoom = 15;

    private static int xTile, yTile;
    private static double xBlock, yBlock;

    public static String url;
    public static String fileName;
    private static File file;
    private BufferedImage pngTile;

    private boolean bFileRead = true;

    public static void main(String[] args)
    {
        System.out.println("Height: " +getTileAndHeightForXZ(2810630,-5390651,0));
    }

    public BlockAPICall(int xTile, int yTile, int zoom)
    {
        this.zoom = zoom;
        this.xTile = xTile;
        this.yTile = yTile;
        this.url = getURL();
    }

    public void loadPicture()
    {
        fileName = APIService.downloadImage(url, xTile, yTile, zoom);

        file = new File(fileName);

        try
        {
            pngTile = ImageIO.read(file);
        }
        catch (Exception e)
        {
            bFileRead = false;
        }
    }
    public int getHeightForXZ(double X, double Z, int iHeight)
    {
        xBlock = X;
        yBlock = Z;

        convertMCCordsToLongLat(X, Z);

        int[] pixel = getPixel();

        int rgb = pngTile.getRGB(pixel[0], pixel[1]);
        int a = (rgb>>24)&0xff;
        int r = (rgb>>16)&0xff;
        int g = (rgb>>8)&0xff;
        int b = rgb&0xff;

        iHeight = (r * 256 + g + b / 256) - 32768;

                    /*
            double[] out = new double[resolution * resolution];

            for (int i = 0; i < resolution * resolution; i++) {
                int c = rgb[i];
                if ((c >>> 24) != 0xFF) //nodata
                {
                    out[i] = Double.NaN;
                } else
                {
                    out[i] = ((c & ~0xFF000000) - 0x00800000) * (1.0d / 256.0d);
                }
            }
        */
        return iHeight;
    }

    public static int getTileAndHeightForXZ(double X, double Z, int iHeight) {
        xBlock = X;
        xBlock = Z;

        convertMCCordsToLongLat(X, Z);

        //Calculates the tile
        getTile(dLatitude, dLongitude, zoom);

        //Checks whether there is Lidar available, then cache and if it isn't in lidar or cache, source is set to the AWS API
        ElevationSource source = determineSource();

        System.out.println("Source: " + source.toString());

        String fileName = "";

        BufferedImage pngTile;

        //Downloads the image if it is not found in cache
        if (source == ElevationSource.AWS_Terrain) {
            //Gets the URL
            String URL = getURL();

            APIService.downloadImage(URL, xTile, yTile, zoom);
        }

        fileName = "C://Elevation/" +zoom +"/" +xTile +"/" +yTile +".png";
        File file = new File(fileName);

        int[] pixel = getPixel();

        try {
            pngTile = ImageIO.read(file);
            int rgb = pngTile.getRGB(pixel[0], pixel[1]);
            int a = (rgb>>24)&0xff;
            int r = (rgb>>16)&0xff;
            int g = (rgb>>8)&0xff;
            int b = rgb&0xff;

            iHeight = (r * 256 + g + b / 256) - 32768;

        } catch (Exception e) {

        }

                    /*
            double[] out = new double[resolution * resolution];

            for (int i = 0; i < resolution * resolution; i++) {
                int c = rgb[i];
                if ((c >>> 24) != 0xFF) //nodata
                {
                    out[i] = Double.NaN;
                } else
                {
                    out[i] = ((c & ~0xFF000000) - 0x00800000) * (1.0d / 256.0d);
                }
            }
        */
        return iHeight;
    }

    public static double[] convertMCCordsToLongLat(double iX, double iZ) {
        double[] longlat = projections.toGeo(iX, iZ);
        dLongitude = longlat[0];
        dLatitude = longlat[1];
        System.out.println("Long: "+longlat[0]);
        System.out.println("Lat: "+longlat[1]);
        return longlat;
    }

    public static ElevationSource determineSource() {
        File file = new File("C://Elevation/" + zoom +"/" + xTile + "/" + yTile + ".png");
        if (file.exists())
            return ElevationSource.Cache;

        //Defaults to AWS
        return ElevationSource.AWS_Terrain;
    }

    private static int[] getTile(final double lat, final double lon, final int zoom) {
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
        xTile = xtile;
        yTile = ytile;
        int[] tile = new int[2];
        tile[0] = xTile;
        tile[1] = yTile;
        return tile;
    }

    public static int[] getTile(double x, double y)
    {
        double[] longLat = convertMCCordsToLongLat(x, y);
        int[] Tile = getTile(longLat[0], longLat[1], zoom);
        return Tile;
    }

    public static double[] longLat(int x, int y, int zoom)
    {
        double[] longlat = new double[2];
        longlat[0] = ( x/Math.pow(2,zoom))*360 - 180;
        double lat_rad = Math.atan(Math.sinh(Math.PI * ( 1 - ( 2 * (y / Math.pow(2,zoom) ) ) )));
        longlat[1] = lat_rad * (180.0 / Math.PI);

        return longlat;
    }

    public static int[] getPixel()
    {
        int[] pixel = new int[2];

        //Find the block represented by the top left pixel
        double[] blockNWCorner = projections.fromGeo(longLat(xTile, yTile, zoom)[0],longLat(xTile, yTile, zoom)[1]);
        double[] blockSECorner = projections.fromGeo(longLat(xTile+1, yTile+1, zoom)[0],longLat(xTile+1, yTile+1, zoom)[1]);

        double c = blockSECorner[1] -  blockNWCorner[1];
        double d = blockSECorner[0] - blockNWCorner[0];

        double a = xBlock - blockNWCorner[0];
        double b = xBlock - blockNWCorner[1];

        double length = Math.sqrt(a*a + b*b);

        double sqrt = Math.sqrt(c * c + d * d);

        double a1 = (d * a + c * b) / (sqrt * length);

        double angleW = Math.acos(Math.abs(a1));

        double angleX = Math.PI/4 - angleW;

        double e = length*Math.cos(angleX);
        double f = length*Math.sin(angleX);

  /*      if (a1 < 0)
        {
            double sub = e;
            e = f;
            f = sub;
        }
*/
        double scalePixelToBlock = sqrt /Math.sqrt(256*256 + 256*256);

        System.out.println("Scale: "+scalePixelToBlock);

        pixel[0] = (int) Math.round(e/scalePixelToBlock);
        pixel[1] = (int) Math.round(f/scalePixelToBlock);

    /*    if (pixel[0] < 0)
            pixel[0] = pixel[0] + 256;

        if (pixel[1] < 0)
            pixel[1] = pixel[1] + 256;
*/
        return pixel;
    }

    public static String getURL() {
        return ("https://s3.amazonaws.com/elevation-tiles-prod/terrarium/" + zoom + "/" + xTile + "/" + yTile + ".png");
    }
}
