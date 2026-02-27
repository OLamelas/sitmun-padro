package com.sitmun.padro.service;

import com.sitmun.padro.config.PadroProperties;
import com.sitmun.padro.dto.ViviendaModel;
import com.sitmun.padro.utils.GestorBD;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ConsultaService {

    private final GestorBD gestorBD;

    private final String aytosRefcatUrl;

    public ConsultaService(PadroProperties padroProperties) {
        this.gestorBD = new GestorBD(padroProperties.dbUrl(), padroProperties.dbUsername(), padroProperties.dbPassword());
        this.aytosRefcatUrl = padroProperties.aytosRefcatUrl();
    }

    public String recuperarInfo(String control, Map<String, String> params) {
        log.debug("init del recuperarInfo");
        String responseSTR = "";

        List<ViviendaModel> hmL = gestorBD.consultaHabitants(control, params, aytosRefcatUrl);

        if (hmL == null) {
            log.debug("Error de recuperar habitants");
            responseSTR = "{\"error\": \"Contacti amb l'administrador\"}";
        } else if (!hmL.isEmpty()) {
            log.debug("Primer elemento encontrado: {}", hmL.get(0).toJson(true));
            responseSTR = hmL.get(0).toJson(true);
        } else {
            log.debug("No existeix element");
            responseSTR = "{\"existe\": \"No\"}";
        }

        return responseSTR;
    }

}
