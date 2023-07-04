package com.simplepathstudios.pbr.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.api.model.BookCategory;
import com.simplepathstudios.pbr.api.model.MusicCategory;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private ArrayList<BookCategory> data;
    public CategoryAdapter(){
        this.data = null;
    }

    public void setData(ArrayList<BookCategory> data){
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.small_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CategoryAdapter.ViewHolder holder, int position) {
        holder.category = this.data.get(position);
        TextView view = holder.textView;
        view.setText(holder.category.Name);
    }

    @Override
    public int getItemCount() {
        if(this.data == null){
            return 0;
        }
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView textView;
        public BookCategory category;

        public ViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavController navController = Navigation.findNavController(MainActivity.getInstance(), R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString("CategoryName", category.Name);
            navController.navigate(R.id.category_view_fragment, bundle);
        }
    }
}
