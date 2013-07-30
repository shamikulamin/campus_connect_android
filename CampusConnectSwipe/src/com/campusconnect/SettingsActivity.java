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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Base64;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {
	private static final String appName = "Campus Connect";
	private OnSharedPreferenceChangeListener spChanged;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.prefs);
        
       spChanged = new OnSharedPreferenceChangeListener() {
					
					@Override
					public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
						if( key.equals("push_notification_on") ) {
							Boolean push_on = sharedPreferences.getBoolean(key, true);
							startChangePushTask(push_on, SettingsActivity.this);
						}
					}
				};
    }
	
	@Override
	protected void onResume() {
	    super.onResume();
	    // Set up a listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(spChanged);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    // Unregister the listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(spChanged);
	}
	
	/** Starts task to remove this regID from our server's database */
    private void startChangePushTask(boolean push_on, Context ctx) {        
    	
        /* Set up a progress dialog for waiting while logging in */
    	ProgressDialog dialog = ProgressDialog.show( this, "Logging In...", "Please wait...", true);
    	
    	/* Attempt to login in a separate thread */
    	ChangePushTask cpt = new ChangePushTask(dialog, ctx);
		cpt.execute(push_on);
		
    }
	
	private void dispServerDown() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle("Server is Down");

		// set dialog message
		alertDialogBuilder.setMessage("Oops...it appears the server is currently down!\n\nPlease try again later...")
						  .setCancelable(false)
						  .setNeutralButton("OK",new DialogInterface.OnClickListener() {
							  public void onClick(DialogInterface dialog,int id) {
							  		dialog.cancel();	// if this button is clicked, close dialog
							  }
						  });

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
	}
	
	private void dispUpdateNotif() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle("Mandatory Update!");

		// set dialog message
		alertDialogBuilder.setMessage("A mandatory update is available")
						  .setCancelable(false)
						  .setNeutralButton("Download",new DialogInterface.OnClickListener() {
							  public void onClick(DialogInterface dialog,int id) {
							  		dialog.cancel();	// if this button is clicked, close dialog
							  		try {
							  		    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appName)));
							  		} catch (android.content.ActivityNotFoundException anfe) {
							  		    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+appName)));
							  		}
							  }
						  });

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		// show it
		alertDialog.show();
	}
	
	/**
     * This inner class is used to Authenticate a user with a nice looking Progress Dialog
     * */
    private class ChangePushTask extends AsyncTask<Boolean, Void, Void> {
    	private ProgressDialog progressDialog = null;
    	private static final String IP_Address = "129.107.116.135";
    	private boolean serverDown = false;
    	private boolean update = false;
    	private Context ctx = null;
    	
    	public ChangePushTask(ProgressDialog progressDialog, Context ctx)
    	{
    		this.progressDialog = progressDialog;
    		this.ctx = ctx;
    	}
    	
    	@Override
    	protected Void doInBackground(Boolean... params) {
    		/* Get SharedPreferences to see if we already have set a user/pass combo */
        	SharedPreferences sp = ctx.getSharedPreferences("prefs", MODE_PRIVATE);
        	
        	// We do not pass the push notification parameters to startLoginActivity() since it will be called only once at the first time after installing.
        	/* Variable stored in SharedPreference: uid = encrypted UTA ID, p = encrypted UTA password */
        	String enc_user = sp.getString("uid", "");
        	String enc_pass = sp.getString("p", "");
        	String regId = sp.getString("regID", "");
        	String type = "";
        	if( params[0].booleanValue() == true )
        		type = "gcmRegister";
        	else 
        		type = "gcmUnregister";
        	
    		HttpClient httpClient = new DefaultHttpClient();
    		HttpPost httpPost = new HttpPost("http://"+IP_Address+":8080/CampusConnectServer/campus_connect_android/" + type );
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
    		return null;
    	}

    	@Override
    	protected void onPreExecute() {
    		progressDialog.show();	// Show Progress Dialog before executing authentication
    	}

    	@Override
    	protected void onPostExecute(Void result) {
    		 progressDialog.dismiss();	// Hide Progress Dialog after executing authentication
    		 if( serverDown ) {
    			 dispServerDown();
    			 serverDown = false;
    		 } else if ( update ) {
    			 dispUpdateNotif();
    			 update = false;
    		 } 
    	}
     }
}