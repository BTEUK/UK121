package net.bteuk.uk121.world.gen.surfacedecoration;

public class Coastline extends JsonAPI
{
    CoastlineInfo info;

    public Coastline(int x, int z)
    {
        super(x, z);
    }

    public Coastline(String url)
    {
        super("https://cloud.daporkchop.net/gis/osm/0/"+url+".json");
    }

    public void getInfo()
    {
        downloadFile();
        System.out.println(jsonText);
        info = gson.fromJson(jsonText, CoastlineInfo.class);
    }
}
