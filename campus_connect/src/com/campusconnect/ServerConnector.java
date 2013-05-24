package com.campusconnect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;


import android.annotation.SuppressLint;
import android.location.Location;
import android.util.Log;

public class ServerConnector 
{
	//final String m_sServerIP = "192.168.2.100:8080";
	final String m_sServerIP = "129.107.116.135:8084";
	//final String m_sServerIP = "129.107.150.190:8080";
	
	ServerConnector(){}
	
	public String getCommunityMsgDescription(int ID)
	{
		
		if (ID == -1)
			return "No ID found.";
		
		//String sRet = "";
		String response = getResponse("getCommMsgDesc", String.valueOf(ID));
		
		return response;
	}
	
	public ArrayList<CommunityMsg> getCommunityMsg()
	{
		String response = getResponse("get","getCommunityMsg");
		ArrayList<CommunityMsg> asRet = new ArrayList<CommunityMsg>();
		try {
			JSONArray jsonArray = new JSONArray(response);
			Log.i(ServerConnector.class.getName(),
					"Number of entries " + jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				
				CommunityMsg comMsg = new CommunityMsg();
				if(jsonObject.has("comm_msg_id"))
					comMsg.setCommMsgId(jsonObject.getInt("comm_msg_id"));
				else
				{
					// this should not happen as its the primary key.
					comMsg.setCommMsgId(-1);
					
				}
				if(jsonObject.has("msg_type"))
				{
					comMsg.setMsgType(jsonObject.getString("msg_type"));					
				}
				else
					comMsg.setMsgType(null);
				
				if(jsonObject.has("latlong"))					
					comMsg.setLatLong(jsonObject.getString("latlong"));
				else
					comMsg.setLatLong(null);
				
				if(jsonObject.has("msg_description"))
					comMsg.setMsgDescription(jsonObject.getString("msg_description"));
				else
					comMsg.setMsgDescription(null);
				
				if(jsonObject.has("msg_title"))
					comMsg.setMsgTitle(jsonObject.getString("msg_title"));
				else
					comMsg.setMsgTitle(null);
				
				if(jsonObject.has("reporting_time"))
					comMsg.setReportingTime(jsonObject.getString("reporting_time"));
				else
					comMsg.setReportingTime(null);
						
				if(jsonObject.has("expiry_time"))
					comMsg.setExpiryTime(jsonObject.getString("reporting_time"));
				else
					comMsg.setExpiryTime(null);
				
				asRet.add(comMsg);
				//Log.i(ServerConnector.class.getName(), jsonObject.getString("msg_title"));
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return asRet;
	}
	
	public ArrayList<CommunityMsg> getCommunityMsgForMap()
	{
		String response = getResponse("get","getCommunityMsgForMap");
		ArrayList<CommunityMsg> asRet = new ArrayList<CommunityMsg>();
		try {
			JSONArray jsonArray = new JSONArray(response);
			Log.i(ServerConnector.class.getName(),
					"Number of entries " + jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				
				CommunityMsg comMsg = new CommunityMsg();
				if(jsonObject.has("comm_msg_id"))
					comMsg.setCommMsgId(jsonObject.getInt("comm_msg_id"));
				else
				{
					// this should not happen as its the primary key.
					comMsg.setCommMsgId(-1);
					
				}
				if(jsonObject.has("msg_type"))
				{
					comMsg.setMsgType(jsonObject.getString("msg_type"));					
				}
				else
					comMsg.setMsgType(null);
				
				if(jsonObject.has("latlong"))					
					comMsg.setLatLong(jsonObject.getString("latlong"));
				else
					comMsg.setLatLong(null);
				
				if(jsonObject.has("msg_description"))
					comMsg.setMsgDescription(jsonObject.getString("msg_description"));
				else
					comMsg.setMsgDescription(null);
				
				if(jsonObject.has("msg_title"))
					comMsg.setMsgTitle(jsonObject.getString("msg_title"));
				else
					comMsg.setMsgTitle(null);
				
				if(jsonObject.has("reporting_time"))
					comMsg.setReportingTime(jsonObject.getString("reporting_time"));
				else
					comMsg.setReportingTime(null);
						
				if(jsonObject.has("expiry_time"))
					comMsg.setExpiryTime(jsonObject.getString("reporting_time"));
				else
					comMsg.setExpiryTime(null);
				
				asRet.add(comMsg);
				//Log.i(ServerConnector.class.getName(), jsonObject.getString("msg_title"));
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return asRet;
	}
	
	public String getHostName()
	{
		return "http://" + m_sServerIP + "/Test1/";
	}
	
	private String getResponse(String sParamName, String sParamVal)
	{
		//String sResponseBody = "";
	
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getHostName() + "campus_connect_servlet?" + sParamName + "=" + sParamVal); 
	
		// Create a response handler
		//ResponseHandler<String> responseHandler = new BasicResponseHandler();   
		StringBuilder builder = new StringBuilder();
		try {			
			HttpResponse response = httpclient.execute(httpget);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			}
			
		} catch (ClientProtocolException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// When HttpClient instance is no longer needed, 
		// shut down the connection manager to ensure
		// immediate deallocation of all system resources
		httpclient.getConnectionManager().shutdown();
		
		return builder.toString();
	}
	
	
	boolean sendIncidentMsg(String sMsg, long lReportTime, Location vUserLocation, String sImageDirectory)
	{		
		try{
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			
			String sMsgTitle = "";
			if(sMsg == null || sMsg.equals(""))
			{
				sMsgTitle = "default message";
			}
			else
			{
				if(sMsg.length() > 10)
					sMsgTitle = sMsg.substring(0,9);
				else sMsgTitle = sMsg;
			}
			entity.addPart("message_title", new StringBody(sMsgTitle));
			entity.addPart("message", new StringBody(sMsg));
			entity.addPart("reporting_time", new StringBody(String.valueOf(lReportTime)));
			
			if(vUserLocation != null)
			{
				entity.addPart("latitude", new StringBody(String.valueOf(vUserLocation.getLatitude())));
				entity.addPart("longitude", new StringBody(String.valueOf(vUserLocation.getLongitude())));
			}
			
			File vDir = new File(sImageDirectory);
			if(vDir.isDirectory())
			{
				int iNumOfImages = 0;
				
				String asFiles[] = vDir.list();
				iNumOfImages = asFiles.length;
				if(iNumOfImages > 4)
					iNumOfImages = 4;
					
				for(int i = 0; i < iNumOfImages; i++)
				{
					String sFilePath = vDir.getPath() + File.separator + asFiles[i];
					File vImage = new File(sFilePath);
					entity.addPart("image", 
							new InputStreamBody(new FileInputStream(vImage), vImage.getName()));
				}
			}
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(getHostName()+"campus_connect_servlet");
			httpPost.setEntity(entity);
			
			HttpResponse servletResponse = httpClient.execute(httpPost);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(servletResponse.getEntity().getContent()));
			String line = null;
			while((line = in.readLine()) != null) {
			    System.out.println(line);
			}

			
		
		}catch(Exception e)
		{
			Log.e("error", e.getMessage());
			return false;
		}
		
		return true;
	}
	
}
