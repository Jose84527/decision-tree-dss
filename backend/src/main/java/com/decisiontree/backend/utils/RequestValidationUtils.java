package com.decisiontree.backend.utils;
import com.decisiontree.backend.constants.DecisionModes;
import com.decisiontree.backend.dto.AlternativaDto;
import com.decisiontree.backend.dto.ConfigDto;
import com.decisiontree.backend.dto.DecisionRequest;
import com.decisiontree.backend.exception.BusinessValidationException;

import java.util.*;
import java.util.stream.Collectors;

public final class RequestValidationUtils {

    private RequestValidationUtils() {}

    public static void validate(DecisionRequest request) {
        List<BusinessValidationException.FieldError> errores = new ArrayList<>();

        //1. Request null
        if (request == null) {
            throw new BusinessValidationException("Request inválido", List.of(
                    new BusinessValidationException.FieldError("body", "El body no puede ser null")
            ));
        }

        // 2. modo permitido
        String modo = DecisionModes.normalize(request.getModo());
        Set<String> modosPermitidos = Set.of(
                DecisionModes.RANKING, DecisionModes.SAW,
                DecisionModes.VALOR_ESPERADO, DecisionModes.EXPECTED_VALUE,
                DecisionModes.MAXIMIN, DecisionModes.MAXIMAX, DecisionModes.LAPLACE,
                DecisionModes.HURWICZ, DecisionModes.MINIMAX_REGRET
        );

        if (!modosPermitidos.contains(modo)) {
            errores.add(new BusinessValidationException.FieldError("modo",
                    "Modo no soportado: '" + request.getModo() + "'. Permitidos: " + modosPermitidos));
        }

        // 3. criterios presentes y no vacíos
        if (request.getCriterios() == null || request.getCriterios().isEmpty()) {
            errores.add(new BusinessValidationException.FieldError("criterios",
                    "Debe enviar al menos 1 criterio"));
        }

        // 4. alternativas presentes y no vacías
        if (request.getAlternativas() == null || request.getAlternativas().isEmpty()) {
            errores.add(new BusinessValidationException.FieldError("alternativas",
                    "Debe enviar al menos 1 alternativa"));
        }

        // Si faltan listas basicas, corta para evitar NullPointer en validaciones mas profundas
        if (!errores.isEmpty()) {
            throw new BusinessValidationException("Request inválido", errores);
        }

        // 5. Validar duplicados en nombres de criterios (por campo criterio) y que no esten vacios
        List<String> criterios = new ArrayList<>();
        for (int i = 0; i < request.getCriterios().size(); i++) {
            ConfigDto c = request.getCriterios().get(i);
            String path = "criterios[" + i + "]";
            if (c == null) {
                errores.add(new BusinessValidationException.FieldError(path, "No puede ser null"));
                continue;
            }
            if (isBlank(c.getCriterio())) {
                errores.add(new BusinessValidationException.FieldError(path + ".criterio", "No puede ser vacío"));
            } else {
                criterios.add(c.getCriterio().trim());
            }

            // peso (solo obligatorio en ranking/saw/valor, en criterios tipo maximin puede ignorarse)
            if (DecisionModes.isRankingMode(modo) || DecisionModes.isExpectedValueMode(modo)) {
                if (c.getPeso() == null) {
                    errores.add(new BusinessValidationException.FieldError(path + ".peso", "Es obligatorio en modo " + modo));
                } else if (c.getPeso() < 0) {
                    errores.add(new BusinessValidationException.FieldError(path + ".peso", "No puede ser negativo"));
                }
            }

            // tipo obligatorio (beneficio/costo)
            if (isBlank(c.getTipo())) {
                errores.add(new BusinessValidationException.FieldError(path + ".tipo", "Es obligatorio (beneficio|costo)"));
            } else {
                String t = c.getTipo().trim().toLowerCase(Locale.ROOT);
                if (!t.equals("beneficio") && !t.equals("costo")) {
                    errores.add(new BusinessValidationException.FieldError(path + ".tipo", "Debe ser 'beneficio' o 'costo'"));
                }
            }
        }

        // duplicados criterios
        addDuplicateErrors("criterios", criterios, errores);

        // 6. validar duplicados en nombres de alternativas y nombre no vacio
        List<String> nombresAlt = new ArrayList<>();
        for (int i = 0; i < request.getAlternativas().size(); i++) {
            AlternativaDto a = request.getAlternativas().get(i);
            String path = "alternativas[" + i + "]";
            if (a == null) {
                errores.add(new BusinessValidationException.FieldError(path, "No puede ser null"));
                continue;
            }
            if (isBlank(a.getNombre())) {
                errores.add(new BusinessValidationException.FieldError(path + ".nombre", "No puede ser vacío"));
            } else {
                nombresAlt.add(a.getNombre().trim());
            }
        }
        addDuplicateErrors("alternativas.nombre", nombresAlt, errores);

        // 7. Validar alpha Hurwicz (0..1) cuando aplique
        if (DecisionModes.HURWICZ.equals(modo)) {
            Double alpha = request.getAlpha();
            if (alpha == null) {
                // permitido (en el calculo usan 0.5)
                //errores.add(new FieldError("alpha", "es obligatorio en modo hurwicz xd"));
            } else if (alpha < 0.0 || alpha > 1.0) {
                errores.add(new BusinessValidationException.FieldError("alpha", "Debe estar entre 0 y 1"));
            }
        }

        // 8. Verificar que cada alternativa tenga payoff para todos los criterios y que no tenga extras
        Set<String> criteriosSet = new LinkedHashSet<>(criterios);
        for (int i = 0; i < request.getAlternativas().size(); i++) {
            AlternativaDto a = request.getAlternativas().get(i);
            if (a == null) continue;

            Map<String, Double> valores = a.getValores();
            String path = "alternativas[" + i + "].valores";

            if (valores == null || valores.isEmpty()) {
                errores.add(new BusinessValidationException.FieldError(path, "Debe incluir valores por criterio"));
                continue;
            }

            // faltantes
            for (String c : criteriosSet) {
                if (!valores.containsKey(c)) {
                    errores.add(new BusinessValidationException.FieldError(path, "Falta valor para criterio: " + c));
                } else if (valores.get(c) == null) {
                    errores.add(new BusinessValidationException.FieldError(path + "." + c, "No puede ser null"));
                }
            }

            // extras
            for (String k : valores.keySet()) {
                if (!criteriosSet.contains(k)) {
                    errores.add(new BusinessValidationException.FieldError(path, "Criterio extra no definido: " + k));
                }
            }
        }

        // 9. si hay errores, lanza excepción
        if (!errores.isEmpty()) {
            throw new BusinessValidationException("Request inválido", errores);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static void addDuplicateErrors(String campo, List<String> items,
                                           List<BusinessValidationException.FieldError> errores) {
        Map<String, Long> counts = items.stream()
                .collect(Collectors.groupingBy(s -> s.toLowerCase(Locale.ROOT), LinkedHashMap::new, Collectors.counting()));

        for (Map.Entry<String, Long> e : counts.entrySet()) {
            if (e.getValue() > 1) {
                errores.add(new BusinessValidationException.FieldError(campo,
                        "Duplicado encontrado: '" + e.getKey() + "'"));
            }
        }
    }
}