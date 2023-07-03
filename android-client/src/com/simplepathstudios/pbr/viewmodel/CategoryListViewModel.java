package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.api.ApiClient;
import com.simplepathstudios.pbr.api.model.CategoryList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryListViewModel extends ViewModel {
    public MutableLiveData<CategoryList> Data;
    public CategoryListViewModel(){
        Data = new MutableLiveData<CategoryList>();
    }

    public void load(){
        LoadingIndicator.setLoading(true);
        ApiClient.getInstance().getCategoryList().enqueue(new Callback< CategoryList >(){

            @Override
            public void onResponse(Call<CategoryList> call, Response<CategoryList> response) {
                LoadingIndicator.setLoading(false);
                Data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<CategoryList> call, Throwable t) {
                Util.error("CategoryListViewModel",t);
                LoadingIndicator.setLoading(false);
            }
        });
    }
}
