package net.bteuk.uk121.world.gen.surfacebuilder;

import com.google.common.collect.BoundType;
import net.bteuk.uk121.world.gen.Projections.GeographicProjection;
import net.bteuk.uk121.world.gen.Projections.ModifiedAirocean;
import net.minecraft.client.util.PngFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class BlockAPICall {
    public static double dLongitude;
    public static double dLatitude;
    public static final int zoom = 13;

    public static int xTile, yTile;

    public static void main(String[] args)
    {
        getHeightforXZ(1,2,0);
    }


    public static int getHeightforXZ(double X, double Z, int iHeight) {
     //   convertMCCordsToLongLat(X, Z);

        //Calculates the tile
        getTile(51.43731532501611, 0.38392490676395113, zoom);

        ElevationSource source = determineSource();

        System.out.println("Source: "+source.toString());

        String fileName = "";

        BufferedImage pngTile;

        if (source == ElevationSource.AWS_Terrain) {
            //Gets the URL
            String URL = getURL();

            APIService.downloadImage(URL, xTile, yTile);
        }

        fileName = "C://Elevation/" + xTile + "/" + yTile + ".png";
        File file = new File(fileName);

        try {
            pngTile = ImageIO.read(file);
            int[] rgb = pngTile.getRGB(0, 0, 16, 16, null, 0, 16);

            iHeight = (rgb[0] * 256 + rgb[1] + rgb[2] / 256) - 32768;
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
        ModifiedAirocean projection = new ModifiedAirocean();
        double[] longlat = projection.toGeo(iX, iZ);
        dLongitude = longlat[0];
        dLatitude = longlat[1];
    }

    public static ElevationSource determineSource() {
        File file = new File("C://Elevation/" + xTile + "/" + yTile + ".png");
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

    public static String getURL() {
        return ("https://s3.amazonaws.com/elevation-tiles-prod/terrarium/" + zoom + "/" + xTile + "/" + yTile + ".png");
    }
}
