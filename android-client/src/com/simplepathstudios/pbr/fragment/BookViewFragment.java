package com.simplepathstudios.pbr.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.api.model.BookView;
import com.simplepathstudios.pbr.api.model.CategoryView;
import com.simplepathstudios.pbr.api.model.MusicFile;
import com.simplepathstudios.pbr.api.model.MusicQueue;
import com.simplepathstudios.pbr.viewmodel.BookViewViewModel;
import com.simplepathstudios.pbr.viewmodel.CategoryViewViewModel;
import com.simplepathstudios.pbr.viewmodel.ObservableMusicQueue;

public class BookViewFragment extends Fragment {
   private static final String TAG = "BookViewFragment";
   private ImageView currentPage;
   private TextView progress;
   private BookViewViewModel bookViewModel;
   private String categoryName;
   private String bookName;
   private BookView book;

   public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      categoryName = getArguments().getString("CategoryName");
      bookName = getArguments().getString("BookName");
      return inflater.inflate(R.layout.book_view_fragment, container, false);
   }

   @Override
   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      currentPage = view.findViewById(R.id.current_page_image);
      progress = view.findViewById(R.id.progress_text);
      progress.setText("(0/0)");
      bookViewModel = new ViewModelProvider(this).get(BookViewViewModel.class);
      bookViewModel.Data.observe(getViewLifecycleOwner(), new Observer<BookView>() {
         @Override
         public void onChanged(BookView bookView) {
            book = bookView;
            currentPage.setImageBitmap(book.Pages.get(book.PageIds.get(0)));
         }
      });
      bookViewModel.load(categoryName, bookName);
   }
}
