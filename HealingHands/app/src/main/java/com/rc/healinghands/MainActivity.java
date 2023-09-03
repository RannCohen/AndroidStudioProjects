package com.rc.healinghands;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    LinearLayout llMinutesLine, llIntervalLine, llMainActivity, llButtons;
    TextView tvTimer;
    ImageView ivHands;
    EditText etMinuteInput, etIntervalInput;
    Button btnStartPause, btnReset;
    boolean countDownIsRunning = false, countUpIsRunning = false;
    CountDownTimer countDownTimer;
    Chronometer countUpTimer;
    long timeInput = -1L, timeIntervalInput = 0L, timeLeftInMillis, endTime, timeCounterUp = 0L;

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
        Log.i(TAG, "onResume");
        NotificationManagerCompat.from(MainActivity.this).cancelAll();
        updateCountDownText(timeLeftInMillis);
        updateButtons();
        if (!countDownIsRunning && !countUpIsRunning) {
            SoundHandler.playWelcomeSound(this);
        }
        if (countUpIsRunning) {
            tvTimer.setVisibility(View.GONE);
            countUpTimer.setVisibility(View.VISIBLE);
            countUpTimer.setBase(timeCounterUp);
            countUpTimer.start();
        }
    }

    View.OnClickListener buttonsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            countUpTimer.stop();
            countUpIsRunning = false;
            countUpTimer.setVisibility(View.GONE);
            tvTimer.setVisibility(View.VISIBLE);

            if (v.getId() == btnStartPause.getId()) {
                Utils.hideKeyboard(MainActivity.this, v);
                if (countDownIsRunning) {
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
        countUpTimer = findViewById(R.id.chTimer);
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
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText(timeLeftInMillis);
            }

            public void onFinish() {
                String done = "Done!";
                tvTimer.setText(done);
                Log.i(TAG, done);
                countDownIsRunning = false;
                SoundHandler.playBigDing(MainActivity.this);
                updateButtons();
                tvTimer.setVisibility(View.GONE);
                countUpTimer.setVisibility(View.VISIBLE);
                countUpTimer.setBase(SystemClock.elapsedRealtime());
                countUpTimer.start();
                countUpIsRunning = true;
            }
        }.start();

        countDownIsRunning = true;
        updateButtons();
    }

    private void pauseTimer() {
        countDownIsRunning = false;
        countDownTimer.cancel();
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
        if (countDownIsRunning) {
            btnReset.setVisibility(View.GONE);
            btnStartPause.setText(R.string.pause);
            btnStartPause.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.pause_sign), null);
            btnStartPause.setBackgroundColor(getResources().getColor(R.color.red));
        } else {
            btnStartPause.setText(R.string.start);
            btnStartPause.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.play_triangle), null);
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
        outState.putLong("timeCounterUp", countUpTimer.getBase());
        outState.putLong("timeIntervalInput", timeIntervalInput);
        outState.putBoolean("countDownIsRunning", countDownIsRunning);
        outState.putBoolean("countUpIsRunning", countUpIsRunning);
        outState.putLong("endTime", endTime);
        outState.putLong("timeInput", timeInput);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        timeLeftInMillis = savedInstanceState.getLong("timeLeftInMillis");
        countDownIsRunning = savedInstanceState.getBoolean("countDownIsRunning");
        countUpIsRunning = savedInstanceState.getBoolean("countUpIsRunning");
        timeIntervalInput = savedInstanceState.getLong("timeIntervalInput");
        timeInput = savedInstanceState.getLong("timeInput");
        timeCounterUp = savedInstanceState.getLong("timeCounterUp");
        updateCountDownText(timeLeftInMillis);
        updateButtons();

        if (countDownIsRunning) {
            endTime = savedInstanceState.getLong("endTime");
            timeLeftInMillis = endTime - System.currentTimeMillis();
            startTimer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        try {
            NotificationManagerCompat.from(MainActivity.this).notifyAll();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}