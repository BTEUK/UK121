package net.bteuk.uk121.world.gen.surfacedecoration;

public class BlockVector3
{
    public static final BlockVector3 ZERO = new BlockVector3(0, 0);
    public static final BlockVector3 UNIT_X = new BlockVector3(1, 0);
    public static final BlockVector3 UNIT_Z = new BlockVector3(0, 1);
    public static final BlockVector3 UNIT_MINUS_X = new BlockVector3(-1, 0);
    public static final BlockVector3 UNIT_MINUS_Z = new BlockVector3(0, -1);
    public static final BlockVector3 ONE = new BlockVector3(1, 1);

    private final int x;
    private final int z;

    /**
     * Construct an instance.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    private BlockVector3(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public static BlockVector3 at(double x, double z) {
        return at((int) Math.floor(x), (int) Math.floor(z));
    }

    public static BlockVector3 at(int x, int z)
    {
        return new BlockVector3(x, z);
    }

    public double getX()
    {
        return x;
    }
    public double getZ()
    {
        return z;
    }

    public int getBlockX()
    {
        return x;
    }
    public int getBlockZ()
    {
        return z;
    }
}