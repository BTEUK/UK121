package net.bteuk.uk121.world.gen.surfacedecoration.geojson;
import com.google.gson.*;
import net.bteuk.uk121.UK121;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

public class JsonAPICall
{
    String jsonText;
    String[] jsonNodes;

    Gson gson = new Gson();
    int x, z;
    protected String szURL;

    public static String directory = UK121.directory + "Ways/";
    public static String fileName;

    public static void main(String[] args)
    {
        Tile test = new Tile(3286, 92);
        test.getInfo();
    }

    //For use if only the tile numbers are known upon creating the object
    public JsonAPICall(int x, int z)
    {
        this.x = x;
        this.z = z;

        fileName = directory+"-"+x+"-"+z+".json";
    }

    //For use if the url is already known upon creating the object
    public JsonAPICall(String url)
    {
        this.szURL = url;
    }

    protected boolean getFile()
    {
        File newDirectory = new File(fileName);

        if (!newDirectory.exists())
        {
            System.out.println("Downloading file");
            downloadFile();
        }
        //Already stored in cache
        try
        {
            InputStream is = newDirectory.toURI().toURL().openStream();
            getData(is);
        }
        catch (Exception e)
        {
        }
        return true;
    }

    private void downloadFile()
    {
        File newDirectory;
        String dirName = "";
        InputStream in = null;
        try
        {
            //Creates the file
            FileWriter fileWriter = new FileWriter(fileName);
            boolean bCreated = false;
            fileWriter.write("");

            //Creates the link to the source
            URL website = new URL(szURL);
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
    }

    private boolean getData(InputStream is)
    {
        BufferedReader bufferedReader;
        try
        {
            is = new URL(szURL).openStream();
            if (is == null)
            {
                System.out.println("Null tile");
                return false;
            }
            bufferedReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            jsonText = readAll(bufferedReader);
            jsonText = jsonText.trim();
            jsonNodes = jsonText.split("\n");
            return true;
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            return false;
        }
        finally
        {
            try
            {
                if (is != null)
                    is.close();
            }
            catch (IOException e)
            {
                return false;
            }
        }
    }

    private String readAll(BufferedReader br)
    {
        StringBuilder sb = new StringBuilder();
        int cp;
        try
        {
            while ((cp = br.read()) != -1)
            {
                sb.append((char) cp);
            }
        }
        catch (IOException e)
        {
            sb = new StringBuilder();
        }
        return sb.toString();
    }
}
