package com.simplepathstudios.pbr.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.CentralCatalog;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.api.model.BookCategory;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    public static final String TAG = "CategoryAdapter";
    private ArrayList<BookCategory> data;
    public CategoryAdapter(){
        this.data = null;
    }

    public void setData(ArrayList<BookCategory> data){
        data.sort(new Comparator<BookCategory>() {
            @Override
            public int compare(BookCategory o1, BookCategory o2) {
                return o1.Name.toLowerCase().compareTo(o2.Name.toLowerCase());
            }
        });
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CategoryAdapter.ViewHolder holder, int position) {
        holder.category = this.data.get(position);
        holder.label.setText(holder.category.Name+"\n("+ CentralCatalog.getInstance().getBooks(holder.category.Name).Books.size()+")");
        File coverFile = CentralCatalog.getInstance().getCategoryThumbnail(holder.category.Name);
        Glide.with(holder.layout).load(coverFile).into(holder.cover);
    }

    @Override
    public int getItemCount() {
        if(this.data == null){
            return 0;
        }
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView label;
        public final ImageView cover;
        public final LinearLayout layout;
        public BookCategory category;

        public ViewHolder(LinearLayout layout) {
            super(layout);
            this.layout = layout;
            this.cover = layout.findViewById(R.id.category_cover);
            this.label = layout.findViewById(R.id.category_name);
            layout.setOnClickListener(this);
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
