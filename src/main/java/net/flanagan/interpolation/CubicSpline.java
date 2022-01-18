package net.flanagan.interpolation;

import net.flanagan.math.Conv;
import net.flanagan.math.Fmath;

public class CubicSpline {
    private int nPoints = 0;
    private int nPointsOriginal = 0;
    private double[] y = null;
    private double[] x = null;
    private double yy = 0.0D / 0.0;
    private double dydx = 0.0D / 0.0;
    private int[] newAndOldIndices;
    private double xMin = 0.0D / 0.0;
    private double xMax = 0.0D / 0.0;
    private double range = 0.0D / 0.0;
    private double[] d2ydx2 = null;
    private double yp1 = 0.0D / 0.0;
    private double ypn = 0.0D / 0.0;
    private boolean derivCalculated = false;
    private boolean checkPoints = false;
    private static boolean supress = false;
    private static boolean averageIdenticalAbscissae = false;
    private static double potentialRoundingError = 5.0E-15D;
    private static boolean roundingCheck = true;

    public CubicSpline(double[] var1, double[] var2) {
        this.nPoints = var1.length;
        this.nPointsOriginal = this.nPoints;
        if (this.nPoints != var2.length) {
            throw new IllegalArgumentException("Arrays x and y are of different length " + this.nPoints + " " + var2.length);
        } else if (this.nPoints < 3) {
            throw new IllegalArgumentException("A minimum of three data points is needed");
        } else {
            this.x = new double[this.nPoints];
            this.y = new double[this.nPoints];
            this.d2ydx2 = new double[this.nPoints];

            for(int var3 = 0; var3 < this.nPoints; ++var3) {
                this.x[var3] = var1[var3];
                this.y[var3] = var2[var3];
            }

            this.orderPoints();
            this.checkForIdenticalPoints();
            this.calcDeriv();
        }
    }

    public CubicSpline(int var1) {
        this.nPoints = var1;
        this.nPointsOriginal = this.nPoints;
        if (this.nPoints < 3) {
            throw new IllegalArgumentException("A minimum of three data points is needed");
        } else {
            this.x = new double[var1];
            this.y = new double[var1];
            this.d2ydx2 = new double[var1];
        }
    }

    public static void noRoundingErrorCheck() {
        roundingCheck = false;
    }

    public static void potentialRoundingError(double var0) {
        potentialRoundingError = var0;
    }

    public void resetData(double[] var1, double[] var2) {
        this.nPoints = this.nPointsOriginal;
        if (var1.length != var2.length) {
            throw new IllegalArgumentException("Arrays x and y are of different length");
        } else if (this.nPoints != var1.length) {
            throw new IllegalArgumentException("Original array length not matched by new array length");
        } else {
            for(int var3 = 0; var3 < this.nPoints; ++var3) {
                this.x[var3] = var1[var3];
                this.y[var3] = var2[var3];
            }

            this.orderPoints();
            this.checkForIdenticalPoints();
            this.calcDeriv();
        }
    }

    public static void averageIdenticalAbscissae() {
        averageIdenticalAbscissae = true;
    }

    public void orderPoints() {
        double[] var1 = new double[this.nPoints];
        this.newAndOldIndices = new int[this.nPoints];
        Fmath.selectionSort(this.x, var1, this.newAndOldIndices);
        Fmath.selectionSort(this.x, this.y, this.x, this.y);
        this.xMin = Fmath.minimum(this.x);
        this.xMax = Fmath.maximum(this.x);
        this.range = this.xMax - this.xMin;
    }

    public double getXmax() {
        return this.xMax;
    }

    public double getXmin() {
        return this.xMin;
    }

    public double[] getLimits() {
        double[] var1 = new double[]{this.xMin, this.xMax};
        return var1;
    }

    public void displayLimits() {
        System.out.println("\nThe limits of the abscissae (x-values) are " + this.xMin + " and " + this.xMax + "\n");
    }

    public void checkForIdenticalPoints() {
        int var1 = this.nPoints;
        boolean var2 = true;
        int var3 = 0;

        while(var2) {
            boolean var4 = true;
            int var5 = var3 + 1;

            while(var4) {
                if (this.x[var3] != this.x[var5]) {
                    ++var5;
                } else {
                    double[] var6;
                    double[] var7;
                    int[] var8;
                    int var9;
                    if (this.y[var3] == this.y[var5]) {
                        if (!supress) {
                            System.out.print("CubicSpline: Two identical points, " + this.x[var3] + ", " + this.y[var3]);
                            System.out.println(", in data array at indices " + this.newAndOldIndices[var3] + " and " + this.newAndOldIndices[var5] + ", latter point removed");
                        }

                        var6 = new double[this.nPoints - 1];
                        var7 = new double[this.nPoints - 1];
                        var8 = new int[this.nPoints - 1];

                        for(var9 = 0; var9 < var5; ++var9) {
                            var6[var9] = this.x[var9];
                            var7[var9] = this.y[var9];
                            var8[var9] = this.newAndOldIndices[var9];
                        }

                        for(var9 = var5; var9 < this.nPoints - 1; ++var9) {
                            var6[var9] = this.x[var9 + 1];
                            var7[var9] = this.y[var9 + 1];
                            var8[var9] = this.newAndOldIndices[var9 + 1];
                        }

                        --this.nPoints;
                        this.x = Conv.copy(var6);
                        this.y = Conv.copy(var7);
                        this.newAndOldIndices = Conv.copy(var8);
                    } else if (averageIdenticalAbscissae) {
                        if (!supress) {
                            System.out.print("CubicSpline: Two identical points on the absicca (x-axis) with different ordinate (y-axis) values, " + this.x[var3] + ": " + this.y[var3] + ", " + this.y[var5]);
                            System.out.println(", average of the ordinates taken");
                        }

                        this.y[var3] = (this.y[var3] + this.y[var5]) / 2.0D;
                        var6 = new double[this.nPoints - 1];
                        var7 = new double[this.nPoints - 1];
                        var8 = new int[this.nPoints - 1];

                        for(var9 = 0; var9 < var5; ++var9) {
                            var6[var9] = this.x[var9];
                            var7[var9] = this.y[var9];
                            var8[var9] = this.newAndOldIndices[var9];
                        }

                        for(var9 = var5; var9 < this.nPoints - 1; ++var9) {
                            var6[var9] = this.x[var9 + 1];
                            var7[var9] = this.y[var9 + 1];
                            var8[var9] = this.newAndOldIndices[var9 + 1];
                        }

                        --this.nPoints;
                        this.x = Conv.copy(var6);
                        this.y = Conv.copy(var7);
                        this.newAndOldIndices = Conv.copy(var8);
                    } else {
                        double var10 = this.range * 5.0E-4D;
                        if (!supress) {
                            System.out.print("CubicSpline: Two identical points on the absicca (x-axis) with different ordinate (y-axis) values, " + this.x[var3] + ": " + this.y[var3] + ", " + this.y[var5]);
                        }

                        boolean var11 = false;
                        if (var3 == 0) {
                            if (this.x[2] - this.x[1] <= var10) {
                                var10 = (this.x[2] - this.x[1]) / 2.0D;
                            }

                            if (this.y[0] > this.y[1]) {
                                if (this.y[1] > this.y[2]) {
                                    var11 = this.stay(var3, var5, var10);
                                } else {
                                    var11 = this.swap(var3, var5, var10);
                                }
                            } else if (this.y[2] <= this.y[1]) {
                                var11 = this.swap(var3, var5, var10);
                            } else {
                                var11 = this.stay(var3, var5, var10);
                            }
                        }

                        if (var5 == this.nPoints - 1) {
                            if (this.x[var1 - 2] - this.x[var1 - 3] <= var10) {
                                var10 = (this.x[var1 - 2] - this.x[var1 - 3]) / 2.0D;
                            }

                            if (this.y[var3] <= this.y[var5]) {
                                if (this.y[var3 - 1] <= this.y[var3]) {
                                    var11 = this.stay(var3, var5, var10);
                                } else {
                                    var11 = this.swap(var3, var5, var10);
                                }
                            } else if (this.y[var3 - 1] <= this.y[var3]) {
                                var11 = this.swap(var3, var5, var10);
                            } else {
                                var11 = this.stay(var3, var5, var10);
                            }
                        }

                        if (var3 != 0 && var5 != this.nPoints - 1) {
                            if (this.x[var3] - this.x[var3 - 1] <= var10) {
                                var10 = (this.x[var3] - this.x[var3 - 1]) / 2.0D;
                            }

                            if (this.x[var5 + 1] - this.x[var5] <= var10) {
                                var10 = (this.x[var5 + 1] - this.x[var5]) / 2.0D;
                            }

                            if (this.y[var3] > this.y[var3 - 1]) {
                                if (this.y[var5] > this.y[var3]) {
                                    if (this.y[var5] > this.y[var5 + 1]) {
                                        if (this.y[var3 - 1] <= this.y[var5 + 1]) {
                                            var11 = this.stay(var3, var5, var10);
                                        } else {
                                            var11 = this.swap(var3, var5, var10);
                                        }
                                    } else {
                                        var11 = this.stay(var3, var5, var10);
                                    }
                                } else if (this.y[var5 + 1] > this.y[var5]) {
                                    if (this.y[var5 + 1] > this.y[var3 - 1] && this.y[var5 + 1] > this.y[var3 - 1]) {
                                        var11 = this.stay(var3, var5, var10);
                                    }
                                } else {
                                    var11 = this.swap(var3, var5, var10);
                                }
                            } else if (this.y[var5] > this.y[var3]) {
                                if (this.y[var5 + 1] > this.y[var5]) {
                                    var11 = this.stay(var3, var5, var10);
                                }
                            } else if (this.y[var5 + 1] > this.y[var3 - 1]) {
                                var11 = this.stay(var3, var5, var10);
                            } else {
                                var11 = this.swap(var3, var5, var10);
                            }
                        }

                        if (!var11) {
                            this.stay(var3, var5, var10);
                        }

                        if (!supress) {
                            System.out.println(", the two abscissae have been separated by a distance " + var10);
                        }

                        ++var5;
                    }

                    if (this.nPoints - 1 == var3) {
                        var4 = false;
                    }
                }

                if (var5 >= this.nPoints) {
                    var4 = false;
                }
            }

            ++var3;
            if (var3 >= this.nPoints - 1) {
                var2 = false;
            }
        }

        if (this.nPoints < 3) {
            throw new IllegalArgumentException("Removal of duplicate points has reduced the number of points to less than the required minimum of three data points");
        } else {
            this.checkPoints = true;
        }
    }

    private boolean swap(int var1, int var2, double var3) {
        double[] var10000 = this.x;
        var10000[var1] += var3;
        var10000 = this.x;
        var10000[var2] -= var3;
        double var5 = this.x[var1];
        this.x[var1] = this.x[var2];
        this.x[var2] = var5;
        var5 = this.y[var1];
        this.y[var1] = this.y[var2];
        this.y[var2] = var5;
        return true;
    }

    private boolean stay(int var1, int var2, double var3) {
        double[] var10000 = this.x;
        var10000[var1] -= var3;
        var10000 = this.x;
        var10000[var2] += var3;
        return true;
    }

    public static void supress() {
        supress = true;
    }

    public static void unsupress() {
        supress = false;
    }

    public static net.flanagan.interpolation.CubicSpline zero(int var0) {
        if (var0 < 3) {
            throw new IllegalArgumentException("A minimum of three data points is needed");
        } else {
            net.flanagan.interpolation.CubicSpline var1 = new net.flanagan.interpolation.CubicSpline(var0);
            return var1;
        }
    }

    public static net.flanagan.interpolation.CubicSpline[] oneDarray(int var0, int var1) {
        if (var1 < 3) {
            throw new IllegalArgumentException("A minimum of three data points is needed");
        } else {
            net.flanagan.interpolation.CubicSpline[] var2 = new net.flanagan.interpolation.CubicSpline[var0];

            for(int var3 = 0; var3 < var0; ++var3) {
                var2[var3] = zero(var1);
            }

            return var2;
        }
    }

    public void setDerivLimits(double var1, double var3) {
        this.yp1 = var1;
        this.ypn = var3;
        this.calcDeriv();
    }

    public void setDerivLimits() {
        this.yp1 = 0.0D / 0.0;
        this.ypn = 0.0D / 0.0;
        this.calcDeriv();
    }

    public void setDeriv(double var1, double var3) {
        this.yp1 = var1;
        this.ypn = var3;
        this.calcDeriv();
    }

    public double[] getDeriv() {
        if (!this.derivCalculated) {
            this.calcDeriv();
        }

        return this.d2ydx2;
    }

    public void setDeriv(double[] var1) {
        this.d2ydx2 = var1;
        this.derivCalculated = true;
    }

    public void calcDeriv() {
        double var1 = 0.0D;
        double var3 = 0.0D;
        double var5 = 0.0D;
        double var7 = 0.0D;
        double[] var9 = new double[this.nPoints];
        if (Double.isNaN(this.yp1)) {
            this.d2ydx2[0] = var9[0] = 0.0D;
        } else {
            this.d2ydx2[0] = -0.5D;
            var9[0] = 3.0D / (this.x[1] - this.x[0]) * ((this.y[1] - this.y[0]) / (this.x[1] - this.x[0]) - this.yp1);
        }

        int var10;
        for(var10 = 1; var10 <= this.nPoints - 2; ++var10) {
            var5 = (this.x[var10] - this.x[var10 - 1]) / (this.x[var10 + 1] - this.x[var10 - 1]);
            var1 = var5 * this.d2ydx2[var10 - 1] + 2.0D;
            this.d2ydx2[var10] = (var5 - 1.0D) / var1;
            var9[var10] = (this.y[var10 + 1] - this.y[var10]) / (this.x[var10 + 1] - this.x[var10]) - (this.y[var10] - this.y[var10 - 1]) / (this.x[var10] - this.x[var10 - 1]);
            var9[var10] = (6.0D * var9[var10] / (this.x[var10 + 1] - this.x[var10 - 1]) - var5 * var9[var10 - 1]) / var1;
        }

        if (Double.isNaN(this.ypn)) {
            var7 = 0.0D;
            var3 = 0.0D;
        } else {
            var3 = 0.5D;
            var7 = 3.0D / (this.x[this.nPoints - 1] - this.x[this.nPoints - 2]) * (this.ypn - (this.y[this.nPoints - 1] - this.y[this.nPoints - 2]) / (this.x[this.nPoints - 1] - this.x[this.nPoints - 2]));
        }

        this.d2ydx2[this.nPoints - 1] = (var7 - var3 * var9[this.nPoints - 2]) / (var3 * this.d2ydx2[this.nPoints - 2] + 1.0D);

        for(var10 = this.nPoints - 2; var10 >= 0; --var10) {
            this.d2ydx2[var10] = this.d2ydx2[var10] * this.d2ydx2[var10 + 1] + var9[var10];
        }

        this.derivCalculated = true;
    }

    public double interpolate(double var1) {
        if (var1 < this.x[0]) {
            if (!roundingCheck || !(Math.abs(this.x[0] - var1) <= Math.pow(10.0D, Math.floor(Math.log10(Math.abs(this.x[0])))) * potentialRoundingError)) {
                throw new IllegalArgumentException("x (" + var1 + ") is outside the range of data points (" + this.x[0] + " to " + this.x[this.nPoints - 1] + ")");
            }

            var1 = this.x[0];
        }

        if (var1 > this.x[this.nPoints - 1]) {
            if (!roundingCheck || !(Math.abs(var1 - this.x[this.nPoints - 1]) <= Math.pow(10.0D, Math.floor(Math.log10(Math.abs(this.x[this.nPoints - 1])))) * potentialRoundingError)) {
                throw new IllegalArgumentException("x (" + var1 + ") is outside the range of data points (" + this.x[0] + " to " + this.x[this.nPoints - 1] + ")");
            }

            var1 = this.x[this.nPoints - 1];
        }

        double var3 = 0.0D;
        double var5 = 0.0D;
        double var7 = 0.0D;
        double var9 = 0.0D;
        boolean var11 = false;
        int var12 = 0;
        int var13 = this.nPoints - 1;

        while(var13 - var12 > 1) {
            int var14 = var13 + var12 >> 1;
            if (this.x[var14] > var1) {
                var13 = var14;
            } else {
                var12 = var14;
            }
        }

        var3 = this.x[var13] - this.x[var12];
        if (var3 == 0.0D) {
            throw new IllegalArgumentException("Two values of x are identical: point " + var12 + " (" + this.x[var12] + ") and point " + var13 + " (" + this.x[var13] + ")");
        } else {
            var7 = (this.x[var13] - var1) / var3;
            var5 = (var1 - this.x[var12]) / var3;
            this.yy = var7 * this.y[var12] + var5 * this.y[var13] + ((var7 * var7 * var7 - var7) * this.d2ydx2[var12] + (var5 * var5 * var5 - var5) * this.d2ydx2[var13]) * var3 * var3 / 6.0D;
            this.dydx = (this.y[var13] - this.y[var12]) / var3 - ((3.0D * var7 * var7 - 1.0D) * this.d2ydx2[var12] - (3.0D * var5 * var5 - 1.0D) * this.d2ydx2[var13]) * var3 / 6.0D;
            return this.yy;
        }
    }

    public double[] interpolate_for_y_and_dydx(double var1) {
        this.interpolate(var1);
        double[] var3 = new double[]{this.yy, this.dydx};
        return var3;
    }

    public static double interpolate(double var0, double[] var2, double[] var3, double[] var4) {
        if (var2.length == var3.length && var2.length == var4.length && var3.length == var4.length) {
            int var5 = var2.length;
            double var6 = 0.0D;
            double var8 = 0.0D;
            double var10 = 0.0D;
            double var12 = 0.0D;
            boolean var14 = false;
            int var15 = 0;
            int var16 = var5 - 1;

            while(var16 - var15 > 1) {
                int var17 = var16 + var15 >> 1;
                if (var2[var17] > var0) {
                    var16 = var17;
                } else {
                    var15 = var17;
                }
            }

            var6 = var2[var16] - var2[var15];
            if (var6 == 0.0D) {
                throw new IllegalArgumentException("Two values of x are identical");
            } else {
                var10 = (var2[var16] - var0) / var6;
                var8 = (var0 - var2[var15]) / var6;
                var12 = var10 * var3[var15] + var8 * var3[var16] + ((var10 * var10 * var10 - var10) * var4[var15] + (var8 * var8 * var8 - var8) * var4[var16]) * var6 * var6 / 6.0D;
                return var12;
            }
        } else {
            throw new IllegalArgumentException("array lengths are not all equal");
        }
    }
}
