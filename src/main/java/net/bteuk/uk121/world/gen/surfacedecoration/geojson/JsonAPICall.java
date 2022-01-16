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
        test.getInfo(1);
    }

    //For use if only the tile numbers are known upon creating the object
    public JsonAPICall(int x, int z)
    {
        this.x = x;
        this.z = z;

        szURL = "https://cloud.daporkchop.net/gis/osm/0/tile/"+x+"/"+z+".json";
        System.out.println(szURL);

        fileName = directory+"-"+x+"-"+z+".json";
    }

    //For use if the url is already known upon creating the object
    public JsonAPICall(String end)
    {
        StringBuilder sb = new StringBuilder("https://cloud.daporkchop.net/gis/osm/0/");
        sb.append(end);

        fileName = directory + end.replace("/", "-");
        this.szURL = sb.toString();
    }

    protected boolean getFile()
    {
        File newDirectory = new File(fileName);

        if (!newDirectory.exists())
        {
            System.out.println("Downloading tile");
            downloadFile();
        }
        //Already stored in cache
        try
        {
           // InputStream is = newDirectory.toURI().toURL().openStream();
           // getData(is);
            return (readDataFromFile(newDirectory));
        }
        catch (Exception e)
        {
            String JsonText = "";
            jsonNodes = jsonText.split("\n");
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
        jsonText = "";
        BufferedReader bufferedReader;
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

    private boolean readDataFromFile(File file)
    {
        jsonText = "";
        BufferedReader bufferedReader;
        FileReader fileReader;

        try
        {
            fileReader = new FileReader(file);

            bufferedReader = new BufferedReader(fileReader);
            jsonText = readAll(bufferedReader);

            fileReader.close();
            bufferedReader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            jsonText = "";
            return false;
        }
        jsonText = jsonText.trim();
        jsonNodes = jsonText.split("\n");
        return true;
    }

    public boolean noDownload()
    {
        InputStream in;
        try
        {
            URL website = new URL(szURL);
            in = website.openStream();
            return getData(in);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private String readAll(BufferedReader br)
    {
        StringBuilder sb = new StringBuilder("");
     //   int cp;
        try
        {
            String line;
            line = br.readLine();
            while (line != null)
            {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            sb = new StringBuilder();
        }
        return sb.toString();
    }
}
