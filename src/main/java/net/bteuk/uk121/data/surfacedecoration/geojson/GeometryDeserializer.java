package net.bteuk.uk121.data.surfacedecoration.geojson;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class GeometryDeserializer implements JsonDeserializer<Geometry> {

    @Override
    public Geometry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();
        Geometry g = new Geometry();
        g.type = obj.get("type").getAsString();

        JsonElement coordsElem = obj.get("coordinates");

        if (coordsElem.isJsonArray())
        {
            JsonArray arr = coordsElem.getAsJsonArray();
            if (arr.size() > 0 && arr.get(0).isJsonArray())
            {
                // 2D+ array (LineString, Polygon, Multipolygon)
                JsonArray secondLevel = arr.get(0).getAsJsonArray();

                if (secondLevel.size() > 0 && secondLevel.get(0).isJsonArray())
                {
                    // 3D+ array (Polygon, Multipolygon)
                    JsonArray thirdLevel = arr.get(0).getAsJsonArray();

                    if (thirdLevel.size() > 0 && thirdLevel.get(0).isJsonArray())
                    {
//                        //4D array (Multipolygon)
//                        int iGroups = arr.size();
//                        g.multiPolygonCoordinates = new ArrayList<>(iGroups);
//
//                        //Goes through each group
//                        for (JsonElement group : arr)
//                        {
//                            //Extracts the list of area making up this group
//                            JsonArray areaListJ = group.getAsJsonArray();
//
//                            //Creates the group
//                            List<double[][]> groupareas = new ArrayList<>(areaListJ.size());
//
//                            for (JsonElement area : areaListJ)
//                            {
//                                //Extracts the list of points making up this area
//                                JsonArray areaCoordsListJ = area.getAsJsonArray();
//                                int iNumCoordindatePairs = areaCoordsListJ.size();
//
//                                //Creates and fills the coordinate list for this area
//                                double[][] areaCoordsList = new double[iNumCoordindatePairs][2];
//
//                                int i = 0;
//                                for (JsonElement pointE : areaCoordsListJ)
//                                {
//                                    JsonArray point = pointE.getAsJsonArray();
//                                    g.lineStringCoordinates[i][0] = point.get(0).getAsDouble();
//                                    g.lineStringCoordinates[i][1] = point.get(1).getAsDouble();
//                                    i++;
//                                }
//
//                                //Adds this area to the groups area
//                                groupareas.add(areaCoordsList);
//                            }
//
//                            //Adds the group to the groups list
//                            g.multiPolygonCoordinates.add(groupareas);
//                        }
                    }
                    else
                    {
                        //3D array (Polygon)
                        int iAreas = arr.size();
                        g.polygonCoordinates = new ArrayList<>(iAreas);

                        //Goes through each area
                        for (JsonElement area : arr)
                        {
                            //Extracts the list of points making up this area
                            JsonArray areaCoordsListJ = area.getAsJsonArray();
                            int iNumCoordindatePairs = areaCoordsListJ.size();

                            //Creates and fills the coordinate list for this area
                            double[][] areaCoordsList = new double[iNumCoordindatePairs][2];

                            int i = 0;
                            for (JsonElement pointE : areaCoordsListJ)
                            {
                                JsonArray point = pointE.getAsJsonArray();
                                g.lineStringCoordinates[i][0] = point.get(0).getAsDouble();
                                g.lineStringCoordinates[i][1] = point.get(1).getAsDouble();
                                i++;
                            }

                            //Adds this area to the polygon areas
                            g.polygonCoordinates.add(areaCoordsList);
                        }
                    }
                }
                else
                {
                    int iCoordinates = arr.size();
                    g.lineStringCoordinates = new double[iCoordinates][2];

                    int i = 0;
                    for (JsonElement e : arr)
                    {
                        JsonArray point = e.getAsJsonArray();
                        g.lineStringCoordinates[i][0] = point.get(0).getAsDouble();
                        g.lineStringCoordinates[i][1] = point.get(1).getAsDouble();
                        i++;
                    }
                }
            }
            else
            {
                // 1D array (Point)
                g.pointCoordinates = new double[] {arr.get(0).getAsDouble(), arr.get(1).getAsDouble()};
            }
        }
        return g;
    }
}
