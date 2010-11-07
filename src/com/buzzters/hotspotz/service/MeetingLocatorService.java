package com.buzzters.hotspotz.service;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class MeetingLocatorService extends Service{

	private static final String HOTSPOTZ_GET_CONTACT_LCNS_URL = "http://hot-spotz.appspot.com/getContactLocations.do";
	private static final String TAG = "hotspotz";
	private static final String USER_DELIMITER = ";";
	private static final String USER_DETAIL_DELIMITER = ":";
	
	private static ArrayList<String> contactsList = new ArrayList<String>();
	
	//TODO: Contacts List is not getting updated. Fix it.
	static
	{
		contactsList.add("the77thsecret@gmail.com");
		contactsList.add("adhishbhobe@gmail.com");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		List<GeoPoint> friendLocations = determineFriendLocations();
	}
	
	private List<GeoPoint> determineFriendLocations()
	{
		Scanner responseScanner = null;
		List<GeoPoint> friendLocations = new ArrayList<GeoPoint>();
		Log.i(TAG, "In determineFriendLocations");
		try
		{
			StringBuilder urlBuilder = new StringBuilder(HOTSPOTZ_GET_CONTACT_LCNS_URL + "?emailIds=");
			for(String contact : contactsList)
			{
				urlBuilder.append(URLEncoder.encode(contact, "UTF-8")).append(",");
			}
			URL getContactLocationsURL = new URL(urlBuilder.toString());
			responseScanner = new Scanner(getContactLocationsURL.openStream());
			String response = responseScanner.next();
			Log.i(TAG, "Friend locations >>> " + response);
			for(String userDetails : response.split(USER_DELIMITER))
			{
				String userLocation [] = userDetails.split(USER_DETAIL_DELIMITER);
				int latitudeE6 = Integer.parseInt(userLocation[2].replace(".", "")); // Remove the decimal point to convert to micro degrees is the current assumption.
				int longitudeE6 = Integer.parseInt(userLocation[3].replace(".", ""));
				GeoPoint geoPoint = new GeoPoint(latitudeE6, longitudeE6);
				friendLocations.add(geoPoint);
			}
		}
		catch(IOException mue)
		{
			mue.printStackTrace();
		}
		return friendLocations;
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

}
