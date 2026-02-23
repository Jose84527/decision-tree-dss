package com.dss.dto;

import java.util.List;

public class DecisionResponse {

    private String mensaje;
    private List<RankingItemDto> ranking;

    public DecisionResponse(String mensaje, List<RankingItemDto> ranking) {
        this.mensaje = mensaje;
        this.ranking = ranking;
    }

    public String getMensaje() { return mensaje; }
    public List<RankingItemDto> getRanking() { return ranking; }
}