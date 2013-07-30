package com.campusconnect;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.conn.ConnectTimeoutException;

import com.campusconnect.ssl.SSLManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ReportIncidentFragment extends Fragment {

	/* Username and Password for networking */
	private String enc_user = null;
    private String enc_pass = null;
    
    // Codes
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 111;
	private static final int CAMERA_ACTIVITY_RESULT = 121;
	
	/* Image Stuff */
	private static final int NUM_OF_IMAGE = 10;
	private int m_iCurrentImageNum = 0;
	private String[] asImagepath = new String[NUM_OF_IMAGE];
    private LocationManager m_vLocationManager;
    private LocationListener m_vListener; 
    private final String m_sProvider = LocationManager.GPS_PROVIDER;
      
    private void setHideKeyboard(View view) {
    	final EditText details = (EditText) view.findViewById(R.id.msgdetails);
    	details.setOnEditorActionListener(new TextView.OnEditorActionListener() {
    		
	    	@Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		    	if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
		    		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(details.getWindowToken(), 0);
	                return true;
		        }
		    	return false;
	    	}
    	});
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.report_incident_layout, container, false);
        setHideKeyboard(v);
        
        Bundle bundle = getArguments();
    	enc_user = bundle.getString("enc_user");
    	enc_pass = bundle.getString("enc_pass");
        
        //RemoveStorageDirectory();
        RemoveStorageDirectoryUpdateMediastore();
        AddStorageDirectory();

        for(int i = 0; i < NUM_OF_IMAGE; i++)
        	asImagepath[i] = new String();
        
        InitializeLocationManager();
               
                   
        Button addPhoto = (Button)v.findViewById(R.id.add_image);  
        addPhoto.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View arg0) {
	        	 startCameraActivity();
	         } 
	    });
        
        Button vReport = (Button)v.findViewById(R.id.btnSendReport);
        vReport.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				startSendIncReportTask();
			}
		});
        
        Button vViewImages = (Button)v.findViewById(R.id.viewCapturedImages);
        vViewImages.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				//Check for no images
				if( m_iCurrentImageNum != 0  ) {
					new ViewImage(getActivity());
				}
			}
		});
        return v;
	}
 
    private boolean sendReport() throws ConnectTimeoutException {
    	try {
	    	EditText vEdit = (EditText)getView().findViewById(R.id.msgdetails);
			ServerConnector vConnector = new ServerConnector(getActivity(), enc_user, enc_pass);
			Location location = m_vLocationManager.getLastKnownLocation(m_sProvider);
			return vConnector.sendIncidentMsg(vEdit.getText().toString(), enc_user, enc_pass, System.currentTimeMillis(), location, getImageDir(), SSLManager.IMAGE_DIRECTORY_TYPE);
    	} catch( ConnectTimeoutException e ) {
    		throw e;
    	}
    }
   
    public void displaySuccessMsg() {
    	new AlertDialog.Builder(getActivity()).setMessage("Message delivered successfully.")  
           .setTitle("Delivery Report")  
           .setCancelable(false)  
           .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {  
	              public void onClick(DialogInterface dialog, int whichButton) {
	            	  getActivity().getActionBar().setSelectedNavigationItem(1);
	              }  
              })  
           .show(); 
    }
    
    public void displayErrorMsg() {
    	new AlertDialog.Builder(getActivity()).setMessage("Message could not be delivered! Please try again.")  
        .setTitle("Delivery Report")  
        .setCancelable(true)  
        .setNeutralButton(android.R.string.ok,  new DialogInterface.OnClickListener() {  
	           public void onClick(DialogInterface dialog, int whichButton){
	              dialog.dismiss();
	           }  
           })
        .show();
    }	
	
	protected void startCameraActivity() {
		//camera stuff
		Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

		//folder stuff
		File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MyImages");
		if(!imagesFolder.exists())
			imagesFolder.mkdirs();

		//String filePath = "/MyImages/QR_" + timeStamp + ".png" ;
		File image = new File(imagesFolder, "QR_" + timeStamp + ".png");
		Uri uriSavedImage = Uri.fromFile(image);
		asImagepath[m_iCurrentImageNum] = image.toString();

		imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
		startActivityForResult(imageIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	    case (CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) : 
	    { 
	    	if (resultCode == Activity.RESULT_OK) {
	    		MediaScannerConnection.scanFile(getActivity(),
	                    new String[] { asImagepath[m_iCurrentImageNum] }, null,
	                    new MediaScannerConnection.OnScanCompletedListener() {
	                public void onScanCompleted(String path, Uri uri) {
	                    Log.i("ExternalStorage", "Scanned " + path + ":");
	                    Log.i("ExternalStorage", "-> uri=" + uri);
	                }
	            });
	    		m_iCurrentImageNum = (m_iCurrentImageNum + 1) % NUM_OF_IMAGE;		      
	    	}
	    }
	  } 
	}

	private String getImageDir() {
		return Environment.getExternalStorageDirectory()+ "/MyImages/";
	}
	
	private void InitializeLocationManager() {
		if(m_vLocationManager != null)
			return;
		
		 // Enable GPS.
        m_vLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        m_vListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {                                   
            }

			@Override
			public void onProviderDisabled(String arg0) {

			}
			@Override
			public void onProviderEnabled(String provider) {

			}
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}
        };
        
        if(m_vListener != null) {
        	m_vLocationManager.requestLocationUpdates(m_sProvider, 1*60*1000, 20, m_vListener);
        }
	}
	
	public void onDestroy() {
		m_vLocationManager.removeUpdates(m_vListener);
		super.onDestroy();
	}
	
	void AddStorageDirectory() {
		File v = new File(getImageDir());
		v.mkdirs();
	}
	
	void RemoveStorageDirectoryUpdateMediastore() {
		RemoveStorageDirectory();
		getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}
	
	void RemoveStorageDirectory() {
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

	private void dispServerDown() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

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
	
	 /** Called to send report over network */
    private void startSendIncReportTask() {        
    	
        /* Set up a progress dialog for waiting while sending report */
    	ProgressDialog dialog = ProgressDialog.show( getActivity(), "Sending Report...", "Please wait...", true);
    	
    	/* Attempt to send report in a separate thread */
    	SendIncReportTask lt = new SendIncReportTask(dialog);
		lt.execute();
    }
    
    /**
     * This inner class is used to send report with a nice looking Progress Dialog
     * */
    private class SendIncReportTask extends AsyncTask<Void, Integer, Boolean> {
    	private ProgressDialog progressDialog = null;
    	private boolean serverDown = false;
    	 
    	public SendIncReportTask(ProgressDialog progressDialog)
    	{
    		this.progressDialog = progressDialog;
    	}
    	
    	@Override
    	protected Boolean doInBackground(Void... params) {
    		boolean sent = false;
			try {
				sent = sendReport();
			} catch (ConnectTimeoutException e) {
				serverDown = true;
			}
			return sent;
    	}

    	@Override
    	protected void onPreExecute() {
    		progressDialog.show();	// Show Progress Dialog before executing authentication
    	}

    	@Override
    	protected void onPostExecute(Boolean result) {
    		 progressDialog.dismiss();	// Hide Progress Dialog after executing authentication
    		 if( serverDown ) {
    			 dispServerDown();
    			 serverDown = false;
    			 //startLoginActivity(false);
    		 } else {
    			if( result.booleanValue() ) {
    				 displaySuccessMsg();						
 				} else {
 					displayErrorMsg();	
 				}
    		 }
    	}
     }
}
