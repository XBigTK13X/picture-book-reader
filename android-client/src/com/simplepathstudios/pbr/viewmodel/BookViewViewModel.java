package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.ObservableCatalog;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.api.ApiClient;
import com.simplepathstudios.pbr.api.model.AlbumView;
import com.simplepathstudios.pbr.api.model.BookView;
import com.simplepathstudios.pbr.api.model.CategoryView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookViewViewModel extends ViewModel {
   public MutableLiveData<BookView> Data;
   public BookViewViewModel(){
      Data = new MutableLiveData<BookView>();
   }

   public void load(String categoryName, String bookName){
      LoadingIndicator.setLoading(true);
      Data.setValue(ObservableCatalog.getInstance().getBook(categoryName, bookName));
      LoadingIndicator.setLoading(false);
   }
}
