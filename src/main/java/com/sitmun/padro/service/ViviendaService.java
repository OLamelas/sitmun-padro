package com.sitmun.padro.service;

import com.sitmun.padro.config.PadroProperties;
import com.sitmun.padro.utils.GestorBD;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ViviendaService {

    private final GestorBD gestorBD;

    public ViviendaService(PadroProperties padroProperties) {
        this.gestorBD = new GestorBD(padroProperties.dbUrl(), padroProperties.dbUsername(), padroProperties.dbPassword());
    }

    public String importViviendas(String municipio, String format) {
        try{
            log.info("tractarPeticio init: Importacio dels domicilis al carrer de sitmun");

            boolean format_html = "html".equalsIgnoreCase(format);
            String responseSTR = "";


            int result = gestorBD.ejecutarProcesoViviendas(municipio);
            if(result >0){
                responseSTR = "Viviendas process finished successfully";
            }else{
                responseSTR = "Error during viviendas importation. Contact administrator";
            }

            if(!format_html){
                responseSTR = "{\"status\":\"" + responseSTR +"\",\"municipio\":\"" + municipio +"\" }";
            }

            log.debug("tractarPeticio end");
            return responseSTR;
        } catch (Exception e) {
            log.error("Error al doGet",e);
        }
        return "{\"status\": Error during viviendas process\"\",\"municipio\":\"" + municipio +"\" }";
    }

}
