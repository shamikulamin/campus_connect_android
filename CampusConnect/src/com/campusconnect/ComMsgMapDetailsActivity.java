package com.campusconnect;

import java.util.ArrayList;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ComMsgMapDetailsActivity extends FragmentActivity implements OnInfoWindowClickListener {
	private GoogleMap mMap;
    private ServerConnector m_vServer;
    private ArrayList<CommunityMsg> m_vActiveMsg;
    private ArrayList<Marker> mMarkers;
    private String enc_user = null;
    private String enc_pass = null;
    
    //private static final double defaultLat = 32.730641, defaultLong = -97.114597;
    
    
    private static  final double UTA_SOUTHWEST_LAT = 32.72963871873095;
    private static  final double UTA_SOUTHWEST_LONG = -97.11575031280518;
    private static final double UTA_NORTHEAST_LAT = 32.73046905458073;
    private static final double UTA_NORTHEAST_LONG = -97.11193084716797;
    
    private static final LatLngBounds defaultBounds = 
    		new LatLngBounds(new LatLng(UTA_SOUTHWEST_LAT, UTA_SOUTHWEST_LONG), new LatLng(UTA_NORTHEAST_LAT, UTA_NORTHEAST_LONG)); //set the mapview to the UTA campus
	
    // The currentBounds will be updated based on the location of the posted messages.
    private static LatLngBounds currentBounds = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // Suppress title bar for more space
        setContentView(R.layout.comm_valid_msg_map);
        
        enc_user = getIntent().getStringExtra("enc_user");
        enc_pass = getIntent().getStringExtra("enc_pass");
        
        setUpMapIfNeeded();
        
        // We set the "CommMsgObject" parameter with the CommunityMsg object whenever we want to display a specific 
        // message on the map, i.e., from the list view or through a push notification.
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
    		m_vServer = new ServerConnector(this, enc_user, enc_pass);
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
            //mMap.setInfoWindowAdapter(new CCInfoWindowAdapter(getLayoutInflater()));
            mMap.setOnInfoWindowClickListener(this);
        }
    }
    
    /** This method is responsible for displaying the community messages on the map. It expects the 
     *  the member variables (mMarkers, m_vServer, m_vActiveMsg) to be initialized (non null). */
    private void displayMessages() {
    	CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(UTA_SOUTHWEST_LAT, UTA_SOUTHWEST_LONG));
    	CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

    	mMap.moveCamera(center);
    	mMap.animateCamera(zoom);
    	mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    	
    	LatLng vFirstPoint = new LatLng(UTA_SOUTHWEST_LAT, UTA_SOUTHWEST_LONG);
         for(int i = 0; i < m_vActiveMsg.size(); i++)
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
         setMapView();
    }
    
    private void setMapView() {
    	WindowManager wm = getWindowManager();
    	Display d = wm.getDefaultDisplay();
    	
    	// Google recommends to use Display.getSize() instead of Display.getWidth() and Display.getHeight()     	
    	if( mMarkers.size() == 0 ) {
    		currentBounds = defaultBounds;
    		mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(defaultBounds,d.getWidth(),d.getHeight(),30));
    		dispNoMarkers();
    	} else {
	    	//Calculate the markers to get their position
	    	LatLngBounds.Builder b = new LatLngBounds.Builder();
	    	for(Marker m : mMarkers) {
	    	    b.include(m.getPosition());
	    	}
	    	// update the currentBounds to display all the markers.
	    	currentBounds = b.build();
	    	mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(currentBounds,d.getWidth(),d.getHeight(),30));
	    	/*if( mMarkers.size() == 1 )
	    		mMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));*/
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
			alertDialogBuilder.setTitle("Server Down");

			// set dialog message
			alertDialogBuilder.setMessage("The server is not responding. \n\nPlease try again later.")
							  .setCancelable(false)
							  .setNeutralButton("OK",new DialogInterface.OnClickListener() {
								  public void onClick(DialogInterface dialog,int id) {
								  		dialog.cancel();	// if this button is clicked, close dialog.
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
	    	ProgressDialog dialog = ProgressDialog.show( ComMsgMapDetailsActivity.this, "Loading...", "Please wait...", true);
	    	
	    	/* Attempt to get messages in a separate thread */
	    	ComMsgDownloaderTask lt = new ComMsgDownloaderTask(dialog);
			lt.execute();
	    }
	    
	    /**
	     * This inner class is used to get messages with a nice looking Progress Dialog
	     * */
	    private class ComMsgDownloaderTask extends AsyncTask<Void, Void, Void> {
	    	private ProgressDialog m_vProgressDialog = null;
	    	private boolean m_bServerDown = false;
	    	 
	    	public ComMsgDownloaderTask(ProgressDialog progressDialog)
	    	{
	    		this.m_vProgressDialog = progressDialog;
	    	}
	    	
	    	@Override
	    	protected Void doInBackground(Void... params) {
				try {
					getCommunityMapMessages();
				} catch (ConnectTimeoutException e) {
					m_bServerDown = true;
				}
				return null;
	    	}

	    	@Override
	    	protected void onPreExecute() {
	    		m_vProgressDialog.show();	// Show Progress Dialog before running doInBackground()
	    	}

	    	@Override
	    	protected void onPostExecute(Void v) {
	    		 m_vProgressDialog.dismiss();	// Hide Progress Dialog after executing doInBackground()
	    		 if( m_bServerDown ) {
	    			 dispServerDown();
	    			 //m_bServerDown = false;
	    		 } else {
	    			 displayMessages();
	    		 }
	    	}
	     }

		@Override
		public void onInfoWindowClick(Marker marker) {
			// TODO Auto-generated method stub
			// Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
			/*Intent intent = new Intent(this, MapPopupActivity.class);
			intent.putExtra("title",marker.getTitle());
			intent.putExtra("snippet", marker.getSnippet());
			startActivity(intent);*/
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	 
				// set title
				alertDialogBuilder.setTitle(marker.getTitle());
	 
				// set dialog message
				alertDialogBuilder
					.setMessage(marker.getSnippet())
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
