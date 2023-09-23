package com.rc.healinghands;

import android.content.pm.ActivityInfo;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    // Layouts
    ScrollView svMainActivity;
    LinearLayout llPrepareLine, llMinutesLine, llIntervalLine, llButtons;
    // Timer views
    TextView tvTimer;
    Chronometer countUpTimer;
    // Image
    ImageView ivHands;
    // Text inputs
    EditText etPrepareInput, etMinuteInput, etIntervalInput;
    // Buttons
    Button btnStartPause, btnReset;
    // Count down timers
    CountDownTimer countDownTimer, prepareCountDownTimer;
    // Logic
    boolean countDownIsRunning = false,
            countUpIsRunning = false,
            preparationTimerRunning = false,
            firstTimeAppLaunch = true;
    long timeInput = -1L,
            timeIntervalInput = 0L,
            timeLeftInMillis,
            prepareTimeLeftInMillis,
            countDownEndTime,
            prepareEndTimeMillis,
            timeCounterUp = 0L,
            prepareInput = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");
        initXml();
    }

    private void initXml() {
        // Layouts
        svMainActivity = findViewById(R.id.svMainActivity);
        llPrepareLine = findViewById(R.id.llPrepareLine);
        llMinutesLine = findViewById(R.id.llMinutesLine);
        llIntervalLine = findViewById(R.id.llIntervalLine);
        llButtons = findViewById(R.id.llButtons);
        // Timer displays
        tvTimer = findViewById(R.id.tvTimer);
        countUpTimer = findViewById(R.id.countUpTimer);
        // Image
        ivHands = findViewById(R.id.ivHands);
        // User inputs
        etPrepareInput = findViewById(R.id.etPrepareInput);
        etMinuteInput = findViewById(R.id.etMinuteInput);
        etIntervalInput = findViewById(R.id.etIntervalInput);
        // Buttons
        btnStartPause = findViewById(R.id.btnStartPause);
        btnStartPause.setOnClickListener(buttonsListener);
        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(buttonsListener);
        // Request focus to preparation time input and show the keyboard
        etPrepareInput.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        // Try to disable notifications
        NotificationManagerCompat.from(MainActivity.this).cancelAll();

        if (!countDownIsRunning && !countUpIsRunning && !preparationTimerRunning) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (firstTimeAppLaunch) {
                SoundHandler.playWelcomeSound(this);
                firstTimeAppLaunch = false;
            }
            countUpTimer.setVisibility(View.GONE);
            tvTimer.setVisibility(View.GONE);
            enableUserInput(true);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            enableUserInput(false);
        }
        updateCountDownText(timeLeftInMillis);
        updateButtons();
        if (countUpIsRunning) {
            tvTimer.setVisibility(View.GONE);
            countUpTimer.setVisibility(View.VISIBLE);
            countUpTimer.setBase(timeCounterUp);
            countUpTimer.start();
        } else if (countDownIsRunning || preparationTimerRunning) {
            tvTimer.setVisibility(View.VISIBLE);
            countUpTimer.setVisibility(View.GONE);
        }
    }

    View.OnClickListener buttonsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == btnStartPause.getId()) {
                Utils.hideKeyboard(MainActivity.this, v);
                if (countDownIsRunning || preparationTimerRunning) {
                    pauseTimer();
                } else if (countUpIsRunning) {
                    countUpIsRunning = false;
                    countUpTimer.stop();
                    countUpTimer.setVisibility(View.GONE);
                } else {
                    startPrepareTimer();
                }
            } else if (v.getId() == btnReset.getId()) {
                resetTimer();
            }
        }
    };

    private void startPrepareTimer() {
        enableUserInput(false);
        countUpTimer.setVisibility(View.GONE);
        tvTimer.setVisibility(View.VISIBLE);
        prepareTimeLeftInMillis = getPreparationInput();
        prepareEndTimeMillis = System.currentTimeMillis() + prepareTimeLeftInMillis;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        prepareCountDownTimer = new CountDownTimer(prepareTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                prepareTimeLeftInMillis = millisUntilFinished;
                updateCountDownText(prepareTimeLeftInMillis);
            }

            @Override
            public void onFinish() {
                SoundHandler.playBigDing(MainActivity.this);
                preparationTimerRunning = false;
                updateButtons();
                startTimer();
            }
        }.start();
        preparationTimerRunning = true;
        updateButtons();
    }

    private void startTimer() {
        enableUserInput(false);
        timeLeftInMillis = getMinuteInput();
        countDownEndTime = System.currentTimeMillis() + timeLeftInMillis;
        timeIntervalInput = getTimeIntervalInput();

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
        } else if (preparationTimerRunning) {
            preparationTimerRunning = false;
            prepareCountDownTimer.cancel();
        }
        updateButtons();
    }

    private void resetTimer() {
        etPrepareInput.setText(R.string._15);
        etMinuteInput.setText(R.string._60);
        etIntervalInput.setText(R.string._3);
        prepareTimeLeftInMillis = getPreparationInput();
        timeLeftInMillis = getMinuteInput();
        updateCountDownText(prepareTimeLeftInMillis);
        updateButtons();
    }

    private void updateCountDownText(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 1000) / 60;
        int seconds = (int) (millisUntilFinished / 1000) % 60;

        // Play intervals
        if ((timeIntervalInput != 0) && (minutes % timeIntervalInput == 0) && (seconds == 0) && (minutes != 0)) {
            SoundHandler.playBigDing(MainActivity.this);
        }
        // Update the on-screen timer
        String displayTime = String.format(Locale.getDefault(), "%02d", minutes) + ":" + String.format(Locale.getDefault(), "%02d", seconds);
        tvTimer.setText(displayTime);
        Log.i(TAG, "Display Time: " + displayTime);
    }

    private void updateButtons() {
       if (countDownIsRunning || preparationTimerRunning) {
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

    private long getPreparationInput() {
        long returnVal = 0;
        if (!etPrepareInput.getText().toString().isEmpty()) {
            returnVal = Long.parseLong(etPrepareInput.getText().toString()) * 1000;
            Log.i(TAG, "Prepare time input = " + (returnVal / 1000) + " seconds");
            etPrepareInput.setText("");
        } else if (prepareTimeLeftInMillis != 0) {
            returnVal = prepareTimeLeftInMillis;
        }
        return returnVal;
    }

    private long getMinuteInput() {
        long returnVal = 0;
        if (!(etMinuteInput.getText().toString().isEmpty())) {
            returnVal = Long.parseLong(etMinuteInput.getText().toString()) * 1000 * 60;
            Log.i(TAG, "Time input = " + (returnVal / 60000) + " minutes");
            etMinuteInput.setText("");
        } else if (timeLeftInMillis != 0) {
            returnVal = timeLeftInMillis;
        }
        return returnVal;
    }

    private long getTimeIntervalInput() {
        long returnVal = 0;
        if (!etIntervalInput.getText().toString().isEmpty()) {
            returnVal = Long.parseLong(etIntervalInput.getText().toString());
            Log.i(TAG, "Time interval = " + returnVal+ " minutes");
            etIntervalInput.setText("");
        } else if (timeIntervalInput != 0) {
            returnVal = timeIntervalInput;
        }
        return returnVal;
    }

    private void enableUserInput(boolean enable) {
        etPrepareInput.setEnabled(enable);
        etIntervalInput.setEnabled(enable);
        etMinuteInput.setEnabled(enable);
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
        outState.putBoolean("preparationInProgress", preparationTimerRunning);
        outState.putBoolean("firstTimeAppLaunch", firstTimeAppLaunch);
        outState.putLong("endTime", countDownEndTime);
        outState.putLong("prepareEndTime", prepareEndTimeMillis);
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
        preparationTimerRunning = savedInstanceState.getBoolean("preparationInProgress");
        firstTimeAppLaunch = savedInstanceState.getBoolean("firstTimeAppLaunch");
        timeIntervalInput = savedInstanceState.getLong("timeIntervalInput");
        timeInput = savedInstanceState.getLong("timeInput");
        prepareInput = savedInstanceState.getLong("prepareInput");
        timeCounterUp = savedInstanceState.getLong("timeCounterUp");
        updateButtons();

        if (preparationTimerRunning) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            prepareEndTimeMillis = savedInstanceState.getLong("prepareEndTime");
            prepareTimeLeftInMillis = prepareEndTimeMillis - System.currentTimeMillis();
            updateCountDownText(prepareTimeLeftInMillis);
            startPrepareTimer();
        }

        if (countDownIsRunning) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            countDownEndTime = savedInstanceState.getLong("endTime");
            timeLeftInMillis = countDownEndTime - System.currentTimeMillis();
            updateCountDownText(timeLeftInMillis);
            startTimer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (prepareCountDownTimer != null) {
            prepareCountDownTimer.cancel();
        }
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