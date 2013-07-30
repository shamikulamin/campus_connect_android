package com.campusconnect.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.campusconnect.R;


public class TosEula {
    private String EULA_PREFIX = "eula_";
    private Activity mActivity;

    public TosEula(Activity context) {
        mActivity = context;
    }

    private PackageInfo getPackageInfo() {
        PackageInfo pi = null;
        try {
             pi = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi;
    }
 
     public void show() {
        PackageInfo versionInfo = getPackageInfo();
 

        final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean hasBeenShown = prefs.getBoolean(eulaKey, false);
        if(hasBeenShown == false){
 

            String title = mActivity.getString(R.string.title) + " v" + versionInfo.versionName;
 
            String message = mActivity.getString(R.string.eula);
 
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false) 
                    .setPositiveButton("Agree", new Dialog.OnClickListener() {
 
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(eulaKey, true);
                            editor.commit();
                            dialogInterface.dismiss();
                        }
                    });
            builder.create().show();
        }
    }
 
}
