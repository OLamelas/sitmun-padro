package com.sitmun.padro.service;

import com.sitmun.padro.config.PadroProperties;
import com.sitmun.padro.dto.SOAPClient;
import com.sitmun.padro.dto.DomiciliosModel;
import com.sitmun.padro.dto.HabitantesModel;
import com.sitmun.padro.utils.PadronManager;
import com.sitmun.padro.utils.UtilsPadron;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import javax.xml.soap.*;
import org.w3c.dom.*;
import org.apache.commons.text.StringEscapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Node;

@Slf4j
@Service
@RequiredArgsConstructor
public class PadroService {

    private final PadroProperties padroProperties;

    private final PadronManager padronManager;

    public String createParSML(String municipioParam, String nucleoParam,String codigoIneParam, String portalParam, String letraDesde) {
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

        sml_params += "<codigoIneVia>"+codigoIneVia+"</codigoIneVia>";
        sml_params += "</par>";
        return sml_params;
    }


    private void tractarPrimeraPeticio (String municipi, String nucli, String codiIne, String portal, String letraDesde, String planta, String porta, HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("tractarPeticio init: obtenir els domicilis per aquesta adreça");
            //municipi -> xxyyy : xx -> provincia | yyy -> municipi
            String sml_par = createParSML(municipi,nucli,codiIne,portal, letraDesde);

            SOAPClient soap = new SOAPClient(padroProperties.aytosSoapUrl());
            String user = padroProperties.aytosUsername();
            String pwd = padroProperties.aytosPassword();
            String sml_sec = soap.getSecurityField(padroProperties.aytosPubKey(),pwd,user,municipi);
            /* --- FUNCIONA --- */
            String sml_ope = "<ope><apl>PAD</apl><tobj>DOM</tobj><cmd>LST</cmd><ver>2.0</ver></ope>";

            String request_xml =  "<![CDATA[<e>" + sml_ope + sml_sec + sml_par + "</e>]]>";

            SOAPMessage soapResponse = soap.sendSOAPRequest(soap.createSOAPRequest(request_xml));
            OutputStream out = response.getOutputStream();
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");

            String responseSTR = "";
            SOAPBody body = soapResponse.getSOAPBody();
            NodeList elements = body.getElementsByTagName("servicioReturn");
            log.debug("elements" + ": " + elements.getLength());

            if(elements.getLength()>0){
                Node element = elements.item(0);//node <s> amb tags HTML
                String resultEscaped = element.getTextContent();
                String resultDecoded = StringEscapeUtils.unescapeHtml4(resultEscaped);
                List<DomiciliosModel> dm_l = padronManager.getDomiciliosResponse(resultDecoded);
                responseSTR += "{\"results\":[";
                int ind= 0;
                log.debug("portal primeraPeticio" + ": " + portal);
                log.debug("letraDesde primeraPeticio" + ": " + letraDesde);
                log.debug("planta primeraPeticio" + ": " + planta);
                log.debug("porta primeraPeticio" + ": " + porta);

                for(DomiciliosModel dm :dm_l){
                    log.debug("getCodigoPortal domicilios" + ": " + dm.getCodigoPortal());

                    if((portal.isEmpty() || dm.getNumeroDesde().equalsIgnoreCase(portal))
                            && ((letraDesde.isEmpty() && dm.getCodigoPortal().isEmpty()) || dm.getCodigoPortal().equalsIgnoreCase(letraDesde))){

                        String resultado = tractarSegonaPeticio(dm.getCodigoDomicilio(), municipi, letraDesde, planta, porta);

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
                    }
                    log.debug("respuesta primeraPeticio" + ": " + responseSTR);

                }
                responseSTR += "],\"status\":\"OK\"}";
            }else{
                responseSTR += "{\"results\":[],\"status\":\"EMPTY\"}";
            }
            out.write(responseSTR.getBytes("UTF-8"));
            log.debug("tractarPeticio end");
        } catch (Exception e) {
            log.error("Error al doGet",e);
        }
    }

    private String tractarSegonaPeticio(String codigoDomicilio, String codigoMunicipio, String letraDesde, String planta, String porta) {
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

                String portaAmbZeros = addZerosToFront(porta, 4);

                for(HabitantesModel hm :hm_l){
                    log.debug("getCodigoPlanta habitantes" + ": " + hm.getCodigoPlanta());
                    log.debug("getCodigoPuerta habitantes" + ": " + hm.getCodigoPuerta());
                    log.debug("planta segonaPeticio" + ": " + planta);
                    log.debug("porta segonaPeticio" + ": " + porta);
                    log.debug("porta con zeros" + ": " + portaAmbZeros);

                    if((planta.isEmpty() || hm.getCodigoPlanta().equalsIgnoreCase(planta))
                            && (porta.isEmpty() || hm.getCodigoPuerta().equalsIgnoreCase(portaAmbZeros))) {
                        responseSTR+= hm.toJson(true);

                        if(ind<hm_l.size()-1){
                            responseSTR+= ",";
                        }
                        ind++;
                    }
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
    /* @param propertiesPath
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Properties readProperties(String propertiesPath)
            throws FileNotFoundException, IOException {
        log.debug("readProperties propertiesPath: " + propertiesPath);
        File f = new File(propertiesPath);
        log.debug("readProperties file: " + f.getAbsolutePath());

        FileInputStream is = new FileInputStream(f);
        Properties props = new Properties();

        try {
            props.load(is);

        } catch (IOException e) {
            log.error("Error cargando conf.properties", e);
            return null;
        }
        return props;
    }




    private void testXML(String sml_ope, String sml_params){
        log.info("------ testXML init ------");
        try {
            SOAPClient soap = new SOAPClient(padroProperties.aytosSoapUrl());
            String user = padroProperties.aytosUsername();
            String pwd = padroProperties.aytosPassword();
            String sml_sec = soap.getSecurityField(padroProperties.aytosPubKey(),pwd,user,"110");
            String request_xml =  "<![CDATA[<e>" + sml_ope + sml_sec + sml_params + "</e>]]>";
            SOAPMessage soapResponse = soap.sendSOAPRequest(soap.createSOAPRequest(request_xml));
            soap.printSOAPResponse(soapResponse);
        } catch (UnsupportedEncodingException e) {
            log.error("Error al testXML",e);
        }
        log.info("------ ----- ------");
    }

}
