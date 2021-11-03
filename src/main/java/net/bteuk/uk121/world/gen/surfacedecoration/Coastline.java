package net.bteuk.uk121.world.gen.surfacedecoration;

public class Coastline extends JSONAPI
{
    CoastlineInfo info;

    public Coastline(String url)
    {
        super(url);
    }

    public void getInfo()
    {
        downloadFile();
        System.out.println(jsonText);
        info = gson.fromJson(jsonText, CoastlineInfo.class);
    }
}
