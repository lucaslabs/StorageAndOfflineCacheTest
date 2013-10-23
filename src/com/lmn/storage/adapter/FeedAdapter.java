package com.lmn.storage.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.lmn.storage.R;
import com.lmn.storage.StorageApp;
import com.lmn.storage.db.FeedTable;
import com.lmn.storage.model.picasa.Entry;
import com.lmn.storage.model.picasa.PicasaPhoto;
import com.lmn.storage.model.picasa.PicasaResponse;
import com.lmn.storage.provider.FeedContentProvider;

/**
 * Custom {@link SimpleCursorAdapter} to show feed.
 * <p>
 * Use Image Cache to save and load images from disk.
 * 
 * @author Lucas Nobile
 */
public class FeedAdapter extends SimpleCursorAdapter {

	private Context mContext;
	private int mLayout;

	public FeedAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		mContext = context;
		mLayout = layout;
	}

	// IMPORTANT: And the ViewHolder pattern???
	// CursorAdapter won't call the newView each time it needs a new row; if it
	// already has a View, it will call the bindView directly, so the created
	// view is actually reused. Thus, there's no need to use a ViewHolder in
	// this case.

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// When the view will be created for first time,
		// we need to tell the adapters, how each item will look
		View view = LayoutInflater.from(context)
				.inflate(mLayout, parent, false);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// here we are setting our data
		// that means, take the data from the cursor and put it in views
		String title = cursor.getString(cursor
				.getColumnIndex(FeedTable.COLUMN_TITLE));

		TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
		tvTitle.setText(title);

		String imageUrl = cursor.getString(cursor
				.getColumnIndex(FeedTable.COLUMN_IMAGE_URL));

		final NetworkImageView ivThumbnail = (NetworkImageView) view
				.findViewById(R.id.ivThumbnail);

		// Use of NetworkImageView
		if (imageUrl.length() > 0) {
			// IMPORTANT: Use Disk Image Cache
			ivThumbnail.setImageUrl(imageUrl,
					StorageApp.IMAGE_CACHE_MANAGER.getImageLoader());
		} else {
			// display placeholder or whatever you want
			ivThumbnail.setImageResource(R.drawable.no_image);
		}
	}

	public void addAll(PicasaResponse response) {
		if (response != null) {
			for (Entry entry : response.getFeed().getEntries()) {
				String title = entry.getMediaGroup().getMediaGroupCredits()
						.get(0).getTitle();
				String thumbnailUrl = entry.getMediaGroup()
						.getMediaGroupContents().get(0).getUrl();
				PicasaPhoto feed = new PicasaPhoto(title, thumbnailUrl);
				addFeed(feed);
			}
		}
	}

	public void addFeed(PicasaPhoto feed) {
		ContentValues values = new ContentValues();
		values.put(FeedTable.COLUMN_TITLE, feed.getTitle());
		values.put(FeedTable.COLUMN_IMAGE_URL, feed.getThumbnailUrl());

		// When you want to access data in a content provider, you use the
		// ContentResolver object in your application's Context to communicate
		// with the provider as a client. The ContentResolver object
		// communicates with the provider object, an instance of a class that
		// implements ContentProvider. The provider object receives data
		// requests from clients, performs the requested action, and returns the
		// results.
		Uri inserted = mContext.getContentResolver().insert(
				FeedContentProvider.CONTENT_URI, values);
	}
}
