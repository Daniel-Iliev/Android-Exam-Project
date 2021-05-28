package com.example.hangman;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private String language = "bg";
    private FirebaseAuth mAuth;
    SignInButton signInButton;
    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "GoogleSignIn";
    GoogleSignInClient signInClient;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this, Game.class);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                signIn();
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        intent.putExtra("userId",account.getId());
        intent.putExtra("user",account.getDisplayName());
        if(account != null){
            signInButton.setVisibility(View.INVISIBLE);
        }
        else {
            AppCompatButton start = findViewById(R.id.start);
            start.setOnClickListener(new View.OnClickListener()
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
        intent.putExtra("language", language);
        if (language.equals("en")) {
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
        }
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
    public void signIn() {
        Intent signInIntent = signInClient.getSignInIntent();
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
            TextView name = findViewById(R.id.name);
            name.setText(account.getDisplayName());
        } catch (ApiException e) {
            Toast.makeText(this, e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }
}
