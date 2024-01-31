package com.simplepathstudios.pbr.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.simplepathstudios.pbr.CentralCatalog;
import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.adapter.BookAdapter;
import com.simplepathstudios.pbr.adapter.CategoryAdapter;
import com.simplepathstudios.pbr.api.model.Book;
import com.simplepathstudios.pbr.api.model.CategoryList;
import com.simplepathstudios.pbr.viewmodel.CategoryListViewModel;
import com.simplepathstudios.pbr.viewmodel.CategoryViewViewModel;
import com.simplepathstudios.pbr.viewmodel.RandomBooksViewModel;
import com.simplepathstudios.pbr.viewmodel.SettingsViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;

public class RandomFragment extends Fragment {
   private final String TAG = "RandomFragment";

   private final int COLUMNS = 5;

   private ImageView coverImage;
   private Button rerollButton;
   private Book book;
   private RecyclerView listElement;
   private BookAdapter adapter;
   private GridLayoutManager layoutManager;
   private RandomBooksViewModel viewModel;

   @Override
   public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.random_fragment, container, false);
   }
   @Override
   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      listElement = view.findViewById(R.id.random_book_list);
      adapter = new BookAdapter();
      listElement.setAdapter(adapter);
      layoutManager = new GridLayoutManager(getActivity(), COLUMNS);
      listElement.setLayoutManager(layoutManager);
      rerollButton = view.findViewById(R.id.reroll_button);
      rerollButton.setOnClickListener(clickedView -> {
         CentralCatalog.getInstance().importLibrary(false).doOnComplete(()-> {
            viewModel.addBook(CentralCatalog.getInstance().getRandomBook());
         }).subscribe();
      });
      viewModel = new ViewModelProvider(MainActivity.getInstance()).get(RandomBooksViewModel.class);
      viewModel.Data.observe(getViewLifecycleOwner(), new Observer<ArrayList<Book>>() {
         @Override
         public void onChanged(ArrayList<Book> books) {
            adapter.setData(books);
            adapter.notifyDataSetChanged();
         }
      });
   }
}

