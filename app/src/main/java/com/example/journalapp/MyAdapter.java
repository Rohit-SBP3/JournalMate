package com.example.journalapp;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.JournalViewHolder> {

    private final List<Journal> journalList;
    private final Context context;

    public MyAdapter(List<Journal> journalList, Context context) {
        this.journalList = journalList;
        this.context = context;
    }

    @NonNull
    @Override
    public JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.journal_row, parent, false);
        return new JournalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalViewHolder holder, int position) {
        Journal entry = journalList.get(position);
        holder.username.setText(entry.getUsername());
        holder.title.setText(entry.getTitle());
        holder.thought.setText(entry.getThoughts());

        String imageUrl = entry.getImageURL();
        String timeAdded = (String) DateUtils.getRelativeTimeSpanString(entry.getTimeAdded().getSeconds()*1000);

        holder.dateAdded.setText(timeAdded);

        GlideApp.with(context).load(imageUrl).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.trees).into(holder.journalImage);

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    // ViewHolder
    static class JournalViewHolder extends RecyclerView.ViewHolder{

        public TextView title, thought, dateAdded, username;
        public ImageView journalImage;

        public JournalViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.journal_title);
            thought = itemView.findViewById(R.id.journal_thought);
            dateAdded = itemView.findViewById(R.id.last_updated_date);
            username = itemView.findViewById(R.id.journal_row_username);

            journalImage = itemView.findViewById(R.id.journal_image);

        }
    }
}
