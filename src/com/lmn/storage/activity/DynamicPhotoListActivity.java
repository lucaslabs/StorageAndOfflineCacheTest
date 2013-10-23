package com.lmn.storage.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.lmn.storage.R;
import com.lmn.storage.adapter.FeedAdapter;
import com.lmn.storage.db.FeedTable;
import com.lmn.storage.model.picasa.PicasaResponse;
import com.lmn.storage.provider.FeedContentProvider;
import com.lmn.storage.receiver.MyAlarmReceiver;
import com.lmn.storage.receiver.MyAlarmReceiver.OnScheduleUpdateListener;
import com.lmn.storage.util.Constants;
import com.lmn.storage.util.request.GsonRequest;

/**
 * Executes a request to get photos from Picasa dynamically.
 * <p>
 * Implements the "Endless" List UI design pattern :). See
 * {@link EndlessScrollListener}.
 * <p>
 * Implements Polling pattern. This is NOT a best practice.
 * 
 * 
 * @author Lucas Nobile
 */
public class DynamicPhotoListActivity extends RequestActivity<PicasaResponse>
		implements OnScheduleUpdateListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = DynamicPhotoListActivity.class
			.getSimpleName();
	private static final int RESULT_PER_PAGE = 20;
	private ListView mListView;
	private FeedAdapter mAdapter;
	private boolean mHasData = false;
	private boolean mErrorOcurred = false;

	// The loader's unique id. Loader ids are specific to the Activity or
	// Fragment in which they reside.
	private static final int LOADER_ID = 1;

	private BroadcastReceiver mAlarmReceiver;
	private boolean isRequestPending;
	private boolean needToScroll;

	public static final String ACTION_UPDATE_DATA = "com.lmn.storage.action.UPDATE_DATA";

	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		setContentView(R.layout.activity_dynamic_list);

		mListView = (ListView) findViewById(R.id.lstFeed);

		// Set List empty view
		mListView.setEmptyView(findViewById(android.R.id.empty));

		// Scroll listener
		mListView.setOnScrollListener(new EndlessScrollListener());

		retrieveLocalFeeds();
	}

	/**
	 * Gets Feeds that are stored locally.
	 */
	private void retrieveLocalFeeds() {

		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] { FeedTable.COLUMN_TITLE,
				FeedTable.COLUMN_IMAGE_URL };
		// Fields on the UI to which we map
		int[] to = new int[] { R.id.tvTitle };

		// Initialize the adapter. Note that we pass a 'null' Cursor as the
		// third argument. We will pass the adapter a Cursor only when the
		// data has finished loading for the first time (i.e. when the
		// LoaderManager delivers the data to onLoadFinished). Also note
		// that we have passed the '0' flag as the last argument. This
		// prevents the adapter from registering a ContentObserver for the
		// Cursor (the CursorLoader will do this for us!).
		mAdapter = new FeedAdapter(this, R.layout.row_dynamic_list, null, from,
				to, 0);

		// Associate the (now empty) adapter with the ListView.
		mListView.setAdapter(mAdapter);

		// Initialize the Loader with LOADER_ID and callbacks this.
		// If the loader doesn't already exist, one is created. Otherwise,
		// the already created Loader is reused. In either case, the
		// LoaderManager will manage the Loader across the Activity/Fragment
		// lifecycle, will receive any new loads once they have completed,
		// and will report this new data back to the 'mCallbacks' object.
		getSupportLoaderManager().initLoader(LOADER_ID, null, this);

	}

	@Override
	public void onResume() {
		super.onResume();
		
		isRunning = true;

		if (!mHasData && !mErrorOcurred) {

			// Execute the request
			// if (!isRequestPending) {
			// performRequest();
			// isRequestPending = true;
			// }
		}

		// Register the receiver
		mAlarmReceiver = new MyAlarmReceiver(this);
		IntentFilter intentFilter = new IntentFilter(ACTION_UPDATE_DATA);
		this.registerReceiver(mAlarmReceiver, intentFilter);

		// After receiver registering, we can schedule the update.
		scheduleAlarmReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();
		isRunning = false;
		this.unregisterReceiver(mAlarmReceiver);

	}

	// Schedule AlarmManager to invoke MyAlarmReceiver and cancel any existing
	// current PendingIntent.
	private void scheduleAlarmReceiver() {
		AlarmManager alarmMgr = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(DynamicPhotoListActivity.ACTION_UPDATE_DATA);

		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0); // PendingIntent.FLAG_CANCEL_CURRENT

		// Use inexact repeating which is easier on battery (system can phase
		// events and not wake at exact times)
		alarmMgr.setInexactRepeating(AlarmManager.RTC,
				Constants.ScheduleUpdate.TRIGGER_AT_TIME, // first poll
				Constants.ScheduleUpdate.INTERVAL, pi);
	}

	@Override
	public void onScheduleUpdate() {
		if (!isRequestPending) {
			performRequest();
			isRequestPending = true;
			needToScroll = true;
		}
	}

	@Override
	protected Request<PicasaResponse> createRequest() {
		String photoOf = "android"; // search criteria
		int thumbSize = 160;
		int startIndex = 1 + mAdapter.getCount();

		// TODO Use Uri.Builder
		String url = "https://picasaweb.google.com/data/feed/api/all?q="
				+ photoOf + "&thumbsize=" + thumbSize + "&max-results="
				+ RESULT_PER_PAGE + "&start-index=" + startIndex + "&alt=json";

		GsonRequest<PicasaResponse> request = new GsonRequest<PicasaResponse>(
				Request.Method.GET, url, PicasaResponse.class, this, this);

		return request;
	}

	@Override
	public void onResponse(PicasaResponse response) {
		Log.d(TAG, "GET success!: " + response.toString());

		mProgressDialog.dismiss();

		isRequestPending = false;

		// Add feeds to the adapter
		mAdapter.addAll(response);

		if (needToScroll) {
			int position = mAdapter.getCount() - RESULT_PER_PAGE;
			mListView.setSelection(position);
			needToScroll = false;
		}		
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		Log.d(TAG, "GET error: " + error.getMessage());
		mProgressDialog.dismiss();
		mErrorOcurred = true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Create a new CursorLoader with the following query parameters.
		String[] projection = { FeedTable.COLUMN_ID, FeedTable.COLUMN_TITLE,
				FeedTable.COLUMN_IMAGE_URL };
		return new CursorLoader(this, FeedContentProvider.CONTENT_URI,
				projection, null, null, null);

	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// A switch-case is useful when dealing with multiple Loaders/IDs
		switch (loader.getId()) {
		case LOADER_ID:
			// The asynchronous load is complete and the data
			// is now available for use. Only now can we associate
			// the queried Cursor with the SimpleCursorAdapter.
			mAdapter.swapCursor(cursor);

			if (mAdapter.getCount() == 0) {
				performRequest();
			}
			break;
		}
		// The listview now displays the queried data.
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// For whatever reason, the Loader's data is now unavailable.
		// Remove any references to the old data by replacing it with
		// a null Cursor.
		mAdapter.swapCursor(null);
	}

	/**
	 * Detects when user is closer to the end of the current page and starts
	 * loading the next page so the user will not have to wait (that much) for
	 * the next entries.
	 */
	public class EndlessScrollListener implements AbsListView.OnScrollListener {
		// How many entries earlier to start loading next page
		private int visibleThreshold = 5;
		private int currentPage = 0;
		private int previousTotal = 0;
		private boolean loading = true;

		public EndlessScrollListener() {
		}

		public EndlessScrollListener(int visibleThreshold) {
			this.visibleThreshold = visibleThreshold;
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (loading) {
				if (totalItemCount > previousTotal) {
					loading = false;
					previousTotal = totalItemCount;
					currentPage++;
				}
			}
			if (!loading
					&& (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
				// I load the next page of gigs using a background task,
				// but you can call any function here.
				if (!isRequestPending) {
					loading = true;
					performRequest();
					isRequestPending = true;
				}

			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		public int getCurrentPage() {
			return currentPage;
		}
	}
}