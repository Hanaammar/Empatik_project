package com.empatik;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.empatik.models.SongModel;

import java.util.List;
//view the list of songs and update it.
public class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> {
    private Context context;
    private List<SongModel> songList;
    private SelectListener listener;

    public CustomAdapter(Context context, List<SongModel> songList, SelectListener selectListener) {
        this.context = context;
        this.songList = songList;
        this.listener = selectListener;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.single_song, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.txtSongName.setText(songList.get(position).getSongName());
        holder.cardView.setOnClickListener(view -> listener.onItemClicked(songList.get(position)));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }
}
