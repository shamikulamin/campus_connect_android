package com.campusconnect;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EmergencyAssistanceActivity extends Activity {
	private static String SECURITY_ESCORT_NUM_URI = "tel:+651234567";
	private static String LOSTANDFOUND_NUM_URI = "tel:+651234567";
	private static String PARKING_SERVICE_NUM_URI = "tel:+651234567";
	private static String POLICE_DISPATCH_NUM_URI = "tel:+651234567";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency_assistance_activity_layout);
        
        Button secEscBtn = (Button) findViewById(R.id.security_escort);
        secEscBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent i = new Intent(android.content.Intent.ACTION_DIAL, Uri.parse(SECURITY_ESCORT_NUM_URI));
            	startActivity(i);
            }
        });
        
        Button lostFoundBtn = (Button) findViewById(R.id.lost_and_found);
        lostFoundBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent i = new Intent(android.content.Intent.ACTION_DIAL, Uri.parse(LOSTANDFOUND_NUM_URI));
            	startActivity(i);
            }
        });
        
        Button parkServBtn = (Button) findViewById(R.id.parking_service);
        parkServBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent i = new Intent(android.content.Intent.ACTION_DIAL, Uri.parse(PARKING_SERVICE_NUM_URI));
            	startActivity(i);
            }
        });
        
        Button polDispBtn = (Button) findViewById(R.id.police_dispatch);
        polDispBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent i = new Intent(android.content.Intent.ACTION_DIAL, Uri.parse(POLICE_DISPATCH_NUM_URI));
            	startActivity(i);
            }
        });
	}
}
