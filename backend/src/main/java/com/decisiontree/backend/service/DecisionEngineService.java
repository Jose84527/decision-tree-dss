package com.decisiontree.backend.service;

import com.decisiontree.backend.constants.DecisionModes;
import com.decisiontree.backend.dto.DecisionRequest;
import com.decisiontree.backend.dto.DecisionResponse;
import com.decisiontree.backend.dto.RankingItemDto;
import com.decisiontree.backend.service.criteria.DecisionCriteriaCalculator;
import com.decisiontree.backend.util.RequestValidationUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DecisionEngineService {

    private final DecisionCriteriaCalculator calculator;

    public DecisionEngineService(DecisionCriteriaCalculator calculator) {
        this.calculator = calculator;
    }

    public DecisionResponse evaluar(DecisionRequest request) {
        RequestValidationUtils.validate(request);

        String modoNormalizado = DecisionModes.normalize(request.getModo());
        List<RankingItemDto> ranking = calculator.calcularRanking(request);

        String mensaje = construirMensaje(modoNormalizado, request.getAlpha());
        return new DecisionResponse(mensaje, ranking);
    }

    private String construirMensaje(String modo, Double alpha) {
        if (DecisionModes.isRankingMode(modo)) {
            return "Evaluación DSS realizada correctamente con método de ranking ponderado (SAW).";
        }

        if (DecisionModes.isExpectedValueMode(modo)) {
            return "Evaluación DSS realizada correctamente con criterio de valor esperado.";
        }

        switch (modo) {
            case DecisionModes.MAXIMIN:
                return "Evaluación DSS realizada correctamente con criterio Maximin.";
            case DecisionModes.MAXIMAX:
                return "Evaluación DSS realizada correctamente con criterio Maximax.";
            case DecisionModes.LAPLACE:
                return "Evaluación DSS realizada correctamente con criterio de Laplace.";
            case DecisionModes.HURWICZ:
                return "Evaluación DSS realizada correctamente con criterio de Hurwicz (alpha="
                        + (alpha == null ? 0.5 : alpha) + ").";
            case DecisionModes.MINIMAX_REGRET:
                return "Evaluación DSS realizada correctamente con criterio Minimax Regret.";
            default:
                return "Evaluación DSS realizada correctamente.";
        }
    }
}