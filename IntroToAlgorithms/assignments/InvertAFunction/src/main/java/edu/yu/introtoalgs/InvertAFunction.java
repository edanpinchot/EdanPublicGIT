package edu.yu.introtoalgs;

public class InvertAFunction {

    public InvertAFunction() {
    }

    private static double pdf(double z, double mean, double stddev) {
        double pi = Math.PI;
        double e = Math.E;

        double part1 = (1 / (stddev * Math.sqrt(2 * pi)));
        double exponent = (Math.pow((z - mean), 2) / (2 * (Math.pow(stddev, 2))));
        double part2 = Math.pow(e, -exponent);

        return part1 * part2;
    }

    private static double taylorSeries(double z) {
        int y = 1;
        double total = 0;

        for (int count = 0; count <= 15; count++) {
            double numerator = Math.pow(z, y);
            double denominator = 1;
            for (int k = y; k >= 1; k-=2) {                                                     // is this bad
                denominator *= k;
            }

            total += (numerator/denominator);
            y += 2;
        }
        return total;
    }

    public static double cdf(final double z, final double mean, double stddev ) {
//        if (z >= 5.8) {
//            return 1;
//        }
//        if (z <= -5.8) {
//            return 0;
//        }

        double zscore = (z-mean) / stddev;
        double pdf = pdf(zscore, 0, 1);
        double tseries = taylorSeries(zscore);
        double constant = 0.5;

        return constant + pdf * tseries;
    }

    private static double inverseGaussianCDF(double y, double delta, double mean, double stddev, double top, double bottom) {
        double middle = bottom + (top - bottom) / 2;
        //if (Math.abs(y - middle) < delta) {
        if ((top - bottom) < delta) {
            return cdf(middle, mean, stddev);
        }
        //if the desired cdf is greater than our "middle", now look for it in boundary between top and middle
        if (y > cdf(middle, mean, stddev)) {
            return inverseGaussianCDF(y, delta, mean, stddev, top, middle);
        }
        //if the desired cdf is less than our "middle", now look for it in boundary between middle and bottom
        if (y < cdf(middle, mean, stddev)) {
            return inverseGaussianCDF(y, delta, mean, stddev, middle, bottom);
        }
        return 0; //?
    }

    public static double inverseGaussianCDF(double y , double delta , double mean, double stddev) {
        return inverseGaussianCDF(y, delta, mean, stddev, 5.8, -5.8);
    }

    public static void main(String[] args) {
        double cdf = cdf(-5, 0.5, 3);
        System.out.println(cdf(300, 1493.46, 1000));
//        System.out.println(inverseGaussianCDF(cdf, 0.4, 0.5, 3));
//        System.out.println(inverseGaussianCDF(cdf, 0.3, 0.5, 3));
//        System.out.println(inverseGaussianCDF(cdf, 0.01, 0.5, 3));


    }
}