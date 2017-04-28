package com.alexharman.warofmaps;

import android.Manifest;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity
		implements GoogleApiClient.OnConnectionFailedListener,
		GoogleApiClient.ConnectionCallbacks,
		LocationListener,
		PermissionDeniedDialogFragment.PermissionDeniedDialogListener,
		OnMapReadyCallback {

	/**
	 * Default map zoom level
	 */
	private static final int DEFAULT_ZOOM_LEVEL = 18;

	/**
	 * Flag to let {@link #onRequestPermissionsResult}  know that permissions were requested from {@link #onCreate}
	 */
	private static final int ONCREATE_PERM_REQ  = 1;

	/**
	 * Flag to let {@link #onRequestPermissionsResult} know that permissions were requested from {@link #configureMap}
	 */
	private static final int CONFIGURE_MAP_PERM_REQ = 2;

	/**
	 * Flag to let {@link #onRequestPermissionsResult} know that permissions were requested from {@link #startLocationUpdates}
	 */
	private static final int START_LOCATION_PERM_REQ = 3;

	/**
	 * If the requestCode passed to {@link #onRequestPermissionsResult} is less than this, it's requesting location.
	 */
	private final static int PERMISSION_REQUEST_LOCATION_MAX = 10;

	/**
	 * GoogleMap object
	 */
	GoogleMap map;

	/**
	 * GoogleApiClient to connect to Google Play services (including maps)
	 */
	GoogleApiClient googleApiClient = null;


	/**
	 * location of the user
 	 */
	private Marker userMarker = null;

	/**
	 * Overlay SurfaceView that graphics are drawn to
	 */
	private MapOverlayView overlay;

	/**
	 * Game State
	 */
	private GameState gameState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_map);

		gameState = new GameState();
		overlay = (MapOverlayView) findViewById(R.id.map_overlay);
		overlay.setZOrderOnTop(true);
		overlay.setGameState(gameState);

		requestLocationPermissions(ONCREATE_PERM_REQ);
		googleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		createMap();
	}

	@Override
	protected void onStart() {
		super.onStart();
		googleApiClient.connect();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopLocationUpdates();
		googleApiClient.disconnect();
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
		configureMap();
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		startLocationUpdates();
	}

	@Override
	public void onConnectionSuspended(int i) { }

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode < PERMISSION_REQUEST_LOCATION_MAX) {
			// Array empty is a failure
			if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				return;
			}

			// If we've got permission we need to double check that we've done the following.
			if (googleApiClient.isConnected()) {
				startLocationUpdates();
			}
			if (map != null) {
				configureMap();
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.i("Location", "Updated location: (" + location.getLatitude() + ", " + location.getLongitude() + ")");
		if (map != null) {
			setMapLocation(location, map.getCameraPosition().zoom, true);
			gameState.setMapProjection(map.getProjection());
		}
	}

	@Override
	public void onPermissionDialogButton() {
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
	}

	/**
	 * Requests permission to use location.
	 *
	 * If permission not given (but "never ask me again" not selected) then prompt with a dialog
	 */
	private void requestLocationPermissions(int requestCode) {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
				DialogFragment popup = new PermissionDeniedDialogFragment();
				popup.show(getFragmentManager(), "nag");
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
			}
		}
	}

	/**
	 * Creates map fragment object and sets basic options for the map.
	 */
	private void createMap() {
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	/**
	 * Once the map has been created, use this to change a few options
	 *
	 * This functions may be recalled if it didn't complete due to not have the correct permissions
	 * Ensure sure that it don't inappropriately chamge the state
	 */
	protected synchronized void configureMap() {

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			Location loc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

			MarkerOptions markO /*polo*/ = new MarkerOptions();
			markO.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
			markO.flat(true);
			markO.anchor(0.5f, 0.5f);
			markO.position(loc != null ? locationToLatLng(loc) : new LatLng(0, 0));
			userMarker = map.addMarker(markO);

			if (loc != null) {
				setMapLocation(loc, DEFAULT_ZOOM_LEVEL, false);
			}
		} else {
			requestLocationPermissions(CONFIGURE_MAP_PERM_REQ);
			return;
		}

		map.getUiSettings().setRotateGesturesEnabled(true);
		map.getUiSettings().setScrollGesturesEnabled(false);
		map.getUiSettings().setTiltGesturesEnabled(false);
		map.getUiSettings().setCompassEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(false);
		map.setMinZoomPreference(16);
		map.setMaxZoomPreference(19);
		map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_json));

		map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latLng) {
				gameState.createMarker(latLng);
			}
		});
		map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
			@Override
			public void onCameraMove() {
				gameState.setMapProjection(map.getProjection());
			}
		});
	}


	/**
	 * Converts a Location object to a LatLng object
 	 * @param location Location object
	 */
	private LatLng locationToLatLng(@NonNull Location location) {
		return new LatLng(location.getLatitude(), location.getLongitude());
	}

	/**
	 * Change the map camera location, using an animation
	 * @param location Location to change map to
	 * @param zoom Zoom level to change to. Between 1 and 20
	 * @param animate Whether to animate the transition
	 */
	private void setMapLocation(Location location, float zoom, boolean animate) {
		LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		CameraUpdate camUpdate;
		camUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);

		LatLng ll = locationToLatLng(location);
		LatLngBounds bounds = new LatLngBounds(ll, ll);
		map.setLatLngBoundsForCameraTarget(bounds);

		userMarker.setPosition(ll);
		userMarker.setRotation(location.getBearing());

		if (animate) {
			map.animateCamera(camUpdate);
		} else {
			map.moveCamera(camUpdate);
		}
	}

	/**
	 * Creates a request to update the location
	 *
	 * This functions may be recalled if it didn't complete due to not have the correct permissions
	 * Ensure sure that it don't inappropriately change the state
	 */
	private synchronized void startLocationUpdates() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			LocationRequest locationRequest = new LocationRequest();
			locationRequest.setInterval(1000);
			locationRequest.setFastestInterval(1000);
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

			LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
		} else {
			requestLocationPermissions(START_LOCATION_PERM_REQ);
		}
	}

	private void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
	}

}
