package com.ziehro.tetrite;


import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Set;

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
}
