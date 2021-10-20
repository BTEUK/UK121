package net.bteuk.uk121.world.gen.surfacebuilder;

import java.awt.image.BufferedImage;

public class APITester
{
    public static void main (String[] agrs)
    {
        String URL = "https://s3.amazonaws.com/elevation-tiles-prod/terrarium/13/51/0.png";
        BufferedImage pngTile = APIService.getImage(URL);
        int iWidth = pngTile.getWidth();
        int iHeight = pngTile.getHeight();

        System.out.println(iWidth +", "+iHeight);

    }
}
