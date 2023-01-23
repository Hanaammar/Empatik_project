package com.empatik;

import com.empatik.models.MoodHistory;
import com.empatik.models.SongModel;

public interface SelectListener {
    void onItemClicked(SongModel song);

    void onItemClicked(MoodHistory moodHistory);
}