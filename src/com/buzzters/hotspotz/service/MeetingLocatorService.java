package com.buzzters.hotspotz.service;

import java.io.ByteArrayInputStream;
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
		if(intent.getStringArrayListExtra("emailIds").size() > 1)
		{
			// We correctly obtained all contacts list
			contactsList = intent.getStringArrayListExtra("emailIds");
		}		
		List<GeoPoint> friendLocations = determineFriendLocations();
		String tag = intent.getStringExtra("tag");
		List<GeoPoint> tagLocations = determineTagLocations(tag);		
		
		GeoPoint bestLocationToMeet = computePlace(friendLocations, tagLocations).get(0);
		String bestRouteMap = getBestRouteForUser(friendLocations.get(0).getLatitudeE6()/1E6, friendLocations.get(0).getLongitudeE6()/1E6,
									bestLocationToMeet.getLatitudeE6()/1E6, bestLocationToMeet.getLongitudeE6()/1E6);
		
		Intent resultsDisplayIntent = new Intent(getApplication(), com.buzzters.hotspotz.ui.HotSpotzMapUI.class);
		resultsDisplayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);		
		resultsDisplayIntent.putExtra("nameOfEvent", intent.getStringExtra("nameOfEvent"));
		resultsDisplayIntent.putExtra("nextBusURL", bestRouteMap);
		resultsDisplayIntent.putExtra("destinationLatitude", Double.valueOf(bestLocationToMeet.getLatitudeE6()/1E6));
		resultsDisplayIntent.putExtra("destinationLongitude",Double.valueOf(bestLocationToMeet.getLongitudeE6()/1E6));
		resultsDisplayIntent.putExtra("bestPlaceToMeet", 
				locationTagDetailsMap.get(bestLocationToMeet).split(USER_DETAIL_DELIMITER)[1]);
		getApplication().startActivity(resultsDisplayIntent);
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
				int latitudeE6 = (int)(Double.parseDouble(userLocation[2]) * 1E6);
				int longitudeE6 = (int)(Double.parseDouble(userLocation[3]) * 1E6);
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
				int latitudeE6 = (int)(Double.parseDouble(tagLocationDetails[2]) * 1E6);
				int longitudeE6 = (int)(Double.parseDouble(tagLocationDetails[3]) * 1E6);
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
			InputStream is = new ByteArrayInputStream(stopsList.getBytes("UTF-8"));
			DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = docbf.newDocumentBuilder();
			Document doc = db.parse(is);
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
	private String stopsList = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><stops><stop id=\"1\"><stop_name>Fitten Hall</stop_name><latitude>33.77804</latitude><longitude>-84.40341</longitude><shortcode>fitten</shortcode><routes>RB</routes></stop><stop id=\"2\"><stop_name>McMillan And 8th Street</stop_name><latitude>33.7795</latitude><longitude>-84.40403</longitude><shortcode>mcm8th</shortcode><routes>RB</routes></stop><stop id=\"3\"><stop_name>8th Street And Hemphill Ave</stop_name><latitude>33.77961</latitude><longitude>-84.40247</longitude><shortcode>8thhemp</shortcode><routes>RB</routes></stop><stop id=\"4\"><stop_name>Ferst Dr And Hemphill Ave</stop_name><latitude>33.77827</latitude><longitude>-84.40116</longitude><shortcode>fershemrt</shortcode><routes>RGBT</routes></stop><stop id=\"5\"><stop_name>Ferst Dr And State Street</stop_name><latitude>33.77818</latitude><longitude>-84.39901</longitude><shortcode>fersstmrt</shortcode><routes>RBG</routes></stop><stop id=\"6\"><stop_name>Ferst Dr And Atlantic Drive</stop_name><latitude>33.77813</latitude><longitude>-84.39770</longitude><shortcode>fersatmrt</shortcode><routes>RBT</routes></stop><stop id=\"7\"><stop_name>Ferst Dr And Cherry Street</stop_name><latitude>33.77752</latitude><longitude>-84.39566</longitude><shortcode>ferschmrt</shortcode><routes>RBT</routes></stop><stop id=\"8\"><stop_name>Ferst Dr And Fowler Street</stop_name><latitude>33.77681</latitude><longitude>-84.39350</longitude><shortcode>5thfowl</shortcode><routes>RBT</routes></stop><stop id=\"9\"><stop_name>Techwood Dr And 5th Street</stop_name><latitude>33.77670</latitude><longitude>-84.39197</longitude><shortcode>tech5th</shortcode><routes>RBT</routes></stop><stop id=\"10\"><stop_name>Techwood Dr And 4th Street</stop_name><latitude>33.77522</latitude><longitude>-84.39197</longitude><shortcode>tech4th</shortcode><routes>RB</routes></stop><stop id=\"11\"><stop_name>Techwood Dr And Bobby Dodd Way</stop_name><latitude>33.77376</latitude><longitude>-84.39187</longitude><shortcode>techbob</shortcode><routes>RB</routes></stop><stop id=\"12\"><stop_name>Techwood Dr And North Ave</stop_name><latitude>33.77116</latitude><longitude>-84.39193</longitude><shortcode>technorth</shortcode><routes>RB</routes></stop><stop id=\"13\"><stop_name>North Avenue Apartments</stop_name><latitude>33.76994</latitude><longitude>-84.39101</longitude><shortcode>nortavea_a</shortcode><routes>RB</routes></stop><stop id=\"14\"><stop_name>Student Centre</stop_name><latitude>33.77383</latitude><longitude>-84.39856</longitude><shortcode>centrstud</shortcode><routes>RGB</routes></stop><stop id=\"15\"><stop_name>CRC</stop_name><latitude>33.77524</latitude><longitude>-84.40281</longitude><shortcode>765femrt</shortcode><routes>RGBT</routes></stop><stop id=\"16\"><stop_name>14th Bus Yard</stop_name><latitude>33.78650</latitude><longitude>-84.40536</longitude><shortcode>14thmcmi</shortcode><routes>G</routes></stop><stop id=\"17\"><stop_name>14th Street And State Street</stop_name><latitude>33.78590</latitude><longitude>-84.3979</longitude><shortcode>14thstat</shortcode><routes>G</routes></stop><stop id=\"18\"><stop_name>GLC</stop_name><latitude>33.78155</latitude><longitude>-84.39625</longitude><shortcode>glc</shortcode><routes>G</routes></stop><stop id=\"19\"><stop_name>Cherry Street And Ferst Drive</stop_name><latitude>33.77228</latitude><longitude>-84.39566</longitude><shortcode>cherferst</shortcode><routes>RGB</routes></stop><stop id=\"20\"><stop_name>NARA</stop_name><latitude>33.76993</latitude><longitude>-84.40281</longitude><shortcode>nara</shortcode><routes>G</routes></stop><stop id=\"21\"><stop_name>TEP</stop_name><latitude>33.76927</latitude><longitude>-84.4027</longitude><shortcode>tep_d</shortcode><routes>G</routes></stop><stop id=\"22\"><stop_name>10th Street And Hemphill Ave</stop_name><latitude>33.78600</latitude><longitude>-84.40397</longitude><shortcode>10thhemp</shortcode><routes>G</routes></stop><stop id=\"23\"><stop_name>Hemphill Ave And Currant Street</stop_name><latitude>33.78416</latitude><longitude>-84.40576</longitude><shortcode>hempcurr</shortcode><routes>G</routes></stop><stop id=\"24\"><stop_name>Technology Square</stop_name><latitude>33.77682</latitude><longitude>-84.38934</longitude><shortcode>techrec</shortcode><routes>T</routes></stop><stop id=\"25\"><stop_name>Publix Supermarket</stop_name><latitude>33.78058</latitude><longitude>-84.38873</longitude><shortcode>publix</shortcode><routes>T</routes></stop><stop id=\"26\"><stop_name>MARTA Midtown</stop_name><latitude>33.78068</latitude><longitude>-84.38663</longitude><shortcode>marta</shortcode><routes>T</routes></stop><stop id=\"27\"><stop_name>Academy of Medicine</stop_name><latitude>33.77888</latitude><longitude>-84.38709</longitude><shortcode>wpe7mrt</shortcode><routes>T</routes></stop><stop id=\"28\"><stop_name>North Deck</stop_name><latitude>33.77964</latitude><longitude>-84.39912</longitude><shortcode>bake</shortcode><routes>G</routes></stop></stops>";
}
