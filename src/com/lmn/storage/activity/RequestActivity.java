package com.lmn.storage.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.lmn.storage.R;
import com.lmn.storage.StorageApp;
import com.lmn.storage.util.Util;

/**
 * Activity that executes a request.
 * 
 * @author Lucas Nobile
 * @param <T>
 */
public abstract class RequestActivity<T> extends FragmentActivity implements
		Response.Listener<T>, Response.ErrorListener {

	public static final String TAG = RequestActivity.class.getSimpleName();

	// Variable needed to control a poll update just in the time the app is
	// closing.
	public boolean isRunning = true;

	/**
	 * Loading progress dialog.
	 */
	protected ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onCreateImpl(savedInstanceState);
	}

	/**
	 * Creates the child activity view.
	 * 
	 * @param savedInstanceState
	 */
	protected abstract void onCreateImpl(Bundle savedInstanceState);

	/**
	 * Creates a new request.
	 * 
	 * @return the request
	 */
	protected abstract Request<T> createRequest();

	public void performRequest() {
		// IMPORTANT: Check if there is network connection
		if (Util.isConnected(this) && isRunning) {
			mProgressDialog = ProgressDialog.show(this,
					getString(R.string.progress_dialog_title),
					getString(R.string.progress_dialog_message));

			Request<T> request = createRequest();

			StorageApp.REQUEST_MANAGER.addToRequestQueue(request,
					StorageApp.TAG);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		StorageApp.REQUEST_MANAGER.cancelPendingRequests(StorageApp.TAG);
		isRunning = false;
	}
}
