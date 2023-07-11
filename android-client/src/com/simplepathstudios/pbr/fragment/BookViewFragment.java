package com.simplepathstudios.pbr.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
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
   private long lastTouchTime = -1;
   private boolean pagePickerCreated = false;

   private final int COLUMNS = 8;
   private LinearLayout pageListWrapper;
   private RecyclerView listElement;
   private PageAdapter adapter;
   private LinearLayoutManager layoutManager;


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
      currentPageImage = view.findViewById(R.id.current_page_image);

      pageListWrapper = view.findViewById(R.id.page_list_wrapper);
      listElement = view.findViewById(R.id.page_list);
      adapter = new PageAdapter();
      listElement.setAdapter(adapter);
      layoutManager = new GridLayoutManager(getActivity(), COLUMNS);
      listElement.setLayoutManager(layoutManager);

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
               if(event.getPointerCount() == 1 && action == MotionEvent.ACTION_DOWN) {
                  long currentTime = System.currentTimeMillis();
                  if (lastTouchTime != -1) {
                     if (currentTime - lastTouchTime < PBRSettings.DoubleTapThreshold) {
                        bookViewModel.setZoomScale(1.0f);
                        MainActivity.getInstance().toolbarHide();
                     }
                  }
                  lastTouchTime = currentTime;
               }
               if (currentPageImage.getZoom() < PBRSettings.PageTurnZoomThreshold) {
                  int deltaX = ((int) event.getRawX()) - touchStartX;
                  int deltaY = ((int) event.getRawY()) - touchStartY;
                  if (action == MotionEvent.ACTION_MOVE) {
                     // Swipe Down
                     if (deltaY > PBRSettings.SwipeThresholdY && Math.abs(deltaX) < PBRSettings.SwipeThresholdX) {
                        MainActivity.getInstance().toolbarShow();
                     }
                     // Swipe Up
                     if (deltaY < -PBRSettings.SwipeThresholdY && Math.abs(deltaX) < PBRSettings.SwipeThresholdX) {
                        showPagePicker();
                     }
                  }
                  if (action == MotionEvent.ACTION_UP) {
                     // Swipe right
                     if (deltaX > PBRSettings.SwipeThresholdX) {
                        turnPageRight();
                     }
                     // Swipe left
                     if (deltaX < -PBRSettings.SwipeThresholdX) {
                        turnPageLeft();
                     }
                     if (MainActivity.getInstance().toolbarIsVisible()) {
                        Handler handler = new Handler();
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
            }
            return false;
         }
      });

      bookViewModel.Data.observe(getViewLifecycleOwner(), new Observer<BookView>() {
         @Override
         public void onChanged(BookView bookView) {
            File page = bookView.getCurrentPage();
            if(!pagePickerCreated && bookView.getPageCount() > 0){
               ArrayList<Integer> pageIndices = new ArrayList<>();
               int pageIndex = 0;
               for(String pageId : bookView.PageIds){
                  pageIndices.add(pageIndex++);
               }
               adapter.setData(pageIndices);
               adapter.notifyDataSetChanged();
               pagePickerCreated = true;
            }
            if(currentPage == null || !currentPage.getAbsoluteFile().equals(page.getAbsoluteFile())){
               pageListWrapper.setVisibility(View.GONE);
               currentPageImage.setVisibility(View.GONE);
               Glide.with(currentPageImage)
                       .load(page.getAbsolutePath())
                       .dontAnimate()
                       .into(currentPageImage);
               MainActivity.getInstance().setActionBarTitle(String.format("(%d / %d) %s", bookView.CurrentPageIndex + 1, bookView.getPageCount(), bookName));
               currentPage = page;
               Handler handler = new Handler();
               // FIXME Terrible work around for when navigating back to covers.
               // Otherwise, they are zoomed in awkwardly.
               // Leaving it for all pages, in case there are some that differ in size
               handler.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                     currentPageImage.setVisibility(View.VISIBLE);
                     currentPageImage.zoomTo(1.0f, false);
                  }
               }, 100);
            } else {
               currentPageImage.zoomTo(bookView.ZoomScale, false);
            }
         }
      });
      bookViewModel.load(categoryName, bookName);
   }

   private void showPagePicker(){
      currentPageImage.setVisibility(View.GONE);
      pageListWrapper.setVisibility(View.VISIBLE);
   }

   private void turnPageLeft(){
      if(bookViewModel.isLastPage()){
         Util.toast("Finished "+bookViewModel.Data.getValue().Name);
         MainActivity.getInstance().navigateUp();
      } else {
         bookViewModel.nextPage();
      }
   }

   private void turnPageRight(){
      if(bookViewModel.isFirstPage()){
         Util.toast("Leaving "+bookViewModel.Data.getValue().Name);
         MainActivity.getInstance().navigateUp();
      } else {
         bookViewModel.previousPage();
      }
   }


}
