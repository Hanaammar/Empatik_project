package com.empatik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.empatik.models.MoodHistory;
import com.empatik.models.SongModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MoodHistoryActivity extends AppCompatActivity implements SelectListener {

    private RecyclerView recyclerViewHistory;
    private List<MoodHistory> moodHistoryList;
    private Button btnResetHistory, btnBackMoodHistory;
    private HistoryCustomAdapter customAdapter;
    private TextView txtMoodHistory;
    private ConstraintLayout constraintLayout;
//We create the history page, in this page we 2 buttons, and the list of the history.
//By click
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        btnResetHistory = findViewById(R.id.btnResetHistory);
        btnBackMoodHistory = findViewById(R.id.btnBackMoodHistory);
        txtMoodHistory = findViewById(R.id.txtMoodHistory);
        constraintLayout = findViewById(R.id.constraint_layout);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        Set<String> moodHistory = pref.getStringSet("moodHistory", null); // getting moodHistory
//When we click on "reset" item, we reset the history list
        btnResetHistory.setOnClickListener(view -> {
            SharedPreferences.Editor editor = getSharedPreferences("MyPref", MODE_PRIVATE).edit();
            editor.putStringSet("moodHistory", null);
            editor.apply();
            recyclerViewHistory.setVisibility(View.INVISIBLE);
            txtMoodHistory.setVisibility(View.VISIBLE);
        });
//When we click on "back" button we return to home page.
        btnBackMoodHistory.setOnClickListener(view -> MoodHistoryActivity.this.startActivity(
                new Intent(MoodHistoryActivity.this, MainActivity.class)));

        displayItems(moodHistory);

        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerViewHistory);
    }

    @Override
    public void onItemClicked(SongModel song) {

    }

    @Override
    public void onItemClicked(MoodHistory moodHistory) {

    }

    private void displayItems(Set<String> moodHistory) {
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setHasFixedSize(true);
        recyclerViewHistory.setLayoutManager(new GridLayoutManager(this, 1));
        moodHistoryList = new ArrayList<>();

        moodHistory.forEach(item -> moodHistoryList.add(new MoodHistory(item)));

        customAdapter = new HistoryCustomAdapter(this, moodHistoryList, this);
        recyclerViewHistory.setAdapter(customAdapter);
    }

    ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }
//when we swiped an entry in the list, we remove it from the history
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Snackbar snackbar = Snackbar.make(constraintLayout, "Entry deleted!", Snackbar.LENGTH_LONG);
            snackbar.show();

            moodHistoryList.remove(viewHolder.getAdapterPosition());
            customAdapter.notifyDataSetChanged();

            Set<String> moodHistorySet = new HashSet<>();
            moodHistoryList.forEach(record -> moodHistorySet.add(record.getMoodHistory()));

            SharedPreferences.Editor editor = getSharedPreferences("MyPref", MODE_PRIVATE).edit();
            if (moodHistorySet.isEmpty()) {
                editor.putStringSet("moodHistory", null);
                recyclerViewHistory.setVisibility(View.INVISIBLE);
                txtMoodHistory.setVisibility(View.VISIBLE);
            } else
                editor.putStringSet("moodHistory", moodHistorySet);
            editor.apply();
        }
    };
}