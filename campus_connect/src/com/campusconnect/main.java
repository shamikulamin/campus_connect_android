package com.campusconnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class main extends Activity {

	// GCM variables.
	private final String SENDER_ID = "610775927206";
	
	// These three arrays should be parallel.
	ArrayList<CommunityMsg> m_asCommAlerts;
	ArrayList<String> m_ComMsgTitles;
	ArrayList<Integer> m_ComMsgID;

	HashMap<Integer, CommunityMsg> m_MapIdToCommMsg;
	
	ServerConnector vConnector;
	ListView vCommMsgList;
	public main()
	{
		vConnector = new ServerConnector();
		loadComMsg();
		
		
		//m_asCommAlerts.add("adnan");
		//m_asCommAlerts.add("sujoy");
	}
	
	private void loadComMsg()
	{
		m_MapIdToCommMsg = new HashMap<Integer, CommunityMsg>();
		
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
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		loadComMsg();		
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // enable push notification.
        /*GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
          GCMRegistrar.register(this, SENDER_ID);
        } else {
          Log.v("Warning", "Already registered");
        }
        */
        
        vCommMsgList = (ListView) findViewById(R.id.list);
        vCommMsgList.setAdapter(new CommunityMsgAdapter(this, R.layout.row, m_asCommAlerts));
        /*vCommMsgList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, m_ComMsgTitles));*/
        vCommMsgList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        
        
        
        vCommMsgList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> asListItems, View vListView, int iPos,
					long id) {			
					
					if(hasCommMsgLocation(m_ComMsgID.get(iPos)))
					{
						Intent vMsgDetailIntent = new Intent(main.this, CommValidMsgMapActivity.class);
						vMsgDetailIntent.putExtra("CommMsgObject", m_asCommAlerts.get(iPos));						
						startActivity(vMsgDetailIntent);
					}
					else
					{
						Intent vMsgDetailIntent = new Intent(main.this, ComMsgDetailsActivity.class);
						vMsgDetailIntent.putExtra("msg_id", m_ComMsgID.get(iPos));
						
						startActivity(vMsgDetailIntent);
					}					
				}
		});
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
}

