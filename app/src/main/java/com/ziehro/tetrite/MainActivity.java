package com.ziehro.tetrite;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Set;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity{

    private LetterTetrisView gameView;
    private Button moveLeftButton;
    private Button moveRightButton;
    private Button rotateButton;
    private Set<String> dictionary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);
        moveLeftButton = findViewById(R.id.moveLeftButton);
        moveRightButton = findViewById(R.id.moveRightButton);
        rotateButton = findViewById(R.id.rotateButton);

        setButtonListeners();

    }


    private void setButtonListeners() {
        moveLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameView.move(-1, 0);
            }
        });

        moveRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameView.move(1, 0);
            }
        });

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameView.rotate();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.speed_settings:
                Intent speedSettingsIntent = new Intent(MainActivity.this, SpeedSettingsActivity.class);
                startActivity(speedSettingsIntent);
                return true;
            case R.id.help:
                // Launch help activity or show dialog
                return true;
            case R.id.restart_game:
                restartGame();
                return true;
            case R.id.pause:
                LetterTetrisView tetrisGameView = findViewById(R.id.gameView);
                if (!tetrisGameView.isPaused.get()) {
                    tetrisGameView.pauseGame();
                    item.setTitle("Resume");
                } else {
                    tetrisGameView.resumeGame();
                    item.setTitle("Pause");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void restartGame() {
        LetterTetrisView tetrisGameView = findViewById(R.id.gameView);
        tetrisGameView.stopGame();
        tetrisGameView.reset();
        tetrisGameView.invalidate();
        tetrisGameView.startGame();
    }
}
