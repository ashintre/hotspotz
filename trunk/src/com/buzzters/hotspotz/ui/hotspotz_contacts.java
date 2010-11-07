package com.buzzters.hotspotz.ui;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class hotspotz_contacts extends Activity {
	
	private static final String HOTSPOTZ_GET_CONTACT_LCNS_URL = "http://hot-spotz.appspot.com/getContactLocations.do";
	private static final String TAG = "hotspotz";
	
	private LinearLayout linear_view;
	private int cnt = 0;
	CheckBox c;
	private static ArrayList<String> contactsList = new ArrayList<String>();
	
	//TODO: Contacts List is not getting updated. Fix it.
	static
	{
		contactsList.add("the77thsecret@gmail.com");
		contactsList.add("adhishbhobe@gmail.com");
	}

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
				Cursor emailCur = cr.query(
						Contacts.ContactMethods.CONTENT_EMAIL_URI, null,
						Contacts.ContactMethods.PERSON_ID + " = ?",
						new String[] { id }, null);
				emailCur.moveToFirst();

				// Add the list of contact email Ids to a list of check boxes so that the user can select friends he wants
				CheckBox cbx = new CheckBox(this);
				cbx.setText(emailCur.getString(emailCur.getColumnIndex(People.PRIMARY_EMAIL_ID)));
				cbx.setId(cnt);
				linear_view.addView(cbx);
				emailCur.close();
			}
		}
		Button btn = new Button(this);
		btn.setText("Ok");
		btn.setId(1000);
		linear_view.addView(btn);
		setContentView(linear_view);

		final Button button = (Button) findViewById(1000);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				find_checked();
			}
		});
		
		Intent meetingServiceIntent = new Intent();
		meetingServiceIntent.setAction("com.buzzters.hotspotz.service.MeetingLocatorService");
		this.startService(meetingServiceIntent);
		//determineFriendLocations();
	}

	public void find_checked() {
		for (int i = 1; i <= cnt; i++) {
			c = (CheckBox) findViewById(i);
			if (c.isChecked()) {
				contactsList.add(c.getText().toString());
			}
		}
	}
	
	private void determineFriendLocations()
	{
		Scanner responseScanner = null;
		try
		{
			StringBuilder urlBuilder = new StringBuilder(HOTSPOTZ_GET_CONTACT_LCNS_URL + "?emailIds=");
			for(String contact : contactsList)
			{
				urlBuilder.append(URLEncoder.encode(contact, "UTF-8")).append(",");
			}
			URL getContactLocationsURL = new URL(urlBuilder.toString());
			responseScanner = new Scanner(getContactLocationsURL.openStream());
			Log.i(TAG, "Printing response to console");
			while(responseScanner.hasNext())
			{
				Log.i(TAG, responseScanner.next());
			}
		}
		catch(IOException mue)
		{
			mue.printStackTrace();
		}		 
	}
}
