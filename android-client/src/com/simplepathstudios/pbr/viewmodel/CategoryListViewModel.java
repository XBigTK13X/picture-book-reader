package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.CentralCatalog;
import com.simplepathstudios.pbr.api.model.CategoryList;

public class CategoryListViewModel extends ViewModel {
    public MutableLiveData<CategoryList> Data;
    public CategoryListViewModel(){
        Data = new MutableLiveData<CategoryList>();
    }

    public void load(){
        LoadingIndicator.setLoading(true);
        Data.setValue(CentralCatalog.getInstance().getCategories());
        LoadingIndicator.setLoading(false);
    }
}
