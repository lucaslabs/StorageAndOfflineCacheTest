package com.lmn.storage.provider;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.lmn.storage.db.FeedDatabaseHelper;
import com.lmn.storage.db.FeedTable;

/**
 * Custom {@link ContentProvider} that encapsulates the data, and provides
 * mechanisms for defining data security.
 * 
 * @author Lucas Nobile.
 */
public class FeedContentProvider extends ContentProvider {

	private FeedDatabaseHelper databaseHelper;

	// Used for the UriMatcher
	private static final int FEEDS = 10;
	private static final int FEED_ID = 20;

	private static final String AUTHORITY = "com.lmn.storage.provider";

	private static final String BASE_PATH = "feeds";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/feeds";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/feed";

	/**
	 * {@link UriMatcher} to determine what is requested to this
	 * {@link ContentProvider}.
	 */
	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, FEEDS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", FEED_ID);
	}

	@Override
	public boolean onCreate() {
		databaseHelper = new FeedDatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(FeedTable.TABLE_FEED);

		int uriType = sURIMatcher.match(uri);

		switch (uriType) {
		case FEEDS:
			break;
		case FEED_ID:
			// adding the ID to the original query
			queryBuilder.appendWhere(FeedTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);

		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);

		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		long id = 0;

		switch (uriType) {
		case FEEDS:
			id = db.insert(FeedTable.TABLE_FEED, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		// Notify that the content has been modified
		getContext().getContentResolver().notifyChange(uri, null);

		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int uriType = sURIMatcher.match(uri);

		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		int rowsUpdated = 0;

		switch (uriType) {
		case FEEDS:
			rowsUpdated = db.update(FeedTable.TABLE_FEED, values, selection,
					selectionArgs);
			break;
		case FEED_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = db.update(FeedTable.TABLE_FEED, values,
						FeedTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = db.update(FeedTable.TABLE_FEED, values,
						FeedTable.COLUMN_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		// Notify that the content has been modified
		getContext().getContentResolver().notifyChange(uri, null);

		return rowsUpdated;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);

		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		int rowsDeleted = 0;

		switch (uriType) {
		case FEEDS:
			rowsDeleted = db.delete(FeedTable.TABLE_FEED, selection,
					selectionArgs);
			break;
		case FEED_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = db.delete(FeedTable.TABLE_FEED,
						FeedTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsDeleted = db.delete(FeedTable.TABLE_FEED,
						FeedTable.COLUMN_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		// Notify that the content has been modified
		getContext().getContentResolver().notifyChange(uri, null);

		return rowsDeleted;
	}

	/**
	 * Validate that a query only requests valid columns.
	 * 
	 * @param projection
	 */
	private void checkColumns(String[] projection) {
		String[] available = { FeedTable.COLUMN_ID, FeedTable.COLUMN_TITLE,
				FeedTable.COLUMN_IMAGE_URL };
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));
			// check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection.");
			}
		}
	}
}
