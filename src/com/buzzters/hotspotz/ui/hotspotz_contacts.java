package com.buzzters.hotspotz.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.buzzters.hotspotz.Constants;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class hotspotz_contacts extends Activity {
		
	private static final String TAG = "hotspotz";
	
	private LinearLayout linear_view;
	private int cnt = 0;
	private static ArrayList<String> contactsList = new ArrayList<String>();
	private Map<String, String> emailContactNumsMap = new HashMap<String, String>();
	private HashMap<String, String> selectedEmailContactNumsMap = new HashMap<String, String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        
		setContentView(R.layout.contact);
		linear_view = (LinearLayout) findViewById(R.id.rel_layout);
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(People.CONTENT_URI, null, null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				cnt++;
				String id = cur.getString(cur.getColumnIndex(People._ID));
				
					Cursor pCur = cr.query(
							Contacts.Phones.CONTENT_URI, 
							null, 
							Contacts.Phones.PERSON_ID +" = ?", 
							new String[]{id}, null);
					
				Cursor emailCur = cr.query(
						Contacts.ContactMethods.CONTENT_EMAIL_URI, null,
						Contacts.ContactMethods.PERSON_ID + " = ?",
						new String[] { id }, null);
				emailCur.moveToFirst();
				pCur.moveToFirst();
				// Add the list of contact email Ids to a list of check boxes so that the user can select friends he wants
				CheckBox cbx = new CheckBox(this);
				cbx.setText(emailCur.getString(6));
				cbx.setId(cnt);
				cbx.setTextColor(Color.rgb(255, 69, 0));
				linear_view.addView(cbx);
				emailContactNumsMap.put(emailCur.getString(6), pCur.getString(7));
				emailCur.close();				
			}
		}
		Button btn = new Button(this);
		btn.setText("Ok");
		btn.setTextColor(Color.rgb(160, 82, 45));
		btn.setTypeface(Typeface.DEFAULT_BOLD);
		btn.setId(1000);
		linear_view.addView(btn);
		setContentView(linear_view);
		
		final Activity currentActivity = this;
		final Button button1 = (Button) findViewById(R.id.back2);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               Intent myIntent=new Intent(currentActivity, com.buzzters.hotspotz.ui.hotspotz.class);                              
               startActivity(myIntent);            	
            }
        });

		final Button button = (Button) findViewById(1000);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				find_checked();
				Intent meetingServiceIntent = new Intent();
				meetingServiceIntent.putExtra("emailIds", contactsList);
				meetingServiceIntent.putExtra(Constants.MY_EMAIL_ID, currentActivity.getIntent().getStringExtra(Constants.MY_EMAIL_ID));
				meetingServiceIntent.putExtra(Constants.EMAIL_CONTACTNUM_MAP, selectedEmailContactNumsMap);
				// Figure out how to get this value from previous screen
				meetingServiceIntent.putExtra(Constants.LOCATION_TAG, currentActivity.getIntent().getStringExtra(Constants.LOCATION_TAG));
				meetingServiceIntent.putExtra(Constants.NAME_OF_EVENT, currentActivity.getIntent().getStringExtra(Constants.NAME_OF_EVENT));
				meetingServiceIntent.setAction("com.buzzters.hotspotz.service.MeetingLocatorService");
				currentActivity.startService(meetingServiceIntent);
			}
		});		
	}

	public void find_checked() {
		for (int i = 1; i <= cnt; i++) {
			CheckBox c = (CheckBox) findViewById(i);
			if (c.isChecked()) {
				String selectedEmailId = c.getText().toString();
				selectedEmailContactNumsMap.put(selectedEmailId, emailContactNumsMap.get(selectedEmailId));				
			}
		}
	}	
		
}
