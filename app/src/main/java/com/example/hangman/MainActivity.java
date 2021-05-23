package com.example.hangman;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private String language = "bg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void startGame(View view){

    }
    public void setLangBg(View view){
        language = "bg";
        TextView heading = (TextView)findViewById(R.id.heading);
        heading.setText("Бесеница");
        Button start = findViewById(R.id.start);
        start.setText("СТАРТ");
    }
    public void setLangEn(View view){
        language = "en";
        TextView heading = (TextView)findViewById(R.id.heading);
        heading.setText("Hangman");
        Button start = findViewById(R.id.start);
        start.setText("START");
    }
}
