package com.campusconnect;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.campusconnect.login.*;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.Marker;

public class StartScreen extends Activity {
	private static final String SENDER_ID = "507557004717";	// This is a constant provided by Google which uniquely identifies the application
	private static final String appName = "Campus Connect";
	private String enc_user = null;
	private String enc_pass = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean bPushNotif = getIntent().getBooleanExtra("bPush", false);
     	int iMsgID = getIntent().getIntExtra("msg_id", -1);
    	
        checkLogin(bPushNotif, iMsgID, this);
        setContentView(R.layout.start_screen);
        
        Button showMapBtn = (Button) findViewById(R.id.show_in_map);
        showMapBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
            	if(status == ConnectionResult.SUCCESS) {
	                // Perform action on click
	            	Intent mapActivity = new Intent(StartScreen.this, ComMsgMapDetailsActivity.class);
	            	mapActivity.putExtra("enc_user",StartScreen.this.enc_user);
	            	mapActivity.putExtra("enc_pass",StartScreen.this.enc_pass);
	        		startActivity(mapActivity);
            	} else {
            		alertNoPlayServices();
            	}
            }
        });
        
        Button showListBtn = (Button) findViewById(R.id.show_as_list);
        showListBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent mainScreen = new Intent(StartScreen.this, ComMsgListActivity.class);
            	mainScreen.putExtra("enc_user",StartScreen.this.enc_user);
            	mainScreen.putExtra("enc_pass",StartScreen.this.enc_pass);
        		startActivity(mainScreen);
            }
        });
        
        Button reportIncidentBtn = (Button) findViewById(R.id.report_incident);
        reportIncidentBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent reportIncident = new Intent(StartScreen.this, ReportIncidentActivity.class);
            	reportIncident.putExtra("enc_user",StartScreen.this.enc_user);
            	reportIncident.putExtra("enc_pass",StartScreen.this.enc_pass);
        		startActivity(reportIncident);
            }
        });
        
        /*
        Button emergencyAssitanceBtn = (Button) findViewById(R.id.emergency_assitance);
        emergencyAssitanceBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent emAssist = new Intent(StartScreen.this, EmergencyAssistanceActivity.class);
        		startActivity(emAssist);
            }
        });*/
        
	}
	
	private void alertNoPlayServices() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
			// set title
			alertDialogBuilder.setTitle("No Google Play Services!");
 
			// set dialog message
			alertDialogBuilder
				.setMessage("Your device doesn't have Google Play Services installed if you would like to download it please click OK.")
				.setCancelable(false)
				.setNeutralButton("OK",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
			  		    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appName)));
						dialog.cancel();
					}
				  });
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
		}
	
	/**
     * See if user has valid login credentials saved. If so then log them in, otherwise launch login activity
     * */
    private void checkLogin(boolean bPush, int iMsgID, Context ctx ) {
    	String enc_user = null, enc_pass = null;
    	
    	/* Get SharedPreferences to see if we already have set a user/pass combo */
    	SharedPreferences sp = this.getSharedPreferences("prefs", MODE_PRIVATE);
    	
    	// We do not pass the push notification parameters to startLoginActivity() since it will be called only once at the first time after installing.
    	/* Variable stored in SharedPreference: uid = encrypted UTA ID, p = encrypted UTA password */
    	if( (enc_user = sp.getString("uid", null)) == null || (enc_pass = sp.getString("p", null)) == null ) {
    		startLoginActivity(false);
    	} else {
    		startStoredCredentialsLoginTask(enc_user,enc_pass, bPush, iMsgID, ctx);	// Check if stored UTA ID and Password are valid
    	}
    	this.enc_user = sp.getString("uid", null);
    	this.enc_pass = sp.getString("p", null);
    }
    
    /**
     * Directs the user to the login activity
     * 
     * @param displayDialog - boolean value representing whether a dialog should be shown to the user upon LoginActivity Starting
     * */
    private void startLoginActivity(boolean displayDialog) {
    	Intent intent = new Intent(this, LoginActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	intent.putExtra("displayDialog", displayDialog);
        startActivity(intent);
    }
    
    /**
     * Ensures user is registered to receive push notification for our application
     * */
    private void pushNotificationRegister() {
    	GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
        	GCMRegistrar.register(this, SENDER_ID);
        } else {
        	Log.v("MainActivity", "Already registered: "+regId);
        }
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
							  		startLoginActivity(false);
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
	
	private CommunityMsg getCommMsgFromPush( int msgID, Context ctx, String enc_user, String enc_pass ) throws ConnectTimeoutException {
		ServerConnector vServConn = new ServerConnector(ctx, enc_user, enc_pass);
		CommunityMsg vRet = vServConn.getCommunityMsgById(msgID);
		return vRet;
	}
    
    /** Called every time the app starts to check credentials */
    private void startStoredCredentialsLoginTask(String enc_uid, String enc_pass, boolean bPush, int iMsgID, Context ctx) {        
    	
        /* Set up a progress dialog for waiting while logging in */
    	ProgressDialog dialog = ProgressDialog.show( this, "Logging In...", "Please wait...", true);
    	
    	/* Attempt to login in a separate thread */
    	StoredCredentialsLoginTask lt = new StoredCredentialsLoginTask(dialog, bPush, iMsgID, ctx);
		lt.execute(enc_uid, enc_pass);
		
    }
    
    /**
     * This inner class is used to Authenticate a user with a nice looking Progress Dialog
     * */
    private class StoredCredentialsLoginTask extends AsyncTask<String, Void, Boolean> {
    	private ProgressDialog progressDialog = null;
    	private boolean serverDown = false;
    	private boolean update = false;
    	private boolean m_bPush = false;
    	private int m_iMsgID = -1;
    	private Context ctx = null;
    	private CommunityMsg m_vPushedComMsg = null;
    	
    	public StoredCredentialsLoginTask(ProgressDialog progressDialog, boolean bPush, int iMsgID, Context ctx)
    	{
    		this.progressDialog = progressDialog;
    		m_bPush = bPush;
    		m_iMsgID = iMsgID;
    		this.ctx = ctx;
    	}
    	
    	@Override
    	protected Boolean doInBackground(String... params) {
    		String enc_uid = params[0]; String enc_pass = params[1];
    		
    		/* Use the Authenticator class to authenticate our user */
    		boolean valid = false;
			try {
				valid = new Authenticator(enc_uid, enc_pass, getBaseContext()).authenticate();
				if( valid ) {
					if( m_bPush ) {
			        	//checkLogin(msgID);
			        	Intent vMsgDetailIntent = null;
			        	m_vPushedComMsg = getCommMsgFromPush(m_iMsgID,ctx, enc_uid, enc_pass);
			        	/* Check if this msg has a location */
			    		String loc = m_vPushedComMsg.getLatLong();//intent.getStringExtra("location");
			    		if( loc != null )//loc != null && !loc.equals("") )
			    		{
			    			vMsgDetailIntent = new Intent(ctx,ComMsgMapDetailsActivity.class);
			    			//vMsgDetailIntent.setClass(this, CommValidMsgMapActivity.class);
			    			vMsgDetailIntent.putExtra("CommMsgObject", m_vPushedComMsg);
			    		}
			    		else
			    		{
			    			vMsgDetailIntent = new Intent(ctx, ComMsgDetailsActivity.class);
			    			//vMsgDetailIntent.setClass(this, ComMsgDetailsActivity.class);
			    			vMsgDetailIntent.putExtra("pushMSG", m_vPushedComMsg);
			    		}
			    		vMsgDetailIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			    		startActivity(vMsgDetailIntent);
			        }  
				}
			} catch (ConnectTimeoutException e) {
				serverDown = true;
				return false;
			} catch (UpdateException e) {
				update = true;
				e.printStackTrace();
			}
    		return valid;
    	}

    	@Override
    	protected void onPreExecute() {
    		progressDialog.show();	// Show Progress Dialog before executing authentication
    	}

    	@Override
    	protected void onPostExecute(Boolean result) {
    		 progressDialog.dismiss();	// Hide Progress Dialog after executing authentication
    		 if( serverDown ) {
    			 dispServerDown();
    			 serverDown = false;
    			 //startLoginActivity(false);
    		 } else if ( update ) {
    			 dispUpdateNotif();
    			 update = false;
    		 } else {
    			 if( result.booleanValue() == false ) {
	    			 startLoginActivity(true);	// Credentials are no longer valid so ask the user for correct credentials again
	    		 }
	    		pushNotificationRegister();		// Make sure user is registered for Push Notifications
    		 }
    	}
     }

}
