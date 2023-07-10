package com.simplepathstudios.pbr.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.adapter.BookAdapter;
import com.simplepathstudios.pbr.api.model.CategoryView;
import com.simplepathstudios.pbr.viewmodel.CategoryViewViewModel;

public class CategoryViewFragment extends Fragment {
   private final String TAG = "CategoryViewFragment";
   private final int COLUMNS = 5;

   private CategoryViewViewModel categoryViewModel;
   private String categoryName;
   private RecyclerView listElement;
   private BookAdapter adapter;
   private LinearLayoutManager layoutManager;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      categoryName = getArguments().getString("CategoryName");
      MainActivity.getInstance().setActionBarTitle("Category | "+categoryName);
      return inflater.inflate(R.layout.category_view_fragment, container, false);
   }

   @Override
   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      listElement = view.findViewById(R.id.category_book_list);
      adapter = new BookAdapter();
      listElement.setAdapter(adapter);
      layoutManager = new GridLayoutManager(getActivity(), COLUMNS);
      listElement.setLayoutManager(layoutManager);
      categoryViewModel = new ViewModelProvider(this).get(CategoryViewViewModel.class);
      categoryViewModel.Data.observe(getViewLifecycleOwner(), new Observer<CategoryView>() {
         @Override
         public void onChanged(CategoryView categoryView) {
            adapter.setData(categoryView.Books);
            adapter.notifyDataSetChanged();
         }
      });
      categoryViewModel.load(categoryName);
   }
}
