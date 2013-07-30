package com.campusconnect.speech;

import com.campusconnect.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ReportBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "ReportBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(HelpWordReportService.REPORT_RESULT_BROADCAST_NAME)) {
	        Log.d(TAG, "ReportBroadcastReceiver taking action");
	        
	        boolean success = intent.getBooleanExtra(HelpWordReportService.REPORT_RESULT_INTENT_KEY, false);
	        
	        NotificationManager vNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	        
	        if( !intent.hasExtra("cancelled") ) {
		        if( success ) {
		        	vNotifyManager.notify(9823, getNotification(context));
		        } else {
		        	vNotifyManager.notify(9823, getFailNotification(context));
		        }
	        }
	        
	        //Notify ListenService that the ReportService has finished
	        Intent i = new Intent(context, ListenService.class);
	        i.putExtra(ListenService.ACTIVATION_STOP_INTENT_KEY, "");
	        context.startService(i);
		}
	}
	
	private Notification getNotification(Context c) {
	 	Intent i = new Intent();
        PendingIntent pi = PendingIntent.getService(c, 0, i, 0);
        return new NotificationCompat.Builder(c)
		        .setSmallIcon(R.drawable.icon)
		        .setAutoCancel(true)
		        .setContentTitle("Incident Report Filed")
		        .setContentText("An incident report has been filed. The police will investigate this incident.")
		        .setContentIntent(pi)
		        .build();
	 }
	
	private Notification getFailNotification(Context c) {
		Intent i = new Intent();
        PendingIntent pi = PendingIntent.getService(c, 0, i, 0);
        return new NotificationCompat.Builder(c)
		        .setSmallIcon(R.drawable.icon)
		        .setAutoCancel(true)
		        .setContentTitle("An Error Occurred")
		        .setContentText("An error has occured while sending the incident report. However, a recording of this incident has been saved.")
		        .setContentIntent(pi)
		        .build();
	}
}
