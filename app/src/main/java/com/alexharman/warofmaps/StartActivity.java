package com.alexharman.warofmaps;

import android.Manifest;
import android.app.ActionBar;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends AppCompatActivity
	implements PermissionDeniedDialogFragment.PermissionDeniedDialogListener{

	/**
	 * Flag to let {@link #onRequestPermissionsResult}  know that permissions were requested from {@link #onCreate}
	 */
	private static final int ONSTART_PERM_REQ  = 1;

	private AnimatedVectorDrawable startAnimation;
	private ImageView startAnimationView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_start);

		startAnimation = (AnimatedVectorDrawable) getDrawable(R.drawable.start_screen_animated_vector);
		startAnimationView = (ImageView) findViewById(R.id.start_animation_view);
		startAnimationView.setImageDrawable(startAnimation);
		startAnimationView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("anim", "we did a click");
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			startAnimationAndTimer();
		} else {
			requestLocationPermissions(ONSTART_PERM_REQ);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == ONSTART_PERM_REQ && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			startAnimationAndTimer();
		}
	}

	@Override
	public void onPermissionDialogButton() {
		//ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
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
	 * Starts the animation and a timer to start the next activity
	 *
	 * Calling something on animation end is reliant on newer SDKs, so we use a timer.
	 */
	private void startAnimationAndTimer() {
		Log.d("anim", "in start animation method");
		startAnimation.start();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				Log.d("anim", "Timer complete");
				Intent intent = new Intent(StartActivity.this, MapActivity.class);
				startActivity(intent);
			}
		}, 3100);
	}
}
