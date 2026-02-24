package com.decisiontree.backend.service.criteria;

import com.decisiontree.backend.constants.DecisionModes;
import com.decisiontree.backend.dto.AlternativaDto;
import com.decisiontree.backend.dto.ConfigDto;
import com.decisiontree.backend.dto.DecisionRequest;
import com.decisiontree.backend.dto.RankingItemDto;
import com.decisiontree.backend.exception.BadRequestException;
import com.decisiontree.backend.util.DecisionMathUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DecisionCriteriaCalculator {

    public List<RankingItemDto> calcularRanking(DecisionRequest request) {
        String modo = DecisionModes.normalize(request.getModo());

        Map<String, ConfigDto> criteriosMap = request.getCriterios().stream()
                .collect(Collectors.toMap(
                        c -> c.getCriterio().trim(),
                        c -> c,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Double> puntajes;

        if (DecisionModes.isRankingMode(modo)) {
            puntajes = calcularSawNormalizado(request.getAlternativas(), request.getCriterios(), criteriosMap);
        } else if (DecisionModes.isExpectedValueMode(modo)) {
            puntajes = calcularValorEsperado(request.getAlternativas(), request.getCriterios(), criteriosMap);
        } else {
            switch (modo) {
                case DecisionModes.MAXIMIN:
                    puntajes = calcularMaximin(request.getAlternativas(), criteriosMap);
                    break;
                case DecisionModes.MAXIMAX:
                    puntajes = calcularMaximax(request.getAlternativas(), criteriosMap);
                    break;
                case DecisionModes.LAPLACE:
                    puntajes = calcularLaplace(request.getAlternativas(), criteriosMap);
                    break;
                case DecisionModes.HURWICZ:
                    puntajes = calcularHurwicz(request.getAlternativas(), criteriosMap, request.getAlpha());
                    break;
                case DecisionModes.MINIMAX_REGRET:
                    puntajes = calcularMinimaxRegret(request.getAlternativas(), criteriosMap);
                    break;
                default:
                    throw new BadRequestException("Modo no soportado: '" + request.getModo() + "'.");
            }
        }

        List<RankingItemDto> ranking = new ArrayList<>();
        for (Map.Entry<String, Double> e : puntajes.entrySet()) {
            ranking.add(new RankingItemDto(e.getKey(), DecisionMathUtils.round(e.getValue(), 6)));
        }

        ranking.sort(Comparator.comparing(
                RankingItemDto::getPuntaje,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        return ranking;
    }

    // =======================
    // MODO RANKING / SAW
    // =======================
    private Map<String, Double> calcularSawNormalizado(List<AlternativaDto> alternativas,
                                                       List<ConfigDto> criterios,
                                                       Map<String, ConfigDto> criteriosMap) {

        Map<String, Double> pesos = normalizarPesos(criterios);

        // min y max por criterio
        Map<String, Double> mins = new HashMap<>();
        Map<String, Double> maxs = new HashMap<>();

        for (ConfigDto c : criterios) {
            String criterio = c.getCriterio().trim();
            List<Double> valoresCriterio = new ArrayList<>();

            for (AlternativaDto a : alternativas) {
                valoresCriterio.add(a.getValores().get(criterio));
            }

            mins.put(criterio, DecisionMathUtils.min(valoresCriterio));
            maxs.put(criterio, DecisionMathUtils.max(valoresCriterio));
        }

        Map<String, Double> puntajes = new LinkedHashMap<>();

        for (AlternativaDto a : alternativas) {
            double score = 0.0;

            for (ConfigDto c : criterios) {
                String criterio = c.getCriterio().trim();
                double valor = a.getValores().get(criterio);
                double min = mins.get(criterio);
                double max = maxs.get(criterio);

                double normalizado;
                if (Double.compare(max, min) == 0) {
                    normalizado = 1.0; // todos iguales en ese criterio
                } else if (DecisionMathUtils.isTipoCosto(criteriosMap.get(criterio).getTipo())) {
                    // menor es mejor
                    normalizado = (max - valor) / (max - min);
                } else {
                    // beneficio (mayor es mejor)
                    normalizado = (valor - min) / (max - min);
                }

                score += (pesos.get(criterio) * normalizado);
            }

            puntajes.put(a.getNombre(), score);
        }

        return puntajes;
    }

    // =======================
    // VALOR ESPERADO
    // =======================
    private Map<String, Double> calcularValorEsperado(List<AlternativaDto> alternativas,
                                                      List<ConfigDto> criterios,
                                                      Map<String, ConfigDto> criteriosMap) {

        Map<String, Double> pesos = normalizarPesos(criterios);
        Map<String, Double> puntajes = new LinkedHashMap<>();

        for (AlternativaDto a : alternativas) {
            double score = 0.0;

            for (ConfigDto c : criterios) {
                String criterio = c.getCriterio().trim();
                double valor = a.getValores().get(criterio);
                double orientado = orientar(valor, criteriosMap.get(criterio).getTipo());
                score += pesos.get(criterio) * orientado;
            }

            puntajes.put(a.getNombre(), score);
        }

        return puntajes;
    }

    // =======================
    // MAXIMIN
    // =======================
    private Map<String, Double> calcularMaximin(List<AlternativaDto> alternativas,
                                                Map<String, ConfigDto> criteriosMap) {

        Map<String, Double> puntajes = new LinkedHashMap<>();

        for (AlternativaDto a : alternativas) {
            List<Double> orientados = obtenerValoresOrientados(a, criteriosMap);
            double peorCaso = DecisionMathUtils.min(orientados);
            puntajes.put(a.getNombre(), peorCaso);
        }

        return puntajes;
    }

    // =======================
    // MAXIMAX
    // =======================
    private Map<String, Double> calcularMaximax(List<AlternativaDto> alternativas,
                                                Map<String, ConfigDto> criteriosMap) {

        Map<String, Double> puntajes = new LinkedHashMap<>();

        for (AlternativaDto a : alternativas) {
            List<Double> orientados = obtenerValoresOrientados(a, criteriosMap);
            double mejorCaso = DecisionMathUtils.max(orientados);
            puntajes.put(a.getNombre(), mejorCaso);
        }

        return puntajes;
    }

    // =======================
    // LAPLACE
    // =======================
    private Map<String, Double> calcularLaplace(List<AlternativaDto> alternativas,
                                                Map<String, ConfigDto> criteriosMap) {

        Map<String, Double> puntajes = new LinkedHashMap<>();

        for (AlternativaDto a : alternativas) {
            List<Double> orientados = obtenerValoresOrientados(a, criteriosMap);
            double promedio = DecisionMathUtils.average(orientados);
            puntajes.put(a.getNombre(), promedio);
        }

        return puntajes;
    }

    // =======================
    // HURWICZ
    // =======================
    private Map<String, Double> calcularHurwicz(List<AlternativaDto> alternativas,
                                                Map<String, ConfigDto> criteriosMap,
                                                Double alphaRequest) {

        double alpha = (alphaRequest == null) ? 0.5 : alphaRequest;
        if (alpha < 0.0 || alpha > 1.0) {
            throw new BadRequestException("El alpha de Hurwicz debe estar entre 0 y 1.");
        }

        Map<String, Double> puntajes = new LinkedHashMap<>();

        for (AlternativaDto a : alternativas) {
            List<Double> orientados = obtenerValoresOrientados(a, criteriosMap);
            double min = DecisionMathUtils.min(orientados);
            double max = DecisionMathUtils.max(orientados);

            double score = (alpha * max) + ((1 - alpha) * min);
            puntajes.put(a.getNombre(), score);
        }

        return puntajes;
    }

    // =======================
    // MINIMAX REGRET
    // =======================
    private Map<String, Double> calcularMinimaxRegret(List<AlternativaDto> alternativas,
                                                      Map<String, ConfigDto> criteriosMap) {

        // Mejor valor orientado por criterio
        Map<String, Double> mejorPorCriterio = new HashMap<>();

        for (String criterio : criteriosMap.keySet()) {
            double mejor = Double.NEGATIVE_INFINITY;
            for (AlternativaDto a : alternativas) {
                double valor = a.getValores().get(criterio);
                double orientado = orientar(valor, criteriosMap.get(criterio).getTipo());
                if (orientado > mejor) {
                    mejor = orientado;
                }
            }
            mejorPorCriterio.put(criterio, mejor);
        }

        Map<String, Double> puntajes = new LinkedHashMap<>();

        for (AlternativaDto a : alternativas) {
            double peorArrepentimiento = Double.NEGATIVE_INFINITY;

            for (String criterio : criteriosMap.keySet()) {
                double valor = a.getValores().get(criterio);
                double orientado = orientar(valor, criteriosMap.get(criterio).getTipo());

                double mejor = mejorPorCriterio.get(criterio);
                double arrepentimiento = mejor - orientado; // >= 0 si está bien orientado

                if (arrepentimiento > peorArrepentimiento) {
                    peorArrepentimiento = arrepentimiento;
                }
            }

            // Para mantener "mayor puntaje = mejor", invertimos el signo
            puntajes.put(a.getNombre(), -peorArrepentimiento);
        }

        return puntajes;
    }

    // =======================
    // Helpers internos
    // =======================
    private List<Double> obtenerValoresOrientados(AlternativaDto alternativa,
                                                  Map<String, ConfigDto> criteriosMap) {
        List<Double> lista = new ArrayList<>();

        for (String criterio : criteriosMap.keySet()) {
            double valor = alternativa.getValores().get(criterio);
            String tipo = criteriosMap.get(criterio).getTipo();
            lista.add(orientar(valor, tipo));
        }

        return lista;
    }

    private double orientar(double valor, String tipo) {
        return DecisionMathUtils.isTipoCosto(tipo) ? -valor : valor;
    }

    private Map<String, Double> normalizarPesos(List<ConfigDto> criterios) {
        Map<String, Double> pesos = new LinkedHashMap<>();
        double suma = 0.0;

        for (ConfigDto c : criterios) {
            suma += (c.getPeso() == null ? 0.0 : c.getPeso());
        }

        if (suma <= 0.0) {
            // Si todos vienen en 0, repartir igual
            double uniforme = 1.0 / criterios.size();
            for (ConfigDto c : criterios) {
                pesos.put(c.getCriterio().trim(), uniforme);
            }
            return pesos;
        }

        for (ConfigDto c : criterios) {
            pesos.put(c.getCriterio().trim(), c.getPeso() / suma);
        }

        return pesos;
    }
}
