package com.campusconnect.login;

import org.apache.http.conn.ConnectTimeoutException;

import com.campusconnect.ssl.SSLManager;

import android.content.Context;

public class Authenticator {
	//private static final String IP_Address = "129.107.116.112";
	private String enc_uid = null, enc_pass = null;
	private SSLManager sslMan = null;
	
	/**
	 * This constructor expects the parameters to already be encrypted.
	 * 	-- Use CryptManager class to encrypt the uid/pass before constructing this object
	 * 
	 * @param enc_uid
	 * @param enc_pass
	 */
	public Authenticator(String enc_uid, String enc_pass, Context ctx) {
		this.enc_uid = enc_uid;
		this.enc_pass = enc_pass;
		this.sslMan = new SSLManager(ctx, enc_uid, enc_pass);
	}
	
	/**
	 * Authenticates the user by contacting UTA's LDAP server
	 * 	-- See SSLManger class for sending user/pass over SSL connection
	 * 
	 * @return returns whether or not the user/pass combo is valid
	 * @throws UpdateException, ConnectTimeoutException 
	 */	
	public boolean authenticate() throws UpdateException, ConnectTimeoutException {
		boolean valid = false;
		try {
			valid = sslMan.sendCredentials(enc_uid,enc_pass);
		} catch (ConnectTimeoutException e ) {
			throw e;
		}
		return valid;
	}
}
