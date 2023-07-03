package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.api.ApiClient;
import com.simplepathstudios.pbr.api.model.AlbumView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumViewViewModel extends ViewModel {
    public MutableLiveData<AlbumView> Data;
    public AlbumViewViewModel(){
        Data = new MutableLiveData<AlbumView>();
    }

    public void load(String albumSlug){
        LoadingIndicator.setLoading(true);
        ApiClient.getInstance().getAlbumView(albumSlug).enqueue(new Callback< AlbumView >(){
            @Override
            public void onResponse(Call<AlbumView> call, Response<AlbumView> response) {
                LoadingIndicator.setLoading(false);
                Data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<AlbumView> call, Throwable t) {
                Util.error("ArtistListViewModel", t);
                LoadingIndicator.setLoading(false);
            }
        });
    }
}
