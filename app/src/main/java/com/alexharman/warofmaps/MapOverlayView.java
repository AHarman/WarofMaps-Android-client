package com.alexharman.warofmaps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;


public class MapOverlayView extends SurfaceView
		implements Runnable,
		           SurfaceHolder.Callback {

	private volatile boolean running;
	SurfaceHolder surfaceHolder;
	Thread thread;

	private VectorDrawableCompat marker;
	private int fps;

	private GameState gameState;

	Paint paint;

	public MapOverlayView(Context context, AttributeSet attributes) {
		super(context, attributes);
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		paint = new Paint();

		marker = VectorDrawableCompat.create(context.getResources(), R.drawable.marker_1, null);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
	}


	@Override
	public void onDraw(Canvas canvas) {
		ArrayList<MapItem> mapItems = gameState.getMapItems();

		canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
		for (int i = 0; i < mapItems.size(); i++) {
			Point markerLocation = mapItems.get(i).getScreenLocation();
			canvas.translate(markerLocation.x - marker.getIntrinsicWidth() * 0.5f,
					markerLocation.y - marker.getIntrinsicHeight() * 0.5f);
			marker.draw(canvas);
			canvas.setMatrix(null);
		}
		canvas.drawText("FPS: " + fps, 20, 20, paint);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("surface", "surfaceCreated");
		resume();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d("surface", "surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("surface", "surfaceDestroyed");
		pause();

	}

	@Override
	public void run() {
		Canvas canvas;
		while (running) {
			long startFrame = System.currentTimeMillis();
			if (surfaceHolder.getSurface().isValid()) {
				canvas = surfaceHolder.lockCanvas();
				draw(canvas);
				surfaceHolder.unlockCanvasAndPost(canvas);
			}
			fps = (int) (1000 / (System.currentTimeMillis() - startFrame));
		}
	}

	public void pause() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			Log.e("Error", "Problem joining thread");
		}
	}

	public void resume() {
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}
}
