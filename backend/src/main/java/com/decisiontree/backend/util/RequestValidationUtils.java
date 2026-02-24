package com.decisiontree.backend.util;

import com.decisiontree.backend.dto.AlternativaDto;
import com.decisiontree.backend.dto.ConfigDto;
import com.decisiontree.backend.dto.DecisionRequest;
import com.decisiontree.backend.exception.BadRequestException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RequestValidationUtils {

    private RequestValidationUtils() {
    }

    public static void validate(DecisionRequest request) {
        if (request == null) {
            throw new BadRequestException("El cuerpo de la solicitud no puede ser nulo.");
        }

        if (request.getAlternativas() == null || request.getAlternativas().isEmpty()) {
            throw new BadRequestException("Debe enviar al menos una alternativa.");
        }

        if (request.getCriterios() == null || request.getCriterios().isEmpty()) {
            throw new BadRequestException("Debe enviar al menos un criterio.");
        }

        if (request.getAlpha() != null && (request.getAlpha() < 0.0 || request.getAlpha() > 1.0)) {
            throw new BadRequestException("El parámetro alpha debe estar entre 0 y 1.");
        }

        validarCriterios(request.getCriterios());
        validarAlternativas(request.getAlternativas(), request.getCriterios());
    }

    private static void validarCriterios(List<ConfigDto> criterios) {
        Set<String> nombres = new HashSet<>();

        for (int i = 0; i < criterios.size(); i++) {
            ConfigDto c = criterios.get(i);

            if (c == null) {
                throw new BadRequestException("El criterio en la posición " + i + " es nulo.");
            }

            String nombre = c.getCriterio();
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new BadRequestException("Todos los criterios deben tener nombre.");
            }

            String key = nombre.trim();
            if (!nombres.add(key)) {
                throw new BadRequestException("Hay criterios duplicados: '" + key + "'.");
            }

            if (c.getPeso() == null) {
                throw new BadRequestException("El criterio '" + key + "' debe tener peso.");
            }

            if (c.getPeso() < 0) {
                throw new BadRequestException("El peso del criterio '" + key + "' no puede ser negativo.");
            }

            String tipo = c.getTipo();
            if (tipo == null || tipo.trim().isEmpty()) {
                throw new BadRequestException("El criterio '" + key + "' debe indicar tipo ('beneficio' o 'costo').");
            }

            String tipoNorm = tipo.trim().toLowerCase();
            if (!tipoNorm.equals("beneficio") && !tipoNorm.equals("costo")) {
                throw new BadRequestException("El criterio '" + key + "' tiene tipo inválido. Usa 'beneficio' o 'costo'.");
            }
        }
    }

    private static void validarAlternativas(List<AlternativaDto> alternativas, List<ConfigDto> criterios) {
        Set<String> nombresAlternativas = new HashSet<>();
        Set<String> criteriosEsperados = new HashSet<>();

        for (ConfigDto c : criterios) {
            criteriosEsperados.add(c.getCriterio().trim());
        }

        for (int i = 0; i < alternativas.size(); i++) {
            AlternativaDto a = alternativas.get(i);

            if (a == null) {
                throw new BadRequestException("La alternativa en la posición " + i + " es nula.");
            }

            if (a.getNombre() == null || a.getNombre().trim().isEmpty()) {
                throw new BadRequestException("Todas las alternativas deben tener nombre.");
            }

            String nombreAlt = a.getNombre().trim();
            if (!nombresAlternativas.add(nombreAlt)) {
                throw new BadRequestException("Hay alternativas duplicadas: '" + nombreAlt + "'.");
            }

            Map<String, Double> valores = a.getValores();
            if (valores == null || valores.isEmpty()) {
                throw new BadRequestException("La alternativa '" + nombreAlt + "' no tiene valores.");
            }

            for (String criterio : criteriosEsperados) {
                if (!valores.containsKey(criterio)) {
                    throw new BadRequestException("La alternativa '" + nombreAlt + "' no contiene el criterio '" + criterio + "'.");
                }
                if (valores.get(criterio) == null) {
                    throw new BadRequestException("La alternativa '" + nombreAlt + "' tiene valor nulo para el criterio '" + criterio + "'.");
                }
            }
        }
    }
}
