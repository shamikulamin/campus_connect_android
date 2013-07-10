package com.campusconnect;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	private static final String SENDER_ID = "507557004717";	// This is a constant provided by Google which uniquely identifies the application
	private static int notificationNum = 1;
	private static final String IP_Address = "129.107.116.135";
	
	public GCMIntentService(){
		super(SENDER_ID);
	}


	
	@Override
	protected void onError(Context context, String regId) {
		/* Not quite sure what to do here so just write the error to the log */
		Log.v("In onError GCMIntentService","Error Registering/Unregistering");
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Intent vMsgDetailIntent = new Intent(context, StartScreen.class);
		String title = intent.getStringExtra("title");
		int iMsgID = Integer.parseInt(intent.getStringExtra("msgID"));
		String msgType = intent.getStringExtra("msgType");
		
		/* This method receives intent sent from server */
		vMsgDetailIntent.putExtra("bPush", true);
		vMsgDetailIntent.putExtra("msg_id",iMsgID);
		vMsgDetailIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		PendingIntent pendingIntent = PendingIntent.getActivity(context,(int)System.currentTimeMillis(), vMsgDetailIntent, PendingIntent.FLAG_UPDATE_CURRENT);	//---PendingIntent to launch activity if the user selects this notification---
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentIntent(pendingIntent)
		        .setWhen(System.currentTimeMillis())
				.setContentTitle(msgType+": "+title)
		        //.setContentText(desc)
				.setVibrate(new long[] { 100, 250, 100, 500});	//---100ms delay, vibrate for 250ms, pause for 100 ms and then vibrate for 500ms---
		
		Notification n = mBuilder.build();
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(notificationNum, n);
		if(notificationNum >= Integer.MAX_VALUE - 1)
			notificationNum = 0;
		notificationNum+=1;
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		/* This method receives registrationID: and is sent to OUR server for storage */

		/* Get SharedPreferences to see if we already have set a user/pass combo */
    	SharedPreferences sp = this.getSharedPreferences("prefs", MODE_PRIVATE);
    	
    	// We do not pass the push notification parameters to startLoginActivity() since it will be called only once at the first time after installing.
    	/* Variable stored in SharedPreference: uid = encrypted UTA ID, p = encrypted UTA password */
    	String enc_user = sp.getString("uid", "");
    	String enc_pass = sp.getString("p", "");
		
    	HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("http://"+IP_Address+":8080/CampusConnectServer/campus_connect_android/gcmRegister");
		String toSend = enc_user+":"+enc_pass;
		String authStr = "Basic " + Base64.encodeToString(toSend.getBytes(), Base64.NO_WRAP);
		httpPost.setHeader("Authorization",authStr);
		
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("regID", regId));
		UrlEncodedFormEntity formEntity = null;
		try {
			formEntity = new UrlEncodedFormEntity(postParameters);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		httpPost.setEntity(formEntity);
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			// writing response to log
	        Log.d("Http Response:", response.toString());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		/* This method receives registrationID after user unregisters: should be sent to OUR server for deletion from storage */
		
		/* Get SharedPreferences to see if we already have set a user/pass combo */
    	SharedPreferences sp = this.getSharedPreferences("prefs", MODE_PRIVATE);
    	
    	// We do not pass the push notification parameters to startLoginActivity() since it will be called only once at the first time after installing.
    	/* Variable stored in SharedPreference: uid = encrypted UTA ID, p = encrypted UTA password */
    	String enc_user = sp.getString("uid", "");
    	String enc_pass = sp.getString("p", "");
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("http://"+IP_Address+":8080/CampusConnectServer/campus_connect_android/gcmUnregister");
		String toSend = enc_user+":"+enc_pass;
		String authStr = "Basic " + Base64.encodeToString(toSend.getBytes(), Base64.NO_WRAP);
		httpPost.setHeader("Authorization",authStr);
		
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("regID", regId));
		UrlEncodedFormEntity formEntity = null;
		try {
			formEntity = new UrlEncodedFormEntity(postParameters);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		httpPost.setEntity(formEntity);
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			// writing response to log
	        Log.d("Http Response:", response.toString());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
