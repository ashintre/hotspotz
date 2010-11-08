package com.buzzters.hotspotz.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.buzzters.hotspotz.ui.R;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class MeetingLocatorService extends Service {

	private static final String HOTSPOTZ_GET_CONTACT_LCNS_URL = "http://hot-spotz.appspot.com/getContactLocations.do";
	private static final String HOTSPOTZ_GET_TAG_LOCATIONS = "http://hot-spotz.appspot.com/getTagLocations.do";
	private static final String TAG = "hotspotz";
	private static final String USER_DELIMITER = ";";
	private static final String USER_DETAIL_DELIMITER = ":";

	private static Map<GeoPoint, String> locationTagDetailsMap = new HashMap<GeoPoint, String>();
	private static ArrayList<String> contactsList = new ArrayList<String>();

	// TODO: Contacts List is not getting updated. Fix it.
	static {
		contactsList.add("the77thsecret@gmail.com");
		contactsList.add("adhishbhobe@gmail.com");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO: Get the contactsList from the intent
		List<GeoPoint> friendLocations = determineFriendLocations();
		String tag = intent.getStringExtra("tag");
		List<GeoPoint> tagLocations = determineTagLocations(tag);
		
		GeoPoint bestLocationToMeet = computePlace(friendLocations, tagLocations).get(0);
		String bestRouteMap = getBestRouteForUser(friendLocations.get(0).getLatitudeE6()/1E6, friendLocations.get(0).getLongitudeE6()/1E6,
									bestLocationToMeet.getLatitudeE6()/1E6, bestLocationToMeet.getLongitudeE6()/1E6);
		
		Intent resultsDisplayIntent = new Intent();
		resultsDisplayIntent.setAction("com.buzzters.hotspotz.ui.HotSpotzMapUI");
		resultsDisplayIntent.putExtra("nameOfEvent", intent.getStringExtra("nameOfEvent"));
		resultsDisplayIntent.putExtra("nextBusURL", bestRouteMap);
		resultsDisplayIntent.putExtra("destinationLatitude", bestLocationToMeet.getLatitudeE6()/1E6);
		resultsDisplayIntent.putExtra("destinationLongitude", bestLocationToMeet.getLongitudeE6()/1E6);
		resultsDisplayIntent.putExtra("bestPlaceToMeet", 
				locationTagDetailsMap.get(bestLocationToMeet).split(USER_DETAIL_DELIMITER)[1]);
		this.startActivity(resultsDisplayIntent);
	}

	private List<GeoPoint> determineFriendLocations() {
		Scanner responseScanner = null;
		List<GeoPoint> friendLocations = new ArrayList<GeoPoint>();
		Log.i(TAG, "In determineFriendLocations");
		try {
			StringBuilder urlBuilder = new StringBuilder(
					HOTSPOTZ_GET_CONTACT_LCNS_URL + "?emailIds=");
			for (String contact : contactsList) {
				urlBuilder.append(URLEncoder.encode(contact, "UTF-8")).append(
						",");
			}
			URL getContactLocationsURL = new URL(urlBuilder.toString());
			responseScanner = new Scanner(getContactLocationsURL.openStream());
			String response = "";
			while (responseScanner.hasNext()) {
				response += responseScanner.next();
			}
			Log.i(TAG, "Friend locations >>> " + response);
			for (String userDetails : response.split(USER_DELIMITER)) {
				String userLocation[] = userDetails
						.split(USER_DETAIL_DELIMITER);
				int latitudeE6 = Integer.parseInt(userLocation[2].replace(".",
						"")); // Remove the decimal point to convert to micro
								// degrees is the current assumption.
				int longitudeE6 = Integer.parseInt(userLocation[3].replace(".",
						""));
				GeoPoint geoPoint = new GeoPoint(latitudeE6, longitudeE6);
				friendLocations.add(geoPoint);
			}
		} catch (IOException mue) {
			mue.printStackTrace();
		}
		return friendLocations;
	}

	private List<GeoPoint> determineTagLocations(String tag) {
		Scanner responseScanner = null;
		locationTagDetailsMap.clear();
		List<GeoPoint> tagLocations = new ArrayList<GeoPoint>();
		try {
			URL getTagLocationsServlet = new URL(HOTSPOTZ_GET_TAG_LOCATIONS
					+ "?tag=" + URLEncoder.encode(tag, "UTF-8"));
			responseScanner = new Scanner(getTagLocationsServlet.openStream());
			String response = "";
			while (responseScanner.hasNext()) {
				response += responseScanner.next();
			}
			Log.i(TAG, "Tag Locations >>>>> " + response);
			for (String tag_Locations : response.split(USER_DELIMITER)) {
				String tagLocationDetails[] = tag_Locations
						.split(USER_DETAIL_DELIMITER);
				int latitudeE6 = Integer.parseInt(tagLocationDetails[2]
						.replace(".", "")); // Remove the decimal point to
											// convert to micro degrees is the
											// current assumption.
				int longitudeE6 = Integer.parseInt(tagLocationDetails[3]
						.replace(".", ""));
				GeoPoint geoPoint = new GeoPoint(latitudeE6, longitudeE6);
				tagLocations.add(geoPoint);
				locationTagDetailsMap.put(geoPoint, tag_Locations);
			}
		} catch (IOException mue) {
			mue.printStackTrace();
		}
		return tagLocations;
	}

	// Compute the three best places to meet.
	public List<GeoPoint> computePlace(List<GeoPoint> friendLocations,
			List<GeoPoint> placeLocations) {
		int l = 0;
		int i = friendLocations.size();
		int k = placeLocations.size();
		float aggregate[] = new float[k];
		float stddev[] = new float[k];
		float mean[] = new float[k];
		// float sum[]=new float[i];
		float distance[] = new float[i];
		ArrayList<GeoPoint> gp = new ArrayList<GeoPoint>();
		TreeMap<Float, GeoPoint> tm = new TreeMap<Float, GeoPoint>();
		ListIterator<GeoPoint> itr1 = placeLocations.listIterator();
		while (itr1.hasNext()) {
			int j = 0;
			float sum = 0;
			GeoPoint location1 = itr1.next();
			ListIterator<GeoPoint> itr2 = friendLocations.listIterator();
			while (itr2.hasNext()) {
				GeoPoint location2 = itr2.next();
				Location.distanceBetween((double) location1.getLatitudeE6(),
						(double) location1.getLongitudeE6(), (double) location2
								.getLatitudeE6(), (double) location2
								.getLongitudeE6(), distance);
				sum = sum + distance[j];
				++j;
			}
			aggregate[l] = sum;
			mean[l] = aggregate[l] / i;
			stddev[l] = standardDeviation(distance, mean[l], i);
			tm.put(stddev[l], location1);
			++l;
		}
		Arrays.sort(stddev);
		gp.add(tm.get(stddev[0]));
		
		return gp;

	}

	public float standardDeviation(float[] distance, float mean, int i) {
		float sum = 0;
		for (int j = 0; j < i; j++) {
			sum = sum + (distance[j] - mean) * (distance[j] - mean);
		}
		float result = (float) Math.sqrt(sum);
		return result;

	}

	// Compute the best route to use for this user.
	public String getTagValue(String Tag, Element element) {

		NodeList nodeList = element.getElementsByTagName(Tag).item(0)
				.getChildNodes();
		Node vNode = (Node) nodeList.item(0);

		return vNode.getNodeValue();
	}

	public String[] findNearestStop(double latitude, double longitude, int du,
			String chosenRoute) {

		NodeList nodes = getStops();
		double stop_latitude, stop_longitude;
		float distance[] = new float[100];
		float min = Float.MAX_VALUE;
		String name_code_route_ll[] = new String[5];

		for (int i = 0; i < nodes.getLength(); i++) {
			Node stopnode = nodes.item(i);

			if (stopnode.getNodeType() == Node.ELEMENT_NODE) {

				Element element = (Element) stopnode;

				if ((du == 0)
						|| ((du == 1) && (getTagValue("routes", element)
								.contains(chosenRoute)))) {

					stop_latitude = Double.parseDouble(getTagValue("latitude",
							element));
					stop_longitude = Double.parseDouble(getTagValue(
							"longitude", element));

					Location.distanceBetween(latitude, longitude,
							stop_latitude, stop_longitude, distance);
					if (distance[i] < min) {
						min = distance[i];
						name_code_route_ll[0] = getTagValue("stop_name",
								element);
						name_code_route_ll[1] = getTagValue("shortcode",
								element);
						name_code_route_ll[2] = getTagValue("routes", element);
						name_code_route_ll[3] = getTagValue("latitude", element);
						name_code_route_ll[4] = getTagValue("longitude",
								element);
					}
				}
			}
		}

		return name_code_route_ll;
	}

	public String getBestRouteForUser(double latitude_user, double longitude_user,
			double latitude_destination, double longitude_destination) {
		String ncrll[], chosenRoute, userNcrll[][] = new String[4][];
		int len, i = 0, finalRouteMarker = 0;
		float min = Float.MAX_VALUE;
		float distance[] = new float[4];
		char finalLine = 'T';		
		String url = "";

		ncrll = findNearestStop(latitude_user, longitude_user, 0, "");
		len = ncrll[2].length();

		if (len > 1) {
			for (i = 0; i < len - 1; i++) {
				chosenRoute = ncrll[2].substring(i, i + 1);
				userNcrll[i] = findNearestStop(latitude_destination,
						longitude_destination, 1, chosenRoute);

				Location.distanceBetween(latitude_user, longitude_user, Double
						.parseDouble(userNcrll[i][3]), Double
						.parseDouble(userNcrll[i][4]), distance);
				if (distance[i] < min) {
					min = distance[i];
					finalRouteMarker = i;
					finalLine = chosenRoute.charAt(0);
				}
			}
		} else {
			userNcrll[0] = findNearestStop(latitude_destination,
					longitude_destination, 1, ncrll[2]);
			finalLine = ncrll[2].charAt(0);
		}
		// Log.i("hotspotz","Chosen Line is from userNcrll[finalRouteMarker][0] ");

		url = "http://www.nextbus.com/predictor/simplePrediction.shtml?a=georgia-tech";

		if (finalLine == 'G')
			url = url + "&r=green";
		else if (finalLine == 'R')
			url = url + "&r=red";
		else if (finalLine == 'B')
			url = url + "&r=blue";
		else
			url = url + "&r=trolley";

		url = url + "&d=" + ncrll[1];
		url = url + "&s=" + userNcrll[finalRouteMarker][1];
		
		return url;
	}

	public NodeList getStops() {
		try {
			//File file = new File("StopsList.xml");
			InputStream ins = getResources().openRawResource(R.xml.stops_list);
			DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = docbf.newDocumentBuilder();
			Document doc = db.parse(ins);
			NodeList nodes = doc.getElementsByTagName("stop");
			return nodes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
		
}
