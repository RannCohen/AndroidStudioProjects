package com.rc.healinghands;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    LinearLayout llTextInput, llIntervalInput, llImage;
    RelativeLayout mainLayout;
    TextView tvTimer, tvTitle;
    EditText etTimeInput, etIntervalInput;
    Button btnStart;
    boolean buttonMode = true; // true = start, false = stop
    CountDownTimer timer;
    long TimeBuff, UpdateTime = 0L, timeInterval = 0L;
    int Seconds, Minutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate");
        NotificationManagerCompat.from(this).cancelAll();
        mainLayout = findViewById(R.id.theme);
        llTextInput = findViewById(R.id.llMinutesInput);
        llIntervalInput = findViewById(R.id.llIntervalInput);
        llImage = findViewById(R.id.llImage);
        tvTimer = findViewById(R.id.tvTimer);
        btnStart = findViewById(R.id.btnStart);
        etTimeInput = findViewById(R.id.etTimeInput);
        etIntervalInput = findViewById(R.id.etIntervalInput);
        tvTitle = findViewById(R.id.tvTitle);
        MediaPlayer shalom = MediaPlayer.create(MainActivity.this, R.raw.shalom);
        shalom.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (buttonMode) {
                            if (!(etTimeInput.getText().toString().isEmpty())) {
                                buttonMode = false;
                                btnStart.setText(R.string.stop);
                                btnStart.setBackgroundColor(getResources().getColor(R.color.red));
                                llTextInput.setVisibility(View.GONE);
                                llIntervalInput.setVisibility(View.GONE);
                                llImage.setVisibility(View.GONE);
                                tvTitle.setVisibility(View.GONE);
                                tvTimer.setBackgroundColor(getResources().getColor(R.color.black));
                                mainLayout.setBackgroundColor(getResources().getColor(R.color.black));
                                tvTimer.setTextColor(getResources().getColor(R.color.white));
                                long timeInput = Long.parseLong(etTimeInput.getText().toString()) * 1000 * 60;
                                if (!etIntervalInput.getText().toString().isEmpty()) {
                                    timeInterval = Long.parseLong(etIntervalInput.getText().toString());
                                }
                                Log.i(TAG, "Time input = " + Long.parseLong(etTimeInput.getText().toString()) + " minutes");
                                Log.i(TAG, "Time intervals = " + timeInterval + " Minutes");
                                timer = new CountDownTimer(timeInput, 1000) {
                                    public void onTick(long millisUntilFinished) {
                                        UpdateTime = TimeBuff + millisUntilFinished;
                                        Seconds = (int) (UpdateTime / 1000);
                                        Minutes = Seconds / 60;
                                        Seconds = Seconds % 60;
                                        if ((timeInterval != 0) && (Minutes % timeInterval == 0) && (Seconds == 0)) {
                                            playSmallDing();
                                        }
                                        String displayTime = String.format("%02d", Minutes) + ":" + String.format("%02d", Seconds);
                                        tvTimer.setText(displayTime);
                                        Log.i(TAG, "Display Time: " + displayTime);
                                    }

                                    public void onFinish() {
                                        String done = "Done!";
                                        tvTimer.setText(done);
                                        MediaPlayer big_ding = MediaPlayer.create(MainActivity.this, R.raw.big_ding);
                                        big_ding.start();
                                        Log.i(TAG, done);
                                        buttonMode = true;
                                        btnStart.setText(R.string.start);
                                        btnStart.setBackgroundColor(getResources().getColor(R.color.green));
                                        tvTimer.setBackgroundColor(getResources().getColor(R.color.white));
                                        mainLayout.setBackgroundColor(getResources().getColor(R.color.white));
                                        tvTimer.setTextColor(getResources().getColor(R.color.black));
                                        llTextInput.setVisibility(View.VISIBLE);
                                        llIntervalInput.setVisibility(View.VISIBLE);
                                        llImage.setVisibility(View.VISIBLE);
                                        tvTitle.setVisibility(View.VISIBLE);
                                    }
                                }.start();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(getApplicationContext(), R.string.input_healing_time, Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.BOTTOM, 0, 250);
                                        toast.show();
                                    }
                                });
                            }
                        } else {
                            llTextInput.setVisibility(View.VISIBLE);
                            llIntervalInput.setVisibility(View.VISIBLE);
                            llImage.setVisibility(View.VISIBLE);
                            tvTitle.setVisibility(View.VISIBLE);
                            tvTimer.setBackgroundColor(getResources().getColor(R.color.white));
                            mainLayout.setBackgroundColor(getResources().getColor(R.color.white));
                            tvTimer.setTextColor(getResources().getColor(R.color.black));
                            buttonMode = true;
                            btnStart.setText(R.string.start);
                            btnStart.setBackgroundColor(getResources().getColor(R.color.green));
                            timer.cancel();
                            tvTimer.setText("");
                        }
                    }
                });
            }
        });
    }

    private void playSmallDing() {
        Log.i(TAG, "Small Ding");
        MediaPlayer small_ding = MediaPlayer.create(MainActivity.this, R.raw.small_ding);
        small_ding.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManagerCompat.from(this).notifyAll();
    }
}