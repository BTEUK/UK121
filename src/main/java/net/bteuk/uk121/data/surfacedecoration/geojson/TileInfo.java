package net.bteuk.uk121.data.surfacedecoration.geojson;

import java.util.HashMap;

public class TileInfo
{
    public String jsonText;

    public String type;
    public String location;
    public Geometry geometry;
    public HashMap<String, String> properties;
    public String id;

    public TileInfo(String json)
    {
        jsonText = json;
    }

    public TileInfo()
    {
    }

    public void tileLoad()
    {

    }
}
