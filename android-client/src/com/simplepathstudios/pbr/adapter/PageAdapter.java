package com.simplepathstudios.pbr.adapter;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.CentralCatalog;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.api.model.Book;
import com.simplepathstudios.pbr.fragment.BookViewFragment;
import com.simplepathstudios.pbr.viewmodel.BookViewViewModel;

import java.io.File;
import java.util.ArrayList;

public class PageAdapter extends RecyclerView.Adapter<PageAdapter.ViewHolder> {
   private ArrayList<Integer> data;
   public PageAdapter(){
      this.data = null;
   }

   public void setData(ArrayList<Integer> data){
      this.data = data;
   }

   @Override
   public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      TextView v = (TextView) LayoutInflater.from(parent.getContext())
              .inflate(R.layout.small_list_item, parent, false);
      return new ViewHolder(v);
   }

   @Override
   public void onBindViewHolder(PageAdapter.ViewHolder holder, int position) {
      holder.pageNumber = this.data.get(position);
      holder.label.setText(""+(holder.pageNumber + 1));
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
      public Integer pageNumber;

      public ViewHolder(TextView view) {
         super(view);
         label = view;
         label.setOnClickListener(this);
      }

      @Override
      public void onClick(View v) {
         BookViewViewModel viewModel = new ViewModelProvider(MainActivity.getInstance()).get(BookViewViewModel.class);
         viewModel.gotoPage(pageNumber);
      }
   }
}
