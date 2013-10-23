package com.lmn.storage.util;

import android.app.AlarmManager;

/**
 * Set of useful constants.
 * 
 * @author Lucas Nobile
 */
public class Constants {

	/**
	 * Get data and picture of Peperino Tester on Facebook.
	 */
	public interface Facebook {
		public static final String URL = "http://graph.facebook.com/";
		public static final String USER_PROFILE = URL + "peperino.tester";
		public static final String USER_PICTURE = USER_PROFILE + "/picture";
	}

	/**
	 * Test GET, POST, PUT and DELETE request.
	 */
	public interface EchoServer {
		public static final String URL = "http://httpbin.org/";
		public static final String GET = URL + "get"; // Returns GET data.
		public static final String POST = URL + "post"; // Returns POST data.
		public static final String PUT = URL + "put"; // Returns PUT data.
		public static final String DELETE = URL + "delete"; // Returns DELETE
															// data
	}

	/**
	 * Fields of the profile response in JSON format.
	 */
	public interface ProfileField {
		public static final String ID = "id";
		public static final String NAME = "name";
		public static final String FIRST_NAME = "first_name";
		public static final String LAST_NAME = "last_name";
		public static final String USERNAME = "username";
		public static final String GENDER = "gender";
		public static final String DOMAIN = "domain";
	}

	/**
	 * Constants values to schedule an update.
	 */
	public interface ScheduleUpdate {
		public static final long INTERVAL = 30000L; // AlarmManager.INTERVAL_FIFTEEN_MINUTES;
		public static final long TRIGGER_AT_TIME = System.currentTimeMillis()
				+ INTERVAL;
	}
}
