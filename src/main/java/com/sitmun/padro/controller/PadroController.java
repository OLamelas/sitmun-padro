package com.sitmun.padro.controller;

import com.sitmun.padro.service.PadroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/padron")
public class PadroController {

    private final PadroService padroService;

    @GetMapping("/habitantes/domicilio/{municipality}/{nucleus}/{INECode}")
    public ResponseEntity<byte[]> getHabitantesByDomicilio(
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
}
