package net.bteuk.uk121.world.gen.surfacedecoration;

public class Tile extends JsonAPI {
    public TileInfo info;

    public Tile(int x, int z)
    {
        super(x, z);
        szURL = "https://cloud.daporkchop.net/gis/osm/0/"+x+"/"+z+".json";
    }

    public void getInfo() {
        downloadFile();
        System.out.println(jsonText);
        info = gson.fromJson(jsonText, TileInfo.class);

        //If the type is a reference, refer to the reference
        if (info.type.equals("Reference"))
        {
            System.out.println(1);
            Coastline coastline = new Coastline(info.location);
            coastline.getInfo();
        }

    }
}