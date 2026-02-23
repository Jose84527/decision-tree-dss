package dto;

import java.util.List;

public class DecisionResponse {

    private String mensaje;
    private List<RankingItemDto> ranking;

    public DecisionResponse() {
    }

    public DecisionResponse(String mensaje, List<RankingItemDto> ranking) {
        this.mensaje = mensaje;
        this.ranking = ranking;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<RankingItemDto> getRanking() {
        return ranking;
    }

    public void setRanking(List<RankingItemDto> ranking) {
        this.ranking = ranking;
    }
}