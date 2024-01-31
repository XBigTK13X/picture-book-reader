package com.simplepathstudios.pbr.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.api.model.PageListItem;
import com.simplepathstudios.pbr.fragment.BookViewFragment;
import com.simplepathstudios.pbr.viewmodel.BookViewViewModel;

import java.util.ArrayList;

public class PageAdapter extends RecyclerView.Adapter<PageAdapter.ViewHolder> {
   private ArrayList<PageListItem> data;
   private BookViewFragment fragment;
   public PageAdapter(BookViewFragment fragment){
      this.data = null;
      this.fragment = fragment;
   }

   public void setData(ArrayList<PageListItem> data){
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
      holder.page = this.data.get(position);
      if(holder.page.Index == 0){
         holder.label.setText(""+(holder.page.Index + 1));
      }
      else if (holder.page.Index == this.data.size() - 1){
         int lastPage = ((this.data.size() - 2) * 2) + 2;
         holder.label.setText(""+(lastPage));
      }
      else {
         holder.label.setText(""+(holder.page.Index * 2)+"             "+(holder.page.Index * 2 + 1));
      }
      TextView textView = holder.itemView.findViewById(R.id.text);
      if(holder.page.IsCurrentPage){
         textView.setBackgroundResource(R.color.primary);
      }
      else{
         textView.setBackgroundResource(R.color.primary_dark);
      }
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
      public PageListItem page;

      public ViewHolder(TextView view) {
         super(view);
         label = view;
         label.setOnClickListener(this);
      }

      @Override
      public void onClick(View v) {
         fragment.hidePagePicker();
         BookViewViewModel viewModel = new ViewModelProvider(MainActivity.getInstance()).get(BookViewViewModel.class);
         viewModel.gotoPage(page.Index);
      }
   }
}
