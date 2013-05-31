package com.campusconnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class ComMsgListActivity extends Activity {

	// GCM variables.
	//private final String SENDER_ID = "610775927206";
	
	// These three arrays should be parallel.
	ArrayList<CommunityMsg> m_asCommAlerts;
	ArrayList<String> m_ComMsgTitles;
	ArrayList<Integer> m_ComMsgID;

	HashMap<Integer, CommunityMsg> m_MapIdToCommMsg;
	
	ServerConnector vConnector;
	ListView vCommMsgList;
	
	/* NOTE: Creating your own constructor for a class that extends activity is a
	 * bad idea. Some variables such as context aren't initialized correctly. Instead,
	 * use the on create method to initialize your variables */
	
	/** Called to load the messages in the background */
    private void startLoadComMsgTask() {        
    	ProgressDialog dialog = ProgressDialog.show( ComMsgListActivity.this, "Loading...", "Please wait...", true);
    	
    	/* Attempt to load community messages */
    	LoadComMsgTask lcmt = new LoadComMsgTask(dialog);
		lcmt.execute();
    }
	
	private void loadComMsg() throws ConnectTimeoutException {
		m_MapIdToCommMsg = new HashMap<Integer, CommunityMsg>();
		
		try {
			m_asCommAlerts = vConnector.getCommunityMsg();
			m_ComMsgTitles = new ArrayList<String>();
			m_ComMsgID = new ArrayList<Integer>();
			
			Iterator<CommunityMsg> it = m_asCommAlerts.iterator();
			
			while(it.hasNext()){
				CommunityMsg v = it.next();
				m_ComMsgTitles.add(v.getMsgTitle());
				m_ComMsgID.add(v.getCommMsgId());
				
				m_MapIdToCommMsg.put(v.getCommMsgId(), v);
			}
		} catch( ConnectTimeoutException e ) {
			throw e;
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		//loadComMsg();		
		startLoadComMsgTask();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        vConnector = new ServerConnector(ComMsgListActivity.this);
		startLoadComMsgTask();
    }
    
    String getCommMsgLocation(int iMsgID)
    {
    	CommunityMsg vTemp = m_MapIdToCommMsg.get(iMsgID);
    	if(vTemp != null)
    	{
    		return vTemp.getLatLong();
    	}
    	else
    	{
    		return "";
    	}
    }
    
    String getCommMsgTitle(int iMsgID)
    {
    	CommunityMsg vTemp = m_MapIdToCommMsg.get(iMsgID);
    	if(vTemp != null)
    	{
    		return vTemp.getMsgTitle();
    	}
    	else
    	{
    		return "";
    	}
    }
    
    String getCommMsgDetails(int iMsgID)
    {
    	CommunityMsg vTemp = m_MapIdToCommMsg.get(iMsgID);
    	if(vTemp != null)
    	{
    		return vTemp.getMsgDescription();
    	}
    	else
    	{
    		return "";
    	}
    }
    
    
    boolean hasCommMsgLocation(int iMsgID)
    {
    	String s =  getCommMsgLocation(iMsgID);
    	
    	if(s == null || s.equals("none"))
    		return false;
    	return true;
    		
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
    
    /**
     * This inner class is used to load the community messages with a nice looking Progress Dialog
     * */
    private class LoadComMsgTask extends AsyncTask<Void, Void, Void> {
    	private ProgressDialog progressDialog = null;
    	private boolean serverDown = false;
    	 
    	public LoadComMsgTask(ProgressDialog progressDialog)
    	{
    		this.progressDialog = progressDialog;
    	}
    	
    	@Override
    	protected Void doInBackground(Void... v) {
    		try { 
    			loadComMsg();
    		} catch (ConnectTimeoutException e) {
				serverDown = true;
			}
    		return null;
    	}

    	@Override
    	protected void onPreExecute() {
    		progressDialog.show();	// Show Progress Dialog before executing
    	}

    	@Override
    	protected void onPostExecute(Void v) {
    		 progressDialog.dismiss();	// Hide Progress Dialog after executing
    		 if( serverDown ) {
    			 dispServerDown();
    		 }
    		 
    		 vCommMsgList = (ListView) findViewById(R.id.list);
    	     vCommMsgList.setAdapter(new CommunityMsgAdapter(ComMsgListActivity.this, R.layout.row, m_asCommAlerts));
    	     vCommMsgList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    	     vCommMsgList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

    				@Override
    				public void onItemClick(AdapterView<?> asListItems, View vListView, int iPos,
    						long id) {			
    						
    						if(hasCommMsgLocation(m_ComMsgID.get(iPos)))
    						{
    							Intent vMsgDetailIntent = new Intent(ComMsgListActivity.this, ComMsgMapDetailsActivity.class);
    							vMsgDetailIntent.putExtra("CommMsgObject", m_asCommAlerts.get(iPos));						
    							startActivity(vMsgDetailIntent);
    						}
    						else
    						{
    							Intent vMsgDetailIntent = new Intent(ComMsgListActivity.this, ComMsgDetailsActivity.class);
    							vMsgDetailIntent.putExtra("msg_id", m_ComMsgID.get(iPos));
    							startActivity(vMsgDetailIntent);
    						}					
    					}
    			});
    	}
     }
}

