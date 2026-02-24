package com.sitmun.padro.utils;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class UtilsPadron {		

	public static byte[] calculateHash(String data, String hashType) throws UnsupportedEncodingException {		
		if(hashType.equals("512")){
			byte[] dataBytes=data.getBytes("US-ASCII");
			return DigestUtils.sha512(dataBytes);
		}
		else if(hashType.equals("1")){
			byte[] dataBytes=data.getBytes("US-ASCII");
			return DigestUtils.sha1(dataBytes);
			
		} else return null;
	}

	public static String toBase64(byte[] encodeThis){
		try {
	        return new String(Base64.encodeBase64(encodeThis));
	    } catch (Exception e) {
	    	throw new RuntimeException("Failed to encode the String", e);
	    }
	}

	public static String getProvincia(String municipi) {
		return municipi.substring(0,2);
	}

	public static String getMunicipi(String municipi) {
		return municipi.substring(2,municipi.length());
	}

	public static String getIneEntitatColectiva(String nucli) {
		return nucli.substring(0,2);
	}
	
	public static String getIneEntitatSingular(String nucli) {
		return nucli.substring(2,4);
	}
	public static String getIneNucleo(String nucli) {
		return nucli.substring(5,nucli.length());
	}

	public static String fromBase64(byte[] textContent) {
		// TODO Auto-generated method stub
		try {
	        return new String(Base64.decodeBase64(textContent), "ISO-8859-15");
	    } catch (Exception e) {
	    	throw new RuntimeException("Failed to encode the String", e);
	    }
	}
	
	public static String fromBase64(String textContent) {
		try {
	        return new String(Base64.decodeBase64(textContent), "ISO-8859-15");
	    } catch (Exception e) {
	    	throw new RuntimeException("Failed to encode the String", e);
	    }
	}
	
}
