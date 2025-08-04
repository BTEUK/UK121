package net.bteuk.uk121.data.elevation;


import net.bteuk.uk121.mod.UK121;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class APIService
{
    public static String directory = UK121.directory + "Elevation/";

    public static String downloadImage(String url, int xTile, int yTile, int zoom)
    {
        BufferedImage image = null;
        InputStream in = null;

        //Determines the file name
        String fileName = directory+zoom+"-"+xTile+"-"+yTile+".png";

        //Initialises the files
        File newDirectory = new File(directory);
        File newImageFile = new File(fileName);

        //Downloads the file if it doesn't exist
        if (!newImageFile.exists())
        {
            try
            {
                //Create new directory if needed
                if (!newDirectory.exists())
                    newDirectory.mkdirs();

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
                e.printStackTrace();
            }
        }
        return fileName;
    }
}
