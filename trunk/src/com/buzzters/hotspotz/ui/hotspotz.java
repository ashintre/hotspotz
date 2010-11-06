package com.buzzters.hotspotz.ui;

import net.oauth.*;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class hotspotz extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	final String[] TYPE = new String[] {"Study","Work","Hangout","Meeting"};

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
     
        Log.d("hotspotz", "started creating activity");
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_typetext);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, TYPE);
        textView.setAdapter(adapter);
                      
    }
    
    /*
    private static final String OAUTH_GET_REQUEST_TOKEN_URL = "https://www.google.com/accounts/OAuthGetRequestToken";
    private static final String OAUTH_AUTHORIZE_TOKEN = "https://www.google.com/accounts/OAuthAuthorizeToken";
    private static final String OAUTH_GET_ACCESS_TOKEN = "https://www.google.com/accounts/OAuthGetAccessToken";
    private static final String HP_CONSUMER_KEY = "hot-spotz.appspot.com";
    private static final String HP_CONSUMER_SECRET = "LbheqsJ2WsfLlVcq5pdCVjtC";
    
    public void doAuth()
    {
    	OAuthServiceProvider serviceProvider = new OAuthServiceProvider(OAUTH_GET_REQUEST_TOKEN_URL, OAUTH_AUTHORIZE_TOKEN, OAUTH_GET_ACCESS_TOKEN);
    	String callBackUrl = "hotspotz-android-app:///";
    	OAuthConsumer oAuthConsumer = new OAuthConsumer(callBackUrl, HP_CONSUMER_KEY, HP_CONSUMER_SECRET, serviceProvider);
    	OAuthAccessor oAuthAccessor = new OAuthAccessor(oAuthConsumer);
    	OAuthClient client = new OAuthClient(new HttpClient4());
    	
    	Intent intent = new Intent(Intent.ACTION_VIEW);
    	intent.setData(Uri.parse(oAuthAccessor.consumer.serviceProvider.userAuthorizationURL + 
    								"?oauth_token=" + oAuthAccessor.requestToken +
    								"?oauth_callback=" + oAuthAccessor.consumer.callbackURL));
    }*/
    
}