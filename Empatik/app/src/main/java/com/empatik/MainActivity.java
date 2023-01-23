package com.empatik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button btnIdentify, btnMoodHistory;
    ImageView imgBtnSettings;
//We create the home page, in this page we have 2 buttons, and setting icon. by clicking on each one of them we translate to another page.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnIdentify = findViewById(R.id.btnIdentify);
        btnMoodHistory = findViewById(R.id.btnMoodHistory);
        imgBtnSettings = findViewById(R.id.imgBtnSettings);

        btnIdentify.setOnClickListener(view -> MainActivity.this.startActivity(
                new Intent(MainActivity.this, CaptureActivity.class)));

        btnMoodHistory.setOnClickListener(view -> {
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
            Set<String> moodHistory = pref.getStringSet("moodHistory", null); // getting moodHistory
            if (moodHistory == null)
                Toast.makeText(getApplicationContext(), "Your mood history is currently empty",
                        Toast.LENGTH_LONG).show();
            else
                MainActivity.this.startActivity(
                        new Intent(MainActivity.this, MoodHistoryActivity.class));
        });

        imgBtnSettings.setOnClickListener(view -> MainActivity.this.startActivity(
                new Intent(MainActivity.this, SettingsActivity.class)));
    }
}