package net.bteuk.uk121.world.gen.surfacedecoration.geojson;

public class Coastline extends JsonAPICall
{
    TileInfo info;

    public Coastline(String url)
    {
        super("https://cloud.daporkchop.net/gis/osm/0/"+url);
        System.out.println(szURL);
    }

    public void getInfo()
    {
        getFile();
        System.out.println(jsonText);
        info = gson.fromJson(jsonText, TileInfo.class);
        System.out.println(info.type);
        System.out.println(info.id);
    }
}
