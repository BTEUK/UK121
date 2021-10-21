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
    public static final int zoom = 15;

    public static int xTile, yTile;
    public static double x, y;

    public static void main(String[] args)
    {
        System.out.println("Height: " +getHeightforXZ(2810630,-5390651,0));
    }


    public static int getHeightforXZ(double X, double Z, int iHeight) {
        x = X;
        y = Z;

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

    public static void convertMCCordsToLongLat(double iX, double iZ) {
        double[] longlat = projections.toGeo(iX, iZ);
        dLongitude = longlat[0];
        dLatitude = longlat[1];
        System.out.println("Initial conversion: ");
        System.out.println("Long: "+longlat[0]);
        System.out.println("Lat: "+longlat[1]);
    }

    public static ElevationSource determineSource() {
        File file = new File("C://Elevation/" + zoom +"/" + xTile + "/" + yTile + ".png");
        if (file.exists())
            return ElevationSource.Cache;

        //Defaults to AWS
        return ElevationSource.AWS_Terrain;
    }

    public static void getTile(final double lat, final double lon, final int zoom) {
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
        System.out.println("NW corner:");
        double[] blockNWCorner = projections.fromGeo(longLat(xTile, yTile, zoom)[0],longLat(xTile, yTile, zoom)[1]);
        System.out.println("SE corner:");
        double[] blockSECorner = projections.fromGeo(longLat(xTile+1, yTile+1, zoom)[0],longLat(xTile+1, yTile+1, zoom)[1]);

        double c = blockSECorner[1] -  blockNWCorner[1];
        double d = blockSECorner[0] - blockNWCorner[0];

        System.out.println("C: "+c);
        System.out.println("D: "+d);

        double a = x - blockNWCorner[0];
        double b = y - blockNWCorner[1];

        System.out.println("A: "+a);
        System.out.println("B: "+b);


        double length = Math.sqrt(a*a + b*b);

        double angleW = Math.acos(Math.abs((d*a + c*b)/(Math.sqrt(c*c + d*d)*length)));

        System.out.println("Angle W: "+angleW);

        double angleX = Math.PI/4 - angleW;

        System.out.println("Angle X: "+angleX);

        System.out.println("Length: "+length);

        double e = length*Math.cos(angleX);
        double f = length*Math.sin(angleX);

        if ((d*a + c*b)/(Math.sqrt(c*c + d*d)*length) < 0)
        {
            double sub = e;
            e = f;
            f = sub;
        }

        System.out.println("e: "+e);
        System.out.println("f: "+f);

        double scalePixelToBlock = Math.sqrt(c*c + d*d)/Math.sqrt(256*256 + 256*256);

        System.out.println("Scale: "+scalePixelToBlock);



        pixel[0] = (int) Math.round(e/scalePixelToBlock);
        pixel[1] = (int) Math.round(f/scalePixelToBlock);

    /*    if (pixel[0] < 0)
            pixel[0] = pixel[0] + 256;

        if (pixel[1] < 0)
            pixel[1] = pixel[1] + 256;
*/
        System.out.println("e scaled "+e/scalePixelToBlock);
        System.out.println("f scaled: "+f/scalePixelToBlock);

        System.out.println(pixel[0] +", "+pixel[1]);
        return pixel;
    }

    public static String getURL() {
        return ("https://s3.amazonaws.com/elevation-tiles-prod/terrarium/" + zoom + "/" + xTile + "/" + yTile + ".png");
    }
}
