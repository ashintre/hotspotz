package com.buzzters.hotspotz.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class LocationUpdaterService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		// Its an internal service that should ideally not allow a Bind from external applications.		
		return null;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Toast.makeText(this, "HotspotZ boot service created", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Toast.makeText(this, "HotspotZ boot service destroyed", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onStart(Intent intent, int startId) 
	{
		super.onStart(intent, startId);
		Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
	}
}
