package Main.Analyzer;

import Main.Main;

import java.util.Arrays;

public class CustomMath {
    public static double FindMinimum(double[] entities) {
        double minimum = Integer.MAX_VALUE;

        if (entities == null || entities.length == 0) {
            return minimum;
        }

        for (double j : entities) {
            minimum = Math.min(minimum, j);
        }
        return minimum;
    }

    public static double FindMaximum(double[] entities) {
        double maximum = Integer.MIN_VALUE;

        if (entities == null || entities.length == 0) {
            return maximum;
        }

        for (double j : entities) {
            maximum = Math.max(maximum, j);
        }

        return maximum;
    }

    public static double FindAverage(double[] entities) {
        int valueAverage = 0;

        if (entities == null || entities.length == 0) {
            return valueAverage;
        }

        for (double j : entities) {
            valueAverage += j;
        }

        return (double) valueAverage / entities.length;
    }

    public static double FindMedian(double[] entities) {
        if (entities == null || entities.length == 0) {
            return 0;
        }

        double[] copyEntities = entities.clone();
        Arrays.sort(copyEntities);

        if (copyEntities.length % 2 == 0) {
            return ((copyEntities[copyEntities.length / 2] + copyEntities[copyEntities.length / 2 - 1]) / 2f);
        }

        return copyEntities[copyEntities.length / 2];
    }

    public static double FindGeometricAverage(double[] entities) {
        if (entities == null || entities.length == 0) {
            return 0;
        }

        double multiplication = 1f;
        for (double j : entities) {
            if (j == 0) {
                return 0;
            } else {
                multiplication *= j;
            }
        }

        if (multiplication < 0 && entities.length % 2 == 0) {
            return 0;
        }
        if (multiplication < 0 && entities.length % 2 != 0) {
            return -(Math.pow(Math.abs(multiplication), 1d / entities.length));
        }
        return (Math.pow(multiplication, 1d / entities.length));
    }
}
