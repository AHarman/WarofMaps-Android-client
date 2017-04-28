package com.alexharman.warofmaps;

import android.graphics.Point;

import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class GameState {

	private ArrayList<MapItem> mapItems;
	private Projection mapProjection;

	public GameState() {
		mapItems = new ArrayList<>();
		mapProjection = null;
	}

	/**
	 * Sets the {@link #mapProjection} and updates the {@link #mapItems}'s screen locations
	 *
	 * @param mapProjection new map projection
	 */
	public void setMapProjection(Projection mapProjection) {
		this.mapProjection = mapProjection;

		for (MapItem mapItem : mapItems) {
			mapItem.setScreenLocation(mapProjection.toScreenLocation(mapItem.getMapLocation()));
		}
	}

	public void createMarker(Point screenLocation) {
		if (mapProjection != null) {
			mapItems.add(new MapItem(mapProjection.fromScreenLocation(screenLocation), screenLocation));
		}
	}

	public void createMarker(LatLng mapLocation) {
		if (mapProjection != null) {
			mapItems.add(new MapItem(mapLocation, mapProjection.toScreenLocation(mapLocation)));
		}
	}

	public ArrayList<MapItem> getMapItems() {
		return mapItems;
	}
}
