package com.campusconnect;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.campusconnect.login.Authenticator;
import com.campusconnect.login.LoginActivity;
import com.campusconnect.login.UpdateException;
import com.campusconnect.speech.ListenService;
import com.google.android.gcm.GCMRegistrar;

public class StartScreen extends FragmentActivity {
	private static final String SENDER_ID = "507557004717";	// This is a constant provided by Google which uniquely identifies the application
	private static final String appName = "Campus Connect";
	static final int LOGIN_REQUEST = 1;
	private String enc_user = null;
	private String enc_pass = null;
	private CommunityMsg vCommMsgToBeShown = null;
    private boolean push_checked = false;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.app_menu, menu);
        final Menu m = menu;
        final MenuItem item = menu.findItem(R.id.menu_listen);
        item.getActionView().findViewById(R.id.buttonlisten).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {   
                m.performIdentifierAction(item.getItemId(), 0);
            }
        });
        return true;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if (keyCode == KeyEvent.KEYCODE_MENU) {
        	Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
	        startActivity(i);
            return true;
        }
        return super.onKeyDown(keyCode, event); 
    } 
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_listen:
            Intent servi = new Intent(this, ListenService.class);
            startService(servi);
            Log.d("StartScreen", "Started ListenService");
            Toast.makeText(this, "Listening...", Toast.LENGTH_LONG).show();
            return true;
        case R.id.settings:
        	Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void setMsgToBeShown( CommunityMsg vCommMsgToBeShown ) {
    	this.vCommMsgToBeShown = vCommMsgToBeShown;
    }
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean bPushNotif = getIntent().getBooleanExtra("bPush", false);
     	int iMsgID = getIntent().getIntExtra("msg_id", -1);

     	if( push_checked == true ) 
     		bPushNotif = false;
     	
     	if( bPushNotif == true )
     		push_checked = true;

     	SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.action_list, android.R.layout.simple_spinner_dropdown_item);

     	OnNavigationListener mOnNavigationListener = new OnNavigationListener() {

     		  @Override
     		  public boolean onNavigationItemSelected(int position, long itemId) {
     			  Fragment fragment = null;
     			  switch (position) {
                  case 0:
                      fragment = new ComMsgListFragment();
                      Bundle b_list = new Bundle();
                      b_list.putString("enc_user", enc_user);
                      b_list.putString("enc_pass", enc_pass);
                      fragment.setArguments(b_list);
                      break;
                  case 1:
             	 	 fragment = new ComMsgMapDetailsFragment();
             	     Bundle b_map = new Bundle();
                     b_map.putString("enc_user", enc_user);
                     b_map.putString("enc_pass", enc_pass);
                     if( vCommMsgToBeShown != null ) 
                    	 b_map.putParcelable("vCommMsgToBeShown", vCommMsgToBeShown);
                     fragment.setArguments(b_map);
                	 break;
                  case 2:
                	 fragment = new ReportIncidentFragment();
                     Bundle b_rep = new Bundle();
                     b_rep.putString("enc_user", enc_user);
                     b_rep.putString("enc_pass", enc_pass);
                     fragment.setArguments(b_rep);
                	 break;
                  case 3:
                	 fragment = new EmergencyAssistanceFragment();
                	 break;
                  default:
                	 fragment = new ComMsgListFragment();
                     Bundle b_def = new Bundle();
                     b_def.putString("enc_user", enc_user);
                     b_def.putString("enc_pass", enc_pass);
                     fragment.setArguments(b_def);
                     break;
     			 }
     		     getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
     		     vCommMsgToBeShown = null;	// Reset back to null after we've sent the parameter
     		     return true;
     		  }
     		  
     		};
    	
 		final ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowTitleEnabled(false); 
 		actionBar.setDisplayShowCustomEnabled(true);
 		
 		actionBar.setCustomView(R.layout.topbar); //load your layout
 		View ib = findViewById(R.id.settings);
 		ib.setOnClickListener(new View.OnClickListener() {

 		    @Override
 		    public void onClick(View view) {
 		    	Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
 	            startActivity(i);
 		    }
 		});
 		
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 		actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
 		
        checkLogin(bPushNotif, iMsgID, this);
        setContentView(R.layout.start_screen);

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
        startActivityForResult(intent, LOGIN_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode,resultCode,data);
        // Check which request we're responding to
        if (requestCode == LOGIN_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
            	enc_user = data.getStringExtra("enc_user");
            	enc_pass = data.getStringExtra("enc_pass");
            }
        }
    }
    
    /**
     * Ensures user is registered to receive push notification for our application
     * */
    private void pushNotificationRegister() {
    	SharedPreferences sp = this.getSharedPreferences("com.campusconnect_preferences", MODE_PRIVATE);
    	Boolean push_on = sp.getBoolean("push_notification_on", true);

    	if( push_on.booleanValue() == true ) {
	    	GCMRegistrar.checkDevice(this);
	        GCMRegistrar.checkManifest(this);
	        final String regId = GCMRegistrar.getRegistrationId(this);
	        if (regId.equals("")) {
	        	GCMRegistrar.register(this, SENDER_ID);
	        } else {
	        	Log.v("MainActivity", "Already registered: "+regId);
	        }
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
    
    public void noLocationPopup(CommunityMsg msg) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
			// set title
			alertDialogBuilder.setTitle(msg.getMsgTitle());
 
			// set dialog message
			alertDialogBuilder
				.setMessage(msg.getMsgDescription())
				.setCancelable(false)
				.setNeutralButton("OK",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, close
						// current activity
						dialog.cancel();
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
			        	m_vPushedComMsg = getCommMsgFromPush(m_iMsgID,ctx, enc_uid, enc_pass);
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
    		 } else if ( update ) {
    			 dispUpdateNotif();
    			 update = false;
    		 } else {
    			 if( result.booleanValue() == false ) {
	    			 startLoginActivity(true);	// Credentials are no longer valid so ask the user for correct credentials again
	    		 }
    			 pushNotificationRegister();		// Make sure user is registered for Push Notifications
    			 if( m_bPush ) {
    				 /* Check if this msg has a location */
    				 String loc = m_vPushedComMsg.getLatLong();
    				 if( loc != null && !loc.equals("none") ) {
    					 ((StartScreen) ctx).setMsgToBeShown(m_vPushedComMsg);
    					 StartScreen.this.getActionBar().setSelectedNavigationItem(1);
    				 } else {
    					 noLocationPopup(m_vPushedComMsg);
    				 }
    			 }
    		 }
    	}
     }
}
