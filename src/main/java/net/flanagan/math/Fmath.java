package net.flanagan.math;

public class Fmath {

    public static double maximum(double[] var0) {
        int var1 = var0.length;
        double var2 = var0[0];

        for(int var4 = 1; var4 < var1; ++var4) {
            if (var0[var4] > var2) {
                var2 = var0[var4];
            }
        }

        return var2;
    }

    public static double minimum(double[] var0) {
        int var1 = var0.length;
        double var2 = var0[0];

        for(int var4 = 1; var4 < var1; ++var4) {
            if (var0[var4] < var2) {
                var2 = var0[var4];
            }
        }

        return var2;
    }

    public static void selectionSort(double[] var0, double[] var1, int[] var2) {
        boolean var3 = false;
        int var4 = -1;
        int var5 = var0.length;
        double var6 = 0.0D;
        boolean var8 = false;

        int var9;
        for(var9 = 0; var9 < var5; var2[var9] = var9++) {
            var1[var9] = var0[var9];
        }

        while(var4 != var5 - 1) {
            int var10 = var4 + 1;

            for(var9 = var4 + 2; var9 < var5; ++var9) {
                if (var1[var9] < var1[var10]) {
                    var10 = var9;
                }
            }

            ++var4;
            var6 = var1[var10];
            var1[var10] = var1[var4];
            var1[var4] = var6;
            int var11 = var2[var10];
            var2[var10] = var2[var4];
            var2[var4] = var11;
        }

    }

    public static void selectionSort(double[] var0, double[] var1, double[] var2, double[] var3) {
        boolean var4 = false;
        int var5 = -1;
        int var6 = var0.length;
        int var7 = var1.length;
        if (var6 != var7) {
            throw new IllegalArgumentException("First argument array, aa, (length = " + var6 + ") and the second argument array, bb, (length = " + var7 + ") should be the same length");
        } else {
            int var8 = var2.length;
            if (var8 < var6) {
                throw new IllegalArgumentException("The third argument array, cc, (length = " + var8 + ") should be at least as long as the first argument array, aa, (length = " + var6 + ")");
            } else {
                int var9 = var3.length;
                if (var9 < var7) {
                    throw new IllegalArgumentException("The fourth argument array, dd, (length = " + var9 + ") should be at least as long as the second argument array, bb, (length = " + var7 + ")");
                } else {
                    double var10 = 0.0D;
                    double var12 = 0.0D;

                    int var14;
                    for(var14 = 0; var14 < var6; ++var14) {
                        var2[var14] = var0[var14];
                        var3[var14] = var1[var14];
                    }

                    while(var5 != var6 - 1) {
                        int var15 = var5 + 1;

                        for(var14 = var5 + 2; var14 < var6; ++var14) {
                            if (var2[var14] < var2[var15]) {
                                var15 = var14;
                            }
                        }

                        ++var5;
                        var10 = var2[var15];
                        var2[var15] = var2[var5];
                        var2[var5] = var10;
                        var12 = var3[var15];
                        var3[var15] = var3[var5];
                        var3[var5] = var12;
                    }

                }
            }
        }
    }
}