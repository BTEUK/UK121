package net.bteuk.uk121.world.gen.surfacebuilder;

// import io.netty.buffer.ByteBufInputStream;

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
   public static String directory = "C:/Users/georg/IdeaProjects/uk121/Elevation/";

    public static String downloadImage(String url, int xTile, int yTile, int zoom)
    {
        BufferedImage image = null;
        File newDirectory;
        String fileName = "";
        String dirName = "";
        InputStream in = null;
        try
        {
            //Makes the folders
            dirName = directory+zoom+"/"+xTile;
            newDirectory = new File(dirName);
            newDirectory.mkdirs();

            //Creates the file
            fileName = directory+zoom+"/"+xTile+"/"+yTile+".png";
            FileWriter fileWriter = new FileWriter(fileName);
            boolean bCreated = false;
            fileWriter.write("");

            //Creates the link to the source
            URL website = new URL(url);
            in = website.openStream();

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
        return fileName;
    }
}
