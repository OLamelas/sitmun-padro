package com.sitmun.padro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitmun.padro.dto.TributosModel;
import com.sitmun.padro.utils.PadronManager;
import com.sitmun.padro.utils.TributoSOAPClient;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TarifasTributosService {

    private final PadronManager padronManager;

    private String tributoSoapMessage_;
    private String tributoSoapUrl_;
    private String crtokenpad_;
    private HashMap<String, String> conversionMunicipios_;

    public byte[] getTributos (String municipi, String ref_cad) {
        try {
            log.info("tractarPeticio init: obtenir els tributs per aquesta ref_cad");

            /*response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            OutputStream out = response.getOutputStream();*/

            String municipiInescat = conversionMunicipios_.get(municipi);

            String responseSTR = "";
            if (municipiInescat == null) {
                responseSTR= "{\"error\":\"municipi no trobat\"}";
            }

            else {
                TributoSOAPClient soap = new TributoSOAPClient(tributoSoapUrl_);

                String request_xml =  tributoSoapMessage_.replace("{municipio}", municipiInescat);
                request_xml = request_xml.replace("{ref_cad}", ref_cad);

                SOAPMessage soapResponse = soap.sendSOAPRequest(soap.createSOAPRequest(request_xml));

                SOAPBody body = soapResponse.getSOAPBody();
                NodeList elements = body.getElementsByTagName("TributsResponse");
                if(elements.getLength()>0){
                    Element element = (Element)elements.item(0);//node <s> amb tags HTML
                    NodeList tributs = element.getElementsByTagName("Tribut");
                    List<TributosModel> tm_l = padronManager.getTributosResponse(tributs);
                    responseSTR += "{\"results\":";
                    ObjectMapper objectMapper = new ObjectMapper();
                    responseSTR += objectMapper.writeValueAsString(tm_l) + ",\"status\":\"OK\"}";
                }else{
                    responseSTR += "{\"results\":[],\"status\":\"EMPTY\"}";
                }
            }

            log.debug("tractarPeticio end");
            return responseSTR.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error al doGet",e);
        }
        return new byte[0];
    }
}
