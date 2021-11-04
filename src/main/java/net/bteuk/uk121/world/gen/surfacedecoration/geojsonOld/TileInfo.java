package net.bteuk.uk121.world.gen.surfacedecoration.geojsonOld;

public class TileInfo
{
    public String jsonText;

    public String type;
    public String location;
    public Geometry geometry;
    public Properties properties;
    public String id;

    public TileInfo(String json)
    {
        jsonText = json;
    }

    public void tileLoad()
    {

    }
}
