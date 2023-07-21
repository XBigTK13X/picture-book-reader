package com.simplepathstudios.pbr.fragment;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.otaliastudios.zoom.ZoomImageView;
import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.PBRSettings;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.adapter.PageAdapter;
import com.simplepathstudios.pbr.api.model.BookView;
import com.simplepathstudios.pbr.viewmodel.BookViewViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class BookViewFragment extends Fragment {
   private static final String TAG = "BookViewFragment";
   private ZoomImageView currentPageImage;
   private BookViewViewModel bookViewModel;
   private String categoryName;
   private String bookName;
   private File currentPage;
   private int touchStartX = 0;
   private int touchStartY = 0;
   private boolean multiTouchHappening = false;
   private long lastMultiTouchTime = -1;
   private long lastTouchTime = -1;

   private final int COLUMNS = 8;
   private LinearLayout pageListWrapper;
   private RecyclerView listElement;
   private PageAdapter adapter;
   private LinearLayoutManager layoutManager;
   private BookView currentBookView;
   private float lastZoom = -1;
   private long lastZoomTime = -1;


   public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      categoryName = getArguments().getString("CategoryName");
      bookName = getArguments().getString("BookName");
      MainActivity.getInstance().setActionBarTitle("Book | " + bookName);
      return inflater.inflate(R.layout.book_view_fragment, container, false);
   }

   @SuppressLint("ClickableViewAccessibility")
   @Override
   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      bookViewModel = new ViewModelProvider(MainActivity.getInstance()).get(BookViewViewModel.class);
      pageListWrapper = view.findViewById(R.id.page_list_wrapper);
      listElement = view.findViewById(R.id.page_list);
      currentPageImage = view.findViewById(R.id.current_page_image);
      adapter = new PageAdapter();
      listElement.setAdapter(adapter);
      layoutManager = new GridLayoutManager(getActivity(), COLUMNS);
      listElement.setLayoutManager(layoutManager);
      DisplayMetrics metrics = new DisplayMetrics();
      Display display = MainActivity.getInstance().getWindowManager().getDefaultDisplay();
      display.getMetrics(metrics);
      float leftBorder = ((float)metrics.widthPixels * PBRSettings.TapBorderThresholdPercent);
      float rightBorder = metrics.widthPixels - leftBorder;

      currentPageImage.setOnTouchListener(new View.OnTouchListener() {
         @Override
         public boolean onTouch(View v, MotionEvent event) {
            int action = event.getActionMasked();
            if(multiTouchHappening){
               if(action == MotionEvent.ACTION_UP && event.getPointerCount() <= 1){
                  multiTouchHappening = false;
               }
            }
            if(event.getPointerCount() > 1 && !multiTouchHappening){
               multiTouchHappening = true;
            }
            if(!multiTouchHappening) {
               long currentTime = System.currentTimeMillis();
               // Give a little time after stopping a pinch to avoid accidental single point gestures
               if(lastMultiTouchTime != -1 && currentTime - lastMultiTouchTime < PBRSettings.PinchReleaseThreshold){
                  return false;
               }
               // Give a little time after double tapping, in case the second tap hit a border
               if(lastZoom != currentPageImage.getZoom()){
                  lastZoom = currentPageImage.getZoom();
                  lastZoomTime = System.currentTimeMillis();
               }
               if(lastZoomTime != -1 && currentTime - lastZoomTime < PBRSettings.PinchReleaseThreshold){
                  return false;
               }
               if(event.getPointerCount() == 1 && action == MotionEvent.ACTION_DOWN) {
                  if (lastTouchTime != -1) {
                     if (currentTime - lastTouchTime < PBRSettings.DoubleTapThreshold) {
                        resetZoom();
                        MainActivity.getInstance().toolbarHide();
                     }
                  }
                  lastTouchTime = currentTime;
                  lastZoom = currentPageImage.getZoom();
               }
               if (currentPageImage.getZoom() < PBRSettings.PageTurnZoomThreshold) {
                  int touchEndX = ((int) event.getRawX());
                  int touchEndY = ((int) event.getRawY());
                  int deltaX = touchEndX - touchStartX;
                  int deltaY = touchEndY - touchStartY;
                  if (action == MotionEvent.ACTION_UP) {
                     //Tap left border
                     if(touchStartX < leftBorder && touchEndX < leftBorder){
                        previousPage();
                     }
                     // Tap right border
                     else if(touchStartX > rightBorder && touchEndX > rightBorder){
                        nextPage();
                     }
                     // Swipe Down
                     else if (deltaY > PBRSettings.SwipeThresholdY && Math.abs(deltaX) < PBRSettings.SwipeThresholdX) {
                        MainActivity.getInstance().toolbarShow();
                     }
                     // Swipe Up
                     else if (deltaY < -PBRSettings.SwipeThresholdY && Math.abs(deltaX) < PBRSettings.SwipeThresholdX) {
                        showPagePicker();
                     }
                     // Swipe Right
                     else if (deltaX > PBRSettings.SwipeThresholdX) {
                        previousPage();
                     }
                     // Swipe Left
                     else if (deltaX < -PBRSettings.SwipeThresholdX) {
                        nextPage();
                     }
                     if (MainActivity.getInstance().toolbarIsVisible()) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                           @Override
                           public void run() {
                              if(MainActivity.getInstance().isCurrentLocation("Book")){
                                 MainActivity.getInstance().toolbarHide();
                              }
                           }
                        }, PBRSettings.ShowToolbarMilliseconds);
                     }
                  }
                  if (action == MotionEvent.ACTION_DOWN) {
                     touchStartX = (int) event.getRawX();
                     touchStartY = (int) event.getRawY();
                  }
               }
            } else {
               lastMultiTouchTime = System.currentTimeMillis();
            }
            return false;
         }
      });

      bookViewModel.Data.observe(getViewLifecycleOwner(), new Observer<BookView>() {
         @Override
         public void onChanged(BookView bookView) {
            File page = bookView.getCurrentPage();
            // The book is different from the last time the fragment was built
            if (currentBookView == null || !currentBookView.Name.equals(bookView.Name)) {
               ArrayList<Integer> pageIndices = new ArrayList<>();
               int pageIndex = 0;
               for (String pageId : bookView.PageIds) {
                  pageIndices.add(pageIndex++);
               }
               adapter.setData(pageIndices);
               adapter.notifyDataSetChanged();
            }
            // The page changed
            if (currentPage == null || !currentPage.getAbsoluteFile().equals(page.getAbsoluteFile())) {
               if(pageListWrapper.getVisibility() == View.VISIBLE){
                  pageListWrapper.setVisibility(View.GONE);
               }
               if(currentPageImage.getVisibility() != View.VISIBLE){
                  currentPageImage.setVisibility(View.VISIBLE);
               }
               Glide.with(currentPageImage)
                       .load(page.getAbsolutePath())
                       .listener(new RequestListener<Drawable>() {
                          @Override
                          public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                             return false;
                          }

                          @Override
                          public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                             new Handler(Looper.getMainLooper()).post(()->{
                                resetZoom();
                             });
                             return false;
                          }
                       })
                       .dontAnimate()
                       .into(currentPageImage);
               MainActivity.getInstance().setActionBarTitle(String.format("(%d / %d) %s", bookView.CurrentPageIndex + 1, bookView.getPageCount(), bookName));
               currentPage = page;
            }
            currentBookView = bookView;
         }
      });

      bookViewModel.load(categoryName, bookName);
   }

   private void resetZoom(){
      currentPageImage.zoomTo(1.0f, false);
   }

   private void showPagePicker(){
      currentPageImage.setVisibility(View.GONE);
      pageListWrapper.setVisibility(View.VISIBLE);
   }

   private void nextPage(){
      if(bookViewModel.isLastPage()){
         Util.toast("Finished "+bookViewModel.Data.getValue().Name);
         MainActivity.getInstance().navigateUp();
      } else {
         bookViewModel.nextPage();
      }
   }

   private void previousPage(){
      if(bookViewModel.isFirstPage()){
         Util.toast("Leaving "+bookViewModel.Data.getValue().Name);
         MainActivity.getInstance().navigateUp();
      } else {
         bookViewModel.previousPage();
      }
   }


}
