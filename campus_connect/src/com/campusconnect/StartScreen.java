package com.campusconnect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartScreen extends Activity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);
        
        Button showMapBtn = (Button) findViewById(R.id.show_in_map);
        showMapBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent mapActivity = new Intent(StartScreen.this, CommValidMsgMapActivity.class);
        		startActivity(mapActivity);
            }
        });
        
        Button showListBtn = (Button) findViewById(R.id.show_as_list);
        showListBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent mainScreen = new Intent(StartScreen.this, main.class);
        		startActivity(mainScreen);
            }
        });
        
        Button reportIncidentBtn = (Button) findViewById(R.id.report_incident);
        reportIncidentBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent reportIncident = new Intent(StartScreen.this, ReportIncidentActivity.class);
        		startActivity(reportIncident);
            }
        });
	}

}
