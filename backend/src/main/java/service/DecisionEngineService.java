package service;

import dto.AlternativaDto;
import dto.DecisionRequest;
import dto.DecisionResponse;
import dto.RankingItemDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class DecisionEngineService {

    public DecisionResponse evaluar(DecisionRequest request) {
        List<RankingItemDto> ranking = new ArrayList<>();

        if (request == null || request.getAlternativas() == null || request.getAlternativas().isEmpty()) {
            return new DecisionResponse("Solicitud recibida, pero no hay alternativas para evaluar (mock).", ranking);
        }

        for (AlternativaDto alternativa : request.getAlternativas()) {
            if (alternativa == null) {
                continue;
            }

            double puntaje = 0.0;
            Map<String, Double> valores = alternativa.getValores();

            // Mock simple: suma de valores (solo para probar flujo)
            if (valores != null) {
                for (Double valor : valores.values()) {
                    if (valor != null) {
                        puntaje += valor;
                    }
                }
            }

            ranking.add(new RankingItemDto(alternativa.getNombre(), puntaje));
        }

        ranking.sort(Comparator.comparing(RankingItemDto::getPuntaje,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return new DecisionResponse("Evaluación mock realizada correctamente.", ranking);
    }
}