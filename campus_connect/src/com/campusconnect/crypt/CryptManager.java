package com.campusconnect.crypt;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import android.util.Base64;

public class CryptManager {
	private byte[] pubKey = { (byte) 0x30, (byte) 0x81, (byte) 0x9F, (byte) 0x30, (byte) 0x0D, (byte) 0x06, (byte) 0x09, (byte) 0x2A, (byte) 0x86, (byte) 0x48, (byte) 0x86, (byte) 0xF7, (byte) 0x0D, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x03, (byte) 0x81, (byte) 0x8D, 
			(byte) 0x00, (byte) 0x30, (byte) 0x81, (byte) 0x89, (byte) 0x02, (byte) 0x81, (byte) 0x81, (byte) 0x00, (byte) 0x8F, (byte) 0x71, (byte) 0x04, (byte) 0xAB, (byte) 0x8D, (byte) 0x60, (byte) 0x1F, (byte) 0xB0, (byte) 0x37, (byte) 0x62, (byte) 0x4C, (byte) 0x27, 
			(byte) 0xFE, (byte) 0x9A, (byte) 0x5A, (byte) 0x32, (byte) 0xD6, (byte) 0x5E, (byte) 0xAE, (byte) 0xD5, (byte) 0x82, (byte) 0xF1, (byte) 0x3E, (byte) 0x93, (byte) 0xA5, (byte) 0x29, (byte) 0x41, (byte) 0x5D, (byte) 0x2C, (byte) 0xB9, (byte) 0x8D, (byte) 0xB6, 
			(byte) 0x45, (byte) 0x21, (byte) 0x5F, (byte) 0x0A, (byte) 0x08, (byte) 0xF4, (byte) 0x2D, (byte) 0x7A, (byte) 0x0F, (byte) 0x7D, (byte) 0x48, (byte) 0x75, (byte) 0x79, (byte) 0xF0, (byte) 0xCC, (byte) 0x3E, (byte) 0x33, (byte) 0x5F, (byte) 0x4A, (byte) 0xBB, 
			(byte) 0xF6, (byte) 0x77, (byte) 0xDD, (byte) 0x16, (byte) 0xE8, (byte) 0x7B, (byte) 0x2C, (byte) 0xB1, (byte) 0x3F, (byte) 0x7D, (byte) 0xF1, (byte) 0x27, (byte) 0x2F, (byte) 0xFC, (byte) 0x65, (byte) 0xA6, (byte) 0xB2, (byte) 0x21, (byte) 0x03, (byte) 0x98, 
			(byte) 0x23, (byte) 0x78, (byte) 0xEA, (byte) 0xC8, (byte) 0xBC, (byte) 0x67, (byte) 0xCB, (byte) 0xE0, (byte) 0xC6, (byte) 0x34, (byte) 0x61, (byte) 0xED, (byte) 0xDC, (byte) 0x42, (byte) 0x75, (byte) 0xC2, (byte) 0x44, (byte) 0x15, (byte) 0xA1, (byte) 0x2D, 
			(byte) 0x2D, (byte) 0x81, (byte) 0xCD, (byte) 0x9F, (byte) 0x22, (byte) 0xA9, (byte) 0xB7, (byte) 0xA4, (byte) 0xC6, (byte) 0xEF, (byte) 0x3C, (byte) 0x3E, (byte) 0x25, (byte) 0x2B, (byte) 0x2C, (byte) 0xA3, (byte) 0x92, (byte) 0x53, (byte) 0xAD, (byte) 0xB3, 
			(byte) 0xD7, (byte) 0x99, (byte) 0x2B, (byte) 0x2C, (byte) 0x58, (byte) 0xC3, (byte) 0x0C, (byte) 0x76, (byte) 0x29, (byte) 0x56, (byte) 0xE9, (byte) 0x33, (byte) 0x4D, (byte) 0x45, (byte) 0xEE, (byte) 0x6F, (byte) 0x02, (byte) 0x03, (byte) 0x01, (byte) 0x00, (byte) 0x01 };
	PublicKey key = null;
	
	public CryptManager() {
		try {
			this.key = KeyFactory.getInstance("RSA").generatePublic((KeySpec) new X509EncodedKeySpec(pubKey));
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	/**
     * Encrypt the plain text using public key.
     * 
     * @param text : original plain text
     * @return Encrypted text as a network safe Base64 encoded String
     * @throws java.lang.Exception
     * */
	public String encrypt(String text) {
		byte[] cipherText = null;
    	try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			// encrypt the plain text using the public key
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return Base64.encodeToString (cipherText, Base64.URL_SAFE|Base64.NO_PADDING|Base64.NO_WRAP);
	}
	
   
}
