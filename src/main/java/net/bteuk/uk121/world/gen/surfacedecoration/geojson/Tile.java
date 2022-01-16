package net.bteuk.uk121.world.gen.surfacedecoration.geojson;

import java.util.Calendar;
import java.util.Date;

public class Tile extends JsonAPICall
{
    public TileInfo[] infos;

    public Tile(int x, int z)
    {
        super(z, x);
    }

    public void getInfo(int iTile)
    {
        Calendar cal;
        Date time;

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeToGetText1 = time.getTime();

        boolean downloaded = getFile();

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeToGetText2 = time.getTime();

        System.out.println("1.1.1."+iTile +".1 Time to get the Json text and split it: "+(lTimeToGetText2-lTimeToGetText1)+" ms");

        if (downloaded == false)
        {
            System.out.println("Not downloaded");
            infos = new TileInfo[0];
            return;
        }
        if (jsonNodes == null)
        {
            infos = new TileInfo[0];
            return;
        }
        infos = new TileInfo[jsonNodes.length];
        //Make this into an arraylist to solve future problems with 1 - many nodes

        int iActualCount;

        final int iNodes = jsonNodes.length;

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeToSanitise1 = time.getTime();

        //Sanitise all lines of text
        for (int i = 0 ; i < iNodes ; i++)
        {
            //Sanitise the text
            santise(i);
        }

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeToSanatise2 = time.getTime();

        System.out.println("1.1.1."+iTile +".2 Time to sanitise all lines: "+(lTimeToSanatise2-lTimeToSanitise1)+" ms");

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeToDeserialise1 = time.getTime();

        //Deserialise all information
        for (int i = 0 ; i < iNodes ; i++)
        {
            //Deserialise the information
            infos[i] = gson.fromJson(jsonNodes[i], TileInfo.class);
        }

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeToDeserialise2 = time.getTime();

        System.out.println("1.1.1."+iTile +".3 Time to deserialise all lines: "+(lTimeToDeserialise2-lTimeToDeserialise1)+" ms");

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeToDoReferences1 = time.getTime();

        //Deal with all references
        for (int i = 0 ; i < iNodes ; i++)
        {
            if (infos[i].type !=null)
            {
                //If the type is a reference, refer to the reference
                if (infos[i].type.equals("Reference"))
                { //Can be ways, coastline or relation
                    //For way references
                    if (infos[i].location.startsWith("way"))
                    {
                        JsonAPICall newCall = new JsonAPICall(infos[i].location);
                        cal = Calendar.getInstance();
                        time = cal.getTime();
                        long lTimeToGetReference1 = time.getTime();

                        newCall.getFile();

                        cal = Calendar.getInstance();
                        time = cal.getTime();
                        long lTimeToGetReference2 = time.getTime();

                    //    System.out.println("1.1.1."+iTile +".3.1 Time to get the Json text of reference: "+(lTimeToGetReference2-lTimeToGetReference1)+" ms");

                        jsonNodes[i] = newCall.jsonText;
                        santise(i);
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
        }
        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeToDoReferences2 = time.getTime();

        System.out.println("1.1.1."+iTile +".4 Time to do references: "+(lTimeToDoReferences2-lTimeToDoReferences1)+" ms");

    }

    private void santise(int i)
    {
    //    Calendar cal;
    //    Date time;

    //    cal = Calendar.getInstance();
    //    time = cal.getTime();
    //    long lTimeToSanatise1 = time.getTime();

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

            StringBuilder sb = new StringBuilder("");
            sb.append(jsonNodes[i], 0, sfnjl+12);
            sb.append(section);
            sb.append(jsonNodes[i].substring(sfnjl+iEnd+1));

            jsonNodes[i] = sb.toString();
        }

    //    cal = Calendar.getInstance();
    //    time = cal.getTime();
    //    long lTimeToSanatise2 = time.getTime();

      //  System.out.println("1.1.1."+ +".2 Time sanatise all lines and deserialise: "+(lTimeToDeseiralise2-lTimeToDesrialise1)+" ms");
    }
}