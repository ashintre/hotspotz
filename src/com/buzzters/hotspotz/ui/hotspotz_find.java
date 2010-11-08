package com.buzzters.hotspotz.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class hotspotz_find extends Activity {
    /** Called when the activity is first created. */
	private EditText nametext; 
	private AutoCompleteTextView autocomplete_typetext;
	private EditText userid_text;
    @Override
    
    public void onCreate(Bundle savedInstanceState) {
    	final String[] TYPE = new String[] {"Study","Coffee","Hangout","Meeting"};

        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_place);
        
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_typetext);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, TYPE);
        textView.setAdapter(adapter); 
        
        nametext = (EditText)findViewById(R.id.nametext);
        autocomplete_typetext=(AutoCompleteTextView)findViewById(R.id.autocomplete_typetext);
        userid_text=(EditText)findViewById(R.id.userid_text);
        final Context ctxt = this;
        
        final Button button = (Button) findViewById(R.id.back);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               Intent myIntent=new Intent(ctxt, com.buzzters.hotspotz.ui.hotspotz.class);                              
               startActivity(myIntent);             	
            }
        });
        
        final Button button2 = (Button) findViewById(R.id.ok);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               if((nametext.getText().length()==0)||(autocomplete_typetext.getText().length()==0)||((userid_text.getText().length()==0)))
               {
            	   AlertDialog.Builder builder = new AlertDialog.Builder(hotspotz_find.this);
            	   builder.setMessage("First Enter the Values")
            	          .setCancelable(false)
            	          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            	              public void onClick(DialogInterface dialog, int id) {
            	                   dialog.cancel();
            	              }
            	          });
        		   builder.show();            	  
               }
               else
               {
            	   Intent myIntent=new Intent(ctxt, com.buzzters.hotspotz.ui.hotspotz_contacts.class);
            	   myIntent.putExtra("myEmailId", userid_text.getText().toString());
                   myIntent.putExtra("nameOfEvent", nametext.getText().toString());
                   myIntent.putExtra("tag", autocomplete_typetext.getText().toString());
                   startActivity(myIntent); 
               }
            }
        });

        final Button button1 = (Button) findViewById(R.id.clear);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //System.out.println("in loop");
                reset();
            	// Perform action on click
            }
        });
       
    }
    
    private void reset()
    {
    	nametext.setText("");
    	autocomplete_typetext.setText("");
    }
}