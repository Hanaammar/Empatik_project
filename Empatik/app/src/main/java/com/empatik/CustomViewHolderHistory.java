package com.empatik;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class CustomViewHolderHistory extends RecyclerView.ViewHolder {
    public TextView txtHistoryRecord;
    public CardView cardView;

    public CustomViewHolderHistory(@NonNull View itemView) {
        super(itemView);
        txtHistoryRecord = itemView.findViewById(R.id.txtHistoryRecord);
        cardView = itemView.findViewById(R.id.main_container_history);
    }
}