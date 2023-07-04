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
import com.simplepathstudios.pbr.api.model.Book;
import com.simplepathstudios.pbr.api.model.BookCategory;
import com.simplepathstudios.pbr.api.model.MusicCategory;

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
      TextView v = (TextView) LayoutInflater.from(parent.getContext())
              .inflate(R.layout.small_list_item, parent, false);
      return new ViewHolder(v);
   }

   @Override
   public void onBindViewHolder(BookAdapter.ViewHolder holder, int position) {
      holder.book = this.data.get(position);
      TextView view = holder.textView;
      view.setText(holder.book.Name);
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
      public Book book;

      public ViewHolder(TextView textView) {
         super(textView);
         this.textView = textView;
         textView.setOnClickListener(this);
      }

      @Override
      public void onClick(View v) {
         NavController navController = Navigation.findNavController(MainActivity.getInstance(), R.id.nav_host_fragment);
         Bundle bundle = new Bundle();
         // TODO Open the book viewer
         //bundle.putString("CategoryName", category.Name);
         //navController.navigate(R.id.category_view_fragment, bundle);
      }
   }
}
