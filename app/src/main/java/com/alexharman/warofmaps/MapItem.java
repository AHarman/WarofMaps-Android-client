package com.alexharman.warofmaps;

import android.graphics.Point;

import com.google.android.gms.maps.model.LatLng;

public class MapItem {

	private LatLng mapLocation;
	private Point screenLocation;

	public MapItem(LatLng mapLocation, Point screenLocation) {
		this.mapLocation = mapLocation;
		this.screenLocation = screenLocation;
	}

	public LatLng getMapLocation() {
		return mapLocation;
	}

	public void setMapLocation(LatLng mapLocation) {
		this.mapLocation = mapLocation;
	}

	public Point getScreenLocation() {
		return screenLocation;
	}

	public void setScreenLocation(Point screenLocation) {
		this.screenLocation = screenLocation;
	}
}
