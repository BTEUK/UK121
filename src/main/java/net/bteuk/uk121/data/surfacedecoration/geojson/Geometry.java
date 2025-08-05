package net.bteuk.uk121.data.surfacedecoration.geojson;


import java.util.List;

public class Geometry
{
    public String type;

    public double[] pointCoordinates;

    public double[][] lineStringCoordinates;

    public List<double[][]> polygonCoordinates;

    public List<List<double[][]>> multiPolygonCoordinates;
}