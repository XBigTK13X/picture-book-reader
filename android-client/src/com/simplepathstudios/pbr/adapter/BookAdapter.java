package com.simplepathstudios.pbr.adapter;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.CentralCatalog;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.api.model.Book;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
   private ArrayList<Book> data;
   public BookAdapter(){
      this.data = null;
   }

   public void setData(ArrayList<Book> data){
      this.data = data;
   }

   @Override
   public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
              .inflate(R.layout.image_list_item, parent, false);
      return new ViewHolder(v);
   }

   @Override
   public void onBindViewHolder(BookAdapter.ViewHolder holder, int position) {
      holder.book = this.data.get(position);
      byte[] thumbBytes = CentralCatalog.getInstance().getBookThumbnail(holder.book.CategoryName, holder.book.Name);
      Glide.with(MainActivity.getInstance()).load(thumbBytes).fitCenter().into(holder.image);
   }

   @Override
   public int getItemCount() {
      if(this.data == null){
         return 0;
      }
      return this.data.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      public final ImageView image;
      public Book book;

      public ViewHolder(ImageView view) {
         super(view);
         image = view;
         image.setOnClickListener(this);
      }

      @Override
      public void onClick(View v) {
         NavController navController = Navigation.findNavController(MainActivity.getInstance(), R.id.nav_host_fragment);
         Bundle bundle = new Bundle();
         bundle.putString("CategoryName", book.CategoryName);
         bundle.putString("BookName", book.Name);
         navController.navigate(R.id.book_view_fragment, bundle);
      }
   }
}
