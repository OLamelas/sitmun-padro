package com.sitmun.padro.service;

import com.sitmun.padro.config.PadroProperties;
import com.sitmun.padro.dto.ViasModel;
import com.sitmun.padro.utils.GestorBD;
import com.sitmun.padro.utils.PadronManager;
import com.sitmun.padro.utils.SOAPClient;
import com.sitmun.padro.utils.UtilsPadron;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import java.util.List;

@Slf4j
@Service
public class ViaService {

    private final GestorBD gestorBD;
    private final PadronManager padronManager;
    private final PadroProperties padroProperties;


    public ViaService(PadroProperties padroProperties, PadronManager padronManager) {
        this.gestorBD = new GestorBD(padroProperties.dbUrl(), padroProperties.dbUsername(), padroProperties.dbPassword());
        this.padroProperties = padroProperties;
        this.padronManager = padronManager;
    }

    public String getViasByMunicipioOrCode(String municipi, String codiIneVia, String nombreVia, String format) {
        try {
            log.info("tractarPeticio init: Consulta las V�as de un Municipio o una V�a en concreto por c�digo Ine o por nombre. Municipio:" + municipi);
            //municipi -> xxyyy : xx -> provincia | yyy -> municipi
            String smlPar = createParSML(municipi,codiIneVia, nombreVia);

            SOAPClient soap = new SOAPClient(padroProperties.aytosSoapUrl());
            String user = padroProperties.aytosUsername();
            String pwd = padroProperties.aytosPassword();

            String smlSec = soap.getSecurityField(padroProperties.aytosPubKey(),pwd,user,municipi);
            /* --- FUNCIONA --- */
            String smlOpe = "<ope><apl>PAD</apl><tobj>VIA</tobj><cmd>LST</cmd><ver>2.0</ver></ope>";

            String requestXml =  "<![CDATA[<e>" + smlOpe + smlSec + smlPar + "</e>]]>";

            log.info("request_xml:" + requestXml);

            SOAPMessage soapResponse = soap.sendSOAPRequest(soap.createSOAPRequest(requestXml));

            boolean formatHtml = "html".equalsIgnoreCase(format);

            String responseSTR = "";
            SOAPBody body = soapResponse.getSOAPBody();

            NodeList elements = body.getElementsByTagName("servicioReturn");
            boolean validResponse = false;
            String resultDecoded = "";

            if(elements.getLength()>0){
                Node element = elements.item(0);//node <s> amb tags HTML
                String resultEscaped = element.getTextContent();
                resultDecoded = StringEscapeUtils.unescapeHtml4(resultEscaped);
                //Si no conte el tag <l_vias> es que hi ha un error en la resposta
                if(resultDecoded.contains("<l_via>")){
                    validResponse = true;
                }
            }

            if(validResponse){
                List<ViasModel> vmL = padronManager.getViasResponse(resultDecoded);

                if(!vmL.isEmpty()) {
                    if((!codiIneVia.isEmpty() || !nombreVia.isEmpty())){
                        Integer resultCodiIneVia = vmL.get(0).getCodigoIneVia();
                        if(resultCodiIneVia != null){
                            int result = gestorBD.ejecutarProcesoVias(municipi, resultCodiIneVia, vmL);
                            if(result >0){
                                responseSTR = "Proces finalitzat correctament";
                            }else{
                                responseSTR = "Error en el proces d'importacio. Sisplau, contacti amb l'administrador";
                            }
                        }else{
                            responseSTR = "Error via no trobada";
                        }
                    }else{
                        int result = gestorBD.ejecutarProcesoVias(municipi, vmL);
                        //Una vez obtenido la lista de via(s), eliminamos municipio y via i las volvemos a inserir
                        if(result >0){
                            responseSTR = "Proc�s finalitzat correctament";
                        }else{
                            responseSTR = "Error en el proces d'importaci�. Sisplau, contacti amb l'administrador";
                        }
                    }
                }else{
                    responseSTR = "Sense vies a importar";
                }
            }else{
                responseSTR = "Error en la resposta d'aytos. Consulti amb l'administrador";
            }

            log.debug("tractarPeticio end");
            return formatHtml ? responseSTR : "{\"status\":\"" + responseSTR +"\"}";

        } catch (Exception e) {
            log.error("Error al doGet",e);
            return "{\"status\":\"An error ocurred\"}";
        }
    }

    private String createParSML(String municipioParam, String codigoIneVia, String nombreVia) {
        String codigoProvincia = UtilsPadron.getProvincia(municipioParam);
        String codigoMunicipio = UtilsPadron.getMunicipi(municipioParam);

        String smlParams = "<par><codigoProvincia>"+codigoProvincia+"</codigoProvincia><codigoMunicipio>"+codigoMunicipio+"</codigoMunicipio>";

        if(!codigoIneVia.isEmpty()){
            smlParams += "<codigoIneVia>"+codigoIneVia+"</codigoIneVia>";
        }
        else if(!nombreVia.isEmpty()){
            smlParams += "<nombreVia>"+nombreVia+"</nombreVia>";
        }
        smlParams += "</par>";
        return smlParams;
    }
    
}
