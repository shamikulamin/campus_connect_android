package com.campusconnect;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class ViewImage implements MediaScannerConnectionClient {
    public String[] allFiles;
    private Context ctx;
    private String SCAN_PATH;
    private static final String FILE_TYPE = "image/png";
    private MediaScannerConnection conn;
    
    
    /** Called when the activity is first created. */
   // @Override
    public ViewImage(Context ctx) {
    	this.ctx = ctx;
    	File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MyImages");
    	allFiles = imagesFolder.list();
    	SCAN_PATH = Environment.getExternalStorageDirectory().toString()+"/MyImages/"+allFiles[0];
    	startScan();
    }
    
	@Override
	public void onMediaScannerConnected() {
		Log.d("onMediaScannerConnected","success"+conn);
		conn.scanFile(SCAN_PATH, FILE_TYPE);    
	}
	
	@Override
	public void onScanCompleted(String path, Uri uri) {
		try {
			Log.d("onScanCompleted",uri + "success"+conn);
			if (uri != null) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				ctx.startActivity(intent);
			}
		} finally {
			conn.disconnect();
			conn = null;
		}
	}
	
	public void startScan() {
		Log.d("Connected","success"+conn);
		if(conn != null) {
			conn.disconnect();
		}
		conn = new MediaScannerConnection(ctx,this);
		conn.connect();
	}
}