package com.campusconnect;

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


public class CommMsgMapActivity extends MapActivity {
    
    private MapController mapControl;

    private MapView mapView;
    
    private Button overlayButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // Suppress title bar for more space
        setContentView(R.layout.commmsgmap);
        
        String sLatLong = getIntent().getStringExtra("latlong");
        // for now we are just taking the first location.
        String[] sTemp = sLatLong.split("\\|");
        if(sTemp.length > 0)
        {
        	sLatLong = sTemp[0];
        }
        /////remove this.
        
        
        String sMsgTitle = getIntent().getStringExtra("msg_title");
        String sMsgDetails = getIntent().getStringExtra("msg_details");
        
        // Add map controller with zoom controls
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setSatellite(false);
        mapView.setTraffic(false);
        mapView.setBuiltInZoomControls(true);   // Set android:clickable=true in main.xml

        int maxZoom = mapView.getMaxZoomLevel();
        int initZoom = maxZoom-2;

        mapControl = mapView.getController();
        mapControl.setZoom(initZoom);
        
        
        List<Overlay> mapOverlays = mapView.getOverlays();

        

        String[] asToken = sLatLong.split(",");
        
        double latitude = 51.545538;
        double longitude = -0.477247;
        
        if(asToken.length == 2)
        {
        	latitude = Double.parseDouble(asToken[0]);
        	longitude = Double.parseDouble(asToken[1]);
        }
        
        Drawable vParkingIcon = this.getResources().getDrawable(R.drawable.icon_park1);
        HelloItemizedOverlay vParkingOverlay = new HelloItemizedOverlay(vParkingIcon, mapView.getContext());
        
        Drawable vWeatherIcon = this.getResources().getDrawable(R.drawable.icon_weather);
        HelloItemizedOverlay vWeatherOverlay = new HelloItemizedOverlay(vWeatherIcon, mapView.getContext());
        
        double lat1 = 51.545577;
        double long1 = -0.477247;
        GeoPoint point1 = new GeoPoint((int)(lat1 * 1e6), (int)(long1 * 1e6));
        OverlayItem overlayItem1 = new OverlayItem(point1, "this is another one", "New one!");
        vWeatherOverlay.addOverlay(overlayItem1);
        mapOverlays.add(vWeatherOverlay);
        
        GeoPoint point = new GeoPoint((int)(latitude * 1e6), (int)(longitude * 1e6));        
        OverlayItem overlayitem = new OverlayItem(point, sMsgTitle, sMsgDetails);
        //overlayitem.setMarker(R.id.);
        vParkingOverlay.addOverlay(overlayitem);
        mapOverlays.add(vParkingOverlay);
        
        mapControl.animateTo(point);
        
        
        
        

        overlayButton = (Button)findViewById(R.id.btnGoBack);
        overlayButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View arg0) {
        		/*Intent i = new Intent(ShowLocationActivity.this, SecondActivity.class);
        		startActivity(i);*/
        		finish();
        	}
        });
        
        

    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
    
}
