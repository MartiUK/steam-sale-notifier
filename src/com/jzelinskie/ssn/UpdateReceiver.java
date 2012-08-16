package com.jzelinskie.ssn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateReceiver extends BroadcastReceiver {
	
	private static final String TAG = "UpdateReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Requesting update service...");
		Intent service = new Intent(context, UpdateService.class);
		context.startService(service);
	}

}
