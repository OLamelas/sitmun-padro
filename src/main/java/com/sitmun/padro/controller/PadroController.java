package com.sitmun.padro.controller;

import com.sitmun.padro.service.PadroService;
import com.sitmun.padro.service.PadronEdiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/padron")
public class PadroController {

    private final PadroService padroService;

    private final PadronEdiService padronEdiService;

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
        return ResponseEntity.ok(padroService.tractarPrimeraPeticio(municipality, nucleus, INECode, portal, letterFrom, floor, door));
    }

    @GetMapping("/habitantes/{municipality}/{nucleus}/{INECode}/all")
    public ResponseEntity<byte[]> getAllHabitantesByDomicilio(
            @PathVariable String municipality,
            @PathVariable String nucleus,
            @PathVariable String INECode
    ) {
        return ResponseEntity.ok(padronEdiService.tractarPrimeraPeticio(municipality, nucleus, INECode));
    }
}
