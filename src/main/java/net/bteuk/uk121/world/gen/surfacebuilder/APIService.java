package net.bteuk.uk121.world.gen.surfacebuilder;

// import io.netty.buffer.ByteBufInputStream;

import net.bteuk.uk121.UK121;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;

public class APIService
{
   // BufferedImage image = ImageIO.read(new ByteBufInputStream(buffer));
   public static String directory = UK121.directory + "Elevation/";

    public static String downloadImage(String url, int xTile, int yTile, int zoom)
    {
        BufferedImage image = null;
        File newDirectory;

        //Determines the file name
        String fileName = directory+zoom+"-"+xTile+"-"+yTile+".png";

        String dirName = "";
        InputStream in = null;

        newDirectory = new File(fileName);

        //Downloads the file if it doesn't exist
        if (!newDirectory.exists())
        {
            try
            {
                //Creates the file
                FileWriter fileWriter = new FileWriter(fileName);
                boolean bCreated = false;
                fileWriter.write("");

                //Creates the link to the source
                URL website = new URL(url);
                in = website.openStream();

                //Copies the file
                //  Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
                ReadableByteChannel readChannel = Channels.newChannel(in);
                FileOutputStream fileOS = new FileOutputStream(fileName);
                FileChannel writeChannel = fileOS.getChannel();
                writeChannel
                        .transferFrom(readChannel, 0, Long.MAX_VALUE);

            }
            catch (Exception e)
            {
                if (in!=null)
                {
                    //   in.close();
                }
                e.printStackTrace();
            }
        }

        return fileName;
    }
}
