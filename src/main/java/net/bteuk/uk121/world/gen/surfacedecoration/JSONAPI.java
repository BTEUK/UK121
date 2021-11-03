package net.bteuk.uk121.world.gen.surfacedecoration;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

public class JSONAPI
{
    String szURL;
    String jsonText;
    Gson gson = new Gson();

    public static void main(String[] args)
    {
        Tile test = new Tile("https://cloud.daporkchop.net/gis/osm/0/tile/3286/92.json");
        test.getInfo();
        System.out.println(test.info.type);
        System.out.println(test.info.location);
    }

    public JSONAPI(String url)
    {
        szURL = url;
    }

    protected void downloadFile()
    {
        BufferedReader bufferedReader;
        InputStream is = null;
        try
        {
            is = new URL(szURL).openStream();
            bufferedReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            jsonText = readAll(bufferedReader);
        }
        catch (IOException e)
        {

        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {

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
