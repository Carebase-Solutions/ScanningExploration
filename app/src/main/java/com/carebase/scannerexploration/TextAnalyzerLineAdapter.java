package com.carebase.scannerexploration;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;;

import java.util.List;

public class TextAnalyzerLineAdapter extends RecyclerView.Adapter<TextAnalyzerLineAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_view);
        }
    }

    private List<String> textList;

    public void setTextList(List<String> textList) {
        this.textList = textList;
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
        String line = textList.get(position);
        holder.textView.setText(line);
    }

    @Override
    public int getItemCount() {
        if (textList != null)
            return textList.size();
        return 0;
    }
}
