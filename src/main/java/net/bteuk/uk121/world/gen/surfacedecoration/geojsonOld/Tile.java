package net.bteuk.uk121.world.gen.surfacedecoration.geojsonOld;

public class Tile extends JsonAPI {
    public TileInfo[] infos;

    public Tile(int x, int z)
    {
        super(x, z);
        szURL = "https://cloud.daporkchop.net/gis/osm/0/tile/"+x+"/"+z+".json";
        System.out.println(szURL);
    }

    public void getInfo()
    {
        boolean downloaded = downloadFile();
        if (downloaded == false)
        {
            System.out.println("Not downloaded");
            return;
        }
        infos = new TileInfo[jsonNodes.length];
        for (int i = 0 ; i < jsonNodes.length ; i++)
        {
            infos[i].tileLoad();
        //    infos[i] = gson.fromJson(jsonNodes[i], TileInfo.class);

            if (infos[i].type !=null)
            {
                //If the type is a reference, refer to the reference
                if (infos[i].type.equals("Reference"))
                {
                    Coastline coastline = new Coastline(infos[i].location);
                    coastline.getInfo();
                }
            }

        }
    }
}