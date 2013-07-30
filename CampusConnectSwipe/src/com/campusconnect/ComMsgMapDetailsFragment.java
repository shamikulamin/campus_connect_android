package com.campusconnect;

import java.util.ArrayList;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.campusconnect.supportmap.CCSupportMapFragment;
import com.campusconnect.supportmap.MapViewCreatedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
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

public class ComMsgMapDetailsFragment extends SupportMapFragment implements OnInfoWindowClickListener, MapViewCreatedListener, LocationListener {
	private GoogleMap mMap;
	private SupportMapFragment fragment;
    private ServerConnector m_vServer;
    private ArrayList<CommunityMsg> m_vActiveMsg;
    private ArrayList<Marker> mMarkers;
    private String enc_user = null;
    private String enc_pass = null;
    private CommunityMsg vCommMsgToBeShown = null;
    private boolean mMapLoaded = false;
    private LocationManager locationManager;
    private Location location;
    private static final String appName = "Campus Connect";
    
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
	public void onMapCreated() {
		mMapLoaded = true;
		this.onResume();
	}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
    	Bundle bundle = getArguments();
    	enc_user = bundle.getString("enc_user");
    	enc_pass = bundle.getString("enc_pass");
    	vCommMsgToBeShown = bundle.getParcelable("vCommMsgToBeShown");
        return inflater.inflate(R.layout.comm_valid_msg_map, container, false);
    }
    
    private void alertNoPlayServices() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
 
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
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity().getApplicationContext());
     	if(status == ConnectionResult.SUCCESS) {
     		fragment = new CCSupportMapFragment();
     		((CCSupportMapFragment)fragment).setMapViewListener(this);
     		getChildFragmentManager().beginTransaction()
     		.replace(R.id.fragment_content, fragment).commit();
     		Bundle b_map = new Bundle();
            b_map.putString("enc_user", enc_user);
            b_map.putString("enc_pass", enc_pass);
            if( vCommMsgToBeShown != null ) 
            	b_map.putParcelable("vCommMsgToBeShown", vCommMsgToBeShown);
            fragment.setArguments(b_map);
            //locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
            //String provider = locationManager.getBestProvider(new Criteria(), true);
    		//locationManager.requestSingleUpdate(provider, this, null);
     	} else {
     		alertNoPlayServices();
     		getActivity().getActionBar().setSelectedNavigationItem(1);
     	}
    }
	
	@Override
	public void onResume() {
		super.onResume();
		if( mMapLoaded ) {
			if( mMap == null ) {
				mMap = fragment.getMap();
				mMap.setOnInfoWindowClickListener(this);
				mMap.setMyLocationEnabled(true);
			}
			if( vCommMsgToBeShown == null ) {
				startGetMapMsgsTask();
			} else {
				vCommMsgToBeShown.setLatLong(stripParenthesis(vCommMsgToBeShown));
	        	mMarkers = new ArrayList<Marker>();
	        	m_vActiveMsg = new ArrayList<CommunityMsg>();
	        	m_vActiveMsg.add(vCommMsgToBeShown);
	        	displayMessages();
			}
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
    		m_vServer = new ServerConnector(getActivity(), enc_user, enc_pass);
    		m_vActiveMsg = m_vServer.getCommunityMsgForMap();
    	} catch ( ConnectTimeoutException e ) {
    		throw e;
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
    	WindowManager wm = getActivity().getWindowManager();
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
	    	
	    	if( location != null ) {
	    		b.include(new LatLng(location.getLatitude(),location.getLongitude()));
	    	}
	    	
	    	if( mMarkers.size() == 1 ) {
	    		CameraUpdate center = CameraUpdateFactory.newLatLng(mMarkers.get(0).getPosition());
	    	    CameraUpdate zoom=CameraUpdateFactory.zoomTo(17);
	    	    mMap.moveCamera(center);
	    	    mMap.animateCamera(zoom);
    		} else {
    			// update the currentBounds to display all the markers.
    	    	currentBounds = b.build();
    	    	mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(currentBounds,d.getWidth(),d.getHeight(),30));
    		}
    	}
    }
    
    private int getMarker(CommunityMsg vMsg) {    	
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
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

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
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

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
	    	ProgressDialog dialog = ProgressDialog.show( getActivity(), "Loading...", "Please wait...", true);
	    	
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
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
	 
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
	    
		@Override
		public void onLocationChanged(Location location) {
			this.location = location;
			setMapView();
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// Future
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// Future
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// Future
		}
}
