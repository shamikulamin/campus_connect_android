package com.campusconnect.supportmap;

import com.google.android.gms.maps.SupportMapFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CCSupportMapFragment extends SupportMapFragment {

	public MapViewCreatedListener listener;
	

	public void setMapViewListener(MapViewCreatedListener listener) {
		this.listener = listener;
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View view = super.onCreateView(inflater, container, savedInstanceState);
	    // Notify the view has been created
	    if( listener != null ) {
	        listener.onMapCreated();
	    }
	    return view;
	}
}
