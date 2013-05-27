package com.campusconnect;

import java.util.ArrayList;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CommValidMsgMapActivity extends FragmentActivity  {
	private GoogleMap mMap;
    private ServerConnector m_vServer;
    private ArrayList<CommunityMsg> m_vActiveMsg;
    private ArrayList<Marker> mMarkers;
    private static final double defaultLat = 32.730641, defaultLong = -97.114597;
    private static final LatLngBounds defaultBounds = new LatLngBounds(new LatLng(32.72963871873095,-97.11575031280518), new LatLng(32.73046905458073,-97.11193084716797));
	private static LatLngBounds bounds = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // Suppress title bar for more space
        setContentView(R.layout.comm_valid_msg_map);
        setUpMapIfNeeded();
        CommunityMsg vCommMsgToBeShown = getIntent().getParcelableExtra("CommMsgObject");
        if(vCommMsgToBeShown != null)
        {
        	vCommMsgToBeShown.setLatLong(stripParenthesis(vCommMsgToBeShown));
        	mMarkers = new ArrayList<Marker>();
        	m_vActiveMsg = new ArrayList<CommunityMsg>();
        	m_vActiveMsg.add(vCommMsgToBeShown);
        	displayMessages();
        }
        else
        {
        	startGetMapMsgsTask();
        }
    }
    
    private String stripParenthesis( CommunityMsg msg ) {
    	String latlng = msg.getLatLong();
    	if( latlng.startsWith("(") )
    		return latlng.substring(1,latlng.length()-1);
    	return latlng;
    }
    
    private void getCommunityMapMessages() throws ConnectTimeoutException {
    	try {
    		mMarkers = new ArrayList<Marker>();
    		m_vServer = new ServerConnector(this);
    		m_vActiveMsg = m_vServer.getCommunityMsgForMap();
    	} catch ( ConnectTimeoutException e ) {
    		throw e;
    	}
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.setOnCameraChangeListener(new OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition arg0) {
                	while(bounds == null);
                    // Move camera.
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
                    if( mMarkers.size() == 1 )
                    	mMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
                    // Remove listener to prevent position reset on camera move.
                    mMap.setOnCameraChangeListener(null);
                }
            });
        }
    }
    
    private void displayMessages() {
    	CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(defaultLat,defaultLong));
    	CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

    	mMap.moveCamera(center);
    	mMap.animateCamera(zoom);
    	mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    	
    	LatLng vFirstPoint = new LatLng(defaultLat, defaultLong);
         for(int i =0; i < m_vActiveMsg.size(); i++)
         {
         	CommunityMsg vMsg = m_vActiveMsg.get(i);
         
         	String asLatLong[] = vMsg.getLatLong().split("\\|");
 	        if(asLatLong.length == 0)
 	        	continue;
 	        
 	        for(int j = 0; j < asLatLong.length; j++)
 	        {	
 	        	String[] latlong = asLatLong[j].split(",");
 	        	if(latlong.length == 2)
 	        	{
 	        		double latitude = Double.parseDouble(latlong[0]);
 	        		double longitude = Double.parseDouble(latlong[1]);
 	        		
 	        		if(i == 0 && j == 0)
 	        		{
 	        			vFirstPoint = new LatLng(latitude,longitude);
 	        		}
 	        		LatLng point = new LatLng(latitude,longitude);
 		        	Marker vMark = mMap.addMarker(new MarkerOptions()
                     .position(point)
                     .title(vMsg.getMsgTitle())
                     .snippet(vMsg.getMsgDescription())
                     .icon(BitmapDescriptorFactory.fromResource(getMarker(vMsg))));
 		        	mMarkers.add(vMark);
 	        	}
 	        }       
         }
         LatLng l = new LatLng(vFirstPoint.latitude,vFirstPoint.longitude);
         mMap.animateCamera(CameraUpdateFactory.newLatLng(l));
         setMapView();
    }
    
    private void setMapView() {
    	if( mMarkers.size() == 0 ) {
    		bounds = defaultBounds;
    		mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(defaultBounds, 5));
    		dispNoMarkers();
    	} else {
	    	//Calculate the markers to get their position
	    	LatLngBounds.Builder b = new LatLngBounds.Builder();
	    	for(Marker m : mMarkers) {
	    	    b.include(m.getPosition());
	    	}
	    	bounds = b.build();
	    	//Change the padding as per needed
	    	//CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 30);
	    	//mMap.animateCamera(cu);
    	}
    }
    
    private int getMarker(CommunityMsg vMsg)
	 {    	
	   	if(vMsg.getMsgType().equalsIgnoreCase("parking"))
	   		return R.drawable.map_icon_parking;
	   	else if(vMsg.getMsgType().equalsIgnoreCase("crimealert"))
	   		return R.drawable.map_icon_crime;
	   	else if(vMsg.getMsgType().equalsIgnoreCase("weather"))
	   		return R.drawable.map_icon_weather;
	   	else
	   		return R.drawable.map_icon_information;    
   }
    
	 private void dispNoMarkers() {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

			// set title
			alertDialogBuilder.setTitle("No Notifications!");

			// set dialog message
			alertDialogBuilder.setMessage("There are currently no notifications to display")
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
	 
	 	/** Called to get community messages in background over network */
	    private void startGetMapMsgsTask() {        
	    	
	        /* Set up a progress dialog for waiting while getting messages */
	    	ProgressDialog dialog = ProgressDialog.show( CommValidMsgMapActivity.this, "Loading...", "Please wait...", true);
	    	
	    	/* Attempt to get messages in a separate thread */
	    	GetCommuMapMsgsTask lt = new GetCommuMapMsgsTask(dialog);
			lt.execute();
	    }
	    
	    /**
	     * This inner class is used to get messages with a nice looking Progress Dialog
	     * */
	    private class GetCommuMapMsgsTask extends AsyncTask<Void, Integer, Void> {
	    	private ProgressDialog progressDialog = null;
	    	private boolean serverDown = false;
	    	 
	    	public GetCommuMapMsgsTask(ProgressDialog progressDialog)
	    	{
	    		this.progressDialog = progressDialog;
	    	}
	    	
	    	@Override
	    	protected Void doInBackground(Void... params) {
				try {
					getCommunityMapMessages();
				} catch (ConnectTimeoutException e) {
					serverDown = true;
				}
				return null;
	    	}

	    	@Override
	    	protected void onPreExecute() {
	    		progressDialog.show();	// Show Progress Dialog before executing authentication
	    	}

	    	@Override
	    	protected void onPostExecute(Void v) {
	    		 progressDialog.dismiss();	// Hide Progress Dialog after executing authentication
	    		 if( serverDown ) {
	    			 dispServerDown();
	    			 serverDown = false;
	    		 } else {
	    			 displayMessages();
	    		 }
	    	}
	     }
    
}
