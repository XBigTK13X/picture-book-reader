package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.api.ApiClient;
import com.simplepathstudios.pbr.api.model.AlbumView;
import com.simplepathstudios.pbr.api.model.CategoryView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryViewViewModel extends ViewModel {
   public MutableLiveData<CategoryView> Data;
   public CategoryViewViewModel(){
      Data = new MutableLiveData<CategoryView>();
   }

   public void load(String categorySlug){
      LoadingIndicator.setLoading(true);
      Data.setValue(new CategoryView());

      LoadingIndicator.setLoading(false);
   }
}
