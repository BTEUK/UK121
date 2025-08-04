package net.bteuk.uk121.util.geometry;

import java.util.ArrayList;
import java.util.Set;

public class Line
{
    public ArrayList<BlockVector3> drawLine(int x1, int z1, int x2, int z2)
    {
        ArrayList<BlockVector3> vset = new ArrayList<BlockVector3>();

            int tipx = x1;
            int tipz = z1;
            int dx = Math.abs(x2 - x1);
            int dz = Math.abs(z2 - z1);

            if (dx + dz == 0)
            {
                vset.add(BlockVector3.at(tipx, tipz));;
            }

            int dMax = Math.max(dx, dz);
            if (dMax == dx) {
                for (int domstep = 0; domstep <= dx; domstep++) {
                    tipx = x1 + domstep * (x2 - x1 > 0 ? 1 : -1);
                    tipz = (int) Math.round(z1 + domstep * ((double) dz) / ((double) dx) * (z2 - z1 > 0 ? 1 : -1));

                    vset.add(BlockVector3.at(tipx, tipz));
                }
            }
            else if (dMax == dz)
            {
                for (int domstep = 0; domstep <= dz; domstep++)
                {
                    tipz = z1 + domstep * (z2 - z1 > 0 ? 1 : -1);
                    tipx = (int) Math.round(x1 + domstep * ((double) dx) / ((double) dz) * (x2 - x1 > 0 ? 1 : -1));

                    vset.add(BlockVector3.at(tipx, tipz));
                }
            }

        vset = getBallooned(vset, 0);
        return vset;
    }


    private static ArrayList<BlockVector3> getBallooned(ArrayList<BlockVector3> vset, double radius)
    {
        ArrayList<BlockVector3> returnset = new ArrayList<>();
        int ceilrad = (int) Math.ceil(radius);

        for (BlockVector3 v : vset)
        {
            int tipx = v.getBlockX();
            int tipz = v.getBlockZ();

            for (int loopx = tipx - ceilrad; loopx <= tipx + ceilrad; loopx++)
            {
                for (int loopz = tipz - ceilrad; loopz <= tipz + ceilrad; loopz++)
                {
                    if (hypot(loopx - tipx, loopz - tipz) <= radius)
                    {
                        returnset.add(BlockVector3.at(loopx, loopz));
                    }
                }
            }
        }
        return returnset;
    }

    private static ArrayList<BlockVector3> getHollowed(Set<BlockVector3> vset) {
        ArrayList<BlockVector3> returnset = new ArrayList<>();
        for (BlockVector3 v : vset) {
            double x = v.getX();
            double z = v.getZ();
            if (!(vset.contains(BlockVector3.at(x + 1, z))
                    && vset.contains(BlockVector3.at(x - 1, z))
                    && vset.contains(BlockVector3.at(x, z + 1))
                    && vset.contains(BlockVector3.at(x, z - 1)))) {
                returnset.add(v);
            }
        }
        return returnset;
    }

    private static double hypot(double... pars) {
        double sum = 0;
        for (double d : pars) {
            sum += Math.pow(d, 2);
        }
        return Math.sqrt(sum);
    }
}