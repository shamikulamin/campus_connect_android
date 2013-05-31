package com.campusconnect.login;

import org.apache.http.conn.ConnectTimeoutException;

import com.campusconnect.R;
import com.campusconnect.crypt.CryptManager;
import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	private static final String SENDER_ID = "507557004717";	// This is a constant provided by Google which uniquely identifies the application
	private static final String appName = "Campus Connect";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setPasswordText();
        boolean dispDialog = getIntent().getBooleanExtra("displayDialog", false);//getStringExtra("displayDialog");
        if( dispDialog )
        	displayInvalidCredentialsAlert();
    }
	
	 @Override
    public void onBackPressed() {
		 return;	// Users should not be able to back out of the login page
    }
	 
	/**
	 * Displays an AlertDialog to the user to notify of changed credentials
	 * */
 	private void  displayInvalidCredentialsAlert() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle("Invalid Credentials");

		// set dialog message
		alertDialogBuilder.setMessage("Your login credentials have changed!\n\nPlease log in...")
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
	
    /**
     * This Method sets password hint font to be the same as normal text
     * while keeping the password hidden (shown as dots)
     */
    private void setPasswordText() {
    	EditText password = (EditText) findViewById(R.id.password);
        password.setTypeface(Typeface.DEFAULT);
    }
    
    /** Called when the user clicks the Login button */
	public void startLoginTask(View view) {        
        /* Get EditText objects */
        EditText userText = (EditText) findViewById(R.id.username);
        EditText passText = (EditText) findViewById(R.id.password);
        
        /* Get Strings the user input */
        String user = userText.getText().toString();
        String pass = passText.getText().toString();
    	
        /* Set up a progress dialog for waiting while logging in */
    	ProgressDialog dialog = ProgressDialog.show( this, "Authenticating...", "Please wait...", true);
		
    	/* Close Software Keyboard */
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(passText.getWindowToken(), 0);
    	
    	/* Attempt to login in a separate thread */
    	LoginTask lt = new LoginTask(this.getSharedPreferences("prefs", MODE_PRIVATE), dialog);
		lt.execute(user, pass);
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
    private class LoginTask extends AsyncTask<String, Void, Boolean> {
    	private ProgressDialog progressDialog = null;
    	private SharedPreferences sp = null;
    	private CryptManager cm = null;
    	private boolean serverDown = false;
    	private boolean update = false;
    	 
    	public LoginTask(SharedPreferences sp, ProgressDialog progressDialog)
    	{
    		this.progressDialog = progressDialog;
    		this.sp = sp;
    		this.cm = new CryptManager();
    	}
    	
    	@Override
    	protected Boolean doInBackground(String... params) {
    		String uid = params[0]; String pass = params[1];
    		String enc_uid = cm.encrypt(uid), enc_pass = cm.encrypt(pass);	// Encrypt our data
    		
    		/* Use the Authenticator class to authenticate our user */
    		boolean authenticated = false;
			try {
				authenticated = new Authenticator(enc_uid, enc_pass, getBaseContext()).authenticate();
			} catch (ConnectTimeoutException e) {
				serverDown = true;
				return false;
			} catch (UpdateException e) {
				update = true;
				return false;
			}
    		
    		/* Valid user therefore save our encrypted data in SharedPreferences */
    		if( authenticated ) {
    			SharedPreferences.Editor spe = sp.edit();
    			spe.putString("uid", enc_uid);
    			spe.putString("p", enc_pass);
    			spe.commit();	// Don't forget to commit the changes
    		}
    		return authenticated;
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
    		 } else if ( update ) {
    			 dispUpdateNotif();
    			 update = false;
    		 } else {
	    		 if( result.booleanValue() == true ) {
	    			 pushNotificationRegister();		// Make sure user is registered for Push Notifications
	    			 finish();	// Now we can direct our users to the main screen by finishing this activity
	    		 } else {
	    			 Toast.makeText(getBaseContext(), "Authentication Failed!", Toast.LENGTH_SHORT).show();
	    		 }
    		 }
    	}
     }
}
