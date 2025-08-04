package net.bteuk.uk121.data.surfacedecoration.geojson;

import java.util.*;

public class Tile extends JsonAPICall
{
    public ArrayList<TileInfo> infos;

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
            infos = new ArrayList<TileInfo>(0);
            return;
        }
        if (jsonNodes == null)
        {
            infos = new ArrayList<TileInfo>(0);
            return;
        }

        int iNodes = jsonNodes.length;

        infos = new ArrayList<TileInfo>(iNodes);

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
            try
            {
                infos.add(gson.fromJson(jsonNodes[i], TileInfo.class));
            }
            catch (Exception e)
            {
                System.out.println(jsonNodes[i]);
                e.printStackTrace();
            }
        }

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeToDeserialise2 = time.getTime();

        System.out.println("1.1.1."+iTile +".3 Time to deserialise all lines: "+(lTimeToDeserialise2-lTimeToDeserialise1)+" ms");

        cal = Calendar.getInstance();
        time = cal.getTime();
        long lTimeToDoReferences1 = time.getTime();

        iNodes = infos.size();

        //Deal with all references
        for (int i = 0 ; i < iNodes ; i++)
        {
            dealWithReference(i);
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
                //3d array
                if (jsonNodes[i].charAt(sfnjl+2) == '[')
                {
                    //Already a 4d array - do nothing
                    if (jsonNodes[i].charAt(sfnjl+3) == '[')
                    {

                    }
                    else
                    {
                        jsonNodes[i] = jsonNodes[i].replace("[[[", "[[[[");
                        jsonNodes[i] = jsonNodes[i].replace("]]]", "]]]]");
                    }
                }
                else
                {
                    jsonNodes[i] = jsonNodes[i].replace("[[", "[[[[");
                    jsonNodes[i] = jsonNodes[i].replace("]]", "]]]]");
                }
            }
            //1d array
            else
            {
                jsonNodes[i] = jsonNodes[i].replace("[", "[[[[");
                jsonNodes[i] = jsonNodes[i].replace("]", "]]]]");
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

    private String santise(String string)
    {
        //Changes the coordinates
        if (string.contains("coordinates"))
        {
            int sfnjl = string.indexOf("[");
            if (string.charAt(sfnjl+1) == '[')
            {
                //3d array
                if (string.charAt(sfnjl+2) == '[')
                {
                    //Already a 4d array - do nothing
                    if (string.charAt(sfnjl+3) == '[')
                    {

                    }
                    else
                    {
                        string = string.replace("[[[", "[[[[");
                        string = string.replace("]]]", "]]]]");
                    }
                }
                else
                {
                    string = string.replace("[[", "[[[[");
                    string = string.replace("]]", "]]]]");
                }
            }
            //1d array
            else
            {
                string = string.replace("[", "[[[[");
                string = string.replace("]", "]]]]");
            }
        }

        //Changes the properties
        if (string.contains("properties\":{"))
        {
            String original;

            int sfnjl = string.indexOf("properties");
            String after = string.substring(sfnjl);
            int iEnd = after.indexOf('}');
            String section = after.substring(12, iEnd+1);

            section = section.replace("{", "[[");
            section = section.replace("}", "]]");
            section = section.replace("\",\"", "],[");
            section = section.replace(':', ',');

            StringBuilder sb = new StringBuilder("");
            sb.append(string, 0, sfnjl+12);
            sb.append(section);
            sb.append(string.substring(sfnjl+iEnd+1));

            string = sb.toString();
        }
        return string;
    }

    public void dealWithReference(int i)
    {
        String type;
        String location;
        int j;

        TileInfo info = infos.get(i);
        type = info.type;
        if (type !=null)
        {
            //If the type is a reference, refer to the reference
            if (type.equals("Reference"))
            { //Can be ways, coastline or relation
                location = info.location;
                //For way references
                if (location.startsWith("way"))
                {
                    JsonAPICall newCall = new JsonAPICall(location);

                    newCall.getFile();

                    jsonNodes[i] = newCall.jsonText;
                    santise(i);
                    infos.remove(i);
                    infos.add(gson.fromJson(jsonNodes[i], TileInfo.class));

                    dealWithReference(infos.size());
                }
                else if (location.startsWith("coastline"))
                {
                    JsonAPICall newCall = new JsonAPICall(location);

                    newCall.getFile();

                    jsonNodes[i] = newCall.jsonText;
                    String[] newJsonNodes = jsonNodes[i].split("\n");
                    int iLength = newJsonNodes.length;

                    infos.remove(i);

                    int iSize = infos.size();

                    for (j = 0 ; j < iLength ; j++)
                    {
                        newJsonNodes[j] = santise(newJsonNodes[j]);
                        infos.add(gson.fromJson(newJsonNodes[j], TileInfo.class));
                        dealWithReference(iSize);
                        iSize++;
                    }
                }
                //  Coastline coastline = new Coastline(infos[i].location);
                //  coastline.getInfo();
            }
        }
        else
        {
            info.type = "N/A";
        }
    }
}