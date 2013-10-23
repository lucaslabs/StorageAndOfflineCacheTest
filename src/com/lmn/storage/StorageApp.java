package com.lmn.storage;

import android.app.Application;
import android.graphics.Bitmap.CompressFormat;

import com.lmn.storage.util.image.ImageCacheManager;
import com.lmn.storage.util.image.ImageCacheManager.CacheType;
import com.lmn.storage.util.request.RequestManager;

/**
 * Main entry point of the app.
 * 
 * @author Lucas Nobile
 */
public class StorageApp extends Application {

	public static final String TAG = StorageApp.class.getSimpleName();

	// Handle requests
	public static RequestManager REQUEST_MANAGER;

	// Handle image cache
	public static ImageCacheManager IMAGE_CACHE_MANAGER;

	// Constants for image cache
	private static int DISK_IMAGECACHE_SIZE = 1024 * 1024 * 10; // 10 MB
	private static CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.PNG;
	private static int DISK_IMAGECACHE_QUALITY = 100; // PNG is lossless so
														// quality is ignored
														// but must be provided

	@Override
	public void onCreate() {
		super.onCreate();

		REQUEST_MANAGER = new RequestManager(this);
		
		IMAGE_CACHE_MANAGER = new ImageCacheManager(this,
				this.getPackageCodePath(), DISK_IMAGECACHE_SIZE,
				DISK_IMAGECACHE_COMPRESS_FORMAT, DISK_IMAGECACHE_QUALITY,
				CacheType.DISK); // Use Disk based LRU Image cache implementation.
	}
}
