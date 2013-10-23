package com.lmn.storage.util.image;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader.ImageCache;

/**
 * Basic "Least Recently Used" in memory cache implementation. It is fast and
 * does not block I/O. This is the recommended approach.
 * 
 * @author Lucas Nobile
 */
public class BitmapLruImageCache extends LruCache<String, Bitmap> implements
		ImageCache {

	private final String TAG = this.getClass().getSimpleName();

	public BitmapLruImageCache(int maxSize) {
		super(maxSize);
	}

	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getRowBytes() * value.getHeight();
	}

	@Override
	public Bitmap getBitmap(String url) {
		Log.d(TAG, "Retrieved item from Mem Cache");
		return get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		Log.d(TAG, "Added item to Mem Cache");
		put(url, bitmap);
	}
}
