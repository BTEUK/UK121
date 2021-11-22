package net.bteuk.uk121.world.gen.surfacedecoration.overpassapi;

public class Node// extends Object
{
    public long ref;
    public double latitude, longitude;

    public Node(long ref, double lat, double lLong)
    {
        this.ref = ref;
        this.latitude = lat;
        this.longitude = lLong;
    }
}
