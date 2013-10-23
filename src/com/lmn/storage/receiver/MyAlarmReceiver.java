package com.lmn.storage.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lmn.storage.activity.DynamicPhotoListActivity;

/**
 * This {@link BroadcastReceiver} executes itself by interval times.
 * 
 * @author Lucas Nobile
 */
public class MyAlarmReceiver extends BroadcastReceiver {

	/**
	 * Lister to handle schedule update.
	 */
	public interface OnScheduleUpdateListener {
		public void onScheduleUpdate();
	}

	private OnScheduleUpdateListener mListener;

	public MyAlarmReceiver(OnScheduleUpdateListener listener) {
		mListener = listener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (DynamicPhotoListActivity.ACTION_UPDATE_DATA.equals(intent
				.getAction())) {
			mListener.onScheduleUpdate();
		}
	}
}