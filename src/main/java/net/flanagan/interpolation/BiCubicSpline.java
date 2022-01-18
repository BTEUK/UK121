package net.flanagan.interpolation;

import net.flanagan.math.Fmath;

public class BiCubicSpline {
    private int nPoints = 0;
    private int mPoints = 0;
    private int nPointsT = 0;
    private int mPointsT = 0;
    private double[][] y = (double[][])null;
    private double[][] yT = (double[][])null;
    private double[] x1 = null;
    private double[] x2 = null;
    private double xx1 = 0.0D / 0.0;
    private double xx2 = 0.0D / 0.0;
    private double[] xMin = new double[2];
    private double[] xMax = new double[2];
    private double[][] d2ydx2inner = (double[][])null;
    private double[][] d2ydx2innerT = (double[][])null;
    private CubicSpline[] csn = null;
    private CubicSpline csm = null;
    private CubicSpline[] csnT = null;
    private CubicSpline csmT = null;
    private double interpolatedValue = 0.0D / 0.0;
    private double interpolatedValueTranspose = 0.0D / 0.0;
    private double interpolatedValueMean = 0.0D / 0.0;
    private boolean derivCalculated = false;
    private boolean averageIdenticalAbscissae = false;
    private static double potentialRoundingError = 5.0E-15D;
    private static boolean roundingCheck = true;

    public BiCubicSpline(double[] var1, double[] var2, double[][] var3) {
        this.nPoints = var1.length;
        this.mPoints = var2.length;
        this.nPointsT = this.mPoints;
        this.mPointsT = this.nPoints;
        if (this.nPoints != var3.length) {
            throw new IllegalArgumentException("Arrays x1 and y-row are of different length " + this.nPoints + " " + var3.length);
        } else if (this.mPoints != var3[0].length) {
            throw new IllegalArgumentException("Arrays x2 and y-column are of different length " + this.mPoints + " " + var3[0].length);
        } else if (this.nPoints >= 3 && this.mPoints >= 3) {
            this.csm = new CubicSpline(this.nPoints);
            this.csn = CubicSpline.oneDarray(this.nPoints, this.mPoints);
            this.csmT = new CubicSpline(this.mPoints);
            this.csnT = CubicSpline.oneDarray(this.nPointsT, this.mPointsT);
            this.x1 = new double[this.nPoints];
            this.x2 = new double[this.mPoints];
            this.y = new double[this.nPoints][this.mPoints];
            this.yT = new double[this.nPointsT][this.mPointsT];
            this.d2ydx2inner = new double[this.nPoints][this.mPoints];
            this.d2ydx2innerT = new double[this.nPointsT][this.mPointsT];

            int var4;
            for(var4 = 0; var4 < this.nPoints; ++var4) {
                this.x1[var4] = var1[var4];
            }

            this.xMin[0] = Fmath.minimum(this.x1);
            this.xMax[0] = Fmath.maximum(this.x1);

            for(var4 = 0; var4 < this.mPoints; ++var4) {
                this.x2[var4] = var2[var4];
            }

            this.xMin[1] = Fmath.minimum(this.x2);
            this.xMax[1] = Fmath.maximum(this.x2);

            int var5;
            for(var4 = 0; var4 < this.nPoints; ++var4) {
                for(var5 = 0; var5 < this.mPoints; ++var5) {
                    this.y[var4][var5] = var3[var4][var5];
                }
            }

            for(var4 = 0; var4 < this.nPointsT; ++var4) {
                for(var5 = 0; var5 < this.mPointsT; ++var5) {
                    this.yT[var4][var5] = var3[var5][var4];
                }
            }

            double[] var8 = new double[this.mPoints];

            int var6;
            for(var5 = 0; var5 < this.nPoints; ++var5) {
                for(var6 = 0; var6 < this.mPoints; ++var6) {
                    var8[var6] = var3[var5][var6];
                }

                this.csn[var5].resetData(var2, var8);
                this.csn[var5].calcDeriv();
                this.d2ydx2inner[var5] = this.csn[var5].getDeriv();
            }

            double[] var9 = new double[this.mPointsT];

            for(var6 = 0; var6 < this.nPointsT; ++var6) {
                for(int var7 = 0; var7 < this.mPointsT; ++var7) {
                    var9[var7] = this.yT[var6][var7];
                }

                this.csnT[var6].resetData(var1, var9);
                this.csnT[var6].calcDeriv();
                this.d2ydx2innerT[var6] = this.csnT[var6].getDeriv();
            }

            this.derivCalculated = true;
        } else {
            throw new IllegalArgumentException("The data matrix must have a minimum size of 3 X 3");
        }
    }

    public BiCubicSpline(int var1, int var2) {
        this.nPoints = var1;
        this.mPoints = var2;
        if (this.nPoints >= 3 && this.mPoints >= 3) {
            this.nPointsT = var2;
            this.mPointsT = var1;
            this.csm = new CubicSpline(this.nPoints);
            this.csmT = new CubicSpline(this.nPointsT);
            if (!roundingCheck) {
                CubicSpline var10000 = this.csm;
                CubicSpline.noRoundingErrorCheck();
            }

            this.csn = CubicSpline.oneDarray(this.nPoints, this.mPoints);
            this.csnT = CubicSpline.oneDarray(this.nPointsT, this.mPointsT);
            this.x1 = new double[this.nPoints];
            this.x2 = new double[this.mPoints];
            this.y = new double[this.nPoints][this.mPoints];
            this.yT = new double[this.nPointsT][this.mPointsT];
            this.d2ydx2inner = new double[this.nPoints][this.mPoints];
            this.d2ydx2innerT = new double[this.nPointsT][this.mPointsT];
        } else {
            throw new IllegalArgumentException("The data matrix must have a minimum size of 3 X 3");
        }
    }

    public static void noRoundingErrorCheck() {
        roundingCheck = false;
        CubicSpline.noRoundingErrorCheck();
    }

    public static void potentialRoundingError(double var0) {
        potentialRoundingError = var0;
        CubicSpline.potentialRoundingError(var0);
    }

    public void averageIdenticalAbscissae() {
        this.averageIdenticalAbscissae = true;

        CubicSpline var10000;
        for(int var1 = 0; var1 < this.csn.length; ++var1) {
            var10000 = this.csn[var1];
            CubicSpline.averageIdenticalAbscissae();
        }

        var10000 = this.csm;
        CubicSpline.averageIdenticalAbscissae();
    }

    public void resetData(double[] var1, double[] var2, double[][] var3) {
        if (var1.length != var3.length) {
            throw new IllegalArgumentException("Arrays x1 and y row are of different length");
        } else if (var2.length != var3[0].length) {
            throw new IllegalArgumentException("Arrays x2 and y column are of different length");
        } else if (this.nPoints != var1.length) {
            throw new IllegalArgumentException("Original array length not matched by new array length");
        } else if (this.mPoints != var2.length) {
            throw new IllegalArgumentException("Original array length not matched by new array length");
        } else {
            int var4;
            for(var4 = 0; var4 < this.nPoints; ++var4) {
                this.x1[var4] = var1[var4];
            }

            for(var4 = 0; var4 < this.mPoints; ++var4) {
                this.x2[var4] = var2[var4];
            }

            int var5;
            for(var4 = 0; var4 < this.nPoints; ++var4) {
                for(var5 = 0; var5 < this.mPoints; ++var5) {
                    this.y[var4][var5] = var3[var4][var5];
                    this.yT[var5][var4] = var3[var4][var5];
                }
            }

            this.csm = new CubicSpline(this.nPoints);
            this.csn = CubicSpline.oneDarray(this.nPoints, this.mPoints);
            double[] var8 = new double[this.mPoints];

            int var6;
            for(var5 = 0; var5 < this.nPoints; ++var5) {
                for(var6 = 0; var6 < this.mPoints; ++var6) {
                    var8[var6] = var3[var5][var6];
                }

                this.csn[var5].resetData(var2, var8);
                this.csn[var5].calcDeriv();
                this.d2ydx2inner[var5] = this.csn[var5].getDeriv();
            }

            this.csmT = new CubicSpline(this.nPointsT);
            this.csnT = CubicSpline.oneDarray(this.nPointsT, this.mPointsT);
            double[] var9 = new double[this.mPointsT];

            for(var6 = 0; var6 < this.nPointsT; ++var6) {
                for(int var7 = 0; var7 < this.mPointsT; ++var7) {
                    var9[var7] = this.yT[var6][var7];
                }

                this.csnT[var6].resetData(var1, var9);
                this.csnT[var6].calcDeriv();
                this.d2ydx2innerT[var6] = this.csnT[var6].getDeriv();
            }

            this.derivCalculated = true;
        }
    }

    public static net.flanagan.interpolation.BiCubicSpline zero(int var0, int var1) {
        if (var0 >= 3 && var1 >= 3) {
            net.flanagan.interpolation.BiCubicSpline var2 = new net.flanagan.interpolation.BiCubicSpline(var0, var1);
            return var2;
        } else {
            throw new IllegalArgumentException("A minimum of three x three data points is needed");
        }
    }

    public static net.flanagan.interpolation.BiCubicSpline[] oneDarray(int var0, int var1, int var2) {
        if (var1 >= 3 && var2 >= 3) {
            net.flanagan.interpolation.BiCubicSpline[] var3 = new net.flanagan.interpolation.BiCubicSpline[var0];

            for(int var4 = 0; var4 < var0; ++var4) {
                var3[var4] = zero(var1, var2);
            }

            return var3;
        } else {
            throw new IllegalArgumentException("A minimum of three x three data points is needed");
        }
    }

    public double[][] getDeriv() {
        return this.d2ydx2inner;
    }

    public double[][] getDerivTranspose() {
        return this.d2ydx2innerT;
    }

    public double[] getXmin() {
        return this.xMin;
    }

    public double[] getXmax() {
        return this.xMax;
    }

    public double[] getLimits() {
        double[] var1 = new double[]{this.xMin[0], this.xMax[0], this.xMin[1], this.xMax[1]};
        return var1;
    }

    public void displayLimits() {
        System.out.println(" ");

        for(int var1 = 0; var1 < 2; ++var1) {
            System.out.println("The limits to the x array " + var1 + " are " + this.xMin[var1] + " and " + this.xMax[var1]);
        }

        System.out.println(" ");
    }

    public void setDeriv(double[][] var1) {
        this.d2ydx2inner = var1;
        this.derivCalculated = true;
    }

    public void setDerivTranspose(double[][] var1) {
        this.d2ydx2innerT = var1;
        this.derivCalculated = true;
    }

    public double interpolate(double var1, double var3) {
        this.xx1 = var1;
        this.xx2 = var3;
        double[] var5 = new double[this.nPoints];

        for(int var6 = 0; var6 < this.nPoints; ++var6) {
            var5[var6] = this.csn[var6].interpolate(var3);
        }

        this.csm.resetData(this.x1, var5);
        this.interpolatedValue = this.csm.interpolate(var1);
        double[] var8 = new double[this.nPointsT];

        for(int var7 = 0; var7 < this.nPointsT; ++var7) {
            var8[var7] = this.csnT[var7].interpolate(var1);
        }

        this.csmT.resetData(this.x2, var8);
        this.interpolatedValueTranspose = this.csmT.interpolate(var3);
        this.interpolatedValueMean = (this.interpolatedValue + this.interpolatedValueTranspose) / 2.0D;
        return this.interpolatedValueMean;
    }

    public double[] getInterpolatedValues() {
        double[] var1 = new double[]{this.interpolatedValueMean, this.interpolatedValue, this.interpolatedValueTranspose, this.xx1, this.xx2};
        return var1;
    }
}