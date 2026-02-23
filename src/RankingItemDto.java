package com.dss.dto;

public class RankingItemDto {

    private String alternativa;
    private Double puntaje;

    public RankingItemDto(String alternativa, Double puntaje) {
        this.alternativa = alternativa;
        this.puntaje = puntaje;
    }

    public String getAlternativa() { return alternativa; }
    public Double getPuntaje() { return puntaje; }
}