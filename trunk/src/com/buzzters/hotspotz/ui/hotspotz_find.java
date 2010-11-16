package com.buzzters.hotspotz.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.buzzters.hotspotz.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class hotspotz_find extends Activity {
    
	private static final String GET_TAG_LOCATIONS_URL = "http://hot-spotz.appspot.com/getTagsList.do";	
	
	private EditText nametext; 
	private AutoCompleteTextView autocomplete_typetext;
	private EditText userid_text;
	final Context ctxt = this;
	final Handler mHandler = new Handler();
	String[] PRELOADED_TAG_LIST = new String[] {"Study","Coffee","Hangout","Meeting"};
	String[] SERVER_TAG_LIST = new String [1];
    
	@Override
    public void onCreate(Bundle savedInstanceState) {		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_place);
        
        updateTypeList();
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_typetext);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, PRELOADED_TAG_LIST);
        textView.setAdapter(adapter); 
        
        nametext = (EditText)findViewById(R.id.nametext);
        autocomplete_typetext=(AutoCompleteTextView)findViewById(R.id.autocomplete_typetext);
        userid_text=(EditText)findViewById(R.id.userid_text);
                
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
            	   myIntent.putExtra(Constants.MY_EMAIL_ID, userid_text.getText().toString());
                   myIntent.putExtra(Constants.NAME_OF_EVENT, nametext.getText().toString());
                   myIntent.putExtra(Constants.LOCATION_TAG, autocomplete_typetext.getText().toString());
                   startActivity(myIntent); 
               }
            }
        });

        final Button button1 = (Button) findViewById(R.id.clear);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {              
                reset();
            }
        });       
    }
    
    private void updateTypeList()
    {
    	Thread t = new Thread() {
    		public void run() {    			
    			Scanner responseScanner = null;
    			List<String> tags = new ArrayList<String>();
    			try
    			{
    				URL getTagsListURL = new URL(GET_TAG_LOCATIONS_URL);
    				responseScanner = new Scanner(getTagsListURL.openStream());
    				responseScanner.useDelimiter(",");        			
        			while (responseScanner.hasNext()) {
        				tags.add(responseScanner.next().replace("[", "").replace("]","").trim());
        			}        			
        			Log.i(Constants.HOTSPOTZ_APP_TAG, "Type obtained from Servlet as : " + tags);
        			SERVER_TAG_LIST = (String[]) tags.toArray(SERVER_TAG_LIST);
    			}
    			catch(IOException e)
    			{
    				e.printStackTrace();
    			}
    			finally
    			{
    				responseScanner.close();
    			}   			
    			mHandler.post(mUpdateTagsList);
    		}
    	};
    	t.start();
    }
    
	final Runnable mUpdateTagsList = new Runnable() {
        public void run() {        	           
        	Log.i(Constants.HOTSPOTZ_APP_TAG, "Setting tags List as : " + SERVER_TAG_LIST);
        	autocomplete_typetext.setAdapter(new ArrayAdapter<String>(ctxt, R.layout.list_item, SERVER_TAG_LIST));
        }
    };
    
    private void reset()
    {
    	nametext.setText("");
    	autocomplete_typetext.setText("");
    }
}