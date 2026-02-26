package com.sitmun.padro.service;

import com.sitmun.padro.config.PadroProperties;
import com.sitmun.padro.dto.ViviendaModel;
import com.sitmun.padro.utils.GestorBD;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TributosService {

    private final GestorBD gestorBD;

    private final String aytosRefcatUrl;

    public TributosService(PadroProperties padroProperties) {
        this.gestorBD = new GestorBD(padroProperties.dbUrl(), padroProperties.dbUsername(), padroProperties.dbPassword());
        this.aytosRefcatUrl = padroProperties.aytosRefcatUrl();
    }

    public String recuperarInfo(String control, Map<String, String> params) {

        log.debug("init del recuperarInfo");
        String responseSTR = "";

        List<ViviendaModel> hmL = gestorBD.consultaHabitants(control, params, aytosRefcatUrl);

        boolean executadaConsultaPerPortal = false;

        if (control.equalsIgnoreCase("PL") && (hmL == null || hmL.size() == 0)) {

            executadaConsultaPerPortal = true;

            log.debug("No hem trobat cap planta amb aquests parametres, busquem portal");

            List<ViviendaModel> hm_l_PT = gestorBD.consultaHabitants("PT", params, aytosRefcatUrl);

            if (hm_l_PT == null || hm_l_PT.size() == 0) {
                log.debug("no hem trobat portal, Retornem no existeix");
                responseSTR = "{\"existe\": \"No\"}";
            } else {
                log.debug("Hem trobat portal, busquem a partir de la seva refcad");
                //Retornem la refCad de 20 si hem trobat una i nomes una refcad de 20 a partir de la de 14 del portal
                if (hm_l_PT.get(0).getReferenciaCatastral() != null && !hm_l_PT.get(0).getReferenciaCatastral().isEmpty()) {
                    String result = gestorBD.consultaRefCad(hm_l_PT.get(0).getReferenciaCatastral());
                    if (result.isEmpty()) {
                        responseSTR = hm_l_PT.get(0).toJson(true);
                    } else {
                        Map<String, String> paramsCD = new HashMap<>();
                        paramsCD.put("ref_cat", result);
                        List<ViviendaModel> hmLRC = gestorBD.consultaHabitants("CD", paramsCD, aytosRefcatUrl);

                        if (hmLRC == null || hmLRC.isEmpty()) {
                            responseSTR = "{\"existe\": \"No\"}";
                        } else {
                            responseSTR = hmLRC.get(0).toJson(true);
                        }
                    }
                } else {
                    responseSTR = "{\"existe\": \"No\"}";
                }
            }
        }


        if (!executadaConsultaPerPortal) {
            if (hmL == null) {
                log.debug("Error de recuperar habitants");
                responseSTR = "{\"error\": \"Contacti amb l'administrador\"}";
            } else if (hmL.size() == 0) {
                responseSTR = "{\"existe\": \"No\"}";
            } else if (hmL.size() == 1) {
                responseSTR = hmL.get(0).toJson(true);
            } else {
                log.debug("existeix mes d'un element. Retornem el primer");
                responseSTR = hmL.get(0).toJson(true);
            }
        }

        log.debug("resposta final: " + responseSTR);

        return responseSTR;
    }
}
