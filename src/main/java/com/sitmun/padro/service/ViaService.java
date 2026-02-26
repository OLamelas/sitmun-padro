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
            String sml_par = createParSML(municipi,codiIneVia, nombreVia);

            SOAPClient soap = new SOAPClient(padroProperties.aytosSoapUrl());
            String user = padroProperties.aytosUsername();
            String pwd = padroProperties.aytosPassword();

            String sml_sec = soap.getSecurityField(padroProperties.aytosPubKey(),pwd,user,municipi);
            /* --- FUNCIONA --- */
            String sml_ope = "<ope><apl>PAD</apl><tobj>VIA</tobj><cmd>LST</cmd><ver>2.0</ver></ope>";

            String request_xml =  "<![CDATA[<e>" + sml_ope + sml_sec + sml_par + "</e>]]>";

            log.info("request_xml:" + request_xml);

            SOAPMessage soapResponse = soap.sendSOAPRequest(soap.createSOAPRequest(request_xml));
            /*PrintWriter out = response.getWriter();
            response.setStatus(HttpServletResponse.SC_OK);*/

            boolean format_html = "html".equalsIgnoreCase(format);
            /*if(request.getParameter("format") !=null && request.getParameter("format").equalsIgnoreCase("html")){
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

            if(elements.getLength()>0){
                Node element = elements.item(0);//node <s> amb tags HTML
                String resultEscaped = element.getTextContent();
                resultDecoded = StringEscapeUtils.unescapeHtml4(resultEscaped);
                //Si no conte el tag <l_vias> es que hi ha un error en la resposta
                if(resultDecoded.indexOf("<l_via>")>-1){
                    validResponse = true;
                }
            }

            if(validResponse){
                List<ViasModel> vm_l = padronManager.getViasResponse(resultDecoded);

                if(vm_l.size()>0) {
                    if((codiIneVia.length()> 0 || nombreVia.length()>0)){
                        Integer resultCodiIneVia = vm_l.get(0).getCodigoIneVia();
                        if(resultCodiIneVia != null){
                            int result = gestorBD.ejecutarProcesoVias(municipi, resultCodiIneVia, vm_l);
                            if(result >0){
                                responseSTR = "Proc�s finalitzat correctament";
                            }else{
                                responseSTR = "Error en el proces d'importaci�. Sisplau, contacti amb l'administrador";
                            }
                        }else{
                            responseSTR = "Error via no trobada";
                        }
                    }else{
                        int result = gestorBD.ejecutarProcesoVias(municipi, vm_l);
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
            return format_html ? responseSTR : "{\"status\":\"" + responseSTR +"\"}";

        } catch (Exception e) {
            log.error("Error al doGet",e);
            return "{\"status\":\"An error ocurred\"}";
        }
    }

    private String createParSML(String municipioParam, String CodigoIneVia, String nombreVia) {
        String codigoProvincia = UtilsPadron.getProvincia(municipioParam);
        String codigoMunicipio = UtilsPadron.getMunicipi(municipioParam);

        String smlParams = "<par><codigoProvincia>"+codigoProvincia+"</codigoProvincia><codigoMunicipio>"+codigoMunicipio+"</codigoMunicipio>";

        if(!CodigoIneVia.isEmpty()){
            smlParams += "<codigoIneVia>"+CodigoIneVia+"</codigoIneVia>";
        }
        else if(!nombreVia.isEmpty()){
            smlParams += "<nombreVia>"+nombreVia+"</nombreVia>";
        }
        smlParams += "</par>";
        return smlParams;
    }
    
}
