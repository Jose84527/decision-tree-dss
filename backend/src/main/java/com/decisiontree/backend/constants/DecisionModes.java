package com.decisiontree.backend.constants;

import java.util.Locale;

public final class DecisionModes {

    public static final String RANKING = "ranking";               // SAW (normalización + pesos)
    public static final String SAW = "saw";

    public static final String VALOR_ESPERADO = "valor_esperado";
    public static final String EXPECTED_VALUE = "expected_value";

    public static final String MAXIMIN = "maximin";
    public static final String MAXIMAX = "maximax";
    public static final String LAPLACE = "laplace";
    public static final String HURWICZ = "hurwicz";
    public static final String MINIMAX_REGRET = "minimax_regret";

    private DecisionModes() {
    }

    public static String normalize(String mode) {
        if (mode == null || mode.trim().isEmpty()) {
            return RANKING;
        }
        return mode.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isRankingMode(String mode) {
        String m = normalize(mode);
        return RANKING.equals(m) || SAW.equals(m);
    }

    public static boolean isExpectedValueMode(String mode) {
        String m = normalize(mode);
        return VALOR_ESPERADO.equals(m) || EXPECTED_VALUE.equals(m);
    }
}
