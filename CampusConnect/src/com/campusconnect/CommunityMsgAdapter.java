package com.campusconnect;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommunityMsgAdapter extends ArrayAdapter<CommunityMsg> {

    private ArrayList<CommunityMsg> items;
    private Context m_vContext;
    public CommunityMsgAdapter(Context context, int textViewResourceId, ArrayList<CommunityMsg> items) {
            super(context, textViewResourceId, items);
            m_vContext = context;
            this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        View v = convertView;
        if (v == null) 
        {
            LayoutInflater vi = (LayoutInflater)m_vContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.row, null);
        }
        CommunityMsg o = items.get(position);
        
        if (o != null) 
        {
        		ImageView vImage = (ImageView) v.findViewById(R.id.icon);
        		vImage.setAdjustViewBounds(true);
        		if(CommMsgHelper.isParkingMsg(o))
        			vImage.setImageResource(R.drawable.icon_parking);
        		else if(CommMsgHelper.isCommMsg(o))
        			vImage.setImageResource(R.drawable.icon_crime);
        		else if(CommMsgHelper.isWeatherMsg(o))
        			vImage.setImageResource(R.drawable.icon_weather);
        		else if(CommMsgHelper.isGeneralMsg(o))
        			vImage.setImageResource(R.drawable.icon_general);
        		else
        			Log.e("error", "unknown message type");
                
        		
        		TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if (tt != null) 
                {
                      tt.setText(o.getMsgTitle());                            
                }
                if(bt != null)
                {
                      bt.setText(o.getReportingTime());
                }
        }
        return v;
    }
}
