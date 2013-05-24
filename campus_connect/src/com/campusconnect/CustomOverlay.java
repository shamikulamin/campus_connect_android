package com.campusconnect;

import java.util.ArrayList;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.Context;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class CustomOverlay extends ItemizedOverlay {

    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private Context mContext = null;
    
    
    public CustomOverlay(Drawable vIcon, Context context) 
    {   	
   	 	super(boundCenterBottom(vIcon));  	 	
        mContext = context;
       }

    public void addOverlay(OverlayItem overlay) {

        mOverlays.add(overlay);
        populate();
    }
    
    @Override
    protected OverlayItem createItem(int i) {
        // TODO Auto-generated method stub
        return mOverlays.get(i);
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return mOverlays.size();
    }

    @Override
    protected boolean onTap(int index) {
      OverlayItem item = mOverlays.get(index);
      //Context mContext = null;
      
      
      TextView text = new TextView(mContext);
      text.setVerticalScrollBarEnabled(true);
      text.setText(item.getSnippet());
    
      
      ScrollView sv = new ScrollView(mContext);
      sv.addView(text);
      sv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT));
      AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
      
      
      
      dialog.setView(sv);
      
      dialog.show();
      
      
      return true;
    }
    
   

}
