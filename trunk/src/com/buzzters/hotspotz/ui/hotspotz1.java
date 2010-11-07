package com.buzzters.hotspotz.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class hotspotz1 extends Activity {
    /** Called when the activity is first created. */
	private EditText placeText; 
	private AutoCompleteTextView autocomplete_typetext1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	final String[] TYPE = new String[] {"Study","Work","Hangout","Meeting"};
    	System.out.println("in hotspotz1");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_place);
        
        placeText = (EditText) findViewById(R.id.placetext);
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_typetext1);
        autocomplete_typetext1 = textView;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, TYPE);
        textView.setAdapter(adapter);   
        final Context ctxt = this;
        
        final Button button= (Button) findViewById(R.id.clear1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //System.out.println("in loop");
                reset();
            	// Perform action on click
            }
        });
        
        final Button button1 = (Button) findViewById(R.id.back1);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //System.out.println("in loop");
               Intent myIntent=new Intent(ctxt, com.buzzters.hotspotz.ui.hotspotz.class);
               startActivity(myIntent); 
            	// Perform action on click
            }
        });
        
        final Context currentContext = this;
        final Button addCurrentLocationButton = (Button)findViewById(R.id.add_current_location);
        addCurrentLocationButton.setOnClickListener(new View.OnClickListener() 
        {			
        	LocationManager locationManager = (LocationManager) currentContext.getSystemService(Context.LOCATION_SERVICE);
        	LocationListener locationListener = null;
        	private static final String HOTSPOTZ_ADD_LOCATION_SERVLET = "http://hot-spotz.appspot.com/addMeetingPlace.do";
			@Override
			public void onClick(View v) {
				// Subscribe to the Location Manager system Service
				// Obtain location from either the GPS PROVIDER or the NETWORK PROVIDER
				Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null ? 
												locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) :
													locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if(lastKnownLocation != null)
				{
					addCurrentLocationToServer(lastKnownLocation);
				}
				else
				{
					// If last Known location from either of these services is null, listener for location updates
					locationListener = new LocationListener() {
						@Override
						public void onLocationChanged(Location location) {								
							addCurrentLocationToServer(location);							
						}						
						public void onProviderDisabled(String msg) {}			
						public void onProviderEnabled(String msg) {}							
						public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}							
					};
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
				}
			}					
			private void addCurrentLocationToServer(Location location)
			{
				// Update the obtained location to the web server component
				try 
				{			
					HttpClient httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost(HOTSPOTZ_ADD_LOCATION_SERVLET);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("latitude", String.valueOf(location.getLatitude())));
					params.add(new BasicNameValuePair("longitude", String.valueOf(location.getLongitude())));
					params.add(new BasicNameValuePair("tag", URLEncoder.encode(autocomplete_typetext1.getText().toString(), "UTF-8")));
					params.add(new BasicNameValuePair("nameOfPlace", URLEncoder.encode(placeText.getText().toString(), "UTF-8")));
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
					httpPost.setEntity(entity);
					httpClient.execute(httpPost);
				}
				catch (MalformedURLException mue) 
				{								
					mue.printStackTrace();
				}
				catch (IOException ioe)
				{					
					ioe.printStackTrace();
				}
				// Now un-subscribe so that we don't consume too much battery.
				if(locationListener != null)
					locationManager.removeUpdates(locationListener);
			}
		});
    }
    
    private void reset()
    {
    	placeText.setText("");
    	autocomplete_typetext1.setText("");
    }       
}