package com.rc.healinghands;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    long TimeBuff, UpdateTime = 0L, timeInput = -1L, timeIntervalInput = 0L, timeLeftInMillis, endTime;
    int Seconds, Minutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");
        NotificationManagerCompat.from(this).cancelAll();
        initXml();
        SoundHandler.playWelcomeSound(this);
        updateCountDownText(timeLeftInMillis, false);
        updateButtons();
    }

    View.OnClickListener buttonsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == btnStartPause.getId()) {
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
    }

    private void startTimer() {
        if (getMinuteInput()) {
            setTimerDark();
            endTime = System.currentTimeMillis() + timeLeftInMillis;
            timerIsRunning = true;
            updateButtons();
            getTimeIntervalInput();
            timer = new CountDownTimer(timeInput, 1000) {

                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    updateCountDownText(timeLeftInMillis, true);
                }

                public void onFinish() {
                    String done = "Done!";
                    tvTimer.setText(done);
                    Log.i(TAG, done);
                    timerIsRunning = false;
                    setTimerNormal();
                    SoundHandler.playBigDing(MainActivity.this);
                    updateButtons();
                }
            }.start();
        }
    }

    private void pauseTimer() {
        timerIsRunning = false;
        timer.cancel();
        setTimerNormal();
        updateButtons();
    }

    private void resetTimer() {
        timeLeftInMillis = timeInput;
        updateCountDownText(timeLeftInMillis, false);
        setTimerNormal();
        updateButtons();
    }

    private void updateCountDownText(long millisUntilFinished, boolean playDing) {
        UpdateTime = TimeBuff + millisUntilFinished;
        Seconds = (int) (UpdateTime / 1000);
        Minutes = Seconds / 60;
        Seconds = Seconds % 60;
        if ((timeIntervalInput != 0) && (Minutes % timeIntervalInput == 0) && (Seconds == 0) && (Minutes != 0) && playDing) {
            SoundHandler.playSmallDing(MainActivity.this);
        }
        String displayTime = String.format(Locale.getDefault(), "%02d", Minutes) + ":" + String.format(Locale.getDefault(), "%02d", Seconds);
        tvTimer.setText(displayTime);
        Log.i(TAG, "Display Time: " + displayTime);
    }

    private void updateButtons() {
        if (timerIsRunning) {
            btnReset.setVisibility(View.GONE);
            btnStartPause.setText(R.string.stop);
            btnStartPause.setBackgroundColor(getResources().getColor(R.color.red));
        } else {
            btnStartPause.setText(R.string.start);
            btnStartPause.setBackgroundColor(getResources().getColor(R.color.green));

            if (timeLeftInMillis < 1000 && timeInput != -1) {
                btnStartPause.setVisibility(View.GONE);
            } else {
                btnStartPause.setVisibility(View.VISIBLE);
            }

            if (timeLeftInMillis < timeInput) {
                btnReset.setVisibility(View.VISIBLE);
            } else {
                btnReset.setVisibility(View.GONE);
            }
        }
    }

    private void setTimerDark() {
        llMainActivity.setBackgroundColor(getResources().getColor(R.color.black));
        tvTimer.setTextColor(getResources().getColor(R.color.white));
        tvTimer.setBackgroundColor(getResources().getColor(R.color.black));
        ivHands.setVisibility(View.GONE);
        llMinutesLine.setVisibility(View.GONE);
        llIntervalLine.setVisibility(View.GONE);
    }

    private void setTimerNormal() {
        llMainActivity.setBackgroundColor(getResources().getColor(R.color.white));
        tvTimer.setTextColor(getResources().getColor(R.color.black));
        tvTimer.setBackgroundColor(getResources().getColor(R.color.white));
        ivHands.setVisibility(View.VISIBLE);
        llMinutesLine.setVisibility(View.VISIBLE);
        llIntervalLine.setVisibility(View.VISIBLE);
    }

    private boolean getMinuteInput() {
        if (!(etMinuteInput.getText().toString().isEmpty())) {
            timeInput = Long.parseLong(etMinuteInput.getText().toString()) * 1000 * 60;
            timeLeftInMillis = timeInput;
            Log.i(TAG, "Time input = " + Long.parseLong(etMinuteInput.getText().toString()) + " minutes");
            return true;
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.input_healing_time, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM, 0, 250);
                    toast.show();
                }
            });
            return false;
        }
    }

    private void getTimeIntervalInput() {
        if (!etIntervalInput.getText().toString().isEmpty()) {
            timeIntervalInput = Long.parseLong(etIntervalInput.getText().toString());
        }
        Log.i(TAG, "Time intervals = " + timeIntervalInput + " Minutes");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("millisLeft", timeLeftInMillis);
        outState.putBoolean("timerRunning", timerIsRunning);
        outState.putLong("endTime", endTime);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        timeLeftInMillis = savedInstanceState.getLong("millisLeft");
        timerIsRunning = savedInstanceState.getBoolean("timerRunning");
        updateCountDownText(timeLeftInMillis, true);
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
        NotificationManagerCompat.from(this).notifyAll();
    }

}