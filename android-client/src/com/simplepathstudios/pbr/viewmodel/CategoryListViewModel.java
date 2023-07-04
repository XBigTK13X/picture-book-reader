package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.ObservableCatalog;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.api.ApiClient;
import com.simplepathstudios.pbr.api.model.CategoryList;

import java.util.ArrayList;

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
        Data.setValue(ObservableCatalog.getInstance().getCategories());
        LoadingIndicator.setLoading(false);
    }
}
