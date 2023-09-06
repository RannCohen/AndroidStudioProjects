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
    LinearLayout llPrepareLine, llMinutesLine, llIntervalLine, llMainActivity, llButtons;
    TextView tvTimer;
    ImageView ivHands;
    EditText etPrepareInput, etMinuteInput, etIntervalInput;
    Button btnStartPause, btnReset;
    boolean countDownIsRunning = false, countUpIsRunning = false, preparationInProgress = false;
    CountDownTimer countDownTimer, prepareCountDownTimer;
    Chronometer countUpTimer;
    long timeInput = -1L, timeIntervalInput = 0L, timeLeftInMillis, prepareTimeLeftInMillis, endTime, prepareEndTime, timeCounterUp = 0L, prepareInput = 0L;

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
        if (!countDownIsRunning && !countUpIsRunning && !preparationInProgress) {
            SoundHandler.playWelcomeSound(this);
        }
        updateCountDownText(timeLeftInMillis);
        updateButtons();
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

            if (countUpIsRunning) {
                countUpIsRunning = false;
                countUpTimer.stop();
                countUpTimer.setVisibility(View.GONE);
                tvTimer.setVisibility(View.VISIBLE);
            }

            if (v.getId() == btnStartPause.getId()) {
                Utils.hideKeyboard(MainActivity.this, v);
                if (countDownIsRunning || preparationInProgress) {
                    pauseTimer();
                } else {
                    startPrepareTimer();
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
        llPrepareLine = findViewById(R.id.llPrepareLine);
        etPrepareInput = findViewById(R.id.etPrepareInput);
        llMinutesLine = findViewById(R.id.llMinutesLine);
        etMinuteInput = findViewById(R.id.etMinuteInput);
        llIntervalLine = findViewById(R.id.llIntervalLine);
        etIntervalInput = findViewById(R.id.etIntervalInput);
        llButtons = findViewById(R.id.llButtons);
        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(buttonsListener);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnStartPause.setOnClickListener(buttonsListener);
        etPrepareInput.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void startPrepareTimer() {
        getPreparationInput();
        prepareEndTime = System.currentTimeMillis() + prepareTimeLeftInMillis;

        prepareCountDownTimer = new CountDownTimer(prepareTimeLeftInMillis, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                prepareTimeLeftInMillis = millisUntilFinished;
                updateCountDownText(prepareTimeLeftInMillis);
            }

            @Override
            public void onFinish() {
                SoundHandler.playBigDing(MainActivity.this);
                preparationInProgress = false;
                updateButtons();
                startTimer();
            }
        }.start();
        preparationInProgress = true;
        updateButtons();
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
                Log.i(TAG, "Done!");
                countDownIsRunning = false;
                SoundHandler.playSmallDing(MainActivity.this);
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
        if (countDownIsRunning) {
            countDownIsRunning = false;
            countDownTimer.cancel();

        } else if (preparationInProgress) {
            preparationInProgress = false;
            prepareCountDownTimer.cancel();
        }
        updateButtons();
    }

    private void resetTimer() {
        prepareTimeLeftInMillis = prepareInput;
        timeLeftInMillis = timeInput;
        updateCountDownText(prepareTimeLeftInMillis);
        updateButtons();
    }

    private void updateCountDownText(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 1000) / 60;
        int seconds = (int) (millisUntilFinished / 1000) % 60;

        if ((timeIntervalInput != 0) && (minutes % timeIntervalInput == 0) && (seconds == 0) && (minutes != 0)) {
            SoundHandler.playBigDing(MainActivity.this);
        }
        String displayTime = String.format(Locale.getDefault(), "%02d", minutes) + ":" + String.format(Locale.getDefault(), "%02d", seconds);
        tvTimer.setText(displayTime);
        Log.i(TAG, "Display Time: " + displayTime);
    }

    private void updateButtons() {
        if (countDownIsRunning || preparationInProgress) {
            btnReset.setVisibility(View.GONE);
            btnStartPause.setText(R.string.pause);
            btnStartPause.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.pause_sign), null);
            btnStartPause.setBackgroundColor(getResources().getColor(R.color.red));
        } else {
            btnStartPause.setText(R.string.start);
            btnStartPause.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.play_triangle), null);
            btnStartPause.setBackgroundColor(getResources().getColor(R.color.green));

            if ((timeLeftInMillis < 1000 || prepareTimeLeftInMillis < 1000) && timeInput != -1) {
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

    private void getPreparationInput() {
        if (!etPrepareInput.getText().toString().isEmpty()) {
            prepareInput = Long.parseLong(etPrepareInput.getText().toString()) * 1000;
            prepareTimeLeftInMillis = prepareInput;
            Log.i(TAG, "Prepare time input = " + Long.parseLong(etPrepareInput.getText().toString()) + " seconds");
            etPrepareInput.setText("");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("timeLeftInMillis", timeLeftInMillis);
        outState.putLong("prepareTimeLeftInMillis", prepareTimeLeftInMillis);
        outState.putLong("timeCounterUp", countUpTimer.getBase());
        outState.putLong("timeIntervalInput", timeIntervalInput);
        outState.putBoolean("countDownIsRunning", countDownIsRunning);
        outState.putBoolean("countUpIsRunning", countUpIsRunning);
        outState.putBoolean("preparationInProgress", preparationInProgress);
        outState.putLong("endTime", endTime);
        outState.putLong("prepareEndTime", prepareEndTime);
        outState.putLong("timeInput", timeInput);
        outState.putLong("prepareInput", prepareInput);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        timeLeftInMillis = savedInstanceState.getLong("timeLeftInMillis");
        prepareTimeLeftInMillis = savedInstanceState.getLong("prepareTimeLeftInMillis");
        countDownIsRunning = savedInstanceState.getBoolean("countDownIsRunning");
        countUpIsRunning = savedInstanceState.getBoolean("countUpIsRunning");
        preparationInProgress = savedInstanceState.getBoolean("preparationInProgress");
        timeIntervalInput = savedInstanceState.getLong("timeIntervalInput");
        timeInput = savedInstanceState.getLong("timeInput");
        prepareInput = savedInstanceState.getLong("prepareInput");
        timeCounterUp = savedInstanceState.getLong("timeCounterUp");
        updateButtons();

        if (preparationInProgress) {
            updateCountDownText(prepareTimeLeftInMillis);
            prepareEndTime = savedInstanceState.getLong("prepareEndTime");
            prepareTimeLeftInMillis = prepareEndTime - System.currentTimeMillis();
            startPrepareTimer();
        }

        if (countDownIsRunning) {
            updateCountDownText(timeLeftInMillis);
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
        if (prepareCountDownTimer != null) {
            prepareCountDownTimer.cancel();
        }
        try {
            NotificationManagerCompat.from(MainActivity.this).notifyAll();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}