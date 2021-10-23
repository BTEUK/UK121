package net.bteuk.uk121.world.gen.surfacebuilder;

import net.bteuk.uk121.world.gen.Projections.ModifiedAirocean;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

public class BlockAPICall {
    public static ModifiedAirocean projections = new ModifiedAirocean();

    public static double dLongitude;
    public static double dLatitude;
    public static int zoom = 15;

    private static int xTile, yTile;
    private static double xBlock, yBlock;
    private final double x0, z0;
    private static double[] blockNWCorner;
    private static double[] blockSECorner;
    private static double c, d;
    private static double diagonal;
    private static double scalePixelToBlock;

    public static String url;
    public static String fileName;
    private static File file;
    private BufferedImage pngTile;
    public int[][] iHeights = new int[16][16];

    public static String directory = System.getProperty("user.dir") + "/uk121/Elevation/";

    private boolean bFileRead = true;

    public static void main(String[] args)
    { //2811800,-5390651
        System.out.println("Height: " +getTileAndHeightForXZ(140, -317,0));
        System.out.println();

        int[] Corner1 = BlockAPICall.getTile(140, -317);
        System.out.println("Corner 1 of non static: ");
        System.out.println("xTile: "+Corner1[0]);
        System.out.println("yTile: "+Corner1[1]);
        System.out.println("zoom: "+zoom);

   //    BlockAPICall test = new BlockAPICall(Corner1[0], Corner1[1], 15);
   //     test.loadPicture();
   //     System.out.println("Height: " +test.getHeightForXZ(140, -317, 0));
    }

    public BlockAPICall(int xTile, int yTile, int zoom, double x0, double z0)
    {
        this.zoom = zoom;
        this.xTile = xTile;
        this.yTile = yTile;
        this.url = getURL();
        this.x0 = x0;
        this.z0 = z0;
        getGeneralTileFigures();
    }

    public void getGeneralTileFigures()
    {
        //Find the block represented by the top left pixel
        blockNWCorner = projections.fromGeo(longLat(xTile, yTile, zoom)[0],longLat(xTile, yTile, zoom)[1]);
        blockSECorner = projections.fromGeo(longLat(xTile+1, yTile+1, zoom)[0],longLat(xTile+1, yTile+1, zoom)[1]);

        c = blockSECorner[1] -  blockNWCorner[1];
        d = blockSECorner[0] - blockNWCorner[0];

        diagonal = Math.sqrt(c * c + d * d);
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
        if (!bFileRead)
        {
            Arrays.fill(iHeights, 0);
            return;
        }

        int[] pixel;
        int rgb, r, g, b;
       // int a;

        for (int i = 0 ; i < 16 ; i++)
        {
            xBlock = x0+i;
            for (int j = 0 ; j < 16 ; j++)
            {
                yBlock = z0+j;

                pixel = getPixel();

                rgb = pngTile.getRGB(pixel[0], pixel[1]);
            //    a = (rgb>>24)&0xff;
                r = (rgb>>16)&0xff;
                g = (rgb>>8)&0xff;
                b = rgb&0xff;

                rgb = (r * 256 + g + b / 256) - 32768;

                iHeights[i][j] = rgb;
            }
        }
    }

    public static int getTileAndHeightForXZ(double X, double Z, int iHeight) {
        xBlock = X;
        yBlock = Z;

        convertMCCordsToLongLat(X, Z);
        if (Double.isNaN(dLatitude))
            return 0;

        //Calculates the tile
        getTile(dLatitude, dLongitude, zoom);

        //Checks whether there is Lidar available, then cache and if it isn't in lidar or cache, source is set to the AWS API
        ElevationSource source = determineSource();

        String fileName = "";

        BufferedImage pngTile;

        //Downloads the image if it is not found in cache
        if (source == ElevationSource.AWS_Terrain) {
            //Gets the URL
            String URL = getURL();

            APIService.downloadImage(URL, xTile, yTile, zoom);
        }

        fileName = directory +zoom +"/" +xTile +"/" +yTile +".png";
        File file = new File(fileName);

        //Find the block represented by the top left and bottom right corners
        blockNWCorner = projections.fromGeo(longLat(xTile, yTile, zoom)[0],longLat(xTile, yTile, zoom)[1]);
        blockSECorner = projections.fromGeo(longLat(xTile+1, yTile+1, zoom)[0],longLat(xTile+1, yTile+1, zoom)[1]);

        c = blockSECorner[1] -  blockNWCorner[1];
        d = blockSECorner[0] - blockNWCorner[0];

        diagonal = Math.sqrt(c * c + d * d);

        int[] pixel = getPixel();

        boolean bFileRead = false;

        try {
            pngTile = ImageIO.read(file);
            int rgb = pngTile.getRGB(pixel[0], pixel[1]);
            //    int a = (rgb>>24)&0xff;
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;
            bFileRead = true;
            iHeight = (r * 256 + g + b / 256) - 32768;
        }
        catch (Exception e)
        {
            bFileRead = false;
        }
        if (!bFileRead)
        {
            return 0;
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
        int[] Tile = getTile(longLat[1], longLat[0], zoom);
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

        double a = xBlock - blockNWCorner[0];
        double b = yBlock - blockNWCorner[1];

        double length = Math.sqrt(a*a + b*b);

        System.out.println("Diagonal: "+diagonal);
        System.out.println("Length: "+length);

        double a1 = (d * a + c * b) / (diagonal * length);

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
        scalePixelToBlock = diagonal / Math.sqrt(256*256 + 256*256);

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
