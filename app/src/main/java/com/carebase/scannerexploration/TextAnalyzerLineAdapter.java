package com.carebase.scannerexploration;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;;

import java.util.List;

public class TextAnalyzerLineAdapter extends RecyclerView.Adapter<TextAnalyzerLineAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;
        public CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_view);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

    private List<Pair<String,Boolean>> textList;

    public void setTextList(List<Pair<String,Boolean>> textList) {
        this.textList = textList;
    }

    public List<Pair<String, Boolean>> getTextList() {
        return textList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.text, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pair<String,Boolean> text = textList.get(position);
        String line = text.first;
        holder.textView.setText(line);
        holder.checkBox.setOnClickListener(view -> {
            Pair<String,Boolean> newText = new Pair<>(text.first,!text.second);
            textList.remove(position);
            textList.add(position,newText);
        });
    }

    @Override
    public int getItemCount() {
        if (textList != null)
            return textList.size();
        return 0;
    }
}
