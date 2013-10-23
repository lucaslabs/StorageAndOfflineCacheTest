package com.lmn.storage.util.image;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.jakewharton.disklrucache.DiskLruCache;

/**
 * Implementation of DiskLruCache by Jake Wharton modified from
 * http://stackoverflow
 * .com/questions/10185898/using-disklrucache-in-android-4-0-
 * does-not-provide-for-opencache-method.
 * 
 * @author Lucas Nobile
 */
public class DiskLruImageCache implements ImageCache {

	private DiskLruCache mDiskCache;
	private CompressFormat mCompressFormat = CompressFormat.JPEG;
	private static int IO_BUFFER_SIZE = 8 * 1024;
	private int mCompressQuality = 70;
	private static final int APP_VERSION = 1;
	private static final int VALUE_COUNT = 1;

	public DiskLruImageCache(Context context, String uniqueName,
			int diskCacheSize, CompressFormat compressFormat, int quality) {
		try {
			final File diskCacheDir = getDiskCacheDir(context, uniqueName);
			mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION,
					VALUE_COUNT, diskCacheSize);
			mCompressFormat = compressFormat;
			mCompressQuality = quality;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor)
			throws IOException, FileNotFoundException {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(editor.newOutputStream(0),
					IO_BUFFER_SIZE);
			return bitmap.compress(mCompressFormat, mCompressQuality, out);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private File getDiskCacheDir(Context context, String uniqueName) {

		final String cachePath = context.getCacheDir().getPath();
		return new File(cachePath + File.separator + uniqueName);
	}

	@Override
	public void putBitmap(String key, Bitmap data) {

		String hashKey = createKey(key);
		DiskLruCache.Editor editor = null;
		try {
			editor = mDiskCache.edit(hashKey);
			if (editor == null) {
				return;
			}

			if (writeBitmapToFile(data, editor)) {
				mDiskCache.flush();
				editor.commit();

				Log.d("cache_test_DISK_", "image put on disk cache " + hashKey);
			} else {
				editor.abort();

				Log.d("cache_test_DISK_", "ERROR on: image put on disk cache "
						+ hashKey);
			}
		} catch (IOException e) {

			Log.d("cache_test_DISK_", "ERROR on: image put on disk cache "
					+ hashKey);
			try {
				if (editor != null) {
					editor.abort();
				}
			} catch (IOException ignored) {
			}
		}

	}

	@Override
	public Bitmap getBitmap(String key) {

		String hashKey = createKey(key);
		Bitmap bitmap = null;
		DiskLruCache.Snapshot snapshot = null;
		try {

			snapshot = mDiskCache.get(hashKey);
			if (snapshot == null) {
				return null;
			}
			final InputStream in = snapshot.getInputStream(0);
			if (in != null) {
				final BufferedInputStream buffIn = new BufferedInputStream(in,
						IO_BUFFER_SIZE);
				bitmap = BitmapFactory.decodeStream(buffIn);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (snapshot != null) {
				snapshot.close();
			}
		}

		Log.d("cache_test_DISK_", bitmap == null ? "" : "image read from disk "
				+ hashKey);

		return bitmap;

	}

	public boolean containsKey(String key) {

		boolean contained = false;
		DiskLruCache.Snapshot snapshot = null;
		try {
			snapshot = mDiskCache.get(key);
			contained = snapshot != null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (snapshot != null) {
				snapshot.close();
			}
		}

		return contained;
	}

	public void clearCache() {
		Log.d("cache_test_DISK_", "disk cache CLEARED");
		try {
			mDiskCache.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getCacheFolder() {
		return mDiskCache.getDirectory();
	}

	/**
	 * Creates a unique cache key based on a url value
	 * 
	 * @param url
	 *            url to be used in key creation
	 * @return cache key value
	 */
	private String createKey(String url) {

		// IMPORTANT: keys must match regex [a-z0-9_-]{1,64}.
		// Valid keys are:
		// 1. Exactly 64.
		// key =
		// "0123456789012345678901234567890123456789012345678901234567890123";
		// 2. Contains all valid characters.
		// key = "abcdefghijklmnopqrstuvwxyz_0123456789";
		// 3. Contains dash.
		// key = "-20384573948576";
		// See
		// https://github.com/JakeWharton/DiskLruCache/blob/master/src/test/java/com/jakewharton/disklrucache/DiskLruCacheTest.java
		return String.valueOf(url.hashCode());
	}
}
