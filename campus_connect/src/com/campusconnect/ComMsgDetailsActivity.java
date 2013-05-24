package com.campusconnect;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ComMsgDetailsActivity extends Activity{
	
	private ServerConnector vConnector;
	
	public ComMsgDetailsActivity() {
		vConnector = new ServerConnector();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commsgdetail);
        
        

        
        int msgID = getIntent().getIntExtra("msg_id", -1); 
        
        String msgDetail = vConnector.getCommunityMsgDescription(msgID);
        
        Button b = (Button) findViewById(R.id.btnReturnToMain);
        b.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View arg0) {
        		/*Intent i = new Intent(ShowLocationActivity.this, SecondActivity.class);
        		startActivity(i);*/
        		finish();
        	}
        });
        
        TextView txt = (TextView) findViewById(R.id.msgdetails);
        txt.setMovementMethod(new ScrollingMovementMethod());
        txt.setText(msgDetail);
        
       
	}
}
