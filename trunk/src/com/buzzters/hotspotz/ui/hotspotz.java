package com.buzzters.hotspotz.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class hotspotz extends Activity {
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
    

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
     
        Log.d("hotspotz", "started creating activity");
        final Context ctxt = this;
        
        final Button button = (Button) findViewById(R.id.find_place);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //System.out.println("in loop");
               Intent myIntent=new Intent(ctxt, com.buzzters.hotspotz.ui.hotspotz_find.class);
               //myIntent.setClassName("com.buzzters.hotspotz.ui", "com.buzzters.hotspotz.ui.hotspotz1");
               //myIntent.setClassName("com.buzzters.hotspotz.ui", "hotspotz1");               
               startActivity(myIntent); 
            	// Perform action on click
            }
        });

        final Button button1 = (Button) findViewById(R.id.add_place);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //System.out.println("in loop");
            	Intent myIntent1=new Intent(ctxt, com.buzzters.hotspotz.ui.hotspotz1.class);
            	startActivity(myIntent1);
            	//reset();
            	// Perform action on click
            }
        });                                   
    }           
}