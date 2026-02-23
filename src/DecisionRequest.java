package com.dss.dto;

import java.util.List;

public class DecisionRequest {

    private String modo; // ejemplo: "ranking"
    private List<AlternativaDto> alternativas;
    private List<ConfigDto> criterios;

    public String getModo() { return modo; }
    public void setModo(String modo) { this.modo = modo; }

    public List<AlternativaDto> getAlternativas() { return alternativas; }
    public void setAlternativas(List<AlternativaDto> alternativas) { this.alternativas = alternativas; }

    public List<ConfigDto> getCriterios() { return criterios; }
    public void setCriterios(List<ConfigDto> criterios) { this.criterios = criterios; }
}