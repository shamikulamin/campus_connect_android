package com.campusconnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.conn.ConnectTimeoutException;

import com.google.android.gms.maps.model.Marker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ComMsgListFragment extends Fragment {
	
	// These three arrays should be parallel.
	private ArrayList<CommunityMsg> m_asCommAlerts;
	private ArrayList<String> m_ComMsgTitles;
	private ArrayList<Integer> m_ComMsgID;

	private HashMap<Integer, CommunityMsg> m_MapIdToCommMsg;
	
	private ServerConnector vConnector;
	private ListView vCommMsgList;
	private String enc_user;
	private String enc_pass;

	/** Called to load the messages in the background */
    private void startLoadComMsgTask() {        
    	ProgressDialog dialog = ProgressDialog.show( getActivity(), "Loading...", "Please wait...", true);
    	
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
	public void onResume() {
		super.onResume();	
		startLoadComMsgTask();
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	Bundle bundle = getArguments();
    	enc_user = bundle.getString("enc_user");
    	enc_pass = bundle.getString("enc_pass");
        
        vConnector = new ServerConnector(getActivity(), enc_user, enc_pass);
		//startLoadComMsgTask();
		return inflater.inflate(R.layout.main, container, false);
    }
    
    private String getCommMsgLocation(int iMsgID) {
    	CommunityMsg vTemp = m_MapIdToCommMsg.get(iMsgID);
    	if(vTemp != null) {
    		return vTemp.getLatLong();
    	} else {
    		return "";
    	}
    }
    
    private String getCommMsgTitle(int iMsgID) {
    	CommunityMsg vTemp = m_MapIdToCommMsg.get(iMsgID);
    	if(vTemp != null) {
    		return vTemp.getMsgTitle();
    	} else {
    		return "";
    	}
    }
    
    private String getCommMsgDetails(int iMsgID) {
    	CommunityMsg vTemp = m_MapIdToCommMsg.get(iMsgID);
    	if(vTemp != null) {
    		return vTemp.getMsgDescription();
    	} else {
    		return "";
    	}
    }
    
    
    private boolean hasCommMsgLocation(int iMsgID) {
    	String s =  getCommMsgLocation(iMsgID);
    	if(s == null || s.equals("none"))
    		return false;
    	return true;	
    }
    
    private void dispServerDown() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

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
    		 
    		 if( getActivity() != null ) {
    		 	 vCommMsgList = (ListView) getActivity().findViewById(R.id.list);
	    	     vCommMsgList.setAdapter(new CommunityMsgAdapter(getActivity(), R.layout.row, m_asCommAlerts));
	    	     vCommMsgList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    	     vCommMsgList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
	    				@Override
	    				public void onItemClick(AdapterView<?> asListItems, View vListView, int iPos, long id) {			
	    						
	    						if(hasCommMsgLocation(m_ComMsgID.get(iPos))) {
	    							((StartScreen) getActivity()).setMsgToBeShown(m_MapIdToCommMsg.get(m_ComMsgID.get(iPos)));
	    							getActivity().getActionBar().setSelectedNavigationItem(1);
	    						} else {
	    							noLocationPopup(m_MapIdToCommMsg.get(m_ComMsgID.get(iPos)));
	    						}
	    					}
	    			});
	    	}
    	}
     }
    
    public void noLocationPopup(CommunityMsg msg) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
 
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
}

