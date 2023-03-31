package com.rc.csl;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    String start = "http://www.example";
    String end = ".com";
    String buffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i = 0; i < 9; i++) {
            URL url;
            try {
                String fullPath = start + end;
                url = new URL(fullPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                buffer = readStream(in);
                Log.i("found:", url.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } // End of for loop
    } // End of OnCreate

    private String readStream(InputStream is) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
            stringBuilder.append(line);
        }
        is.close();
        return stringBuilder.toString();
    } // End of readStream()
} // End of MainActivity