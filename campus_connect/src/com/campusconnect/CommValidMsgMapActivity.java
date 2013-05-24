package com.campusconnect;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


public class CommValidMsgMapActivity extends MapActivity {
    
    private MapController mapControl;

    private MapView mapView;
    
    private Button overlayButton;
    
    private ServerConnector m_vServer;
    
    ArrayList<CommunityMsg> m_vActiveMsg;
    
    public CommValidMsgMapActivity() {
		
	}
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommunityMsg vCommMsgToBeShown = (CommunityMsg)getIntent().getSerializableExtra("CommMsgObject");
        if(vCommMsgToBeShown != null)
        {
        	m_vActiveMsg = new ArrayList<CommunityMsg>();
        	m_vActiveMsg.add(vCommMsgToBeShown);
        }
        else
        {
        	m_vServer = new ServerConnector();
    		m_vActiveMsg = m_vServer.getCommunityMsgForMap();
        }
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // Suppress title bar for more space
        setContentView(R.layout.comm_valid_msg_map);

        // Add map controller with zoom controls
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setSatellite(false);
        mapView.setTraffic(false);
        mapView.setBuiltInZoomControls(true);   // Set android:clickable=true in main.xml

        int maxZoom = mapView.getMaxZoomLevel();
        int initZoom = maxZoom-2;

        mapControl = mapView.getController();
        mapControl.setZoom(initZoom);
        
        double defaultLat = 32.70295540655629;
        double defaultLong = -97.14206452645874;
        
        GeoPoint vFirstPoint = new GeoPoint((int)( defaultLat* 1e6), (int)(defaultLong* 1e6));
        List<Overlay> mapOverlays = mapView.getOverlays();
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
	        			vFirstPoint = new GeoPoint((int)( latitude* 1e6), (int)(longitude* 1e6));
	        		}
	        		
		        	GeoPoint point = new GeoPoint((int)( latitude* 1e6), (int)(longitude * 1e6));        
		            OverlayItem overlayitem = new OverlayItem(point, vMsg.getMsgTitle(), vMsg.getMsgDescription());
		            CustomOverlay vIconOverlay = new CustomOverlay(getMarker(vMsg), mapView.getContext());
		            vIconOverlay.addOverlay(overlayitem);
		            mapOverlays.add(vIconOverlay);
	        	}
	        }       
        } 
        mapControl.animateTo(vFirstPoint);
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	 private Drawable getMarker(CommunityMsg vMsg)
	 {    	
    	if(vMsg.getMsgType().equalsIgnoreCase("parking"))
    		return getResources().getDrawable(R.drawable.map_icon_parking);
    	else if(vMsg.getMsgType().equalsIgnoreCase("crimealert"))
    		return getResources().getDrawable(R.drawable.map_icon_crime);
    	else if(vMsg.getMsgType().equalsIgnoreCase("weather"))
    		return getResources().getDrawable(R.drawable.map_icon_weather);
    	else
    		return getResources().getDrawable(R.drawable.map_icon_information);    		 
    	
    }
    
}
