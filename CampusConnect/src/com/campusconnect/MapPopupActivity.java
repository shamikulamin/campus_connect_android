package com.campusconnect;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class MapPopupActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.setContentView(R.layout.map_popup_activity);
			/*Button closeButton = (Button) findViewById(R.id.close);
			closeButton.setOnClickListener(new OnClickListener() {
				    @Override
				    public void onClick(View v) {
				      finish();
				    }
			  });*/
			String title = getIntent().getStringExtra("title");
			String snippet = getIntent().getStringExtra("snippet");
			
			TextView tv = (TextView) findViewById(R.id.title);
			tv.setText(title);
			tv= (TextView) findViewById(R.id.snippet);
			tv.setText(snippet);
	}
}
