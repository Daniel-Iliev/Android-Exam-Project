package com.example.hangman;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;

public class Game extends AppCompatActivity {
    private String word;
    private String username;
    private ArrayList<Integer> showPositions;
    private ArrayList<Character> usedChars;
    private TextView wordView;
    private int failTries = 7;
    private int currentWins = 0;
    private int currentLoses = 0;
    private String userId;
    private User user;
    private Intent intent;
    private EditText guessField;
    private DatabaseReference reference;
    private TextView usedCharsField;
    private String difficulty;
    private TextView remainingTries;
    private TextView wordMsg;
    private Button check;
    private TextView triesText;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getSupportActionBar().hide();
        guessField = findViewById(R.id.guess);
        intent = getIntent();
        reference = FirebaseDatabase.getInstance("https://hangman-1663345481301-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");
        userId =intent.getExtras().getString("userId");
        wordView = findViewById(R.id.guessWord);
        showPositions = new ArrayList<Integer>();
        usedChars = new ArrayList<Character>();
        usedCharsField = findViewById(R.id.usedChars);
        remainingTries = findViewById(R.id.remainingTries);
        remainingTries.setText(String.valueOf(failTries));
        check = findViewById(R.id.check);
        triesText = findViewById(R.id.triesText);
        difficulty = intent.getExtras().getString("difficulty");
        if (savedInstanceState==null) {
            if (difficulty.equals("easy")) {
                getWordEasy();
            } else if (difficulty.equals("hard")) {
                getWordHard();
            }
        }
        else {
            word = savedInstanceState.getString("word");
            showPositions = savedInstanceState.getIntegerArrayList("showPositions");
            String usedCharsTemp = savedInstanceState.getString("usedChars");
            for (char c : usedCharsTemp.toCharArray()) {
                usedChars.add(c);
            }
            failTries = savedInstanceState.getInt("failTries");
            updateUsedChars();
            updateHiddenWord();
            remainingTries.setText(String.valueOf(failTries));
        }
        username = intent.getExtras().getString("user");
        getCurrentStats();
    }
    public interface AwaitCallBack {
        void onSuccess();
    }
    public void getWord(final AwaitCallBack callBack){

        RequestQueue queue = Volley.newRequestQueue(this);


        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        processResponse(response);
                        callBack.onSuccess();
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
            word = jsonArray.getString(0);
        }
        catch (Exception e){
            return;
        }
    }
    public void hideWord(){
        Random r = new Random();
        int random = r.nextInt(word.length());
        usedChars.add(word.charAt(random));
        updateUsedChars();
        for (int i=0;i<word.length();i++){
            if (word.charAt(i)==word.charAt(random)){
                showPositions.add(i);
            }
        }
        updateHiddenWord();
    }
    public void checkChar(View view){
        if (guessField.getText()!=null&&guessField.getText().toString().length()!=0) {
            if (usedChars.contains(guessField.getText().toString().charAt(0))) {
                Toast.makeText(this, "You already tried that letter!", Toast.LENGTH_SHORT).show();
            } else {
                int showPosUpdate = showPositions.size();
                for (int i = 0; i < word.length(); i++) {
                    if (word.charAt(i) == guessField.getText().toString().charAt(0)) {
                        if (!showPositions.contains(i)) {
                            showPositions.add(i);
                        }
                    }
                }
                usedChars.add(guessField.getText().toString().charAt(0));
                guessField.setText("");
                if (showPosUpdate == showPositions.size()) {
                    failTries--;
                }
                updateHiddenWord();
                if (showPositions.size() == word.length()) {
                    win(view);
                }
                if (failTries <= 0) {
                    lose(view);
                }
                remainingTries.setText(String.valueOf(failTries));
                updateUsedChars();
            }
        }
        else {
            Toast.makeText(this, "Add a character to check!", Toast.LENGTH_SHORT).show();
        }
    }
    public void updateUsedChars(){
        String text = "";
        for (int i = 0 ;i<usedChars.size();i++) {
            text+=usedChars.get(i) + " ";
        }
        usedCharsField.setText(text);
    }
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
    public void win(View view){
        currentWins++;
        pushToDb();
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.win_popup, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        hideKeyboard(this);
        wordMsg  = popupView.findViewById(R.id.word);
        wordMsg.setText(word);
        usedChars = new ArrayList<>();
        updateUsedChars();
        check.setVisibility(View.INVISIBLE);
        wordView.setVisibility(View.INVISIBLE);
        guessField.setVisibility(View.INVISIBLE);
        triesText.setVisibility(View.INVISIBLE);
        remainingTries.setVisibility(View.INVISIBLE);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.schedule(this::goHomePage, 3, TimeUnit.SECONDS);
    }
    public void lose(View view){
        currentLoses++;
        pushToDb();
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.lose_popup,
                (ViewGroup) Game.this.findViewById(R.id.root));

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        hideKeyboard(this);
        wordMsg  = popupView.findViewById(R.id.word);
        wordMsg.setText(word);
        usedChars = new ArrayList<>();
        updateUsedChars();
        check.setVisibility(View.INVISIBLE);
        wordView.setVisibility(View.INVISIBLE);
        guessField.setVisibility(View.INVISIBLE);
        triesText.setVisibility(View.INVISIBLE);
        remainingTries.setVisibility(View.INVISIBLE);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.schedule(this::goHomePage, 3, TimeUnit.SECONDS);
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public void goHomePage(){
        Intent intent = new Intent(Game.this,MainActivity.class);
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
                if (snapshot.child("name").exists()) {
                    currentWins = parseInt(snapshot.child("wins").getValue().toString());
                    currentLoses = parseInt(snapshot.child("loses").getValue().toString());
                }
                else {
                    currentLoses = 0;
                    currentWins = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public String getRandomNumber(int min, int max) {
        Random random = new Random();
        return String.valueOf(random.nextInt(max - min) + min);
    }
    public void getWordEasy(){
        url ="https://random-word-api.herokuapp.com/word?length="+getRandomNumber(3,7);
        getWord(new AwaitCallBack() {
            @Override
            public void onSuccess() {
                    showPositions.clear();
                    hideWord();
            }
        });
    }

    public void getWordHard(){
        url ="https://random-word-api.herokuapp.com/word?length="+getRandomNumber(7,15);
        getWord(new AwaitCallBack() {
            @Override
            public void onSuccess() {
                    showPositions.clear();
                    hideWord();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList("showPositions",showPositions);
        StringBuilder sb = new StringBuilder();
        for (Character ch: usedChars) {
            sb.append(ch);
        }
        String usedCharsString = sb.toString();
        outState.putString("usedChars",usedCharsString);
        outState.putString("word",word);
        outState.putInt("failTries",failTries);
    }
}