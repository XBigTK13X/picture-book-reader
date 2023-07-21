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

import com.simplepathstudios.pbr.CentralCatalog;
import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.adapter.CategoryAdapter;
import com.simplepathstudios.pbr.api.model.Book;
import com.simplepathstudios.pbr.api.model.CategoryList;
import com.simplepathstudios.pbr.viewmodel.CategoryListViewModel;
import com.simplepathstudios.pbr.viewmodel.SettingsViewModel;

import java.io.File;

import io.reactivex.rxjava3.functions.Consumer;

public class RandomFragment extends Fragment {
   private final String TAG = "RandomFragment";

   private ImageView coverImage;
   private Button rerollButton;
   private Book book;

   @Override
   public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.random_fragment, container, false);
   }
   @Override
   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      coverImage = view.findViewById(R.id.cover_image);
      coverImage.setOnClickListener(clickedView->{
         if(book != null){
            NavController navController = Navigation.findNavController(MainActivity.getInstance(), R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString("CategoryName", book.CategoryName);
            bundle.putString("BookName", book.Name);
            navController.navigate(R.id.book_view_fragment, bundle);
         }
      });
      rerollButton = view.findViewById(R.id.reroll_button);
      rerollButton.setOnClickListener(clickedView -> {
         LoadingIndicator.setLoading(true);
         CentralCatalog.getInstance().importLibrary(false, false).doOnComplete(()-> {
            book = CentralCatalog.getInstance().getRandomBook();
            File thumbnail = CentralCatalog.getInstance().getBookThumbnail(book.CategoryName, book.Name);
            coverImage.setImageBitmap(BitmapFactory.decodeFile(thumbnail.getAbsolutePath()));
            LoadingIndicator.setLoading(false);
         }).subscribe();
      });
   }
}

