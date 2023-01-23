package com.empatik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.empatik.models.MoodHistory;
import com.empatik.models.SongModel;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//We get to this page after we get the answer, in this page we have some buttons to control the song that is played and to control the list of songs (remove song).
//from this page we
public class ResponseActivity extends AppCompatActivity implements SelectListener {

    private RecyclerView recyclerView;
    private List<SongModel> songList;
    private List<SongModel> markedSongList;
    private CustomAdapter customAdapter;
    private TextView txtSentence, txtMarkedSongs;
    private Button btnPlay, btnPause, btnView, btnUnmark, btnBack;
    private MediaPlayer mediaPlayer;
    private List<String> sad, fear, happy, angry, disgust, surprise;
    private String response;
    private Set<String> songPreference;
    private ConstraintLayout constraintLayout;
    private Map<String, String> songMapping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        sad = Arrays.asList(getString(R.string.sad1),
                getString(R.string.sad2), getString(R.string.sad3), getString(R.string.sad4),
                getString(R.string.sad5), getString(R.string.sad6), getString(R.string.sad7));
        fear = Arrays.asList(getString(R.string.fear1),
                getString(R.string.fear2), getString(R.string.fear3), getString(R.string.fear4),
                getString(R.string.fear5), getString(R.string.fear6), getString(R.string.fear7));
        happy = Arrays.asList(getString(R.string.happy1),
                getString(R.string.happy2), getString(R.string.happy3), getString(R.string.happy4),
                getString(R.string.happy5), getString(R.string.happy6), getString(R.string.happy7));
        angry = Arrays.asList(getString(R.string.angry1),
                getString(R.string.angry2), getString(R.string.angry3), getString(R.string.angry4),
                getString(R.string.angry5), getString(R.string.angry6), getString(R.string.angry7));
        disgust = Arrays.asList(getString(R.string.disgust1),
                getString(R.string.disgust2), getString(R.string.disgust3), getString(R.string.disgust4),
                getString(R.string.disgust5), getString(R.string.disgust6), getString(R.string.disgust7));
        surprise = Arrays.asList(getString(R.string.surprise1),
                getString(R.string.surprise2), getString(R.string.surprise3), getString(R.string.surprise4),
                getString(R.string.surprise5), getString(R.string.surprise6), getString(R.string.surprise7));

        Bundle b = getIntent().getExtras();
        response = null;
        if (b != null)
            response = b.getString("response");

        txtSentence = findViewById(R.id.txtSentence);
        txtMarkedSongs = findViewById(R.id.txtMarkedSongs);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnView = findViewById(R.id.btnView);
        btnUnmark = findViewById(R.id.btnUnmark);
        btnBack = findViewById(R.id.btnBackResponse);
        constraintLayout = findViewById(R.id.constraint_layout_response);
        songMapping();

        AtomicInteger randomNumber = new AtomicInteger(getRandomNumber(1, 3));

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        Set<String> moodHistory = pref.getStringSet("moodHistory", null); // getting moodHistory
//here we add the new mood to the list of history in the SharedPreference
        songPreference = pref.getStringSet("songPref", null); // getting Song Preferences.
        SharedPreferences.Editor editor = getSharedPreferences("MyPref", MODE_PRIVATE).edit();
        if (moodHistory == null) {
            moodHistory = new HashSet<>();
        }
        moodHistory.add(LocalDateTime.now().toString() + " : " + titleCase(response));
        editor.putStringSet("moodHistory", moodHistory)
                .apply();

        if (songPreference != null && songPreference.contains(songMapping.get(response + 1)) && songPreference.contains(songMapping.get(response + 2))) {
            Toast.makeText(getApplicationContext(), "All songs in this category are marked not to play a second time.",
                    Toast.LENGTH_LONG).show();
            randomNumber.set(-1);
        } else if (songPreference != null && songPreference.contains(songMapping.get(response + 1)))
            randomNumber.set(2);
        else if (songPreference != null && songPreference.contains(songMapping.get(response + 2)))
            randomNumber.set(1);
//Here we play a random song
        playSong(response, randomNumber, true);

        btnPlay.setOnClickListener(view -> mediaPlayer.start());

        btnPause.setOnClickListener(view -> mediaPlayer.pause());

        btnView.setOnClickListener(view -> {
            songPreference = getApplicationContext().getSharedPreferences("MyPref", 0)
                    .getStringSet("songPref", null);
//Check it there is marked songs and if so, then we will change the current page in order to match it to display the marked songs.
            if (btnView.getText().toString().matches("View")) {
                if (songPreference == null) {
                    Snackbar.make(constraintLayout, "Marked song list is currently empty!", Snackbar.LENGTH_LONG).show();
                    return;
                }
                btnView.setText(R.string.unview);
                txtMarkedSongs.setVisibility(View.VISIBLE);
                markedSongList = new ArrayList<>();

                TreeSet<String> songPreferenceCopy = new TreeSet<>(songPreference);
                songPreferenceCopy.forEach(song -> markedSongList.add(new SongModel(song)));

                customAdapter = new CustomAdapter(this, markedSongList, this);
                recyclerView.setAdapter(customAdapter);
            } else {
                btnView.setText(R.string.view);
                txtMarkedSongs.setVisibility(View.INVISIBLE);
                displayItems(response);
                customAdapter = new CustomAdapter(this, songList, this);
                recyclerView.setAdapter(customAdapter);
            }
            customAdapter.notifyDataSetChanged();
        });
//Back btn - return to the home page
        btnBack.setOnClickListener(view -> {
            if (mediaPlayer != null)
                mediaPlayer.stop();
            ResponseActivity.this.startActivity(
                    new Intent(ResponseActivity.this, MainActivity.class));
        });

        // Reset all songs marked.
        btnUnmark.setOnClickListener(view -> {
            songPreference = null;
            editor.putStringSet("songPref", null)
                    .apply();
            randomNumber.set(getRandomNumber(1, 3));
            if (mediaPlayer != null)
                mediaPlayer.stop();
            playSong(response, randomNumber, false);
            Toast.makeText(getApplicationContext(), "All songs unmarked successfully!",
                    Toast.LENGTH_LONG).show();
            displayItems(response);
        });

        displayItems(response);

        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
    }
//mapping the songs to each specific mood
    private void songMapping() {
        songMapping = new HashMap<>();
        songMapping.put("angry1", "1. Take My Breath Away - Berlin");
        songMapping.put("angry2", "2. Control - Zoe Wees");
        songMapping.put("disgust1", "1. Lovely - Billie Eilish and Khalid");
        songMapping.put("disgust2", "2. Let It Go - Idina Menzel");
        songMapping.put("fear1", "1. Everything I Wanted - Billie Eilish");
        songMapping.put("fear2", "2. I Will Make It Out - Crispin Earl and The Veer Union");
        songMapping.put("happy1", "1. Best Day of My Life - American Authors");
        songMapping.put("happy2", "2. Three Little Birds - Bob Marley and the Wailers");
        songMapping.put("sad1", "1. Don't Worry Be Happy - Bob Marley");
        songMapping.put("sad2", "2. It's Time - Imagine Dragons");
        songMapping.put("surprise1", "1. Happy - Pharrell Williams");
        songMapping.put("surprise2", "2. Good Vibrations - The Beach Boys");
    }

    private void displayItems(String response) {
        recyclerView = findViewById(R.id.recyclerViewSongs);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        songList = new ArrayList<>();

        switch (response) {
            case "angry":
                if (songPreference != null && !songPreference.contains(songMapping.get("angry1")))
                    songList.add(new SongModel(songMapping.get("angry1")));
                if (songPreference != null && !songPreference.contains(songMapping.get("angry2")))
                    songList.add(new SongModel(songMapping.get("angry2")));
                if (songPreference == null) {
                    songList.add(new SongModel(songMapping.get("angry1")));
                    songList.add(new SongModel(songMapping.get("angry2")));
                }
                break;
            case "disgust":
                if (songPreference != null && !songPreference.contains(songMapping.get("disgust1")))
                    songList.add(new SongModel(songMapping.get("disgust1")));
                if (songPreference != null && !songPreference.contains(songMapping.get("disgust2")))
                    songList.add(new SongModel(songMapping.get("disgust2")));
                if (songPreference == null) {
                    songList.add(new SongModel(songMapping.get("disgust1")));
                    songList.add(new SongModel(songMapping.get("disgust2")));
                }
                break;
            case "fear":
                if (songPreference != null && !songPreference.contains(songMapping.get("fear1")))
                    songList.add(new SongModel(songMapping.get("fear1")));
                if (songPreference != null && !songPreference.contains(songMapping.get("fear2")))
                    songList.add(new SongModel(songMapping.get("fear2")));
                if (songPreference == null) {
                    songList.add(new SongModel(songMapping.get("fear1")));
                    songList.add(new SongModel(songMapping.get("fear2")));
                }
                break;
            case "happy":
                if (songPreference != null && !songPreference.contains(songMapping.get("happy1")))
                    songList.add(new SongModel(songMapping.get("happy1")));
                if (songPreference != null && !songPreference.contains(songMapping.get("happy2")))
                    songList.add(new SongModel(songMapping.get("happy2")));
                if (songPreference == null) {
                    songList.add(new SongModel(songMapping.get("happy1")));
                    songList.add(new SongModel(songMapping.get("happy2")));
                }
                break;
            case "sad":
                if (songPreference != null && !songPreference.contains(songMapping.get("sad1")))
                    songList.add(new SongModel(songMapping.get("sad1")));
                if (songPreference != null && !songPreference.contains(songMapping.get("sad2")))
                    songList.add(new SongModel(songMapping.get("sad2")));
                if (songPreference == null) {
                    songList.add(new SongModel(songMapping.get("sad1")));
                    songList.add(new SongModel(songMapping.get("sad2")));
                }
                break;
            case "surprise":
                if (songPreference != null && !songPreference.contains(songMapping.get("surprise1")))
                    songList.add(new SongModel(songMapping.get("surprise1")));
                if (songPreference != null && !songPreference.contains(songMapping.get("surprise2")))
                    songList.add(new SongModel(songMapping.get("surprise2")));
                if (songPreference == null) {
                    songList.add(new SongModel(songMapping.get("surprise1")));
                    songList.add(new SongModel(songMapping.get("surprise2")));
                }
                break;
        }

        customAdapter = new CustomAdapter(this, songList, this);
        recyclerView.setAdapter(customAdapter);
    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public String titleCase(String text) {
        if (text == null)
            return null;

        Pattern pattern = Pattern.compile("\\b([a-zÀ-ÖØ-öø-ÿ])([\\w]*)");
        Matcher matcher = pattern.matcher(text.toLowerCase());

        StringBuffer buffer = new StringBuffer();

        while (matcher.find())
            matcher.appendReplacement(buffer, matcher.group(1).toUpperCase() + matcher.group(2));

        return matcher.appendTail(buffer).toString();
    }

    public void playSong(String response, AtomicInteger randomNumber, Boolean changeSentence) {
        if (response != null)
            switch (response) {
                case "angry":
                    if (changeSentence)
                        txtSentence.setText(angry.get((int) (Math.random() * 7)));
                    if (randomNumber.get() == 1)
                        mediaPlayer = MediaPlayer.create(this, R.raw.angry1);
                    else if (randomNumber.get() == 2)
                        mediaPlayer = MediaPlayer.create(this, R.raw.angry2);
                    if (randomNumber.get() != -1)
                        mediaPlayer.start();
                    break;
                case "disgust":
                    if (changeSentence)
                        txtSentence.setText(disgust.get((int) (Math.random() * 7)));
                    if (randomNumber.get() == 1)
                        mediaPlayer = MediaPlayer.create(this, R.raw.disgust1);
                    else if (randomNumber.get() == 2)
                        mediaPlayer = MediaPlayer.create(this, R.raw.disgust2);
                    if (randomNumber.get() != -1)
                        mediaPlayer.start();
                    break;
                case "fear":
                    if (changeSentence)
                        txtSentence.setText(fear.get((int) (Math.random() * 7)));
                    if (randomNumber.get() == 1)
                        mediaPlayer = MediaPlayer.create(this, R.raw.fear1);
                    else if (randomNumber.get() == 2)
                        mediaPlayer = MediaPlayer.create(this, R.raw.fear2);
                    if (randomNumber.get() != -1)
                        mediaPlayer.start();
                    break;
                case "happy":
                    if (changeSentence)
                        txtSentence.setText(happy.get((int) (Math.random() * 7)));
                    if (randomNumber.get() == 1)
                        mediaPlayer = MediaPlayer.create(this, R.raw.happy1);
                    else if (randomNumber.get() == 2)
                        mediaPlayer = MediaPlayer.create(this, R.raw.happy2);
                    if (randomNumber.get() != -1)
                        mediaPlayer.start();
                    break;
                case "sad":
                    if (changeSentence)
                        txtSentence.setText(sad.get((int) (Math.random() * 7)));
                    if (randomNumber.get() == 1)
                        mediaPlayer = MediaPlayer.create(this, R.raw.sad1);
                    else if (randomNumber.get() == 2)
                        mediaPlayer = MediaPlayer.create(this, R.raw.sad2);
                    if (randomNumber.get() != -1)
                        mediaPlayer.start();
                    break;
                case "surprise":
                    if (changeSentence)
                        txtSentence.setText(surprise.get((int) (Math.random() * 7)));
                    if (randomNumber.get() == 1)
                        mediaPlayer = MediaPlayer.create(this, R.raw.surprise1);
                    else if (randomNumber.get() == 2)
                        mediaPlayer = MediaPlayer.create(this, R.raw.surprise2);
                    if (randomNumber.get() != -1)
                        mediaPlayer.start();
                    break;
            }
    }

    @Override
    public void onItemClicked(SongModel song) {
        if (songPreference != null)
            if (songPreference.contains(songMapping.get(response
                    + Character.getNumericValue(song.getSongName().charAt(0))))) {
                Toast.makeText(getApplicationContext(), "This song is marked not to play!",
                        Toast.LENGTH_LONG).show();
                return;
            }

        mediaPlayer.stop();
        playSong(response, new AtomicInteger(Character.getNumericValue(song.getSongName().charAt(0))),
                false);
    }

    @Override
    public void onItemClicked(MoodHistory moodHistory) {

    }

    ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            SharedPreferences.Editor editor = getSharedPreferences("MyPref", MODE_PRIVATE).edit();

            // Marked Songs are showing. Unmark them here.
            if (txtMarkedSongs.getVisibility() == View.VISIBLE) {
                Snackbar.make(constraintLayout, "Song unmarked successfully!", Snackbar.LENGTH_LONG).show();
                songPreference.remove(markedSongList.get(viewHolder.getAdapterPosition()).getSongName());

                if (songPreference.isEmpty())
                    editor.putStringSet("songPref", null).apply();
                else
                    editor.putStringSet("songPref", songPreference).apply();

                markedSongList.remove(viewHolder.getAdapterPosition());
                customAdapter.notifyDataSetChanged();
                return;
            }

            Snackbar.make(constraintLayout, "Song marked not to play!", Snackbar.LENGTH_LONG).show();
            if (songPreference == null) {
                Set<String> markedSongHistorySet = new HashSet<>();
                markedSongHistorySet.add(songList.get(viewHolder.getAdapterPosition()).getSongName());
                editor.putStringSet("songPref", markedSongHistorySet).apply();
                songPreference = getApplicationContext().getSharedPreferences("MyPref", 0)
                        .getStringSet("songPref", null);
            } else {
                songPreference.add(songList.get(viewHolder.getAdapterPosition()).getSongName());
                editor.putStringSet("songPref", songPreference).apply();
            }

            songList.remove(viewHolder.getAdapterPosition());
            customAdapter.notifyDataSetChanged();
        }
    };
}