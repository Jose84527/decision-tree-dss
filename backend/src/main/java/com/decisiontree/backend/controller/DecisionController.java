package com.decisiontree.backend.controller;

import com.decisiontree.backend.dto.DecisionRequest;
import com.decisiontree.backend.dto.DecisionResponse;
import com.decisiontree.backend.service.DecisionEngineService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DecisionController {

    private final DecisionEngineService service;

    public DecisionController(DecisionEngineService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public String health() {
        return "Backend DSS funcionando correctamente";
    }

    @PostMapping("/dss/evaluar")
    public DecisionResponse evaluar(@RequestBody DecisionRequest request) {
        return service.evaluar(request);
    }
}