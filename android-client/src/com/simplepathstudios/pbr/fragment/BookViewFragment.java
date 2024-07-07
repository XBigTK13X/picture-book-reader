package com.simplepathstudios.pbr.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
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
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.request.transition.Transition;
import com.otaliastudios.zoom.ZoomImageView;
import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.PBRSettings;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.adapter.PageAdapter;
import com.simplepathstudios.pbr.api.model.BookView;
import com.simplepathstudios.pbr.api.model.PageListItem;
import com.simplepathstudios.pbr.viewmodel.BookViewViewModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class BookViewFragment extends Fragment {
   private static final String TAG = "BookViewFragment";
   private ZoomImageView currentPageImage;
   private BookViewViewModel bookViewModel;
   private String categoryName;
   private String bookName;
   private Uri currentPage;
   private int touchStartX = 0;
   private int touchStartY = 0;
   private boolean multiTouchHappening = false;
   private long lastMultiTouchTime = -1;
   private long lastTouchTime = -1;
   private long doubleTapTime = -1;

   private boolean imageLocked = false;

   private final int COLUMNS = 8;
   private LinearLayout pageListWrapper;
   private RecyclerView listElement;
   private PageAdapter adapter;
   private LinearLayoutManager layoutManager;
   private BookView currentBookView;

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
      adapter = new PageAdapter(this);
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
            if(multiTouchHappening) {
               lastMultiTouchTime = System.currentTimeMillis();
            }
            else{
               long currentTime = System.currentTimeMillis();
               // Give a little time after stopping a pinch to avoid accidental single point gestures
               if(lastMultiTouchTime != -1 && currentTime - lastMultiTouchTime < PBRSettings.PinchReleaseThreshold){
                  debugGesture("Ignore input so soon after a pinch");
                  return false;
               }
               // Give a little time after double tapping, in case the second tap hit a border
               if(doubleTapTime != -1 && currentTime - doubleTapTime < PBRSettings.PinchReleaseThreshold){
                  debugGesture("Ignore input if a double tap just happened");
                  return false;
               }
               if(action == MotionEvent.ACTION_DOWN) {
                  if (lastTouchTime != -1 && currentTime - lastTouchTime < PBRSettings.DoubleTapThreshold) {
                     debugGesture("Reset zoom on double tap");
                     resetZoom();
                     MainActivity.getInstance().toolbarHide();
                     doubleTapTime = System.currentTimeMillis();
                  }
                  debugGesture("Finger touched the screen");
                  touchStartX = (int) event.getRawX();
                  touchStartY = (int) event.getRawY();
                  lastTouchTime = currentTime;
                  return false;
               }
               if (currentPageImage.getZoom() < PBRSettings.PageTurnZoomThreshold) {
                  if (action == MotionEvent.ACTION_UP) {
                     int touchEndX = ((int) event.getRawX());
                     int touchEndY = ((int) event.getRawY());
                     int deltaX = touchEndX - touchStartX;
                     int deltaY = touchEndY - touchStartY;
                     //Util.log(TAG, "tsX: "+touchStartX + ", teX: "+touchEndX+", tsY: "+touchStartY+", teY: "+touchEndY+", dX: "+deltaX+", dY: "+deltaY);
                     //Tap left border
                     if(touchStartX < leftBorder && touchEndX < leftBorder){
                        debugGesture("Tapping left");
                        previousPage();
                     }
                     // Tap right border
                     else if(touchStartX > rightBorder && touchEndX > rightBorder){
                        debugGesture("Tapping right");
                        nextPage();
                     }
                     // Swipe Down
                     else if (deltaY > PBRSettings.SwipeThresholdY && Math.abs(deltaX) < PBRSettings.SwipeThresholdX) {
                        debugGesture("Swiping down");
                        MainActivity.getInstance().toolbarShow();
                     }
                     // Swipe Up
                     else if (deltaY < -PBRSettings.SwipeThresholdY && Math.abs(deltaX) < PBRSettings.SwipeThresholdX) {
                        debugGesture("Swiping up");
                        showPagePicker();
                     }
                     // Swipe Right
                     else if (deltaX > PBRSettings.SwipeThresholdX) {
                        debugGesture("Swiping right");
                        previousPage();
                     }
                     // Swipe Left
                     else if (deltaX < -PBRSettings.SwipeThresholdX) {
                        debugGesture("Swiping left");
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
                  else if (action == MotionEvent.ACTION_DOWN) {
                     debugGesture("Tap happening at the end");
                     touchStartX = (int) event.getRawX();
                     touchStartY = (int) event.getRawY();
                  }
               }
            }
            return false;
         }
      });

      bookViewModel.Data.observe(getViewLifecycleOwner(), new Observer<BookView>() {
         @Override
         public void onChanged(BookView bookView) {
            String foundPage = bookView.getCurrentPage();
            if(foundPage == null){
               currentPage = null;
               return;
            }
            Uri page = Uri.parse(foundPage);
            boolean bookChanged = currentBookView == null || !currentBookView.Name.equals(bookView.Name);
            boolean pageChanged = currentPage == null || !currentPage.toString().equals(page.toString());
            if(bookChanged){
               bookView.gotoPage(0);
            }
            if (bookChanged || pageChanged) {
               ArrayList<PageListItem> pages = new ArrayList<>();
               int pageIndex = 0;
               for (String pageId : bookView.PageIds) {
                  PageListItem item = new PageListItem();
                  item.Index = pageIndex++;
                  item.IsCurrentPage = item.Index == bookView.CurrentPageIndex;
                  pages.add(item);
               }
               int currentLeftPage = 0;
               int lastPage = ((bookView.getPageCount() - 2) * 2) + 2;
               if (bookView.CurrentPageIndex == 0) {
                  currentLeftPage = 1;
               } else if (bookView.CurrentPageIndex == bookView.getPageCount() - 1) {
                  currentLeftPage = lastPage;
               } else {
                  currentLeftPage = bookView.CurrentPageIndex * 2;
               }
               MainActivity.getInstance().setActionBarTitle(String.format("(%d / %d) %s", currentLeftPage, lastPage, bookName));
               adapter.setData(pages);
               adapter.notifyDataSetChanged();
            }
            if (pageChanged) {
               if (!imageLocked) {
                  imageLocked = true;
                  currentPageImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                     @Override
                     public boolean onPreDraw() {
                        imageLocked = false;
                        resetZoom();
                        currentPage = page;
                        currentPageImage.getViewTreeObserver().removeOnPreDrawListener(this);
                        return false;
                     }
                  });
                  try {
                     Bitmap bitmap = Util.fullImage(page);

                     currentPageImage.setImageBitmap(bitmap);
                  }
                  catch(Exception fullFail){
                     try {
                        Bitmap subsampled = Util.subsample(page);
                        currentPageImage.setImageBitmap(subsampled);
                     }
                     catch(Exception subsampleFail){
                        Util.log(TAG, "Unable to load full and subsample image. Page "+bookView.CurrentPageIndex);
                        Util.error(TAG,subsampleFail);
                        Util.toast("Unable to load page.");
                     }
                  }
               }
            }
            currentBookView = bookView;
         }
      });
      bookViewModel.load(categoryName, bookName);
   }

   private void debugGesture(String message){
      //Util.log(TAG, message);
   }

   private void resetZoom(){
      currentPageImage.zoomTo(1.0f, false);
   }

   public void showPagePicker(){
      if(currentPageImage.getVisibility() == View.VISIBLE){
         currentPageImage.setVisibility(View.GONE);
      }
      if(pageListWrapper.getVisibility() == View.GONE){
         pageListWrapper.setVisibility(View.VISIBLE);
      }
   }

   public void hidePagePicker(){
      if(currentPageImage.getVisibility() == View.GONE){
         currentPageImage.setVisibility(View.VISIBLE);
      }
      if(pageListWrapper.getVisibility() == View.VISIBLE){
         pageListWrapper.setVisibility(View.GONE);
      }
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
