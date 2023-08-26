package com.rc.healinghands;

import androidx.appcompat.app.AppCompatActivity;

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

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    LinearLayout llTextInput, llIntervalInput;
    TextView tvTimer;
    EditText etTimeInput, etIntervalInput;
    Button btnStart;
    boolean buttonMode = true; // true = start, false = stop
    CountDownTimer timer;
    long TimeBuff, UpdateTime = 0L;
    int Seconds, Minutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        llTextInput = findViewById(R.id.llMinutesInput);
        llIntervalInput = findViewById(R.id.llIntervalInput);
        tvTimer = findViewById(R.id.tvTimer);
        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(buttonClickListener);
        etTimeInput = findViewById(R.id.etTimeInput);
        etIntervalInput = findViewById(R.id.etIntervalInput);
        Log.i(TAG, "onCreate");
    }

    View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == btnStart.getId()) {
                if (buttonMode) {
                    if (!(etTimeInput.getText().toString().isEmpty())) {
                        buttonMode = false;
                        btnStart.setText(R.string.stop);
                        btnStart.setBackgroundColor(getResources().getColor(R.color.red));
                        llTextInput.setVisibility(View.GONE);
                        llIntervalInput.setVisibility(View.GONE);
                        long timeInput = Long.parseLong(etTimeInput.getText().toString()) * 1000 * 60;
                        long timeInterval = Long.parseLong(etIntervalInput.getText().toString()) * 1000 * 60;
                        Log.i(TAG, "Time input = " + timeInput + " millis or " + Long.parseLong(etTimeInput.getText().toString()) + " minutes");
                        timer = new CountDownTimer(timeInput, 1000) {
                            public void onTick(long millisUntilFinished) {
                                UpdateTime = TimeBuff + millisUntilFinished;
                                Seconds = (int) (UpdateTime / 1000);
                                Minutes = Seconds / 60;
                                Seconds = Seconds % 60;
                                if (Minutes % timeInterval == 0 && Seconds == 0) {
                                    playSmallDing(timeInterval);
                                }
                                String displayTime = Minutes + ":" + String.format("%02d", Seconds);
                                tvTimer.setText(displayTime);
                                Log.i(TAG, "Display Time: " + displayTime);
                            }

                            public void onFinish() {
                                String done = "Done!";
                                tvTimer.setText(done);
                                Log.i(TAG, done);
                                buttonMode = true;
                                btnStart.setText(R.string.start);
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
                    buttonMode = true;
                    btnStart.setText(R.string.start);
                    btnStart.setBackgroundColor(getResources().getColor(R.color.green));
                    timer.cancel();
                    tvTimer.setText("");
                }
            }
        }
    };

    private void playSmallDing(long sleepMillis) {
        Log.i(TAG, "Small Ding");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Small Ding", Toast.LENGTH_LONG).show();
            }
        });
    }
}