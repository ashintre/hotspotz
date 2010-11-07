package com.buzzters.hotspotz.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class hotspotz1 extends Activity {
    /** Called when the activity is first created. */
	private EditText placetext; 
	private AutoCompleteTextView autocomplete_typetext1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	final String[] TYPE = new String[] {"Study","Work","Hangout","Meeting"};
    	System.out.println("in hotspotz1");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_place);
        
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_typetext1);
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
    }
    
    private void reset()
    {
    	placetext.setText("");
    	autocomplete_typetext1.setText("");
    }
}