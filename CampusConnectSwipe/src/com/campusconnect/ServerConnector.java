package com.campusconnect;

import java.util.ArrayList;

import org.apache.http.conn.ConnectTimeoutException;

import android.content.Context;
import android.location.Location;

import com.campusconnect.ssl.SSLManager;

public class ServerConnector 
{
	//final String m_sServerIP = "129.107.116.135:8084";
	private SSLManager man = null;
	
	public ServerConnector( Context ctx, String enc_user, String enc_pass ) {
		man = new SSLManager(ctx, enc_user, enc_pass);
	}
	
	public String getCommunityMsgDescription(int ID) throws ConnectTimeoutException
	{
		try {
			return man.getCommunityMsgDescription(ID);
		} catch (ConnectTimeoutException e) {
			throw e;
		}
	}
	
	public CommunityMsg getCommunityMsgById( int msgID ) throws ConnectTimeoutException
	{
		try {
			return man.getCommunityMsgById(msgID);
		} catch (ConnectTimeoutException e) {
			throw e;
		}
	}
	
	public ArrayList<CommunityMsg> getCommunityMsg() throws ConnectTimeoutException
	{
		try {
			return man.getCommunityMsg();
		} catch (ConnectTimeoutException e) {
			throw e;
		}
	}
	
	public ArrayList<CommunityMsg> getCommunityMsgForMap() throws ConnectTimeoutException
	{
		try {
			return man.getCommunityMsgForMap();
		} catch (ConnectTimeoutException e) {
			throw e;
		}
	}
	
	public String getHostName()
	{
		return man.getHostName();
	}
	
	
	public boolean sendIncidentMsg(String sMsg, String enc_user, String enc_pass, long lReportTime, Location vUserLocation, String sDirectory, int directoryType ) throws ConnectTimeoutException
	{	
		try {
			return man.sendIncidentMsg(sMsg, enc_user, enc_pass, lReportTime, vUserLocation, sDirectory, directoryType);
		} catch (ConnectTimeoutException e) {
			throw e;
		}
	}
}
