package com.sitmun.padro.controller;

import com.sitmun.padro.service.DomicilioService;
import com.sitmun.padro.service.PadroService;
import com.sitmun.padro.service.PadronEdiService;
import com.sitmun.padro.service.TarifasTributosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/padron")
public class PadroController {

    private final PadroService padroService;
    private final DomicilioService domicilioService;
    private final PadronEdiService padronEdiService;
    private final TarifasTributosService tarifasTributosService;

    @GetMapping("/habitantes/{municipality}/{nucleus}/{INECode}")
    public ResponseEntity<byte[]> getHabitantesWithFilters(
            @PathVariable String municipality,
            @PathVariable String nucleus,
            @PathVariable String INECode,
            @RequestParam(defaultValue = "") String portal,
            @RequestParam(defaultValue = "") String letterFrom,
            @RequestParam(defaultValue = "") String floor,
            @RequestParam(defaultValue = "") String door
    ) {
        return ResponseEntity.ok(padroService.getHabitantes(municipality, nucleus, INECode, portal, letterFrom, floor, door));
    }

    @GetMapping("/habitantes/{municipality}/{nucleus}/{INECode}/all")
    public ResponseEntity<byte[]> getAllHabitantesByDomicilio(
            @PathVariable String municipality,
            @PathVariable String nucleus,
            @PathVariable String INECode
    ) {
        return ResponseEntity.ok(padronEdiService.getHabitantes(municipality, nucleus, INECode));
    }

    @GetMapping("/tributos/{municipio}/{refCad}")
    public ResponseEntity<byte[]> getTributos(
            @PathVariable String municipio,
            @PathVariable String refCad
    ) {
        return ResponseEntity.ok(tarifasTributosService.getTributos(municipio, refCad));
    }

    @GetMapping("/domicilios/{municipio}")
    public ResponseEntity<String> getDomicilios(
            @PathVariable String municipio,
            @RequestParam(defaultValue = "json") String format
    ) {
        return ResponseEntity.ok(domicilioService.getDomicilios(municipio, format));
    }
}
