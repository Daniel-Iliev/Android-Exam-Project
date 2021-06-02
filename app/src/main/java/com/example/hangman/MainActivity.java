package com.example.hangman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {
    private String difficulty = "easy";
    private AppCompatButton signOutButton;
    private SignInButton signInButton;
    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient signInClient;
    private Intent intent;
    private TextView nameField;
    private AppCompatButton startBtn;
    private Intent signInIntent;
    private String userShowName;
    private DatabaseReference reference;
    private String userId;
    private User user;
    private AppCompatButton easyBtn;
    private AppCompatButton hardBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        reference = FirebaseDatabase.getInstance("https://androidproject-f7ca1-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");
        intent = new Intent(this, Game.class);
        user = new User();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);
        signInIntent = signInClient.getSignInIntent();
        signInButton = findViewById(R.id.sign_in_button);
        signOutButton = findViewById(R.id.singOut);
        signInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                signIn(view);
            }
        });
        nameField = findViewById(R.id.name);
        startBtn = findViewById(R.id.start);
        easyBtn = findViewById(R.id.easy);
        hardBtn = findViewById(R.id.hard);

    }
    @Override
    public void onStart() {
        super.onStart();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            userShowName = account.getDisplayName();
            if (userShowName.isEmpty()){
                userShowName = account.getGivenName();
                if (userShowName.isEmpty()){
                    userShowName = account.getEmail();
                }
            }
            userId = account.getId();
            nameField.setText(userShowName);
            nameField.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.INVISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
            intent.putExtra("userId",userId);
            intent.putExtra("user",userShowName);
            getCurrentStats();
        }
        else {
            userShowName = "";
            nameField.setText(userShowName);
            nameField.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.INVISIBLE);
            signInButton.setVisibility(View.VISIBLE);
            startBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Toast.makeText(MainActivity.this, "User not logged in. Please log in first.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public void startGame(View view){
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }
    public void easyDiff(View view){
        difficulty = "easy";
        easyBtn.setTextColor(ContextCompat.getColor(this,R.color.DarkCyan));
        hardBtn.setTextColor(ContextCompat.getColor(this,R.color.black));
    }
    public void hardDiff(View view){
        difficulty = "hard";
        hardBtn.setTextColor(ContextCompat.getColor(this,R.color.DarkCyan));
        easyBtn.setTextColor(ContextCompat.getColor(this,R.color.black));
    }
    public void signIn(View view) {
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            userShowName = account.getDisplayName();
            if (userShowName.isEmpty()){
                userShowName = account.getGivenName();
                if (userShowName.isEmpty()){
                    userShowName = account.getEmail();
                }
            }
            userId = account.getId();
            nameField.setText(userShowName);
            nameField.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.INVISIBLE);
            startBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startGame(v);
                }
            });
            intent.putExtra("userId",userId);
            intent.putExtra("user",userShowName);
            getCurrentStats();
        } catch (ApiException e) {
            Toast.makeText(this, e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }
    public void signOut(View view){
        signInClient.signOut();
        nameField.setText("");
        nameField.setVisibility(View.INVISIBLE);
        signInButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.INVISIBLE);
        startBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(MainActivity.this, "User not logged in. Please log in first.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void showRanking(View view){
        Intent intent = new Intent(this,Ranking.class);
        startActivity(intent);
    }
    public void getCurrentStats(){
        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child("name").exists()) {
                    user = new User(userShowName,0,0);
                    reference.child(userId).setValue(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}

