package com.buzzters.hotspotz.ui;


import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;




public class hotspotz_contacts extends Activity {
    /** Called when the activity is first created. */
	private LinearLayout linear_view;
	private int cnt=0;
	CheckBox c;
	ArrayList arrayList = new ArrayList();
	@Override
    
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.contact);
        linear_view= (LinearLayout)findViewById(R.id.rel_layout);
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(People.CONTENT_URI, 
			null, null, null, null);
        
        if (cur.getCount() > 0) {
	     while (cur.moveToNext()) {
	         cnt++;
	    	 String id = cur.getString(cur.getColumnIndex(People._ID));
	         //String name = cur.getString(cur.getColumnIndex(People.DISPLAY_NAME));
	     
        
        Cursor emailCur = cr.query( 
    			Contacts.ContactMethods.CONTENT_EMAIL_URI, 
    			null,
    			Contacts.ContactMethods.PERSON_ID + " = ?", 
    			new String[]{id}, null); 
        emailCur.moveToFirst();
         
        //System.out.println("cnt ->"+cnt);
        CheckBox cbx=new CheckBox(this);
        cbx.setText(emailCur.getString(6));
        cbx.setId(cnt);
        linear_view.addView(cbx);
        
		 emailCur.close();
	     }
        }
        Button btn=new Button(this);
        btn.setText("Ok");
        btn.setId(1000);
        linear_view.addView(btn);
        setContentView(linear_view);
        
        final Button button = (Button) findViewById(1000);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //System.out.println("in loop");
                find_checked();
            	// Perform action on click
            }
        });
        

	}
	
	public void find_checked()
	{
		for(int i=1;i<=cnt;i++)
		{
			c = (CheckBox) findViewById(i);
			//System.out.println("c ->"+c);
			if(c.isChecked())
			{
				//System.out.println("in");
				arrayList.add(c.getText());
				//System.out.println(c.getText());
			}
		}
		System.out.println(arrayList);
	}
}

