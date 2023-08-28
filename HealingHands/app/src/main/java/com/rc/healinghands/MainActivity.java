package com.rc.healinghands;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    LinearLayout llMinutesLine, llIntervalLine, llMainActivity, llButtons;
    TextView tvTimer;
    EditText etMinuteInput, etIntervalInput;
    Button btnStartPause, btnReset;
    boolean isTimerRunning = false; // true = show stop button , false = show start button
    CountDownTimer timer;

    long TimeBuff, UpdateTime = 0L, timeInterval = 0L;
    int Seconds, Minutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");
        NotificationManagerCompat.from(this).cancelAll();
        initXml();
        playWelcome();
    }
    
    View.OnClickListener buttonsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == btnStartPause.getId()) {
                if (!isTimerRunning) {
                    if (!(etMinuteInput.getText().toString().isEmpty())) {
                        isTimerRunning = true;
                        btnStartPause.setText(R.string.stop);
                        btnStartPause.setBackgroundColor(getResources().getColor(R.color.red));
                        long timeInput = Long.parseLong(etMinuteInput.getText().toString()) * 1000 * 60;
                        if (!etIntervalInput.getText().toString().isEmpty()) {
                            timeInterval = Long.parseLong(etIntervalInput.getText().toString());
                        }
                        Log.i(TAG, "Time input = " + Long.parseLong(etMinuteInput.getText().toString()) + " minutes");
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
                                String displayTime = String.format(Locale.getDefault(), "%02d", Minutes) + ":" + String.format(Locale.getDefault(), "%02d", Seconds);
                                tvTimer.setText(displayTime);
                                Log.i(TAG, "Display Time: " + displayTime);
                            }

                            public void onFinish() {
                                String done = "Done!";
                                tvTimer.setText(done);
                                Log.i(TAG, done);
                                playBigDing();
                                isTimerRunning = false;
                                btnStartPause.setText(R.string.start);
                                btnStartPause.setBackgroundColor(getResources().getColor(R.color.green));
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
                    isTimerRunning = false;
                    btnStartPause.setText(R.string.start);
                    btnStartPause.setBackgroundColor(getResources().getColor(R.color.green));
                    timer.cancel();
                    tvTimer.setText("");
                }
            } else if (v.getId() == btnReset.getId()) {


            }
        }
    };

    private void initXml() {
        llMainActivity = findViewById(R.id.llMainActivity);
        tvTimer = findViewById(R.id.tvTimer);
        llMinutesLine = findViewById(R.id.llMinutesLine);
        etMinuteInput = findViewById(R.id.etMinuteInput);
        llIntervalLine = findViewById(R.id.llIntervalLine);
        etIntervalInput = findViewById(R.id.etIntervalInput);
        llButtons = findViewById(R.id.llButtons);
        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(buttonsListener);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnStartPause.setOnClickListener(buttonsListener);
    }
    
    private void playWelcome() {
        MediaPlayer shalom = MediaPlayer.create(MainActivity.this, R.raw.shalom);
        shalom.start();
    }
    
    private void playBigDing() {
        Log.i(TAG, "Big Ding");
        MediaPlayer big_ding = MediaPlayer.create(MainActivity.this, R.raw.big_ding);
        big_ding.start();
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