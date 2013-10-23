package com.lmn.storage.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Table of Feeds.
 * <p>
 * IMPORTANT: Have one separate class per table as best practice. This way we
 * are prepared in case our database schema grows.
 * 
 * @author Lucas Nobile
 */
public class FeedTable {

	public static final String TABLE_FEED = "feed";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_IMAGE_URL = "image_url";

	private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_FEED
			+ "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_TITLE + " TEXT NOT NULL, " + COLUMN_IMAGE_URL
			+ " TEXT NOT NULL);";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.d(FeedTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy the old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_FEED);
		onCreate(database);
	}
}