package com.campusconnect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ReportIncidentActivity extends Activity implements MediaScannerConnectionClient
{
	
	public String[] allFiles;
	private String SCAN_PATH ;
	private static final String FILE_TYPE = "image/*";

	private MediaScannerConnection conn;

	
	final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 111;
	int NUM_OF_IMAGE = 4;
	String[] asImagepath = new String[NUM_OF_IMAGE];
	
	int THUMBNAIL_HEIGHT = 0;
    int THUMBNAIL_WIDTH = 0;
    
    LocationManager m_vLocationManager;
    LocationListener m_vListener;
    
    int m_iCurrentImageNum;
    static final int CAMERA_ACTIVITY_RESULT = 121;
    
    final String m_sProvider = LocationManager.GPS_PROVIDER;
    private static final int DIALOG_SENDING_REPORT = 0;
    
    Activity m_ReportActivity;
    protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.report_incident_layout);
        
        RemoveStorageDirectory();
        AddStorageDirectory();
        
        m_ReportActivity = this;
        
        m_iCurrentImageNum = 0;
        for(int i = 0; i < NUM_OF_IMAGE; i++)
        	asImagepath[i] = new String();
        
        InitializeLocationManager();
               
        Display display = getWindowManager().getDefaultDisplay();
        
        //THUMBNAIL_WIDTH  = (int)(display.getWidth()/NUM_OF_IMAGE);
        //THUMBNAIL_HEIGHT = (int)(display.getHeight()*.2);
        
        
        Button addPhoto = (Button)findViewById(R.id.add_image);
        /*if(validImageIndex(m_iCurrentImageNum + 1))
        	addPhoto.setClickable(false);
        else
        	addPhoto.setClickable(true);
        */
        
        addPhoto.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View arg0) {
	        	 startCameraActivity();	        	 
	         } 
	    });
        
        Button vReport = (Button)findViewById(R.id.btnSendReport);
        vReport.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) 
			{
				EditText vEdit = (EditText)findViewById(R.id.msgdetails);				
				ServerConnector vConnector = new ServerConnector();
				Location location = m_vLocationManager.getLastKnownLocation(m_sProvider);
				if(vConnector.sendIncidentMsg(vEdit.getText().toString(), 
						System.currentTimeMillis(), location, getImageDir()))
				{
					displaySuccessMsg();						
				}
				else
				{
					displayErrorMsg();								
				}
			}
		});
        
        Button vViewImages = (Button)findViewById(R.id.viewCapturedImages);
        vViewImages.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) 
			{
				SCAN_PATH = getFirstImageName();
				startScan();				
			}
		});
        
	}
   
    public void displaySuccessMsg()
    {
    	new AlertDialog.Builder(this).setMessage("Message delivered successfully.")  
           .setTitle("Delivery Report")  
           .setCancelable(false)  
           .setNeutralButton(android.R.string.ok,  
              new DialogInterface.OnClickListener() {  
              public void onClick(DialogInterface dialog, int whichButton){
                  m_ReportActivity.finish();
              }  
              })  
           .show(); 
    }
    
    public void displayErrorMsg()
    {
    	new AlertDialog.Builder(this).setMessage("Message could not be delivered! Please try again.")  
        .setTitle("Delivery Report")  
        .setCancelable(true)  
        .setNeutralButton(android.R.string.ok,  
           new DialogInterface.OnClickListener() {  
           public void onClick(DialogInterface dialog, int whichButton){
              dialog.dismiss();
           }  
           })  
        .show();
    }

	Context GetContext()
	{
		return getBaseContext();
	}
	
	
	protected void startCameraActivity() 
	{
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		
		File vOutput = new File(getNextImageName());
		
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(vOutput));
		startActivityForResult(intent,CAMERA_ACTIVITY_RESULT);
    }
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {     
	  super.onActivityResult(requestCode, resultCode, intent); 
	  switch(requestCode) { 
	    case (CAMERA_ACTIVITY_RESULT) : 
	    { 
	    	if (resultCode == Activity.RESULT_OK) 
	    	{        
	    		/*Bundle extras = intent.getExtras();
	    		Bitmap bm = (Bitmap)extras.get("data");
		      
		      
		      try{
		    	  
		    	  FileOutputStream out = new FileOutputStream(new File(getNextImageName()));
		    	  
		          bm.compress(Bitmap.CompressFormat.PNG, 100, out);
		      }catch(Exception e){
		    	  Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
		      }
		      
		      loadImage(bm);
		      bm.recycle();*/
		      m_iCurrentImageNum++;		      
	    	} 	    
	      
	    } 
	  } 
	}

	/*void loadImage(Bitmap imageBitmap)
	{
		int iResourceID = -1;
		if(m_iCurrentImageNum == 0)
			iResourceID = R.id.img1;
		else if(m_iCurrentImageNum == 1)
			iResourceID = R.id.img2;
		else if(m_iCurrentImageNum == 2)
			iResourceID = R.id.img3;
		else if(m_iCurrentImageNum == 3)
			iResourceID = R.id.img4;
		else 
			return;
		
		ImageView vImg = (ImageView) findViewById(iResourceID);
        
        Float width  = new Float(imageBitmap.getWidth());
        Float height = new Float(imageBitmap.getHeight());
        Float ratio = width/height;
        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, (int)(THUMBNAIL_HEIGHT*ratio), THUMBNAIL_HEIGHT, false);
        
        vImg.setPadding(5, 5, 5, 5);
        vImg.setImageBitmap(imageBitmap);
  	}
	*/
		
	private String getImageDir()
	{
		return Environment.getExternalStorageDirectory()+ File.separator + "campus_connect" + File.separator;
	}
	
	private String getNextImageName()
	{
		String sDir = getImageDir();
		if(validImageIndex(m_iCurrentImageNum))
		{
			sDir = sDir + m_iCurrentImageNum + ".jpg";
			return sDir;
		}
		return "";
			
	}
	private String getFirstImageName()
	{
		String sDir = getImageDir();
		if(m_iCurrentImageNum > 0)
		{
			sDir = sDir + (m_iCurrentImageNum-1) + ".jpg";
			return sDir;
		}
		return "";
	}
	
	protected void onStart()
	{
		super.onStart();
		/*if(m_iCurrentImageNum >= 3)
		{
			Button bAddImage = (Button)findViewById(R.id.add_image);
			if(bAddImage != null)
				bAddImage.setEnabled(false);
		}*/
		
		Button b = (Button)findViewById(R.id.btnSendReport);
		if(b != null)
			b.requestFocus();
		
		
	}
	
	private void InitializeLocationManager()
	{
		if(m_vLocationManager != null)
			return;
		
		//m_vLocationManager.
		
		 // Enable GPS.
        m_vLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        m_vListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {                                   
                }

			@Override
			public void onProviderDisabled(String arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}
            
        };
        if(m_vListener != null)
        {
        	m_vLocationManager.requestLocationUpdates(m_sProvider, 1*60*1000, 20, m_vListener);
        }
        

	}
	
	/*protected void onStop() {
	    super.onStop();
	    m_vLocationManager.removeUpdates(m_vListener);
	}*/
	
	protected void onDestroy()
	{
		m_vLocationManager.removeUpdates(m_vListener);
		super.onDestroy();
	}
	
	private boolean validImageIndex(int iVal)
	{
		return (iVal >= 0 && iVal < NUM_OF_IMAGE);
	}
	
	
	
	void AddStorageDirectory()
	{
		File v = new File(getImageDir());
		v.mkdirs();
	}
	
	void RemoveStorageDirectory()
	{
		File v = new File(getImageDir());
		if(v.exists())
			DeleteRecursive(v);
	}
	
	void DeleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            DeleteRecursive(child);

	    fileOrDirectory.delete();
	}
	
	private void startScan()
    {
		Log.d("Connected","success"+conn);
		if(conn!=null)
	    {
			conn.disconnect();
	    }
		if(getFirstImageName() != "")
		{
			conn = new MediaScannerConnection(this,this);
			conn.connect();
		}
    }


	@Override
	public void onMediaScannerConnected() {
		Log.d("onMediaScannerConnected","success"+conn);
	    conn.scanFile(SCAN_PATH, FILE_TYPE); 
		
	}

	@Override
	public void onScanCompleted(String path, Uri uri) 
	{
		try 
		{
	        Log.d("onScanCompleted",uri + "success"+conn);
	        if (uri != null) 
	        {
		        Intent intent = new Intent(Intent.ACTION_VIEW);
		        intent.setData(uri);
		        startActivity(intent);
	        }
		}
		finally 
	    {
	        conn.disconnect();
	        conn = null;
	    }
	 }

}
