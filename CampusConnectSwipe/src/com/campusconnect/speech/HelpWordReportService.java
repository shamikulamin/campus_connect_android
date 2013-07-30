package com.campusconnect.speech;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.campusconnect.R;
import com.campusconnect.ServerConnector;
import com.campusconnect.ssl.SSLManager;

public class HelpWordReportService extends Service implements LocationListener {
	
	/**
     * send this when external code wants the Service to stop
     */
    public static final String REPORT_STOP_INTENT_KEY = "REPORT_STOP_INTENT_KEY";
    private static final String DEFAULT_REPORT_BODY = "This report was automatically generated. Please check the recording.";
    public static final String REPORT_RESULT_BROADCAST_NAME = "com.campusconnect.speech.ACTIVATION";
    public static final String REPORT_RESULT_INTENT_KEY = "ACTIVATION_RESULT_INTENT_KEY";
    private static final String TAG = "HelpWordReportService";
    private static final int NOTIFICATION_ID = 16928;
    
    private boolean isStarted;
    private int recordingTime;
    private String mRecFileName;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private LocationManager locationManager;
    private Location location;
    private String provider;
    private MediaRecorder mRecorder;
    private static Handler mHandler;
    private Runnable r;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
    public void onCreate() {
		Log.d(TAG,"On create");
        super.onCreate();
        RemoveStorageDirectoryUpdateMediastore();
        AddStorageDirectory();
        isStarted = false;
        mRecFileName = null;
        mHandler = new Handler();
        mRecorder = new MediaRecorder();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG,"onStartCommand");
		if (intent != null) {
            if (intent.hasExtra(REPORT_STOP_INTENT_KEY)) {
                Log.d(TAG, "stop service intent");
                //mNotifyManager.cancel(NOTIFICATION_ID);
                isStarted = false;
                cancelReport();
            } else {
                if (isStarted) {
                	Log.d(TAG, "already started");
                } else {
                    // activator not started, start it
                    startTiming();
                }
            }
        }
        return START_STICKY;
	}
	
	private void startTiming() {
		Log.d(TAG,"Started recording for time");
		/* Get SharedPreferences */
    	SharedPreferences sp = getSharedPreferences("com.campusconnect_preferences", MODE_PRIVATE);
    	recordingTime = Integer.parseInt(sp.getString("help_word_recording_time", "30"));
    	recordingTime *= 1000;	// milliseconds
    	isStarted = true;
    	getLocation();
      	startRecording();
      	startForeground(NOTIFICATION_ID, getNotification());
      	showTimeProgress();
	}
	
	private void startRecording() {
		try {
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile(getRecordingFileName());
			mRecorder.prepare();
			mRecorder.start();   // Recording is now started
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		r = new Runnable() {
			@Override
			public void run() {
				stopRecording();
				sendAutoReport();
			}
		};
		mHandler.postDelayed(r, recordingTime);
	}
	
	private void stopRecording() {
		mRecorder.stop();
		mRecorder.release(); // Now the object cannot be reused
	}
	
	private void showTimeProgress() {
		// Start a lengthy operation in a background thread
		new Thread(
				new Runnable() {
					@Override
					public void run() {
						int incr;
						for (incr = (recordingTime/1000); incr > 0; incr-=1) {
							mBuilder.setProgress((recordingTime/1000), incr, false);
							if( isStarted ) {
								mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								Log.d("TimeUpdate", "sleep failure");
							}
						}
					}
				}).start();
	}
	
	private void sendAutoReport() {
    	SendIncReportTask lt = new SendIncReportTask();
		lt.execute();
	}
	
	private void getLocation() {
		provider = locationManager.getBestProvider(new Criteria(), true);
		locationManager.requestSingleUpdate(provider, HelpWordReportService.this, null);
	}
	
	private boolean sendReportNetwork() throws ConnectTimeoutException {
    	try {
    		/* Get SharedPreferences */
        	SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        	String enc_user = sp.getString("uid", "");
        	String enc_pass = sp.getString("p", "");
			ServerConnector vConnector = new ServerConnector(this, enc_user, enc_pass);
			if( location == null ) {
				location = locationManager.getLastKnownLocation(provider);
			}
			return vConnector.sendIncidentMsg(DEFAULT_REPORT_BODY, enc_user, enc_pass, System.currentTimeMillis(), location, getRecordingFileName(), SSLManager.RECORDING_DIRECTORY_TYPE);
    	} catch( ConnectTimeoutException e ) {
    		throw e;
    	}
    }
	
	private void finishReport(boolean success) {
	/*	if( success ) {
			mBuilder.setContentText("Incident Report Filed")
			.setContentTitle("An incident report has been filed. The police will investigate this incident.")
			.setAutoCancel(true)
			.setContentIntent(PendingIntent.getService(this, 0, new Intent(), 0))
			// Removes the progress bar
	        .setProgress(0,0,false);
			//mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
			Notification n = new NotificationCompat.Builder(this)
			.setContentText("Incident Report Filed")
			.setContentTitle("An incident report has been filed. The police will investigate this incident.")
			.setAutoCancel(true)
			.build();
			startForeground(NOTIFICATION_ID, n);
		} else {
			mBuilder.setContentText("An Error Occurred")
			.setContentTitle("An error has occured while sending the incident report. However, a recording of this incident has been saved.")
			.setAutoCancel(true)
			.setContentIntent(PendingIntent.getService(this, 0, new Intent(), 0))
			// Removes the progress bar
	        .setProgress(0,0,false);
			mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
		}*/
		
		//mNotifyManager.cancel(NOTIFICATION_ID);
		stopForeground(true);
		
		// broadcast result
        Intent intent = new Intent(REPORT_RESULT_BROADCAST_NAME);
        intent.putExtra(REPORT_RESULT_INTENT_KEY, success);
        sendBroadcast(intent);
        
		stopSelf();
	}
	
	private Notification getNotification() {
	 	Intent i = new Intent(this, HelpWordReportService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        Intent canceli = new Intent(this, HelpWordReportService.class);
        canceli.putExtra(REPORT_STOP_INTENT_KEY, "");
        PendingIntent cancelpi = PendingIntent.getService(this, (int)System.currentTimeMillis(), canceli, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(this);
        return 	mBuilder
		        .setSmallIcon(R.drawable.icon)
		        .setContentTitle("Recording...")
		        .setContentText("Recording audio for report.\n Click cancel to cancel this report.")
		        .setContentIntent(pi)
		        .setPriority(Notification.PRIORITY_MAX)
		        .addAction(android.R.drawable.ic_menu_close_clear_cancel,"Cancel", cancelpi)
		        .setLights(Color.parseColor("blue"), 1000, 1000)
		        .build();
	}
	
	private void cancelReport() {
		mHandler.removeCallbacks(r);
		locationManager.removeUpdates(this);
		Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
		
		if( mRecorder != null ) {
			mRecorder.stop();
			mRecorder.release(); // Now the object cannot be reused
		}
		
		// broadcast result
        Intent intent = new Intent(REPORT_RESULT_BROADCAST_NAME);
        intent.putExtra(REPORT_RESULT_INTENT_KEY, false);
        intent.putExtra("cancelled", true);
        sendBroadcast(intent);
        
		stopSelf();
	}
	
	private String getRecordingDir() {
		return Environment.getExternalStorageDirectory()+ "/IncidentRecordings/";
	}
	
	private String getRecordingFileName() {
		if( mRecFileName == null ) {
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			mRecFileName = getRecordingDir() + "audio_" + timeStamp + ".3gp";
		}
		return mRecFileName;
	}
	
	void AddStorageDirectory() {
		File v = new File(getRecordingDir());
		v.mkdirs();
	}
	
	private void RemoveStorageDirectoryUpdateMediastore() {
		RemoveStorageDirectory();
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}
	
	private void RemoveStorageDirectory() {
		File v = new File(getRecordingDir());
		if(v.exists())
			DeleteRecursive(v);
	}
	
	private void DeleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            DeleteRecursive(child);
	    fileOrDirectory.delete();
	}

	/**
     * This inner class is used to send report with a nice looking Progress Dialog
     * */
    private class SendIncReportTask extends AsyncTask<Void, Integer, Boolean> {
    	private boolean serverDown = false;
    	
    	@Override
    	protected Boolean doInBackground(Void... params) {
    		boolean sent = false;
			try {
				//getLocation();
				sent = sendReportNetwork();
			} catch (ConnectTimeoutException e) {
				serverDown = true;
			}
			return sent;
    	}

    	@Override
    	protected void onPreExecute() {
    		// Nothing to do here
    	}

    	@Override
    	protected void onPostExecute(Boolean result) {
    		 if( serverDown ) {
    			 finishReport(false);
    		 } else {
    			 finishReport(result.booleanValue());
    		 }
    	}
     }

	@Override
	public void onLocationChanged(Location location) {
		this.location = location;
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
