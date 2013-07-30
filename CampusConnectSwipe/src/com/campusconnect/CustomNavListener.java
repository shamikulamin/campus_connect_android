package com.campusconnect;
import com.campusconnect.ComMsgListFragment;
import com.campusconnect.ComMsgMapDetailsFragment;
import com.campusconnect.CommunityMsg;
import com.campusconnect.EmergencyAssistanceFragment;
import com.campusconnect.R;
import com.campusconnect.ReportIncidentFragment;

import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;


public class CustomNavListener implements OnNavigationListener {
	
	private boolean bPush = false;
	private String enc_user = "";
	private String enc_pass = "";
	private CommunityMsg vCommMsgToBeShown;
	private Context ctx = null;
	
	public CustomNavListener( Context ctx, boolean bPush, String enc_user, String enc_pass, CommunityMsg vCommMsgToBeShown ) {
		this.ctx = ctx;
		this.bPush = bPush;
		this.enc_user = enc_user;
		this.enc_pass = enc_pass;
		this.vCommMsgToBeShown = vCommMsgToBeShown;
	}

	@Override
	  public boolean onNavigationItemSelected(int position, long itemId) {
		  Fragment fragment = null;
		  if( bPush == true ) {
			  fragment = new EmergencyAssistanceFragment();
			  bPush = false;
		  } else {
			  switch (position) {
            case 0:
                fragment = new ComMsgListFragment();
                Bundle b_list = new Bundle();
                b_list.putString("enc_user", enc_user);
                b_list.putString("enc_pass", enc_pass);
                fragment.setArguments(b_list);
                break;
            case 1:
       	 	 fragment = new ComMsgMapDetailsFragment();
       	     Bundle b_map = new Bundle();
               b_map.putString("enc_user", enc_user);
               b_map.putString("enc_pass", enc_pass);
               if( vCommMsgToBeShown != null ) 
              	 b_map.putParcelable("vCommMsgToBeShown", vCommMsgToBeShown);
               fragment.setArguments(b_map);
          	 break;
            case 2:
          	 fragment = new ReportIncidentFragment();
          	 break;
            case 3:
          	 fragment = new EmergencyAssistanceFragment();
          	 break;
            default:
          	 fragment = new ComMsgListFragment();
               Bundle b_def = new Bundle();
               b_def.putString("enc_user", enc_user);
               b_def.putString("enc_pass", enc_pass);
               fragment.setArguments(b_def);
               break;
			 }
		  }
		  ((FragmentActivity) ctx).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
	     vCommMsgToBeShown = null;	// Reset back to null after we've sent the parameter
	     return true;
	  }

}
