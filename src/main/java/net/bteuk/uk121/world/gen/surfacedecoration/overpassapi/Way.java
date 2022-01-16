package net.bteuk.uk121.world.gen.surfacedecoration.overpassapi;

import java.util.ArrayList;

public class Way// extends Object
{
    ArrayList<Node> nodes;
    ArrayList<Tag> tags;
    String id;

    public Way()
    {
        nodes = new ArrayList<Node>();
        tags = new ArrayList<Tag>();
    }

    public ArrayList<Node> getNodes()
    {
        return nodes;
    }

    public ArrayList<Tag> getTags()
    {
        return tags;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
