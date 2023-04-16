package com.ziehro.tetrite;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SpeedSettingsActivity extends AppCompatActivity {
    private static final String SHARED_PREFERENCES_NAME = "tetris_preferences";
    private static final String KEY_GAME_SPEED = "game_speed";

    private TextView speedValueTextView;
    private SeekBar speedSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_settings);

        speedValueTextView = findViewById(R.id.speed_value);
        speedSeekBar = findViewById(R.id.speed_seekbar);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int gameSpeed = sharedPreferences.getInt(KEY_GAME_SPEED, 500);

        speedValueTextView.setText(getString(R.string.app_name, gameSpeed));
        speedSeekBar.setProgress(gameSpeed);

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedValueTextView.setText(getString(R.string.speed_value, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int newSpeed = seekBar.getProgress();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(KEY_GAME_SPEED, newSpeed);
                editor.apply();
            }
        });
    }
}
