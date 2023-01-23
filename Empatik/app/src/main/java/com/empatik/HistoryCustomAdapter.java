package com.empatik;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.empatik.models.MoodHistory;

import java.util.List;
//view the list in the history and update it.
public class HistoryCustomAdapter extends RecyclerView.Adapter<CustomViewHolderHistory> {
    private Context context;
    private List<MoodHistory> historyList;
    private SelectListener listener;

    public HistoryCustomAdapter(Context context, List<MoodHistory> historyList, SelectListener selectListener) {
        this.context = context;
        this.historyList = historyList;
        this.listener = selectListener;
    }

    @NonNull
    @Override
    public CustomViewHolderHistory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolderHistory(LayoutInflater.from(context)
                .inflate(R.layout.single_history_record, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolderHistory holder, int position) {
        holder.txtHistoryRecord.setText(historyList.get(position).getMoodHistory());
        holder.cardView.setOnClickListener(view -> listener.onItemClicked(historyList.get(position)));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}
