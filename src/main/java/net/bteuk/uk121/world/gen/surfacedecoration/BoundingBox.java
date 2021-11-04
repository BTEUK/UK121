package net.bteuk.uk121.world.gen.surfacedecoration;

import lombok.NonNull;
import net.minecraft.util.math.ChunkPos;
import com.google.gson.*;

public class BoundingBox
{
    double minX, maxX, minZ, maxZ;
    int size = 64;

    public BoundingBox(double min, double max, double min1, double max1)
    {
        this.minX = min;
        this.maxX = max;
        this.minZ = min1;
        this.maxZ = max1;
    }

    //Getters
    public double minX()
    {
        return minX;
    }
    public double minZ()
    {
        return minZ;
    }
    public double maxX()
    {
        return maxX;
    }
    public double maxZ()
    {
        return maxZ;
    }

    public static void main (String[] args) {
        double[] degrees = {51.354565, 1.430773, 51.354565, 1.430773};

        BoundingBox bb = new BoundingBox(degrees);
        ChunkPos[] positions = bb.toTiles(64);
        for (int i = 0; i < positions.length; i++)
        {
            System.out.println("http://cloud.daporkchop.net/gis/osm/0/tile/"+positions[i].x +"/"+positions[i].z+".json");
        }
    }

    public BoundingBox (double[] degrees)
    {
        minX = degrees[0];
        minZ = degrees[1];
        maxX = degrees[2];
        maxZ = degrees[3];
    }

    /**
     * Assuming this bounding box is located on a grid of square tiles, gets the positions of every tile that intersects this bounding box.
     *
     * @param size the side length of a tile
     * @return the positions of every tile that intersects this bounding box
     */

    public ChunkPos[] toTiles(double size) {
        int minXi = floorI(this.minX * size);
        int maxXi = ceilI(this.maxX * size);
        int minZi = floorI(this.minZ * size);
        int maxZi = ceilI(this.maxZ * size);

        ChunkPos[] out = new ChunkPos[(maxXi - minXi + 1) * (maxZi - minZi + 1)];
        for (int i = 0, x = minXi; x <= maxXi; x++) {
            for (int z = minZi; z <= maxZi; z++) {
                out[i++] = new ChunkPos(x, z);
            }
        }
        return out;
    }

    public static int floorI(double d) {
        int i = (int) d;
        return d < i ? i - 1 : i;
    }

    public static int ceilI(double d) {
        int i = (int) d;
        return d < i ? i : i + 1;
    }

    public static BoundingBox of(double x0, double x1, double z0, double z1) {
        return new BoundingBox(Math.min(x0, x1), Math.max(x0, x1), Math.min(z0, z1), Math.max(z0, z1));
    }
    /**
     * Returns a bounding box that contains this bounding box and the given one.
     *
     * @param other the other bounding box
     * @return the minimum bounding box containing both bounding boxes
     */
    public BoundingBox union(@NonNull BoundingBox other) {
        return of(Math.max(this.minX(), other.minX()), Math.max(this.maxX(), other.maxX()), Math.min(this.minZ(), other.minZ()), Math.max(this.maxZ(), other.maxZ()));
    }
}
