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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	private static final String SENDER_ID = "507557004717";	// This is a constant provided by Google which uniquely identifies the application
	
	public GCMIntentService(){
		super(SENDER_ID);
	}

	/* These constants uniquely identify types of notifications e.g.(PARKING_NOTIFICATION, CRIME_NOTIFICATION, ...etc)*/
	private static final int TEST_NOTIFICATION = 1;
	private static final String IP_Address = "129.107.116.135";
	
	@Override
	protected void onError(Context context, String regId) {
		/* Not quite sure what to do here so just write the error to the log */
		Log.v("In onError GCMIntentService","Error Registering/Unregistering");
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Intent vMsgDetailIntent = null;
		String title = intent.getStringExtra("title");
		String desc = intent.getStringExtra("text");
		String msgType = intent.getStringExtra("msgType");
		
		/* This method receives intent sent from server */
		String loc = intent.getStringExtra("location");
		if(loc != null && !loc.equals("") )
		{
			vMsgDetailIntent = new Intent(Intent.ACTION_MAIN);
			vMsgDetailIntent.setClass(context, CommValidMsgMapActivity.class);
			vMsgDetailIntent.putExtra("CommMsgObject", new CommunityMsg(msgType,title,desc,loc));
		}
		else
		{
			vMsgDetailIntent = new Intent(Intent.ACTION_MAIN);
			vMsgDetailIntent.setClass(context, ComMsgDetailsActivity.class);
			vMsgDetailIntent.putExtra("pushMSG", new CommunityMsg(msgType,title,desc,null));
		}	
		
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, vMsgDetailIntent, 0);	//---PendingIntent to launch activity if the user selects this notification---
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentIntent(pendingIntent)
		        .setWhen(System.currentTimeMillis())
				.setContentTitle(title)
		        .setContentText(desc)
				.setVibrate(new long[] { 100, 250, 100, 500});	//---100ms delay, vibrate for 250ms, pause for 100 ms and then vibrate for 500ms---
		
		Notification n = mBuilder.build();
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(TEST_NOTIFICATION, n);
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		/* This method receives registrationID: and is sent to OUR server for storage */
		
		HttpClient httpClient = new DefaultHttpClient();
		Log.v("In onRegistered","regId: "+regId);
		HttpPost httpPost = new HttpPost("http://"+IP_Address+":8084/Test1/Register");
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
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("http://"+IP_Address+":8084/Test1/Unregister");
		
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
