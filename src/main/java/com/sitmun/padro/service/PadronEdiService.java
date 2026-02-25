package com.sitmun.padro.service;

import com.sitmun.padro.config.PadroProperties;
import com.sitmun.padro.dto.DomiciliosModel;
import com.sitmun.padro.dto.HabitantesModel;
import com.sitmun.padro.utils.SOAPClient;
import com.sitmun.padro.utils.PadronManager;
import com.sitmun.padro.utils.UtilsPadron;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PadronEdiService {

    private final PadroProperties padroProperties;
    
    private final PadronManager padronManager;

    public String createParSML(String municipioParam, String nucleoParam,String codigoIneParam) {
        String codigoProvincia = UtilsPadron.getProvincia(municipioParam);
        String codigoMunicipio = UtilsPadron.getMunicipi(municipioParam);

        String codigoIneEntidadColectiva = UtilsPadron.getIneEntitatColectiva(nucleoParam);
        String codigoIneEntidadSingular = UtilsPadron.getIneEntitatSingular(nucleoParam);
        if(codigoIneEntidadColectiva.equalsIgnoreCase("00")){
            codigoIneEntidadColectiva = "";
            if(codigoIneEntidadSingular.equalsIgnoreCase("00")){
                codigoIneEntidadSingular = "";
            }
        }

        String codigoIneNucleo = UtilsPadron.getIneNucleo(nucleoParam);
        if(codigoIneNucleo.equalsIgnoreCase("00")){
            codigoIneNucleo = "";
        }
        //codiIne -> xxxxx : fet un int per treure els posibles 0 que hi hagi davant.
        String codigoIneVia = Integer.parseInt(codigoIneParam)+"";
        //portal -> xxxx : fet un int per treure els posibles 0 que hi hagi davant.

        String sml_params = "<par><codigoProvincia>"+codigoProvincia+"</codigoProvincia><codigoMunicipio>"+codigoMunicipio+"</codigoMunicipio>";
        if(codigoIneEntidadColectiva.length()>0){
            sml_params += "<codigoIneEntidadColectiva>"+codigoIneEntidadColectiva+"</codigoIneEntidadColectiva>";
        }
        else if(codigoIneEntidadSingular.length()>0){
            sml_params += "<codigoIneEntidadSingular>"+codigoIneEntidadSingular+"</codigoIneEntidadSingular>";
        }

        sml_params += "<codigoINEPseudovia>"+codigoIneVia+"</codigoINEPseudovia>";
        sml_params += "</par>";
        return sml_params;
    }


    public byte[] getHabitantes(String municipi, String nucli, String codiIne) {
        try {
            log.info("tractarPeticio init: obtenir els domicilis per aquesta adreça");
            //municipi -> xxyyy : xx -> provincia | yyy -> municipi
            String sml_par = createParSML(municipi,nucli,codiIne);

            SOAPClient soap = new SOAPClient(padroProperties.aytosSoapUrl());
            String user = padroProperties.aytosUsername();
            String pwd = padroProperties.aytosPassword();
            String sml_sec = soap.getSecurityField(padroProperties.aytosPubKey(),pwd,user,municipi);
            /* --- FUNCIONA --- */
            String sml_ope = "<ope><apl>TER</apl><tobj>DOM</tobj><cmd>LST</cmd><ver>2.0</ver></ope>";

            String request_xml =  "<![CDATA[<e>" + sml_ope + sml_sec + sml_par + "</e>]]>";


            SOAPMessage soapResponse = soap.sendSOAPRequest(soap.createSOAPRequest(request_xml));

            String responseSTR = "";
            SOAPBody body = soapResponse.getSOAPBody();
            NodeList elements = body.getElementsByTagName("servicioReturn");
            log.debug("elements" + ": " + elements.getLength());

            if(elements.getLength()>0){
                Node element = elements.item(0);//node <s> amb tags HTML
                String resultEscaped = element.getTextContent();
                String resultDecoded = StringEscapeUtils.unescapeHtml4(resultEscaped);
                log.debug("resultDecoded: " + resultDecoded);

                List<DomiciliosModel> dm_l = padronManager.getDomiciliosResponse(resultDecoded);
                responseSTR += "{\"results\":[";
                int ind= 0;

                for(DomiciliosModel dm :dm_l){
                    log.debug("getCodigoPortal domicilios" + ": " + dm.getCodigoPortal());

                    String resultado = tractarSegonaPeticio(dm.getCodigoDomicilio(), municipi);

                    if (resultado != null && !resultado.isEmpty()) {
                        // El resultado contiene algún texto.
                        if(ind < dm_l.size() && ind > 0){
                            responseSTR+= ",";
                        }
                        responseSTR += resultado;
                        ind++;
                    } else {
                        // El resultado está vacío o nulo.
                        System.out.println("La segonaPeticio no devolvió ningún texto.");
                    }

                    log.debug("respuesta primeraPeticio" + ": " + responseSTR);

                }
                responseSTR += "],\"status\":\"OK\"}";
            }else{
                responseSTR += "{\"results\":[],\"status\":\"EMPTY\"}";
            }
            log.debug("tractarPeticio end");
            return responseSTR.getBytes("UTF-8");
        } catch (Exception e) {
            log.error("Error al doGet",e);
            return new byte[0];
        }
    }

    private String tractarSegonaPeticio(String codigoDomicilio, String codigoMunicipio) {
        String responseSTR = "";
        try {
            SOAPClient soap = new SOAPClient(padroProperties.aytosSoapUrl());
            String user = padroProperties.aytosUsername();
            String pwd = padroProperties.aytosPassword();
            String sml_sec = soap.getSecurityField(padroProperties.aytosPubKey(),pwd,user,codigoMunicipio);

            /* --- FUNCIONA --- */
            String sml_ope = "<ope><apl>PAD</apl><tobj>HAB</tobj><cmd>CONSULTAHOJAS</cmd><ver>2.0</ver></ope>";
            String sml_params = "<par><codigoDomicilio>"+codigoDomicilio+"</codigoDomicilio></par>";

            String request_xml =  "<![CDATA[<e>" + sml_ope + sml_sec + sml_params + "</e>]]>";

            SOAPMessage soapResponse = soap.sendSOAPRequest(soap.createSOAPRequest(request_xml));


            SOAPBody body = soapResponse.getSOAPBody();
            NodeList elements = body.getElementsByTagName("servicioReturn");
            if(elements.getLength()>0){
                Node element = elements.item(0);//node <s> amb tags HTML
                String resultEscaped = element.getTextContent();
                String resultDecoded = StringEscapeUtils.unescapeHtml4(resultEscaped);
                List<HabitantesModel> hm_l= padronManager.getHabitantesResponse(resultDecoded);
                int ind = 0;



                for(HabitantesModel hm :hm_l){
                    log.debug("getCodigoPlanta habitantes" + ": " + hm.getCodigoPlanta());
                    log.debug("getCodigoPuerta habitantes" + ": " + hm.getCodigoPuerta());


                    responseSTR+= hm.toJson(true);

                    if(ind<hm_l.size()-1){
                        responseSTR+= ",";
                    }
                    ind++;

                    log.debug("respuesta segonaPeticio" + ": " + responseSTR);
                }
            }
        } catch (IOException e) {
            log.error("Ups, això ja es més cosa del servlet,no he pogut obrir el OutputStream de la resposta. Raro raro",e);
        } catch (SOAPException e) {
            log.error("Problema amb la petició SOAP. Repassa que estigui ben formada.",e);
        } catch (Exception e) {
            log.error("Excepció inexplicable :( ja l'has cagat!",e);
        }
        return responseSTR;
    }

    public static String addZerosToFront(String str, int targetLength) {
        if (str.length() >= targetLength) {
            return str;
        } else {
            int zerosToAdd = targetLength - str.length();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < zerosToAdd; i++) {
                sb.append('0');
            }
            sb.append(str);
            return sb.toString();
        }
    }
    
}
