package net.bteuk.uk121.world.gen.surfacebuilder;

import net.bteuk.uk121.world.gen.Projections.GeographicProjection;
import net.bteuk.uk121.world.gen.Projections.ModifiedAirocean;
import net.minecraft.client.util.PngFile;

import java.awt.image.BufferedImage;

public class BlockAPICall
{
    public static double dLongitude;
    public static double dLatitude;
    public static final int resolution = 15;

    public static int getHeightforXZ(int X, int Z)
    {
        convertMCCordsToLongLat(X, Z);

        ElevationSource source = determineSource();

        if (source == ElevationSource.AWS_Terrain)
        {
            String URL = "https://s3.amazonaws.com/elevation-tiles-prod/terrarium/15/" +dLongitude +"/" +dLatitude+".png";
            BufferedImage pngTile = APIService.getImage(URL);
        //    pngTile.getRGB(X, Y);

            int[] rgb = pngTile.getRGB(0, 0, resolution, resolution, null, 0, resolution);
            double[] out = new double[resolution * resolution];

            for (int i = 0; i < resolution * resolution; i++) {
                int c = rgb[i];
                if ((c >>> 24) != 0xFF) { //nodata
                    out[i] = Double.NaN;
                } else {
                    out[i] = ((c & ~0xFF000000) - 0x00800000) * (1.0d / 256.0d);
                }
            }

        }

        return 77;
    }

    public static void convertMCCordsToLongLat(int iX, int iZ)
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
}
