package net.flanagan.math;

public class Conv {

    public static double[] copy(double[] var0) {
        if (var0 == null) {
            return null;
        } else {
            int var1 = var0.length;
            double[] var2 = new double[var1];

            for(int var3 = 0; var3 < var1; ++var3) {
                var2[var3] = var0[var3];
            }

            return var2;
        }
    }

    public static int[] copy(int[] var0) {
        if (var0 == null) {
            return null;
        } else {
            int var1 = var0.length;
            int[] var2 = new int[var1];

            for(int var3 = 0; var3 < var1; ++var3) {
                var2[var3] = var0[var3];
            }

            return var2;
        }
    }
}
