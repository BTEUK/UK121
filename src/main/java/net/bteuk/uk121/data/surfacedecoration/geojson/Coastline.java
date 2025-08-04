package net.bteuk.uk121.data.surfacedecoration.geojson;

public class Coastline extends JsonAPICall
{
    TileInfo info;

    public Coastline(String url)
    {
        super(url);
        System.out.println(szURL);
    }

    public void getInfo()
    {
        getFile();
      //  System.out.println(jsonText);
        info = gson.fromJson(jsonText, TileInfo.class);
      //  System.out.println(info.type);
      //  System.out.println(info.id);
    }
}
