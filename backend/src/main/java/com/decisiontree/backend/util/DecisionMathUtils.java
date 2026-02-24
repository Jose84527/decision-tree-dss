package com.decisiontree.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class DecisionMathUtils {

    private DecisionMathUtils() {
    }

    public static double min(List<Double> values) {
        double min = Double.POSITIVE_INFINITY;
        for (Double v : values) {
            if (v != null && v < min) {
                min = v;
            }
        }
        return min;
    }

    public static double max(List<Double> values) {
        double max = Double.NEGATIVE_INFINITY;
        for (Double v : values) {
            if (v != null && v > max) {
                max = v;
            }
        }
        return max;
    }

    public static double average(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        int count = 0;
        for (Double v : values) {
            if (v != null) {
                sum += v;
                count++;
            }
        }
        return count == 0 ? 0.0 : (sum / count);
    }

    public static double round(double value, int decimals) {
        return BigDecimal.valueOf(value)
                .setScale(decimals, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static boolean isTipoCosto(String tipo) {
        return tipo != null && tipo.trim().toLowerCase(Locale.ROOT).equals("costo");
    }

    public static boolean isTipoBeneficio(String tipo) {
        if (tipo == null) return true;
        return tipo.trim().toLowerCase(Locale.ROOT).equals("beneficio");
    }

    public static double safeSum(Collection<Double> values) {
        double sum = 0.0;
        if (values == null) return sum;
        for (Double v : values) {
            if (v != null) {
                sum += v;
            }
        }
        return sum;
    }
}