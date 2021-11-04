package net.bteuk.uk121.world.gen.surfacedecoration;

public class Tile extends JsonAPI {
    public TileInfo info;

    public Tile(String url) {
        super(url);
    }

    public void getInfo() {
        downloadFile();
        System.out.println(jsonText);
        info = gson.fromJson(jsonText, TileInfo.class);
        if (info.type.equals("Reference")) {
            System.out.println(1);
            Coastline coastline = new Coastline("https://cloud.daporkchop.net/gis/osm/0/" + info.location);
            coastline.getInfo();
        }

    }
}