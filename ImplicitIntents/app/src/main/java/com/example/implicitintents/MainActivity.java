package com.example.implicitintents;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText mWebsiteEditText; // for holding the object for the website uri
    private EditText mLocationEditText; // for holding object for location string
    private EditText mShareTextEditText; // for holding object for share text


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebsiteEditText = findViewById(R.id.website_edittext); // object link to the xml
        mLocationEditText = findViewById(R.id.location_edittext); // object link to the xml
        mShareTextEditText = findViewById(R.id.share_edittext);
    }


    public void openWebsite(View view) {
        // get the website as string
        String url = mWebsiteEditText.getText().toString();

        //parse the string to valid webpage address (URI object)
        Uri webPage = Uri.parse(url);

        // create implicit intent to view the webpage
        Intent webSiteIntent = new Intent(Intent.ACTION_VIEW, webPage);

        // check if there is app that can handle opening website
        if (webSiteIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(webSiteIntent);
        } else {
            Log.d("ImplicitIntents", "Can't handle this!");
        }
    }

    public void openLocation(View view) {
        // get the location as string
        String loc = mLocationEditText.getText().toString();

        // parse the sting with geo search query
        Uri addressUri = Uri.parse("geo:0,0?q=" + loc);

        // create intent for location:
        Intent geoIntent = new Intent(Intent.ACTION_VIEW, addressUri);

        // check if the system can run this command and if so - run it
        if (geoIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(geoIntent);
        } else {
            Log.d("ImplicitIntents", "Can't handle this intent!");
        }

    }


    public void shareText(View view) {
        String txt = mShareTextEditText.getText().toString();
        String mimeType = "text/plain";
        ShareCompat.IntentBuilder
                .from(this)
                .setType(mimeType)
                .setChooserTitle("Share this text with: ")
                .setText(txt)
                .startChooser();
    }
}