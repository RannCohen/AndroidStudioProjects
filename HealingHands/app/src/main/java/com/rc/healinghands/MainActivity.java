package com.rc.healinghands;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    LinearLayout llMinutesLine, llIntervalLine, llMainActivity, llButtons;
    TextView tvTimer;
    ImageView ivHands;
    EditText etMinuteInput, etIntervalInput;
    Button btnStartPause, btnReset;
    boolean timerIsRunning = false; // true = show stop button , false = show start button
    CountDownTimer timer;

    long timeInput = -1L, timeIntervalInput = 0L, timeLeftInMillis, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");
        initXml();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManagerCompat.from(MainActivity.this).cancelAll();
        if (!timerIsRunning) {
            SoundHandler.playWelcomeSound(this);
        }
        updateCountDownText(timeLeftInMillis);
        updateButtons();
    }

    View.OnClickListener buttonsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == btnStartPause.getId()) {
                Utils.hideKeyboard(MainActivity.this, v);
                if (timerIsRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            } else if (v.getId() == btnReset.getId()) {
                resetTimer();
            }
        }
    };

    private void initXml() {
        llMainActivity = findViewById(R.id.llMainActivity);
        tvTimer = findViewById(R.id.tvTimer);
        ivHands = findViewById(R.id.ivHands);
        llMinutesLine = findViewById(R.id.llMinutesLine);
        etMinuteInput = findViewById(R.id.etMinuteInput);
        llIntervalLine = findViewById(R.id.llIntervalLine);
        etIntervalInput = findViewById(R.id.etIntervalInput);
        llButtons = findViewById(R.id.llButtons);
        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(buttonsListener);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnStartPause.setOnClickListener(buttonsListener);
        etMinuteInput.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void startTimer() {
        getMinuteInput();
        endTime = System.currentTimeMillis() + timeLeftInMillis;

        getTimeIntervalInput();
        timer = new CountDownTimer(timeLeftInMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText(timeLeftInMillis);
            }

            public void onFinish() {
                String done = "Done!";
                tvTimer.setText(done);
                Log.i(TAG, done);
                timerIsRunning = false;
                SoundHandler.playBigDing(MainActivity.this);
                updateButtons();
            }
        }.start();

        timerIsRunning = true;
        updateButtons();
    }

    private void pauseTimer() {
        timerIsRunning = false;
        timer.cancel();
        updateButtons();
    }

    private void resetTimer() {
        timeLeftInMillis = timeInput;
        updateCountDownText(timeLeftInMillis);
        updateButtons();
    }

    private void updateCountDownText(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 1000) / 60;
        int seconds = (int) (millisUntilFinished / 1000) % 60;

        if ((timeIntervalInput != 0) && (minutes % timeIntervalInput == 0) && (seconds == 0) && (minutes != 0)) {
            SoundHandler.playSmallDing(MainActivity.this);
        }
        String displayTime = String.format(Locale.getDefault(), "%02d", minutes) + ":" + String.format(Locale.getDefault(), "%02d", seconds);
        tvTimer.setText(displayTime);
        Log.i(TAG, "Display Time: " + displayTime);
    }

    private void updateButtons() {
        if (timerIsRunning) {
            btnReset.setVisibility(View.GONE);
            btnStartPause.setText(R.string.pause);
            btnStartPause.setBackgroundColor(getResources().getColor(R.color.red));
        } else {
            btnStartPause.setText(R.string.start);
            btnStartPause.setBackgroundColor(getResources().getColor(R.color.green));

            if (timeLeftInMillis < 1000 && timeInput != -1) {
                btnStartPause.setVisibility(View.GONE);
            } else {
                btnStartPause.setVisibility(View.VISIBLE);
            }

            if (timeLeftInMillis < timeInput || timeInput == -1) {
                btnReset.setVisibility(View.VISIBLE);
            } else {
                btnReset.setVisibility(View.GONE);
            }
        }
    }

    private void getMinuteInput() {
        if (!(etMinuteInput.getText().toString().isEmpty())) {
            timeInput = Long.parseLong(etMinuteInput.getText().toString()) * 1000 * 60;
            timeLeftInMillis = timeInput;
            Log.i(TAG, "Time input = " + Long.parseLong(etMinuteInput.getText().toString()) + " minutes");
            etMinuteInput.setText("");
        } else if (timeLeftInMillis == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.input_healing_time, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM, 0, 250);
                    toast.show();
                }
            });
        }
    }

    private void getTimeIntervalInput() {
        if (!etIntervalInput.getText().toString().isEmpty()) {
            timeIntervalInput = Long.parseLong(etIntervalInput.getText().toString());
            etIntervalInput.setText("");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("timeLeftInMillis", timeLeftInMillis);
        outState.putLong("timeIntervalInput", timeIntervalInput);
        outState.putBoolean("timerIsRunning", timerIsRunning);
        outState.putLong("endTime", endTime);
        outState.putLong("timeInput", timeInput);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        timeLeftInMillis = savedInstanceState.getLong("timeLeftInMillis");
        timerIsRunning = savedInstanceState.getBoolean("timerIsRunning");
        timeIntervalInput = savedInstanceState.getLong("timeIntervalInput");
        timeInput = savedInstanceState.getLong("timeInput");
        updateCountDownText(timeLeftInMillis);
        updateButtons();

        if (timerIsRunning) {
            endTime = savedInstanceState.getLong("endTime");
            timeLeftInMillis = endTime - System.currentTimeMillis();
            startTimer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        try {
            NotificationManagerCompat.from(MainActivity.this).notifyAll();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}