package com.seamas.pttrsslibrary;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.LongDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter {

    private ArrayList<Article> articles;
    private View out;

    public Adapter(ArrayList<Article> articles, View out) {
        this.articles = articles;
        this.out = out;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.floating_service_recycler_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((MyViewHolder) viewHolder).title.setText(articles.get(i).getTitle());
        ((MyViewHolder) viewHolder).content.setText(articles.get(i).getContent());
        ((MyViewHolder) viewHolder).parent.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(articles.get(i).getAddress()));
            v.getContext().startActivity(intent);
            out.performClick();
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView content;
        View parent;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            parent = itemView;
        }
    }
}
