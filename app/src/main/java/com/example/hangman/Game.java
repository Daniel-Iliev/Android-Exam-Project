package com.example.hangman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Integer.parseInt;

public class Game extends AppCompatActivity {
    private String word;
    private String username;
    private List<Integer> showPositions;
    private TextView wordView;
    private  int failTries = 7;
    private int currentWins = 0;
    private int currentLoses = 0;
    private String userId;
    private User user;
    private Intent intent;
    private DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        intent = getIntent();
        reference = FirebaseDatabase.getInstance("https://androidproject-f7ca1-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");
        userId =intent.getExtras().getString("userId");
        wordView = (TextView) findViewById(R.id.guessWord);
        showPositions = new ArrayList<Integer>();
        String language = intent.getExtras().getString("language");
        if (language.equals("en")){
        getWordEn();
        }
        else if (language.equals("bg")){

        }
        username = intent.getExtras().getString("user");
        getCurrentStats();
    }
    public void getWordEn(){

        RequestQueue queue = Volley.newRequestQueue(this);
        String url1 ="https://random-words-api.vercel.app/word";
        String url2 ="https://api.wordnik.com/v4/words.json/randomWords?hasDictionaryDef=true&minCorpusCount=0&minLength=1&maxLength=1&limit=1&api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5";


        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url2,null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        processResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Game.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonRequest);
    }
    public void processResponse(JSONArray jsonArray){
        try {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String processedWord = jsonObject.optString("word").toLowerCase();
            word = processedWord;
            hideWord(processedWord);
        }
        catch (Exception e){
            return;
        }
    }
    public void hideWord(String processedWord){
        String hiddenWord = "";
        Random r = new Random();
        int random = r.nextInt(processedWord.length())+1;
        for (int i=0;i<processedWord.length();i++){
            if (i==random-1){
                hiddenWord+=processedWord.charAt(i)+" ";
            }
            else {
                hiddenWord+="_ ";
            }
        }
        //CHECK IF INIT CHAR EXISTS
//        for (int i=0;i<word.length();i++){
//            if (word.charAt(i)==word.charAt(random-1)){
//                showPositions.add(i);
//            }
//        }
        wordView.setText(hiddenWord);
        showPositions.add(random-1);
//        countTries();
    }
    public void checkChar(View view){
        EditText guessField = (EditText)findViewById(R.id.guess);
        int showPosUpdate = showPositions.size();
        for (int i=0;i<word.length();i++){
            if (word.charAt(i)==guessField.getText().toString().charAt(0)){
                if (!showPositions.contains(i)) {
                    showPositions.add(i);
                }
            }
            else{
                guessField.clearComposingText();
            }
        }
        if (showPosUpdate == showPositions.size()){
            failTries--;
        }
        updateHiddenWord();
        if(showPositions.size()==word.length()){
            win();
        }
        if (failTries<=0){
            lose();
        }

    }
//    public void countTries(){
//        List<Character> diffChars = new ArrayList<Character>();
//        for (int i = 0;i<word.length();i++){
//            if (!diffChars.contains(word.charAt(i)))
//            diffChars.add(word.charAt(i));
//            maxTries++;
//        }
//    }
    public void updateHiddenWord(){
        String hiddenWord = "";
        for (int i=0;i<word.length();i++){
            if (showPositions.contains(i)){
                hiddenWord+=word.charAt(i)+" ";
            }
            else {
                hiddenWord+="_ ";
            }
        }
        wordView.setText(hiddenWord);
    }
    public void win(){
        currentWins++;
        pushToDb();
        Toast.makeText(Game.this, "You Win", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Game.this,MainActivity.class);
        startActivity(intent);

    }
    public void lose(){
        currentLoses++;
        pushToDb();
        Toast.makeText(this, "You Lose", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
    public void pushToDb(){
        user = new User(username,currentWins,currentLoses);
        reference.child(userId).setValue(user);
    }
    public void getCurrentStats(){
        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentWins = parseInt(snapshot.child("wins").getValue().toString());
                currentLoses = parseInt(snapshot.child("loses").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}