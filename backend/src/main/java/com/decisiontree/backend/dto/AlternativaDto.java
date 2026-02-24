package com.decisiontree.backend.dto;
import java.util.Map;

public class AlternativaDto {

    private String nombre;
    private Map<String, Double> valores;

    public AlternativaDto() {
    }

    public AlternativaDto(String nombre, Map<String, Double> valores) {
        this.nombre = nombre;
        this.valores = valores;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Map<String, Double> getValores() {
        return valores;
    }

    public void setValores(Map<String, Double> valores) {
        this.valores = valores;
    }
}