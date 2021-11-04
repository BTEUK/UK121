package net.bteuk.uk121.world.gen.surfacedecoration.geojsonOld;
import com.google.gson.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

public class JsonAPI
{
    String jsonText;
    String[] jsonNodes;

    Gson gson = new Gson();
    int x, z;
    protected String szURL;

    public static void main(String[] args)
    {
        Tile test = new Tile(3286, 92);
        test.getInfo();
    }

    //For use if only the tile numbers are known upon creating the object
    public JsonAPI(int x, int z)
    {
        this.x = x;
        this.z = z;
    }

    //For use if the url is already known upon creating the object
    public JsonAPI(String url)
    {
        this.szURL = url;
    }

    protected boolean downloadFile()
    {
        BufferedReader bufferedReader;
        InputStream is = null;
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
