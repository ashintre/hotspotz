package com.buzzters.hotspotz.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class hotspotz extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	final String[] TYPE = new String[] {"Study","Work","Hangout","Meeting"};

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_typetext);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, TYPE);
        textView.setAdapter(adapter);        
    }
}