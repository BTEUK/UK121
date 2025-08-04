package net.bteuk.uk121.data.elevation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class APITester
{
    public static void main (String[] agrs)
    {
        int xTile = 4104;
        int yTile = 2726;
        String URL = "https://s3.amazonaws.com/elevation-tiles-prod/terrarium/13/4104/2726.png";
        String fileName = APIService.downloadImage(URL, xTile, yTile, 13);

        File file = new File(fileName);
        BufferedImage pngTile;
        int iHeight = 0;

        try
        {
            pngTile = ImageIO.read(file);
            int[] rgb = pngTile.getRGB(0, 0, 16, 16, null, 0, 16);

         //   iHeight = (rgb[0] * 256 + rgb[1] + rgb[2] / 256) - 32768;
            iHeight = rgb[0];
        }
        catch (Exception e)
        {

        }
        System.out.println(iHeight);
    }
}
