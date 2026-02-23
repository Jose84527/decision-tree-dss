package com.dss.service;

import com.dss.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DecisionEngineService {

    public DecisionResponse evaluar(DecisionRequest request) {

        List<RankingItemDto> ranking = new ArrayList<>();
        Random random = new Random();

        for (AlternativaDto alt : request.getAlternativas()) {
            ranking.add(new RankingItemDto(
                    alt.getNombre(),
                    random.nextDouble() * 100 // puntaje mock
            ));
        }

        return new DecisionResponse("Evaluación mock generada correctamente", ranking);
    }
}