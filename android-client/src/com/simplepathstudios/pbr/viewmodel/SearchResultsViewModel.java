package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.CentralCatalog;
import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.api.model.SearchResults;

public class SearchResultsViewModel extends ViewModel {
   public MutableLiveData<SearchResults> Data;
   public SearchResultsViewModel(){
      Data = new MutableLiveData<>();
   }

   public void load(String needle){
      LoadingIndicator.setLoading(true);
      Data.setValue(CentralCatalog.getInstance().search(needle));
      LoadingIndicator.setLoading(false);
   }
}
