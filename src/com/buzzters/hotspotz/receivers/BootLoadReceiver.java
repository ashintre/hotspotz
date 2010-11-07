package com.buzzters.hotspotz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootLoadReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {		
		Log.i("hotspotz", "In broadcastReceiver for boot Load");
		Intent locationUpdaterServiceIntent = new Intent();
		locationUpdaterServiceIntent.setAction("com.buzzters.hotspotz.service.LocationUpdaterService");
		// Call the service that matches this intent
		context.startService(locationUpdaterServiceIntent);
	}

}
