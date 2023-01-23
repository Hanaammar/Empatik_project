package com.empatik;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class CustomViewHolder extends RecyclerView.ViewHolder {
    public TextView txtSongName;
    public CardView cardView;

    public CustomViewHolder(@NonNull View itemView) {
        super(itemView);
        txtSongName = itemView.findViewById(R.id.txtSongName);
        cardView = itemView.findViewById(R.id.main_container);
    }
}