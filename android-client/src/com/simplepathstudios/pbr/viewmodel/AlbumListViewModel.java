package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.api.ApiClient;
import com.simplepathstudios.pbr.api.model.AlbumList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumListViewModel extends ViewModel {

    public MutableLiveData<AlbumList> Data;
    public AlbumListViewModel(){
        Data = new MutableLiveData<AlbumList>();
    }

    public void load(){
        LoadingIndicator.setLoading(true);
        ApiClient.getInstance().getAlbumList().enqueue(new Callback< AlbumList >(){

            @Override
            public void onResponse(Call<AlbumList> call, Response<AlbumList> response) {
                LoadingIndicator.setLoading(false);
                Data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<AlbumList> call, Throwable t) {
                Util.error("AlbumListViewModel.load",t);
                LoadingIndicator.setLoading(false);
            }
        });
    }
}
