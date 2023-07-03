package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.api.ApiClient;
import com.simplepathstudios.pbr.api.model.MusicPlaylist;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RandomListViewModel extends ViewModel {
    public MutableLiveData<MusicPlaylist> Data;
    public RandomListViewModel(){
        Data = new MutableLiveData<MusicPlaylist>();
    }

    public void load(){
        LoadingIndicator.setLoading(true);
        ApiClient.getInstance().getRandomList().enqueue(new Callback< MusicPlaylist >(){
            @Override
            public void onResponse(Call<MusicPlaylist> call, Response<MusicPlaylist> response) {
                LoadingIndicator.setLoading(false);
                Data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<MusicPlaylist> call, Throwable t) {
                Util.error("RandomListViewModel.load",t);
                LoadingIndicator.setLoading(false);
            }
        });
    }
}
