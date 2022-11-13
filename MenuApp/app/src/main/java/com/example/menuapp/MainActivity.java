package com.example.menuapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Rinfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "onCreate is happening!!!");
    }

    public void showSettings(View v) {
        String settings = " Settings";
        showToast(settings);
        Log.d(LOG_TAG, "setting button clicked!");
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }


    public void showToast(String tv) {
        String myString = getResources().getString(R.string.toast_message);
        Toast t = Toast.makeText(this, myString + tv ,Toast.LENGTH_SHORT);
        t.show();
    }
}