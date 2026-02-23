package controller;

import dto.DecisionRequest;
import dto.DecisionResponse;
import org.springframework.web.bind.annotation.*;
import service.DecisionEngineService;

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