package com.simplepathstudios.pbr.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.api.model.BookView;
import com.simplepathstudios.pbr.viewmodel.BookViewViewModel;

public class BookViewFragment extends Fragment {
   private static final String TAG = "BookViewFragment";
   private ImageView firstBufferImage;
   private TextView progress;
   private BookViewViewModel bookViewModel;
   private String categoryName;
   private String bookName;

   public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      categoryName = getArguments().getString("CategoryName");
      bookName = getArguments().getString("BookName");
      MainActivity.getInstance().setActionBarTitle("Book | " + bookName);
      return inflater.inflate(R.layout.book_view_fragment, container, false);
   }

   @Override
   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      firstBufferImage = view.findViewById(R.id.first_buffer_image);
      progress = view.findViewById(R.id.progress_text);
      progress.setText("(0/0)");
      bookViewModel = new ViewModelProvider(MainActivity.getInstance()).get(BookViewViewModel.class);
      bookViewModel.Data.observe(getViewLifecycleOwner(), new Observer<BookView>() {
         @Override
         public void onChanged(BookView bookView) {
            Glide.with(MainActivity.getInstance())
                 .load(bookView.getCurrentPage().getAbsolutePath())
                    .into(firstBufferImage);
            progress.setText(String.format("(%d / %d)", bookView.CurrentPageIndex + 1, bookView.getPageCount()));
         }
      });
      bookViewModel.load(categoryName, bookName);
   }
}
