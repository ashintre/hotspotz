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
	private static final String HOTSPOTZ_GET_TAG_LOCATIONS = "http://hot-spotz.appspot.com/getTagLocations.do";
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
		// TODO: Get the contactsList from the intent
		List<GeoPoint> friendLocations = determineFriendLocations();
		String tag = intent.getStringExtra("tag");
		List<GeoPoint> tagLocations = determineTagLocations(tag);
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
			String response = "";
			while(responseScanner.hasNext())
			{
				response += responseScanner.next();
			}	
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
	
	private List<GeoPoint> determineTagLocations(String tag)
	{
		Scanner responseScanner = null;
		List<GeoPoint> tagLocations = new ArrayList<GeoPoint>();
		try
		{
			URL getTagLocationsServlet = new URL(HOTSPOTZ_GET_TAG_LOCATIONS + "?tag=" + URLEncoder.encode(tag, "UTF-8"));
			responseScanner = new Scanner(getTagLocationsServlet.openStream());
			String response = "";
			while(responseScanner.hasNext())
			{
				response += responseScanner.next();
			}			 
			Log.i(TAG, "Tag Locations >>>>> " + response);
			for(String tag_Locations : response.split(USER_DELIMITER))
			{
				String tagLocationDetails [] = tag_Locations.split(USER_DETAIL_DELIMITER);
				int latitudeE6 = Integer.parseInt(tagLocationDetails[2].replace(".", "")); // Remove the decimal point to convert to micro degrees is the current assumption.
				int longitudeE6 = Integer.parseInt(tagLocationDetails[3].replace(".", ""));
				GeoPoint geoPoint = new GeoPoint(latitudeE6, longitudeE6);
				tagLocations.add(geoPoint);
			}
		}
		catch(IOException mue)
		{
			mue.printStackTrace();
		}
		return tagLocations;
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

}
