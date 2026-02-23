package dto;

public class ConfigDto {

    private String criterio;
    private Double peso;
    private String tipo; // "beneficio" o "costo"

    public ConfigDto() {
    }

    public ConfigDto(String criterio, Double peso, String tipo) {
        this.criterio = criterio;
        this.peso = peso;
        this.tipo = tipo;
    }

    public String getCriterio() {
        return criterio;
    }

    public void setCriterio(String criterio) {
        this.criterio = criterio;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}