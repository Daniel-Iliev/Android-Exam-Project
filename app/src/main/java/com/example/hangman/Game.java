package com.example.hangman;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class Game extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }
    public void getWord(View view){
        Ion.with(this)
                .load("https://random-words-api.vercel.app/word")
                .asJsonObject()
                .setCallback( new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        proccessWord(result);

                    }
                });
    }
    public void proccessWord(JsonObject word){
        try {
            JsonObject value = word.get("value").getAsJsonObject();
            String processedWord = value.get("word").getAsString();
            int symbolsCount = processedWord.length();
            TextView wordView = (TextView)findViewById(R.id.guessWord);
            Random r = new Random();
            int random = r.nextInt(symbolsCount);
            String newWord = null;
            for (int i=0;i<symbolsCount;i++){
                if (i==random-1){
                    newWord+=processedWord.charAt(i);
                }
                else {
                    newWord+="_";
                }
            }
            wordView.setText(newWord);
        }
        catch (Exception e){
        }
    }
    public void startGame(View view){
        getWord(view);
    }
}