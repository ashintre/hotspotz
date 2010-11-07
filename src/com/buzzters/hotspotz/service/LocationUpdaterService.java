package com.buzzters.hotspotz.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LocationUpdaterService extends Service{

	private LocationManager locationManager = null;
	private static final String TAG = "hotspotz";
	private static final String HOTSPOTZ_LOC_UPDATE_SERVLET = "http://hot-spotz.appspot.com/updateLoc.do";	
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
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Log.i(TAG, "Testing hotspotz tag");
		LocationListener locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {	
				Log.i(TAG, "Latitude : " + location.getLatitude());
				Log.i(TAG, "Longititude : " + location.getLongitude());
				Log.i(TAG, "Provider : " + location.getProvider());
				Log.i(TAG, "Accuracy : " + location.getAccuracy());
				updateServerTables(location);
			}
			@Override
			public void onProviderDisabled(String msg) {			
				Log.i(TAG, "Provider has been disabled");
			}
			@Override
			public void onProviderEnabled(String msg) {
				Log.i("hotspotz", "Provider has been enabled");
			}
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				Log.i("hotspotz", "Status changed");
			}			
		};
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);			
	}
	
	private void updateServerTables(Location location)
	{
		Log.i(TAG, "Updating server tables");
		try 
		{			
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(HOTSPOTZ_LOC_UPDATE_SERVLET);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("latitude", String.valueOf(location.getLatitude())));
			params.add(new BasicNameValuePair("longitude", String.valueOf(location.getLongitude())));
			params.add(new BasicNameValuePair("userEmail", "adhishbhobe@gmail.com"));
			params.add(new BasicNameValuePair("userGroup", "default"));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			httpPost.setEntity(entity);
			httpClient.execute(httpPost);
		}
		catch (MalformedURLException mue) 
		{			
			Log.e(TAG, "MalformedURLException while trying to update server");
			mue.printStackTrace();
		}
		catch (IOException ioe)
		{
			Log.e(TAG, "IOException while trying to update server");
			ioe.printStackTrace();
		}
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
		// Get reference to the System location Manager. Screw you googleLatitude
					
	}
	
	
}
