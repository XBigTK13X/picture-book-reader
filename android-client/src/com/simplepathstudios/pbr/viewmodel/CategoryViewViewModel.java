package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.CentralCatalog;
import com.simplepathstudios.pbr.api.model.CategoryView;

public class CategoryViewViewModel extends ViewModel {
   public MutableLiveData<CategoryView> Data;
   public CategoryViewViewModel(){
      Data = new MutableLiveData<CategoryView>();
   }

   public void load(String categoryName){
      LoadingIndicator.setLoading(true);
      Data.setValue(CentralCatalog.getInstance().getBooks(categoryName));
      LoadingIndicator.setLoading(false);
   }
}
