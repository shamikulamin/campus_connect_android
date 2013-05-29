package com.campusconnect.ssl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.util.Log;

import com.campusconnect.CommunityMsg;
import com.campusconnect.ServerConnector;
import com.campusconnect.login.UpdateException;

public class SSLManager {
	private static final String m_sServerIP = "129.107.116.135:8443";
	private static final int connectionTimeout = 5000, socketTimeout = 5000;
	private Context ctx = null;
	
	public SSLManager( Context ctx ) {
		this.ctx = ctx;
	}
	
	public String getCommunityMsgDescription(int ID) throws ConnectTimeoutException
	{
		String response = "";
		if (ID == -1)
			return "No ID found.";
		try {
			response = getResponse("getCommMsgDesc", String.valueOf(ID));
		} catch (ConnectTimeoutException e) {
			throw e;
		}
		return response;
	}
	
	public CommunityMsg getCommunityMsgById( int msgID ) throws ConnectTimeoutException
	{
		CommunityMsg comMsg = new CommunityMsg();
		try {
			String response = getResponse("get","getMsgById@" + msgID);
			//JSONArray jsonArray = new JSONArray(response);
			//Log.i(ServerConnector.class.getName(),
			//		"Number of entries " + jsonArray.length());
			//for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject =  new JSONObject(response);
				
				//CommunityMsg comMsg = new CommunityMsg();
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
				
				//asRet.add(comMsg);
			//}
		} catch (ConnectTimeoutException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return comMsg;
	}
	
	public ArrayList<CommunityMsg> getCommunityMsg() throws ConnectTimeoutException
	{
		ArrayList<CommunityMsg> asRet = new ArrayList<CommunityMsg>();
		try {
			String response = getResponse("get","getCommunityMsg");
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
			}
		} catch (ConnectTimeoutException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return asRet;
	}
	
	public ArrayList<CommunityMsg> getCommunityMsgForMap() throws ConnectTimeoutException
	{
		ArrayList<CommunityMsg> asRet = new ArrayList<CommunityMsg>();
		try {
			String response = getResponse("get","getCommunityMsgForMap");
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
			}
		} catch (ConnectTimeoutException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return asRet;
	}
	
	public String getHostName()
	{
		return "https://" + m_sServerIP + "/Test1/";
	}
	
	private String getResponse(String sParamName, String sParamVal) throws ConnectTimeoutException
	{
		HttpParams httpParameters = new BasicHttpParams();
		
		// Set the timeout in milliseconds until a connection is established.
		HttpConnectionParams.setConnectionTimeout(httpParameters, connectionTimeout);
		// Set the default socket timeout (SO_TIMEOUT) 
		HttpConnectionParams.setSoTimeout(httpParameters, socketTimeout);
	
		HttpClient httpclient = new CCHttpClient(this.ctx, httpParameters);
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
			
		} catch (ConnectTimeoutException e) {
			throw e;
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
	
	
	public boolean sendIncidentMsg(String sMsg, String enc_user, String enc_pass, long lReportTime, Location vUserLocation, String sImageDirectory) throws ConnectTimeoutException
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
			entity.addPart("uid",new StringBody(enc_user));
			entity.addPart("pass",new StringBody(enc_pass));
			
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
			
			HttpParams httpParameters = new BasicHttpParams();
			
			// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, connectionTimeout);
			// Set the default socket timeout (SO_TIMEOUT) 
			HttpConnectionParams.setSoTimeout(httpParameters, socketTimeout);
			
			HttpClient httpClient = new CCHttpClient(this.ctx, httpParameters);
			HttpPost httpPost = new HttpPost(getHostName()+"campus_connect_servlet");
			httpPost.setEntity(entity);
			
			HttpResponse servletResponse = httpClient.execute(httpPost);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(servletResponse.getEntity().getContent()));
			String line = null;
			while((line = in.readLine()) != null) {
			    System.out.println(line);
			}

			
		
		} catch (ConnectTimeoutException e) {
			throw e;
		} catch(Exception e) {
			Log.e("error", e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public boolean sendCredentials(String enc_uid, String enc_pass) throws ConnectTimeoutException, UpdateException {
		try {
			// Get version
			PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			String version = pInfo.versionName;
			
			HttpParams httpParameters = new BasicHttpParams();
			
			// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, connectionTimeout);
			// Set the default socket timeout (SO_TIMEOUT) 
			HttpConnectionParams.setSoTimeout(httpParameters, socketTimeout);
			
			ByteArrayOutputStream outstream = new ByteArrayOutputStream();
			HttpClient httpClient = new CCHttpClient(ctx, httpParameters);
			HttpPost httpPost = new HttpPost("https://"+m_sServerIP+"/LDAP");
			httpPost.setParams(httpParameters);
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("netid", enc_uid));
			postParameters.add(new BasicNameValuePair("password", enc_pass));
			postParameters.add(new BasicNameValuePair("version", version));
			UrlEncodedFormEntity formEntity = null;
		
			formEntity = new UrlEncodedFormEntity(postParameters);
			httpPost.setEntity(formEntity);

			HttpResponse response = httpClient.execute(httpPost);
			response.getEntity().writeTo(outstream);
			String respData = new String(outstream.toByteArray());
			if( respData.equals("true") ) {
				Log.d("sendCredentials","Successful login");
				return true;
			} else if( respData.equals("update") ) {
				Log.d("sendCredentials","Update Available");
				throw new UpdateException("update");
			}
		} catch (ConnectTimeoutException e) {
			throw e;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

}
