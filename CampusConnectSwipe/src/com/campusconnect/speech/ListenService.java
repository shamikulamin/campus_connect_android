package com.campusconnect.speech;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.campusconnect.R;

public class ListenService extends Service implements SpeechActivationListener {
	private static final String ACTIVATION_TYPE_INTENT_KEY = "ACTIVATION_TYPE_INTENT_KEY";
	private static final String ACTIVATION_RESULT_INTENT_KEY = "ACTIVATION_RESULT_INTENT_KEY";
	private static final String ACTIVATION_RESULT_BROADCAST_NAME = "com.campusconnect.speech.ACTIVATION";
	
    /**
     * send this when external code wants the Service to stop
     */
    public static final String ACTIVATION_STOP_INTENT_KEY = "ACTIVATION_STOP_INTENT_KEY";
	
	private static final int NOTIFICATION_ID = 11342;
	private SpeechActivator activator;
	private boolean isStarted = false;
	private boolean reportServiceStarted = false;
	private int listenTime;
	private Handler mHandler;
	private Runnable r;
	
	@Override
    public void onCreate() {
        super.onCreate();
        isStarted = false;
        mHandler = new Handler();
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
            if (intent.hasExtra(ACTIVATION_STOP_INTENT_KEY)) {
                Log.d("ListenService", "stop service intent");
                activated(false);
                if( reportServiceStarted ) {
                	mHandler.removeCallbacks(r);
                	stopSelf();
                }
            } else {
                if (isStarted) {
                	Log.d("ListenService", "already started");
                } else {
                    // activator not started, start it
                    startDetecting(intent);
                    /* Get SharedPreferences */
                	SharedPreferences sp = getSharedPreferences("com.campusconnect_preferences", MODE_PRIVATE);
                	listenTime = Integer.parseInt(sp.getString("help_word_listen_time", "5"));
                	listenTime = listenTime * 60 * 1000; // milliseconds
                    r = new Runnable() {
            			@Override
            			public void run() {
            				Log.d("INSIDE HANDLER","CALLING ACTIVATED FALSE");
            				activated(false);
            			}
            		};
            		mHandler.postDelayed(r, listenTime);
                }
            }
        }
        return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
        Log.d("ListenService", "On destroy");
        super.onDestroy();
        if( !reportServiceStarted )
        	stopActivator();
        stopForeground(true);
	}
	
	@Override
    public void activated(boolean success) {
        // make sure the activator is stopped before doing anything else
        stopActivator();
        
        if( success ) {
        	Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        	// Vibrate for 7 Seconds
        	v.vibrate(7000);
        	reportServiceStarted = true;
        	NotificationManager vNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        	NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        	builder.setContentTitle("Help Word Heard!");
        	builder.setContentText("Sending report! Click cancel to cancel the report");
			vNotifyManager.notify(NOTIFICATION_ID, builder.build());
        	Intent servi = new Intent(this, HelpWordReportService.class);
            startService(servi);
        } else {
        	if( !reportServiceStarted )
        		Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
        }

        if( !reportServiceStarted ) {
        	mHandler.removeCallbacks(r);
        	stopSelf();
        }
    }
	
	private void startDetecting(Intent intent) {
		 /* Get SharedPreferences */
     	SharedPreferences sp = getSharedPreferences("com.campusconnect_preferences", MODE_PRIVATE);
     	String help_word = sp.getString("help_word", "help");
        activator = new WordActivator(this, this, help_word);
        isStarted = true;
        activator.detectActivation();
        startForeground(NOTIFICATION_ID, getNotification());
    }
	
	private void stopActivator() {
        if (activator != null) {
            Log.d("ListenService", "stopped: " + activator.getClass().getSimpleName());
            activator.stop();
            //isStarted = false;
        }
    }
	
	 private Notification getNotification() {
	 	Intent i = new Intent(this, ListenService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        Intent canceli = new Intent(this, ListenService.class);
        canceli.putExtra(ACTIVATION_STOP_INTENT_KEY, "");
        PendingIntent cancelpi = PendingIntent.getService(this, (int)System.currentTimeMillis(), canceli, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.icon)
		        .setContentTitle("Listening...")
		        .setContentText("Listening for your help word")
		        .setContentIntent(pi)
		        .addAction(android.R.drawable.ic_menu_close_clear_cancel,"Stop Listening", cancelpi)
		        .build();
	 }
}