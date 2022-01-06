package net.bteuk.uk121.world.gen.surfacedecoration.geojson;

public class Tile extends JsonAPICall
{
    public TileInfo[] infos;

    public Tile(int z, int x)
    {
        super(z, x);
        szURL = "https://cloud.daporkchop.net/gis/osm/0/tile/"+x+"/"+z+".json";
        System.out.println(szURL);
    }

    public void getInfo()
    {
        boolean downloaded = getFile();
        if (downloaded == false)
        {
            System.out.println("Not downloaded");
            return;
        }
        infos = new TileInfo[jsonNodes.length];

        int iActualCount;

        for (int i = 0 ; i < jsonNodes.length ; i++)
        {
            infos[i] = new TileInfo();
            if (infos[i].type !=null)
            {
                //If the type is a reference, refer to the reference
                if (infos[i].type.equals("Reference"))
                {
                    if (infos[i].location.startsWith("way"));
                    {
                        JsonAPICall newCall = new JsonAPICall("https://cloud.daporkchop.net/gis/osm/0/"+infos[i].location);
                        newCall.getFile();
                        jsonNodes[i] = newCall.jsonText;
                        infos[i] = gson.fromJson(jsonNodes[i], TileInfo.class);
                    }
                    //  Coastline coastline = new Coastline(infos[i].location);
                    //  coastline.getInfo();
                }
            }
            else
            {
                infos[i].type = "N/A";
            }

            //Changes the coordinates
            if (jsonNodes[i].contains("coordinates"))
            {
                int sfnjl = jsonNodes[i].indexOf("[");
                if (jsonNodes[i].charAt(sfnjl+1) == '[')
                {
                    //3d array - do nothing
                    if (jsonNodes[i].charAt(sfnjl+2) == '[')
                    {
                    }
                    else
                    {
                        jsonNodes[i] = jsonNodes[i].replace("[[", "[[[");
                        jsonNodes[i] = jsonNodes[i].replace("]]", "]]]");
                    }
                }
                //1d array
                else
                {
                    jsonNodes[i] = jsonNodes[i].replace("[", "[[[");
                    jsonNodes[i] = jsonNodes[i].replace("]", "]]]");
                }
            }

            //Changes the properties
            if (jsonNodes[i].contains("properties\":{"))
            {
                String original;

                int sfnjl = jsonNodes[i].indexOf("properties");
                String after = jsonNodes[i].substring(sfnjl);
                int iEnd = after.indexOf('}');
                String section = after.substring(12, iEnd+1);
                section = section.replace("{", "[[");
                section = section.replace("}", "]]");
                section = section.replace(",", "],[");
                section = section.replace(':', ',');

                jsonNodes[i] = jsonNodes[i].substring(0,sfnjl+12) + section + jsonNodes[i].substring(sfnjl+iEnd+1);
            }
        //    System.out.println(jsonNodes[i]);
            infos[i] = gson.fromJson(jsonNodes[i], TileInfo.class);

        }
    }
}