package com.sitmun.padro.service;

import com.sitmun.padro.config.PadroProperties;
import com.sitmun.padro.dto.DomiciliosModel;
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
public class DomicilioService {

    private final PadroProperties padroProperties;

    private final PadronManager padronManager;

    private final GestorBD gestorBD;

    public DomicilioService(PadroProperties padroProperties, PadronManager padronManager) {
        this.padroProperties = padroProperties;
        this.padronManager = padronManager;
        this.gestorBD = new GestorBD(padroProperties.dbUrl(), padroProperties.dbUsername(), padroProperties.dbPassword());
    }

    public String importDomicilios(String municipio, String format) {
        try {
            log.info("tractarPeticio init: Consulta  dels domicilis d'un municipi");
            //municipi -> xxyyy : xx -> provincia | yyy -> municipi
            String sml_par = createParSML(municipio);

            SOAPClient soap = new SOAPClient(padroProperties.aytosSoapUrl());
            String user = padroProperties.aytosUsername();
            String pwd = padroProperties.aytosPassword();
            String sml_sec = soap.getSecurityField(padroProperties.aytosPubKey(), pwd, user, municipio);
            /* --- FUNCIONA --- */
            String sml_ope = "<ope><apl>PAD</apl><tobj>DOM</tobj><cmd>LST</cmd><ver>2.0</ver></ope>";

            String request_xml = "<![CDATA[<e>" + sml_ope + sml_sec + sml_par + "</e>]]>";

            SOAPMessage soapResponse = soap.sendSOAPRequest(soap.createSOAPRequest(request_xml));
            boolean format_html = format !=null && format.equalsIgnoreCase("html");
            /*PrintWriter out = response.getWriter();
            response.setStatus(HttpServletResponse.SC_OK);

            if(format !=null && format.equalsIgnoreCase("html")){
                format_html = true;
                response.setContentType("text/html;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");

            }else{
                response.setContentType("application/json;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
            }*/
            String responseSTR = "";
            SOAPBody body = soapResponse.getSOAPBody();

            NodeList elements = body.getElementsByTagName("servicioReturn");
            boolean validResponse = false;
            String resultDecoded = "";

            if (elements.getLength() > 0) {
                Node element = elements.item(0);//node <s> amb tags HTML
                String resultEscaped = element.getTextContent();
                resultDecoded = StringEscapeUtils.unescapeHtml4(resultEscaped);
                //Si no conte el tag <l_vias> es que hi ha un error en la resposta
                if (resultDecoded.indexOf("<l_domicilio>") > -1) {
                    validResponse = true;
                }
            }

            if (validResponse) {
                List<DomiciliosModel> dm_l = padronManager.getDomiciliosResponse(resultDecoded);

                if (dm_l != null && dm_l.size() > 0) {
                    //Una vez obtenido la lista de domicilio(s), eliminamos municipio y los volvemos a inserir
                    int result = gestorBD.ejecutarProcesoDivisionHorizontal(municipio, dm_l);
                    if (result > 0) {
                        responseSTR = "Proc�s finalitzat correctament";
                    } else {
                        responseSTR = "Error en el proc�s d'importaci�. Sisplau, contacti amb l'administrador";
                    }
					/*responseSTR += "{\"results\":[";
					int ind = 0;
					for(DomiciliosModel dm :dm_l){
						responseSTR += dm.toJson(true);
						if(ind<dm_l.size()-1){
							responseSTR+= ",";
						}
						ind++;
					}			
					responseSTR += "],\"status\":\"OK\"}";*/
                } else {
                    responseSTR = "Sense domicilis a importar";
                }
            } else {
                responseSTR = "Error en la resposta d'aytos. Consulti amb l'administrador";
            }

            log.debug("tractarPeticio end");

            if (format_html) {
                return responseSTR;
            } else {
                responseSTR = "{\"status\":\"" + responseSTR + "\",\"municipio\":\"" + municipio + "\" }";
                return responseSTR;
            }

        } catch (Exception e) {
            log.error("Error al doGet", e);
        }
        return municipio;
    }

    public String createParSML(String municipioParam) {
        String codigoProvincia = UtilsPadron.getProvincia(municipioParam);
        String codigoMunicipio = UtilsPadron.getMunicipi(municipioParam);

        String sml_params = "<par><codigoProvincia>" + codigoProvincia + "</codigoProvincia><codigoMunicipio>" + codigoMunicipio + "</codigoMunicipio>";

        sml_params += "</par>";
        return sml_params;
    }
}
