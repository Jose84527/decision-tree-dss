package dto;

public class RankingItemDto {

    private String alternativa;
    private Double puntaje;

    public RankingItemDto() {
    }

    public RankingItemDto(String alternativa, Double puntaje) {
        this.alternativa = alternativa;
        this.puntaje = puntaje;
    }

    public String getAlternativa() {
        return alternativa;
    }

    public void setAlternativa(String alternativa) {
        this.alternativa = alternativa;
    }

    public Double getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Double puntaje) {
        this.puntaje = puntaje;
    }
}