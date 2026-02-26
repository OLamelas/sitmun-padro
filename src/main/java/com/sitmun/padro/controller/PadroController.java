package com.sitmun.padro.controller;

import com.sitmun.padro.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/padron")
public class PadroController {

    private final ViaService viaService;
    private final PadroService padroService;
    private final ViviendaService viviendaService;
    private final ConsultaService consultaService;
    private final DomicilioService domicilioService;
    private final PadronEdiService padronEdiService;
    private final TarifasTributosService tarifasTributosService;

    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

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
    public ResponseEntity<byte[]> getTributos(@PathVariable String municipio, @PathVariable String refCad) {
        return ResponseEntity.ok(tarifasTributosService.getTributos(municipio, refCad));
    }

    @GetMapping("/domicilios/{municipio}")
    public ResponseEntity<String> getDomicilios(
            @PathVariable String municipio,
            @RequestParam(defaultValue = "json") String format
    ) {
        String contentType = "html".equalsIgnoreCase(format) ? "application/html" : "application/json";
        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .body(domicilioService.getDomicilios(municipio, format));
    }

    @PutMapping("/vias")
    public ResponseEntity<String> updateMismatchedVias(
            @RequestParam String municipio,
            @RequestParam(defaultValue = "") String codiIneVia,
            @RequestParam(defaultValue = "") String nombreVia,
            @RequestParam(defaultValue = "json") String format
    ) {
        String contentType = "html".equalsIgnoreCase(format) ? "application/html" : "application/json";
        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .body(viaService.getViasByMunicipioOrCode(municipio, codiIneVia, nombreVia, format));
    }


    @PostMapping("/viviendas/{municipio}")
    public ResponseEntity<String> importViviendas(
            @PathVariable String municipio,
            @RequestParam(defaultValue = "json") String format
    ) {
        String contentType = "html".equalsIgnoreCase(format) ? "application/html" : "application/json";
        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .body(viviendaService.importViviendas(municipio, format));
    }

    @GetMapping("/consulta")
    public ResponseEntity<String> consulta(
            @RequestParam String control,
            @RequestParam String origen,
            @RequestParam(required = false) String refCat,
            @RequestParam(required = false) String codViaIne,
            @RequestParam(required = false) String codPseIne,
            @RequestParam(required = false) String munIne,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) String planta,
            @RequestParam(required = false) String puerta,
            @RequestParam(required = false) String escalera,
            @RequestParam(required = false) String codUpobIne,
            @RequestParam(required = false) String complemento,
            @RequestParam(required = false) String bloque,
            @RequestParam(required = false) String cBloque,
            @RequestParam(defaultValue = "json") String format
    ) {
        if (!origen.equalsIgnoreCase("PMH")) {
            return ResponseEntity.badRequest()
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .body("{\"status\":\"Error\",\"message\":\"Parametro origen erroneo\"}");
        }

        if (!control.equalsIgnoreCase("PT") && !control.equalsIgnoreCase("PL") && !control.equalsIgnoreCase("CD")) {
            return ResponseEntity.badRequest()
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .body("{\"status\":\"Error\",\"message\":\"Parámetro control erróneo\"}");
        }

        if (control.equalsIgnoreCase("CD") && (refCat == null || (refCat.length() != 14 && refCat.length() != 20))) {
            return ResponseEntity.badRequest()
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .body("{\"status\":\"Error\",\"message\":\"Parámetro referencia catastral erróneo\"}");
        }

        if ((control.equalsIgnoreCase("PT") || control.equalsIgnoreCase("PL")) &&
                ((codViaIne == null || codViaIne.isEmpty()) && (codPseIne == null || codPseIne.isEmpty()))) {
            return ResponseEntity.badRequest()
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .body("{\"status\":\"Error\",\"message\":\"Parámetro código INE requerido\"}");
        }

        if ((control.equalsIgnoreCase("PT") || control.equalsIgnoreCase("PL")) && munIne == null) {
            return ResponseEntity.badRequest()
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .body("{\"status\":\"Error\",\"message\":\"Parámetro munIne requerido\"}");
        }

        if ((control.equalsIgnoreCase("PT") || control.equalsIgnoreCase("PL")) && codViaIne != null && numero == null) {
            return ResponseEntity.badRequest()
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .body("{\"status\":\"Error\",\"message\":\"Parámetro número requerido\"}");
        }

        if (control.equalsIgnoreCase("PL") && planta == null && puerta == null && escalera == null) {
            return ResponseEntity.badRequest()
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .body("{\"status\":\"Error\",\"message\":\"Parámetro planta, puerta o escalera requerido\"}");
        }

        String contentType = "html".equalsIgnoreCase(format) ? CONTENT_TYPE_HTML : CONTENT_TYPE_JSON;

        // Crear un mapa con los parámetros
        Map<String, String> parametros = new HashMap<>();
        parametros.put("refCat", refCat);
        parametros.put("cod_via_ine", codViaIne);
        parametros.put("cod_pse_ine", codPseIne);
        parametros.put("mun_ine", munIne);
        parametros.put("numero", numero);
        parametros.put("planta", planta);
        parametros.put("puerta", puerta);
        parametros.put("escalera", escalera);
        parametros.put("cod_upob_ine", codUpobIne);
        parametros.put("complemento", complemento);
        parametros.put("bloque", bloque);
        parametros.put("cbloque", cBloque);

        return ResponseEntity.ok()
                .header(HEADER_CONTENT_TYPE, contentType)
                .body(consultaService.recuperarInfo(control, parametros));
    }
}
