package net.bteuk.uk121.world.gen.surfacebuilder;

import com.google.common.collect.BoundType;
import net.bteuk.uk121.world.gen.Projections.GeographicProjection;
import net.bteuk.uk121.world.gen.Projections.ModifiedAirocean;
import net.minecraft.client.util.PngFile;

import java.awt.image.BufferedImage;

public class BlockAPICall
{
    public static double dLongitude;
    public static double dLatitude;
    public static final int resolution = 15;

    public static void main(String[] args)
    {
        System.out.println(getURL(51.43733237692021, 0.38402722835131914, 13 ));
        System.out.println(getURL(51.43733237692021, 0.38402722835131914, 13 ));
        System.out.println(getURL(51.43733237692021, 0.38402722835131914, 13 ));

        dLatitude = 51.43733237692021;
        dLongitude = 0.38402722835131914;
        System.out.println(getHeightforXZ(1, 2, 0));
        //     System.out.println(getHeightforXZ(51.43733237692021, 0.38402722835131914 , 0));
    }


    public static int getHeightforXZ(double X, double Z, int iHeight)
    {
      //  convertMCCordsToLongLat(X, Z);

        ElevationSource source = determineSource();

        if (source == ElevationSource.AWS_Terrain)
        {

            String URL = getURL(dLatitude, dLongitude, 14);
            System.out.println(URL);
            BufferedImage pngTile = APIService.getImage(URL);
            int[] rgb = pngTile.getRGB(0, 0, resolution, resolution, null, 0, resolution);

            iHeight = (rgb[0] * 256 + rgb[1] + rgb[2] / 256) - 32768;

            return iHeight;

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

        }
        return iHeight;
    }

    public static void convertMCCordsToLongLat(double iX, double iZ)
    {
        ModifiedAirocean projection = new ModifiedAirocean();
        double[] longlat = projection.toGeo(iX, iZ);
        dLongitude = longlat[0];
        dLatitude = longlat[1];
    }

    public static ElevationSource determineSource()
    {
        //Defaults to AWS for now
        return ElevationSource.AWS_Terrain;
    }

    public static String getURL(final double lat, final double lon, final int zoom) {
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
        return("https://s3.amazonaws.com/elevation-tiles-prod/terrarium/"+zoom +"/" + xtile + "/" + ytile +".png");
    }

}
