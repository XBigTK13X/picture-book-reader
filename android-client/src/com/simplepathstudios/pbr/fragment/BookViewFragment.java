package com.simplepathstudios.pbr.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.otaliastudios.zoom.ZoomImageView;
import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.PBRSettings;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.api.model.BookView;
import com.simplepathstudios.pbr.viewmodel.BookViewViewModel;

import java.io.File;

public class BookViewFragment extends Fragment {
   private static final String TAG = "BookViewFragment";
   private ZoomImageView firstBufferImage;
   private BookViewViewModel bookViewModel;
   private String categoryName;
   private String bookName;
   private File currentPage;
   private int touchStartX = 0;
   private int touchStartY = 0;

   public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      categoryName = getArguments().getString("CategoryName");
      bookName = getArguments().getString("BookName");
      MainActivity.getInstance().setActionBarTitle("Book | " + bookName);
      return inflater.inflate(R.layout.book_view_fragment, container, false);
   }

   @Override
   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      firstBufferImage = view.findViewById(R.id.current_page_image);
      firstBufferImage.setOnTouchListener(new View.OnTouchListener() {
         @Override
         public boolean onTouch(View v, MotionEvent event) {
            int action = event.getActionMasked();
            if(firstBufferImage.getZoom() < PBRSettings.PageTurnZoomThreshold) {
               int deltaX = ((int) event.getRawX()) - touchStartX;
               int deltaY = ((int) event.getRawY()) - touchStartY;
               if (action == MotionEvent.ACTION_MOVE) {
                  if (deltaY > PBRSettings.SwipeThresholdY && Math.abs(deltaX) < PBRSettings.SwipeThresholdX) {
                     //toolbar.setVisibility(View.VISIBLE);
                  }
               }
               if (action == MotionEvent.ACTION_UP) {
                  if (deltaX > PBRSettings.SwipeThresholdX) {
                     turnPageRight();
                  }
                  if (deltaX < -PBRSettings.SwipeThresholdX) {
                     turnPageLeft();
                  }
                     /*if(toolbar.getVisibility() == View.VISIBLE){
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                           @Override
                           public void run() {
                              //toolbar.setVisibility(View.GONE);
                           }
                        }, PBRSettings.ShowToolbarMilliseconds);
                     }*/
               }
               if (action == MotionEvent.ACTION_DOWN) {
                  touchStartX = (int) event.getRawX();
                  touchStartY = (int) event.getRawY();
               }
            }
            return false;
         }
      });
      bookViewModel = new ViewModelProvider(MainActivity.getInstance()).get(BookViewViewModel.class);
      bookViewModel.Data.observe(getViewLifecycleOwner(), new Observer<BookView>() {
         @Override
         public void onChanged(BookView bookView) {
            File page = bookView.getCurrentPage();
            if(currentPage != page){
               firstBufferImage.zoomBy(1.0f, false);
               Glide.with(MainActivity.getInstance())
                    .load(page.getAbsolutePath())
                       .into(firstBufferImage);
               MainActivity.getInstance().setActionBarTitle(String.format("(%d / %d) %s", bookView.CurrentPageIndex + 1, bookView.getPageCount(), bookName));
               currentPage = page;
            } else {
               firstBufferImage.zoomBy(bookView.ZoomScale, false);
            }
         }
      });
      bookViewModel.load(categoryName, bookName);
   }

   private void turnPageLeft(){
      if(bookViewModel.isLastPage()){
         Util.toast("Finished "+bookViewModel.Data.getValue().Name);
         //navController.navigateUp();
      } else {
         bookViewModel.nextPage();
      }
   }

   private void turnPageRight(){
      if(bookViewModel.isFirstPage()){
         Util.toast("Leaving "+bookViewModel.Data.getValue().Name);
         //navController.navigateUp();
      } else {
         bookViewModel.previousPage();
      }
   }


}
