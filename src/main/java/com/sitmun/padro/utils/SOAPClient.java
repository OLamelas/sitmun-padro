package com.sitmun.padro.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;

import java.util.Date;



import javax.xml.soap.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SOAPClient {
	private String urlAytos ="https://urlpadro/services/Ci";
	public SOAPClient(String url) {
		super();
		this.urlAytos = url;
	}
	
	public String getSecurityField(String publicKey, String pwd, String usr,  String codiEntMunicipi ) throws UnsupportedEncodingException
	{
		try {
			String municipi = UtilsPadron.getMunicipi(codiEntMunicipi);
			// FECHA
			//Obtenemos el CREATED
			Date currentDate = new Date();
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			String CREATED =df.format(currentDate);
            log.debug("CREATED '{}'", CREATED);
			
			// NONCE
			SecureRandom random = new SecureRandom();		
			String NONCE = Long.toString(random.nextLong());// O int?
			log.debug("NONCE '" + NONCE + "'");
			
			//Obtener el TOKEN
			//string token = Convert.ToBase64String(calcularHash.CalcularHashByte(nonce + fecha + "llave1", eMunicipalClassLibrary.Algorithm.SHA512)).ToString();
			String origen = NONCE + CREATED + publicKey;
			log.debug("origen '" + origen + "'");
			
			//La funci�n codificar realiza una codificaci�n SHA512 de origen y devuelve, 
			//en Base64, dicha codificaci�n, eliminando los saltos de l�nea.
			byte[] TOKEN = UtilsPadron.calculateHash(origen,"512");
			log.debug("TOKEN '" + TOKEN + "'");
			String TOKEN64 = UtilsPadron.toBase64(TOKEN);
			log.debug("TOKEN64 '" + TOKEN64 + "'");
			
			
			byte[] PWD = UtilsPadron.calculateHash(pwd,"1");
			log.debug("PWD '" + PWD + "'");
			String PWD64 = UtilsPadron.toBase64(PWD);
			log.debug("PWD64 '" + PWD64 + "'");

			return "<sec><cli>ACCEDE</cli><org>0</org><ent>"+municipi+"</ent><usu>"+ usr +"</usu>"
					+ "<pwd>" + PWD64 + "</pwd>"
					+ "<fecha>" + CREATED + "</fecha>"
					+ "<nonce>" + NONCE + "</nonce>"
					+ "<token>" + TOKEN64 + "</token></sec>";
			
		} catch(UnsupportedEncodingException uee) {
			log.error("Error a getSecurityField",uee);
			throw uee;
		}
		
	} 
	
	public SOAPMessage sendSOAPRequest(SOAPMessage message){
		// Create SOAP Connection
        
		try {
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();		
	        SOAPConnection soapConnection = soapConnectionFactory.createConnection();
	        
	        URL soapEndPoint = new URL(this.urlAytos);
	        URLConnection urlConnection =  soapEndPoint.openConnection(); 		
	        urlConnection.setConnectTimeout(60000); 		
	        urlConnection.setReadTimeout(60000);	
	        
	        // Send SOAP Message to SOAP Server       
	        SOAPMessage soapResponse = soapConnection.call(message, soapEndPoint);	
	        // Process the SOAP Response
	        soapConnection.close();	        
	        return soapResponse;
		} catch (UnsupportedOperationException e) {
			log.error("Error operaci� no suportada",e);
		} catch (SOAPException e) {
			log.error("Error SOAP",e);
		}catch (Exception e) {
			log.error("Error general",e);
		}
        return null;       
	}
	
	public SOAPMessage createSOAPRequest(String xmlData) {
		try {
			// Load the XML text into a DOM Document			
			// Use SAAJ to convert Document to SOAPElement
			// Create SoapMessage
			final MessageFactory msgFactory = MessageFactory.newInstance();
			final SOAPMessage message = msgFactory.createMessage();
			SOAPPart soapPart = message.getSOAPPart();

	        String serverURI = "http://ci.sw.aytos";

	        // SOAP Envelope
	        SOAPEnvelope envelope = soapPart.getEnvelope();
	        envelope.addNamespaceDeclaration("ci", serverURI);
			final SOAPBody soapBody = message.getSOAPBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("servicio", "ci");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("in0");
			soapBodyElem1.addTextNode(xmlData);			
			
			// Mime Headers
			final MimeHeaders headers = message.getMimeHeaders();
			
			headers.addHeader("SOAPAction", this.urlAytos + "servicio");

			message.saveChanges();
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			message.writeTo(baos);  
    		log.debug("request to SOAP server= "+baos);  
			return message;
	        
		} catch (SOAPException e) {
			log.error("Error SOAP",e);
		} catch (IOException e) {
			log.error("Error IO",e);		
		}catch (Exception e) {
			log.error("Error general",e);
		}
		return null;
    }

    /**
     * Method used to print the SOAP Response
     */
    public void printSOAPResponse(SOAPMessage soapResponse) {
    	try {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
    		soapResponse.writeTo(baos);  
    		log.debug("response from SOAP server= "+baos);  	        	
		} catch (SOAPException e) {
			log.error("Error amb el SOAP",e);
		} catch (Exception e) {
			log.error("Error general",e);
		}
    }
	
}
