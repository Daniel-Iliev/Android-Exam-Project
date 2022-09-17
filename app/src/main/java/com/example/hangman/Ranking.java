package com.example.hangman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Ranking extends AppCompatActivity {
    private ListView listView;
    private DatabaseReference reference;
    private ArrayList<String> list;
    private ArrayAdapter<String> adapter;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        getSupportActionBar().hide();
        user = new User();
        listView = findViewById(R.id.rankingList);
        reference = FirebaseDatabase.getInstance("https://hangman-1663345481301-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");
        list = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this,R.layout.user_info,R.id.userInfo,list);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds:snapshot.getChildren()){
                        user = ds.getValue(User.class);
                        list.add(user.getName() + "\nWins: " + user.getWins() + "\nLoses: " + user.getLoses());
                    }
                    Collections.sort(list);
                    listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}