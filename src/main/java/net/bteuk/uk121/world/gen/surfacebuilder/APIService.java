

// import io.netty.buffer.ByteBufInputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class APIService
{
   // BufferedImage image = ImageIO.read(new ByteBufInputStream(buffer));

    public static BufferedImage getImage(String url)
    {
        BufferedImage image = null;
        try
        {
            image = ImageIO.read(new File(url));
        } catch (IOException e)
        {
        	System.out.println("Error: "+e.getMessage());
        	e.printStackTrace();
        }
        return image;
    }
}
