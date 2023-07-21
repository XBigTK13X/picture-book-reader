package com.simplepathstudios.pbr.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.PBRSettings;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.adapter.BookAdapter;
import com.simplepathstudios.pbr.api.model.CategoryView;
import com.simplepathstudios.pbr.api.model.SearchResults;
import com.simplepathstudios.pbr.viewmodel.CategoryViewViewModel;
import com.simplepathstudios.pbr.viewmodel.SearchResultsViewModel;

public class SearchFragment extends Fragment {
   private final String TAG = "SearchFragment";
   private final int COLUMNS = 5;

   private SearchResultsViewModel searchResultsViewModel;
   private EditText searchQuery;
   private RecyclerView listElement;
   private BookAdapter adapter;
   private GridLayoutManager layoutManager;
   private Handler debouncedHandler;
   private String needle;
   private boolean keyboardShowing = true;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      debouncedHandler = new Handler(Looper.getMainLooper());
   }

   @Override
   public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.search_fragment, container, false);
   }

   @Override
   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      MainActivity.getInstance().setActionBarTitle("Search");
      listElement = view.findViewById(R.id.search_results);
      searchQuery = view.findViewById(R.id.search_query);
      adapter = new BookAdapter();
      listElement.setAdapter(adapter);
      layoutManager = new GridLayoutManager(getActivity(),COLUMNS);
      listElement.setLayoutManager(layoutManager);
      searchResultsViewModel = new ViewModelProvider(this).get(SearchResultsViewModel.class);
      searchResultsViewModel.Data.observe(getViewLifecycleOwner(), new Observer<SearchResults>() {
         @Override
         public void onChanged(SearchResults searchResults) {
            MainActivity.getInstance().setActionBarTitle("Found ["+searchResults.Books.size() + "] books for query [" + needle + "].");
            adapter.setData(searchResults.Books);
            adapter.notifyDataSetChanged();
         }
      });
      listElement.setOnTouchListener(new View.OnTouchListener() {
         @Override
         public boolean onTouch(View v, MotionEvent event) {
            if(keyboardShowing){
               InputMethodManager imm = (InputMethodManager) MainActivity.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
               imm.hideSoftInputFromWindow(searchQuery.getWindowToken(), 0);
               searchQuery.clearFocus();
               keyboardShowing = false;
            }

            return false;
         }
      });

      searchQuery.requestFocus();
      searchQuery.setOnFocusChangeListener(new View.OnFocusChangeListener() {
         @Override
         public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
               keyboardShowing = true;
            }
         }
         });
      searchQuery.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {

         }

         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {
            String check = s.toString();
            if(check.length() > 2){
               needle = check;
               debouncedHandler.removeCallbacksAndMessages(null);
               debouncedHandler.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                     searchResultsViewModel.load(needle);
                  }
               }, PBRSettings.SearchDelayMilliseconds);
            }
         }

         @Override
         public void afterTextChanged(Editable s) {

         }
      });
      searchQuery.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            keyboardShowing = true;
         }
      });
   }
}
