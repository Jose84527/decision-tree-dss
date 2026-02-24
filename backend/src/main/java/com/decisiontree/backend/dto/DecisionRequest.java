package com.decisiontree.backend.dto;

import java.util.List;


public class DecisionRequest {

    private String modo; // ranking, valor_esperado, maximin, maximax, laplace, hurwicz, minimax_regret
    private Double alpha; // opcional, usado en hurwicz (0 a 1)
    private List<AlternativaDto> alternativas;
    private List<ConfigDto> criterios;

    public DecisionRequest() {
    }

    public DecisionRequest(String modo, Double alpha, List<AlternativaDto> alternativas, List<ConfigDto> criterios) {
        this.modo = modo;
        this.alpha = alpha;
        this.alternativas = alternativas;
        this.criterios = criterios;
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }

    public Double getAlpha() {
        return alpha;
    }

    public void setAlpha(Double alpha) {
        this.alpha = alpha;
    }

    public List<AlternativaDto> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<AlternativaDto> alternativas) {
        this.alternativas = alternativas;
    }

    public List<ConfigDto> getCriterios() {
        return criterios;
    }

    public void setCriterios(List<ConfigDto> criterios) {
        this.criterios = criterios;
    }
}