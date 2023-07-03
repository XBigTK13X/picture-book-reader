package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.api.ApiClient;
import com.simplepathstudios.pbr.api.model.SearchResults;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultsViewModel extends ViewModel {
    public MutableLiveData<SearchResults> Data;
    public SearchResultsViewModel(){
        Data = new MutableLiveData<>();
    }

    public void load(String query) {
        if(query.isEmpty() || query.length() == 0){
            return;
        }
        LoadingIndicator.setLoading(true);
        ApiClient.getInstance().search(query).enqueue(new Callback<SearchResults>() {

            @Override
            public void onResponse(Call<SearchResults> call, Response<SearchResults> response) {
                LoadingIndicator.setLoading(false);
                Data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<SearchResults> call, Throwable t) {
                Util.error("SearchResultsViewModel.load",t);
                LoadingIndicator.setLoading(false);
            }
        });
    }
}
