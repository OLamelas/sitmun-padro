package com.sitmun.padro.utils;

import com.sitmun.padro.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PadronManager {
    public List<DomiciliosModel> getDomiciliosResponse(String responseXML){
        /* Formato: <s>
         * 				<sec>...</sec>
         * 				<res>...</res>
         * 				<par>
         * 					<l_domicilio> <domicilio> </domicilio> ... </l_domicilio>
         * 				</par>
         * 			</s>*/
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        ArrayList<DomiciliosModel> dm_l = new ArrayList<DomiciliosModel>();
        //log.debug(responseXML);

        try
        {
            DomiciliosModel dm = new DomiciliosModel();

            log.debug("Inicio del getDomiciliosResponse");

            builder = factory.newDocumentBuilder();
            Document doc = builder.parse( new InputSource( new StringReader( responseXML ) ) );
            log.debug("Despues del parseo de la respuesta getDomiciliosResponse");

            NodeList nodeElements = doc.getElementsByTagName("domicilio");
            log.debug("Hemos encontrado " + nodeElements.getLength() + " elementos");
            for(int indexDomicilios=0;indexDomicilios<nodeElements.getLength();indexDomicilios++){
                dm = new DomiciliosModel();
                Node elementDom = nodeElements.item(indexDomicilios);
                NodeList elementDomChilds = elementDom.getChildNodes();
                for(int index=0;index<elementDomChilds.getLength();index++){
                    Node domicilioXML = elementDomChilds.item(index);
                    String textContent = domicilioXML.getTextContent();
                    String base64textContent = UtilsPadron.fromBase64(textContent);
                    if(domicilioXML.getNodeName().equalsIgnoreCase( "codigoDomicilio" ))
                    {
                        dm.setCodigoDomicilio( textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "cadenaDomicilio" ))
                    {
                        dm.setCadenaDomicilio( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "cadenaDomicilioCompleta" ))
                    {
                        dm.setCadenaDomicilioCompleta( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "codigoEscalera" ))
                    {
                        dm.setCodigoEscalera( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "codigoIneVia" ))
                    {
                        dm.setCodigoIneVia( textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase("codigoPlanta" ))
                    {
                        dm.setCodigoPlanta( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "codigoPortal" ))
                    {
                        dm.setCodigoPortal( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "codigoPostal" ))
                    {
                        dm.setCodigoPostal( textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "codigoPuerta" ))
                    {
                        dm.setCodigoPuerta( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "distrito" ))
                    {
                        dm.setDistrito( textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "nombreEscalera" ))
                    {
                        dm.setNombreEscalera( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "nombrePlanta" ))
                    {
                        dm.setNombrePlanta( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "nombreVia" ))
                    {
                        dm.setNombreVia( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "numeroDesde" ))
                    {
                        dm.setNumeroDesde( textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "letraDesde" ))
                    {
                        dm.setLetraDesde( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "numeroHasta" ))
                    {
                        dm.setNumeroHasta( textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "letraHasta" ))
                    {
                        dm.setLetraHasta( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "referenciaCatastral" ))
                    {
                        dm.setReferenciaCatastral( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "codigoIneNucleo" ))
                    {
                        if(textContent!= null && textContent.length()>0) dm.setCodigoIneNucleo( Integer.parseInt(textContent ));
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "nombreNucleo" ))
                    {
                        dm.setNombreNucleo( base64textContent );
                    }else if(domicilioXML.getNodeName().equalsIgnoreCase( "codigoBloque" ))
                    {
                        dm.setCodigoBloque( base64textContent );
                    }
                }

                log.debug("Afegint domicili: " + dm.toJson(false));
                dm_l.add(dm);
            }

            log.debug("la lista de domicilios contiene :" + dm_l.size() + " elementos");
            return dm_l;

        } catch (Exception e) {
            log.error("Problemas parseando el XML de Domicilios",e);
            return null;
        }

    }
    public List<HabitantesModel> getHabitantesResponse(String responseXML) {
        /* Formato: <s>
         * 				<sec>...</sec>
         * 				<res>...</res>
         * 				<par>
         * 					<l_domicilio> <domicilio> </domicilio> ... </l_domicilio>
         * 				</par>
         * 			</s>*/
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        ArrayList<HabitantesModel> hm_l = new ArrayList<HabitantesModel>();
        //log.debug(responseXML);

        try
        {
            //log.debug("responseXML - "+responseXML);
            HabitantesModel hm = new HabitantesModel();
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse( new InputSource( new StringReader( responseXML ) ) );
            NodeList nodeElements = doc.getElementsByTagName("tercero");
            for(int indexHabitantes=0;indexHabitantes<nodeElements.getLength();indexHabitantes++){
                hm = new HabitantesModel();
                Node elementHab = nodeElements.item(indexHabitantes);
                NodeList elementHabChilds = elementHab.getChildNodes();
                for(int index=0;index<elementHabChilds.getLength();index++){
                    Node habitanteXML = elementHabChilds.item(index);

                    String textContent = habitanteXML.getTextContent();
                    String base64TextContent = UtilsPadron.fromBase64(textContent);

                    if( habitanteXML.getNodeName().equalsIgnoreCase( "nombre" ))
                    {
                        hm.setNombre( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "particula1" ))
                    {
                        hm.setParticula1( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "apellido1" ))
                    {
                        hm.setApellido1( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "particula2" ))
                    {
                        hm.setParticula2( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "apellido2" ))
                    {
                        hm.setApellido2( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "distrito" ))
                    {
                        hm.setDistrito( textContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "session" ))
                    {
                        hm.setSession( textContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "nombreCompleto" ))
                    {
                        hm.setNombreCompleto( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "nombreVia" ))
                    {
                        hm.setNombreVia( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "numeroDesde" ))
                    {
                        hm.setNumeroDesde( textContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "nombrePlanta" ))
                    {
                        hm.setNombrePlanta( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "codigoPlanta" ))
                    {
                        hm.setCodigoPlanta( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "codigoPuerta" ))
                    {
                        hm.setCodigoPuerta( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "referenciaCadastral" ))
                    {
                        hm.setReferenciaCatastral( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "codigoPostal" ))
                    {
                        hm.setCodigoPostal( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "cadenaDomicilio" ))
                    {
                        hm.setCadenaDomicilio( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "cadenaDomicilioCompleta" ))
                    {
                        hm.setCadenaDomicilioCompleta( base64TextContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "codigoDomicilio" ))
                    {
                        hm.setCodigoDomicilio( textContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "codigoHabitante" ))
                    {
                        hm.setCodigoHabitante( textContent );
                    } else if(habitanteXML.getNodeName().equalsIgnoreCase( "letraDesde" ))
                    {
                        hm.setLetraDesde( base64TextContent );
                        //log.debug("letraDesde segona peticio" + ": " + base64TextContent + " / " + textContent);
                    }
                    if(hm.getFieldValue(habitanteXML.getNodeName())== null) {
                        if (habitanteXML.getNodeName().indexOf("fecha")<0 ){
                            try{
                                int textInt = Integer.parseInt(textContent);
                                hm.setNewField(habitanteXML.getNodeName(),textInt+"");
                            }catch(Exception ex){
                                hm.setNewField(habitanteXML.getNodeName(),base64TextContent);
                            }
                        }
                        else{
                            hm.setNewField(habitanteXML.getNodeName(),textContent);
                        }
                    }
                }
                log.debug("Afegint habitant: " + hm.toJson(false));
                hm_l.add(hm);
            }
        } catch (Exception e) {
            log.error("Problemas parseando el XML de Domicilios",e);
        }
        return hm_l;
    }

    public List<ViasModel> getViasResponse(String responseXML){
        /* Formato: <s>
         * 				<sec>...</sec>
         * 				<res>...</res>
         * 				<par>
         * 					<l_domicilio> <domicilio> </domicilio> ... </l_domicilio>
         * 				</par>
         * 			</s>*/
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        ArrayList<ViasModel> vm_l = new ArrayList<ViasModel>();
        log.debug(responseXML);
        try
        {

            builder = factory.newDocumentBuilder();
            Document doc = builder.parse( new InputSource( new StringReader( responseXML ) ) );
            NodeList nodeElements = doc.getElementsByTagName("via");
            for(int indexVias=0;indexVias<nodeElements.getLength();indexVias++){
                ViasModel vm = new ViasModel();
                Node elementDom = nodeElements.item(indexVias);
                NodeList elementDomChilds = elementDom.getChildNodes();
                for(int index=0;index<elementDomChilds.getLength();index++){
                    Node viaXML = elementDomChilds.item(index);
                    String textContent = viaXML.getTextContent();
                    String base64textContent = UtilsPadron.fromBase64(textContent);
                    if(viaXML.getNodeName().equalsIgnoreCase( "codigoVia" ))
                    {
                        if(textContent!= null && textContent.length()>0) vm.setCodigoVia(Integer.parseInt(textContent ));
                    }else if(viaXML.getNodeName().equalsIgnoreCase( "codigoIneVia" ))
                    {
                        if(textContent!= null && textContent.length()>0) vm.setCodigoIneVia( Integer.parseInt(textContent) );
                    }else if(viaXML.getNodeName().equalsIgnoreCase( "nombreVia" ))
                    {
                        vm.setNombreVia( base64textContent );
                    }else if(viaXML.getNodeName().equalsIgnoreCase("situacionVia" ))
                    {
                        vm.setSituacionVia( base64textContent );
                    }else if(viaXML.getNodeName().equalsIgnoreCase( "codigoTipoVia" ))
                    {
                        vm.setCodigoTipoVia( base64textContent );
                    }else if(viaXML.getNodeName().equalsIgnoreCase( "nombreTipoVia" ))
                    {
                        vm.setNombreTipoVia( base64textContent );
                    }else if(viaXML.getNodeName().equalsIgnoreCase( "codigoNucleo" ))
                    {
                        if(textContent!= null && textContent.length()>0) vm.setCodigoNucleo( Integer.parseInt(textContent ));
                    }else if(viaXML.getNodeName().equalsIgnoreCase( "codigoIneNucleo" ))
                    {
                        if(textContent!= null && textContent.length()>0) vm.setCodigoIneNucleo( Integer.parseInt(textContent ));
                    }else if(viaXML.getNodeName().equalsIgnoreCase( "nombreNucleo" ))
                    {
                        vm.setNombreNucleo( base64textContent );
                    }
                }
                vm_l.add(vm);
            }
        } catch (Exception e) {
            log.error("Problemas parseando el XML de vias",e);
        }
        return vm_l;
    }

    public List<TributosModel> getTributosResponse(NodeList nodeTibuts){
        log.debug("Inici del getTributosResponse");

        ArrayList<TributosModel> dm_l = new ArrayList<TributosModel>();

        try
        {
            log.debug("Inicio del getTributosResponse");

            log.debug("Hemos encontrado " + nodeTibuts.getLength() + " elementos");
            for(int indexTributs=0;indexTributs<nodeTibuts.getLength();indexTributs++){
                TributosModel dm = new TributosModel();
                Node elementDom = nodeTibuts.item(indexTributs);
                NodeList elementDomChilds = elementDom.getChildNodes();
                for(int index=0;index<elementDomChilds.getLength();index++){
                    Node tributoXML = elementDomChilds.item(index);

                    if(tributoXML.getNodeName().equalsIgnoreCase( "incidencia" ))
                    {
                        String textContent = tributoXML.getTextContent();
                        dm.setIncidencia(textContent);
                    }

                    if(tributoXML.getNodeName().equalsIgnoreCase( "domicili" ))
                    {
                        String textContent = tributoXML.getTextContent();
                        dm.setDomicili(textContent);

                    }else if(tributoXML.getNodeName().equalsIgnoreCase( "Conceptes" ))
                    {
                        //recuperem conceptes
                        Element element = (Element) tributoXML;
                        NodeList nodeElementsConceptes = element.getElementsByTagName("Concepte");
                        log.debug("Hemos encontrado " + nodeElementsConceptes.getLength() + " conceptes");
                        List<ConcepteModel> list_conceptes = new ArrayList<ConcepteModel>();

                        for(int indexConceptes=0;indexConceptes<nodeElementsConceptes.getLength();indexConceptes++){

                            ConcepteModel con = new ConcepteModel();

                            Node concepteXML = nodeElementsConceptes.item(indexConceptes);
                            NodeList elementConceptesChilds = concepteXML.getChildNodes();
                            for(int indexElementsConcepte=0;indexElementsConcepte<elementConceptesChilds.getLength();indexElementsConcepte++){
                                Node concepteNodeXML = elementConceptesChilds.item(indexElementsConcepte);

                                if(concepteNodeXML.getNodeName().equalsIgnoreCase( "IDConcepte" ))
                                {
                                    String textCon = concepteNodeXML.getTextContent();
                                    con.setIdConcepte(textCon);

                                }else if(concepteNodeXML.getNodeName().equalsIgnoreCase( "desConcepte" ))
                                {
                                    String textCon = concepteNodeXML.getTextContent();
                                    con.setDesConcepte(textCon);
                                }
                                else if(concepteNodeXML.getNodeName().equalsIgnoreCase( "Tarifes" ))
                                {
                                    List<TarifaModel> list_tarifa = new ArrayList<TarifaModel>();

                                    Element elementTarifes = (Element) concepteNodeXML;
                                    NodeList nodeElementsTarifes = elementTarifes.getElementsByTagName("Tarifa");
                                    log.debug("Hemos encontrado " + nodeElementsTarifes.getLength() + " tarifes");

                                    for(int indexElementsTarifa=0;indexElementsTarifa<nodeElementsTarifes.getLength();indexElementsTarifa++){

                                        TarifaModel tar = new TarifaModel();

                                        Node tarifaChildsXML = nodeElementsTarifes.item(indexElementsTarifa);
                                        NodeList elementTarifesChilds = tarifaChildsXML.getChildNodes();

                                        for(int indexAtributsTarifa=0;indexAtributsTarifa<elementTarifesChilds.getLength();indexAtributsTarifa++) {
                                            Node tarifaNodeXML = elementTarifesChilds.item(indexAtributsTarifa);

                                            if(tarifaNodeXML.getNodeName().equalsIgnoreCase( "IDTarifa" ))
                                            {
                                                String textCon = tarifaNodeXML.getTextContent();
                                                tar.setIdTarifa(textCon);

                                            }else if(tarifaNodeXML.getNodeName().equalsIgnoreCase( "desTarifa" ))
                                            {
                                                String textCon = tarifaNodeXML.getTextContent();
                                                tar.setDesTarifa(textCon);
                                            }
                                            else if(tarifaNodeXML.getNodeName().equalsIgnoreCase( "unitats" ))
                                            {
                                                String textCon = tarifaNodeXML.getTextContent();
                                                tar.setUnitats(textCon);
                                            }
                                        }

                                        list_tarifa.add(tar);

                                    }

                                    con.setTarifes(list_tarifa);
                                }
                            }
                            list_conceptes.add(con);
                        }
                        //Afegim els conceptes als tributs
                        dm.setConceptes(list_conceptes);

                    }else if(tributoXML.getNodeName().equalsIgnoreCase( "Factures" )){
                        Element element = (Element) tributoXML;
                        //Recuperem factures
                        NodeList nodeElementsFactures = element.getElementsByTagName("Factura");
                        log.debug("Hemos encontrado " + nodeElementsFactures.getLength() + " facturas");

                        List<FacturaModel> list_fra = new ArrayList<FacturaModel>();

                        for(int indexFra=0;indexFra<nodeElementsFactures.getLength();indexFra++){

                            FacturaModel fra = new FacturaModel();

                            Node fraXML = nodeElementsFactures.item(indexFra);
                            NodeList elementsFraChilds = fraXML.getChildNodes();
                            for(int indexElementsFra=0;indexElementsFra<elementsFraChilds.getLength();indexElementsFra++){
                                Node fraNodeXML = elementsFraChilds.item(indexElementsFra);
                                String textFra = fraNodeXML.getTextContent();

                                if(fraNodeXML.getNodeName().equalsIgnoreCase( "IDFactura" ))
                                {
                                    fra.setIdFactura(textFra);

                                }else if(fraNodeXML.getNodeName().equalsIgnoreCase( "dataFactura" ))
                                {
                                    fra.setDataFactura(textFra);
                                }
                                else if(fraNodeXML.getNodeName().equalsIgnoreCase( "IDConcepte" ))
                                {
                                    fra.setIdConcepte(textFra);
                                }
                                else if(fraNodeXML.getNodeName().equalsIgnoreCase( "desConcepte" ))
                                {
                                    fra.setDesConcepte(textFra);
                                }
                                else if(fraNodeXML.getNodeName().equalsIgnoreCase( "importFactura" ))
                                {
                                    fra.setImportFactura(new BigDecimal(textFra));
                                }
                                else if(fraNodeXML.getNodeName().equalsIgnoreCase( "pendent" ))
                                {
                                    fra.setPendent(Boolean.parseBoolean(textFra));
                                }
                            }
                            list_fra.add(fra);
                        }
                        //Afegim les factures al tribut
                        dm.setFactures(list_fra);
                    }
                }
                dm_l.add(dm);
            }

            log.debug("la lista de tributos contiene :" + dm_l.size() + " elementos");
            return dm_l;

        } catch (Exception e) {
            log.error("Problemas parseando el XML de tributos",e);
            return null;
        }

    }
}
