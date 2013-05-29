// This class is responsible to display the community message without a location.

package com.campusconnect;



import org.apache.http.conn.ConnectTimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class ComMsgDetailsActivity extends Activity {
	
	private ServerConnector vConnector;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commsgdetail);
        
        vConnector = new ServerConnector(ComMsgDetailsActivity.this);
        int msgID = getIntent().getIntExtra("msg_id", -1);
        Button b = (Button) findViewById(R.id.btnReturnToMain);
        b.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View arg0) {
        		finish();
        	}
        });
        
        // This only occurs when this activity is started from clicking a push notification
        if( msgID == -1 ) {	
        	CommunityMsg msg = getIntent().getParcelableExtra("pushMSG");
        	TextView textView = (TextView) findViewById(R.id.msgdetails);
        	textView.setMovementMethod(new ScrollingMovementMethod());
 	        textView.setText(msg.getMsgDescription());
        } else {
        	startGetMsgsDetailsTask(msgID);
        }
	}
	
	private String getMsgDetail( int msgID ) throws ConnectTimeoutException {
		try {
	        return vConnector.getCommunityMsgDescription(msgID);
		} catch( ConnectTimeoutException e) {
			throw e;
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
								  		//startLoginActivity(false);
								  }
							  });

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
		}
	 
	 /** Called to send report over network */
	    private void startGetMsgsDetailsTask( int msgID ) {        
	    	
	        /* Set up a progress dialog for waiting while getting messages */
	    	ProgressDialog dialog = ProgressDialog.show( ComMsgDetailsActivity.this, "Loading...", "Please wait...", true);
	    	
	    	TextView txt = (TextView) findViewById(R.id.msgdetails);
	    	
	    	/* Attempt to get messages in a separate thread */
	    	GetMsgsDetailsTask lt = new GetMsgsDetailsTask(dialog, msgID, txt);
			lt.execute();
	    }
	    
	    /**
	     * This inner class is used to send report with a nice looking Progress Dialog
	     * */
	    private class GetMsgsDetailsTask extends AsyncTask<Void, Integer, String> {
	    	private ProgressDialog progressDialog = null;
	    	private boolean serverDown = false;
	    	private TextView textView = null;
	    	private int msgID = -1;
	    	 
	    	public GetMsgsDetailsTask(ProgressDialog progressDialog, int msgID, TextView textView )
	    	{
	    		this.progressDialog = progressDialog;
	    		this.msgID = msgID;
	    		this.textView = textView;
	    	}
	    	
	    	@Override
	    	protected String doInBackground(Void... params) {
	    		String ret = null;
				try {
					ret = getMsgDetail(msgID);
				} catch (ConnectTimeoutException e) {
					serverDown = true;
				}
				return ret;
	    	}

	    	@Override
	    	protected void onPreExecute() {
	    		progressDialog.show();	// Show Progress Dialog before going into doInBackground()
	    	}

	    	@Override
	    	protected void onPostExecute(String res) {
	    		 progressDialog.dismiss();	// Hide Progress Dialog after  doInBackground() finishes
	    		 if( serverDown ) {
	    			 dispServerDown();
	    			 serverDown = false;
	    		 }
	    		 
	 	        textView.setMovementMethod(new ScrollingMovementMethod());
	 	        textView.setText(res);
	    	}
	     }
}
