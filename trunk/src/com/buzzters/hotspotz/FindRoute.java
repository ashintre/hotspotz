package com.buzzters.hotspotz;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;

import com.google.android.maps.MapActivity;

public class FindRoute extends MapActivity {
				
	    public FindRoute() {
	    	
	    }
	      
	    @Override
		protected boolean isRouteDisplayed() {
		    return false;
		}
	    
	    public String getTagValue(String Tag,Element element) {
	    	
	    	NodeList nodeList= element.getElementsByTagName(Tag).item(0).getChildNodes();
	        Node vNode = (Node) nodeList.item(0); 
	     
	        return vNode.getNodeValue();  
	    }
		
	    public String[] findNearestStop(double latitude, double longitude, int du, String chosenRoute) {
	    
	    	NodeList nodes = getStops();
			double stop_latitude, stop_longitude;
			float distance[] = new float[100];
			float min = Float.MAX_VALUE;
			String name_code_route_ll[] = new String[5];
			
			for(int i=0; i < nodes.getLength(); i++) {
				Node stopnode = nodes.item(i);
				 
				if (stopnode.getNodeType() == Node.ELEMENT_NODE) {
					  
					  Element element = (Element) stopnode;
					  
					  if ( (du == 0) || ( (du == 1) && ( getTagValue("routes",element).contains(chosenRoute) ) ) ){
					  
							  stop_latitude = Double.parseDouble(getTagValue("latitude",element));
							  stop_longitude = Double.parseDouble(getTagValue("longitude",element));
			          
							  Location.distanceBetween(latitude, longitude, stop_latitude, stop_longitude, distance);
							  if(distance[i] < min) {
								  min = distance[i];
					        	  name_code_route_ll[0] = getTagValue("stop_name",element);
								  name_code_route_ll[1] = getTagValue("shortcode",element);
					        	  name_code_route_ll[2] = getTagValue("routes",element);
					           	  name_code_route_ll[3] = getTagValue("latitude",element);
					        	  name_code_route_ll[4] = getTagValue("longitude",element);
					          }
						  }
			    	  }
			}
		
		return name_code_route_ll;
    }
    
	    
		public void calculateRoute(double latitude_user, double longitude_user, double latitude_destination, double longitude_destination)  {
				String ncrll[], chosenRoute, userNcrll[][] = new String[4][];
				int len, i = 0, finalRouteMarker = 0;
				float min = Float.MAX_VALUE; 
				float distance[] = new float[4];
				char finalLine= 'T';
				Intent intent = new Intent("android.intent.action.VIEW");
				String url = "";
				
				ncrll = findNearestStop(latitude_user,longitude_user,0,"");	
				len = ncrll[2].length();
				
				if( len > 1) {
					for(i=0; i < len-1 ; i++ ) {
						chosenRoute = ncrll[2].substring(i, i+1);
						userNcrll[i] = findNearestStop(latitude_destination,longitude_destination,1,chosenRoute);
					
						Location.distanceBetween(latitude_user, longitude_user, Double.parseDouble(userNcrll[i][3]), Double.parseDouble(userNcrll[i][4]), distance);
						if(distance[i] < min) {
							min = distance[i];
							finalRouteMarker = i;
							finalLine = chosenRoute.charAt(0);
						}
					}
				}
				else{
					userNcrll[0] = findNearestStop(latitude_destination,longitude_destination,1,ncrll[2]);
					finalLine = ncrll[2].charAt(0);
				}
				//Log.i("hotspotz","Chosen Line is from userNcrll[finalRouteMarker][0] ");
				
				url = "http://www.nextbus.com/predictor/simplePrediction.shtml?a=georgia-tech";
				
				if(finalLine == 'G')
					url = url + "&r=green";
				else if(finalLine == 'R')
					url = url + "&r=red";
				else if(finalLine == 'B')
					url = url + "&r=blue";
				else
					url = url + "&r=trolley";
				
				url = url + "&d=" + ncrll[1];
				url = url + "&s=" + userNcrll[finalRouteMarker][1];
				Uri routeToBeDisplayed = Uri.parse(url);
				intent.setData(routeToBeDisplayed);
				
		}
		
		public NodeList getStops() {
			try {
				File file = new File("C:\\StopsList.xml");
				DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = docbf.newDocumentBuilder();
				Document doc = db.parse(file);
				NodeList nodes = doc.getElementsByTagName("stop");	
				return nodes;
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
}