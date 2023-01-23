package com.empatik;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class CaptureActivity extends AppCompatActivity {
//This activity will shown to the user when the user ask to know his mood
    private StorageReference mStorageRef;
    private Uri videoURI;
    private Button btnCapture, btnBack, btnDelete;
    private VideoView videoView;
    private RequestQueue requestQueue;
    private String ipAddress = null; // Default IP Address
    private String url = "http://" + ipAddress + "/detectEmotion/";

//In this function we create the capture activity page
//We define every field by id and then we set the btncapture and the btnDelete as disabled
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        btnCapture = findViewById(R.id.btnCapture);
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);
        videoView = findViewById(R.id.videoView);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        btnCapture.setEnabled(false);
        btnDelete.setEnabled(false);
//Here we check the camera permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 111);
        else
            btnCapture.setEnabled(true);

        ipAddress = getApplicationContext().getSharedPreferences("MyPref", 0)
                .getString("ipAddress", null);

        btnCapture.setOnClickListener(view -> {
            if (ipAddress == null) {
                Toast.makeText(getApplicationContext(), "Kindly enter a valid IP address first...",
                        Toast.LENGTH_LONG).show();
                return;
                //Check that the taken video is good
            } else if (videoView == null && btnCapture.getText().toString().equals("Identify Mood")) {
                Toast.makeText(getApplicationContext(), "Kindly record a new video...",
                        Toast.LENGTH_LONG).show();
            } else if (btnCapture.getText().toString().equals("Identify Mood")) {
                url = "http://" + ipAddress + "/detectEmotion/";
                Toast.makeText(getApplicationContext(), "Identifying mood...",
                        Toast.LENGTH_LONG).show();
                Uri file = videoURI;
                String videoName = String.valueOf(Calendar.getInstance().getTimeInMillis()).concat(".mp4");
                //Save the vedio in the shared preference
                final StorageReference storageReference = mStorageRef.child("videos/" + videoName);
// Get response from server and show the response to the user
                storageReference.putFile(file)
                        .addOnSuccessListener(taskSnapshot -> {
                            StringRequest stringRequest = new StringRequest(Request.Method.GET, createURL(videoName),
                                    response -> {
                                        Intent myIntent = new Intent(this, ResponseActivity.class);
                                        switch (response.toLowerCase()) {
                                            case "angry":
                                                myIntent.putExtra("response", "angry");
                                                break;
                                            case "disgust":
                                                myIntent.putExtra("response", "disgust");
                                                break;
                                            case "fear":
                                                myIntent.putExtra("response", "fear");
                                                break;
                                            case "happy":
                                                myIntent.putExtra("response", "happy");
                                                break;
                                            case "sad":
                                                myIntent.putExtra("response", "sad");
                                                break;
                                            case "surprise":
                                                myIntent.putExtra("response", "surprise");
                                                break;
                                        }
                                        startActivity(myIntent);
                                    },
                                    error -> Toast.makeText(getApplicationContext(), "Error getting response from server...",
                                            Toast.LENGTH_LONG).show());
                            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                    300000,
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            requestQueue = Volley.newRequestQueue(CaptureActivity.this);
                            requestQueue.add(stringRequest);
                        })
                        .addOnFailureListener(exception ->
                                Toast.makeText(getApplicationContext(), "Error uploading file...",
                                        Toast.LENGTH_LONG).show());
                return;
            }
            videoView = findViewById(R.id.videoView);
            startActivityForResult(new Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    .putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5), 1111);
        });

        btnDelete.setOnClickListener(view -> {
            videoView = null;
            Toast.makeText(getApplicationContext(), "Video removed",
                    Toast.LENGTH_LONG).show();
        });

        btnBack.setOnClickListener(view ->
        {
//            startActivity(
//                    new Intent(this, ResponseActivity.class)
//                            .putExtra("response", "angry"));
            CaptureActivity.this.startActivity(
                    new Intent(CaptureActivity.this, MainActivity.class));
        });
    }
//set the buttons
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1111) {
            videoView.setVideoURI(data != null ? data.getData() : null);
            videoView.start();
            btnDelete.setEnabled(true);
            btnCapture.setText(R.string.identifyMood);
            videoURI = data != null ? data.getData() : null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            btnCapture.setEnabled(true);
    }

    public String createURL(String videoUrl) {
        return url + videoUrl;
    }
}