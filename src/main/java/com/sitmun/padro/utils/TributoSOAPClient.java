package com.sitmun.padro.utils;

import lombok.extern.slf4j.Slf4j;

import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

@Slf4j
public class TributoSOAPClient {
	private String TRIBUTO_AYTOS="";
	
	public TributoSOAPClient(String url) {
		super();
		this.TRIBUTO_AYTOS = url;
	}
	
	
	
	public SOAPMessage sendSOAPRequest(SOAPMessage message){
		// Create SOAP Connection
        
		try {
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();		
	        SOAPConnection soapConnection = soapConnectionFactory.createConnection();
	        
	        URL soapEndPoint = new URL(this.TRIBUTO_AYTOS);  		                          
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
			InputStream is = new ByteArrayInputStream(xmlData.getBytes());
			
			SOAPMessage message = MessageFactory.newInstance().createMessage(null, is);
			
			// Mime Headers
			final MimeHeaders headers = message.getMimeHeaders();
			
			headers.addHeader("SOAPAction", "http://tempuri.org/Tributs");
		
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
