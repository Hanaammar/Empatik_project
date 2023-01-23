package com.empatik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText txtIpAddress;
    private Button btnBack;
//this page to update the IP. there is just one button "back" and text field.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        txtIpAddress = findViewById(R.id.txtIpAddress);
        btnBack = findViewById(R.id.btnBackSettings);

        btnBack.setOnClickListener(view -> {
            SharedPreferences.Editor editor = getSharedPreferences("MyPref", MODE_PRIVATE).edit();
            editor.putString("ipAddress", txtIpAddress.getText().toString())
                    .apply();
            SettingsActivity.this.startActivity(
                    new Intent(SettingsActivity.this, MainActivity.class));
        });
    }
}