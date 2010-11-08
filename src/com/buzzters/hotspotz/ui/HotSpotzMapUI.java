package com.buzzters.hotspotz.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HotSpotzMapUI extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.mapui);
        final Activity currentActivity = this;
        
        TextView eventNameView = (TextView) findViewById(R.id.event_name);
        eventNameView.setText("Event Name : " + this.getIntent().getStringExtra("nameOfEvent"));
        
        TextView bestPlaceToMeet = (TextView) findViewById(R.id.best_place_to_meet);
        bestPlaceToMeet.setText("Best Place to meet : " + this.getIntent().getStringExtra("bestPlaceToMeet"));
        
        Button showRouteButton = (Button) findViewById(R.id.show_route);
        showRouteButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {			
				Intent showRouteIntent = new Intent(Intent.ACTION_VIEW);
				showRouteIntent.setData(Uri.parse(currentActivity.getIntent().getStringExtra("nextBusURL")));
				currentActivity.startActivity(showRouteIntent);
			}
		});
        
        Button showGoogleMapsButton = (Button) findViewById(R.id.show_google_map);
        showGoogleMapsButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {			
				Intent showGoogleMapsIntent = new Intent(Intent.ACTION_VIEW);				
				Double lat = currentActivity.getIntent().getDoubleExtra("destinationLatitude", 33.778425);
				Double longtd = currentActivity.getIntent().getDoubleExtra("destinationLongitude", -84.398856);
				//String googleStreetViewUri = "geo:" + lat + "," + longtd + "?z=18";
				String googleStreetViewUri = "google.streetview:cbll=" + lat +
												"," + longtd + "&cbp=1," +
												"0,,0,1.0,mz=18";
				showGoogleMapsIntent.setData(Uri.parse(googleStreetViewUri));
				currentActivity.startActivity(showGoogleMapsIntent);
			}
		});
	}
}
