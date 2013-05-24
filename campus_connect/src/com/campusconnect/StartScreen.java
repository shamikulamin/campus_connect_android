package com.campusconnect;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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

public class StartScreen extends Activity {
	private static final String SENDER_ID = "507557004717";	// This is a constant provided by Google which uniquely identifies the application
	private static final String appName = "Campus Connect";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin();
        setContentView(R.layout.start_screen);
        
        Button showMapBtn = (Button) findViewById(R.id.show_in_map);
        showMapBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent mapActivity = new Intent(StartScreen.this, CommValidMsgMapActivity.class);
        		startActivity(mapActivity);
            }
        });
        
        Button showListBtn = (Button) findViewById(R.id.show_as_list);
        showListBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent mainScreen = new Intent(StartScreen.this, main.class);
        		startActivity(mainScreen);
            }
        });
        
        Button reportIncidentBtn = (Button) findViewById(R.id.report_incident);
        reportIncidentBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent reportIncident = new Intent(StartScreen.this, ReportIncidentActivity.class);
        		startActivity(reportIncident);
            }
        });
	}
	
	/**
     * See if user has valid login credentials saved. If so then log them in, otherwise launch login activity
     * */
    private void checkLogin() {
    	String enc_user = null, enc_pass = null;
    	
    	/* Get SharedPreferences to see if we already have set a user/pass combo */
    	SharedPreferences sp = this.getSharedPreferences("prefs", MODE_PRIVATE);
    	
    	/* Variable stored in SharedPreference: uid = encrypted UTA ID, p = encrypted UTA password */
    	if( (enc_user = sp.getString("uid", null)) == null || (enc_pass = sp.getString("p", null)) == null ) {
    		startLoginActivity(false);
    	} else {
    		startStoredCredentialsLoginTask(enc_user,enc_pass);	// Check if stored UTA ID and Password are valid
    	}
    }
    
    /**
     * Directs the user to the login activity
     * 
     * @param displayDialog - boolean value representing whether a dialog should be shown to the user upon LoginActivity Starting
     * */
    private void startLoginActivity(boolean displayDialog) {
    	Intent intent = new Intent(this, LoginActivity.class);
    	intent.putExtra("displayDialog", displayDialog);
    	
        /* Adds the FLAG_ACTIVITY_NO_HISTORY which will not allow users to go back to login screen once successfully logged in */
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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
    
    /** Called every time the app starts to check credentials */
    private void startStoredCredentialsLoginTask(String enc_uid, String enc_pass) {        
    	
        /* Set up a progress dialog for waiting while logging in */
    	ProgressDialog dialog = ProgressDialog.show( this, "Logging In...", "Please wait...", true);
    	
    	/* Attempt to login in a separate thread */
    	StoredCredentialsLoginTask lt = new StoredCredentialsLoginTask(dialog);
		lt.execute(enc_uid, enc_pass);
    }
    
    /**
     * This inner class is used to Authenticate a user with a nice looking Progress Dialog
     * */
    private class StoredCredentialsLoginTask extends AsyncTask<String, Void, Boolean> {
    	private ProgressDialog progressDialog = null;
    	private boolean serverDown = false;
    	private boolean update = false;
    	 
    	public StoredCredentialsLoginTask(ProgressDialog progressDialog)
    	{
    		this.progressDialog = progressDialog;
    	}
    	
    	@Override
    	protected Boolean doInBackground(String... params) {
    		String enc_uid = params[0]; String enc_pass = params[1];
    		
    		/* Use the Authenticator class to authenticate our user */
    		boolean valid = false;
			try {
				valid = new Authenticator(enc_uid, enc_pass, getBaseContext()).authenticate();
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
