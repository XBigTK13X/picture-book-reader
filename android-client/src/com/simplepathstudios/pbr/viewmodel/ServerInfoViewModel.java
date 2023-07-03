package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.api.ApiClient;
import com.simplepathstudios.pbr.api.model.ServerInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServerInfoViewModel extends ViewModel {
    public MutableLiveData<ServerInfo> Data;
    public MutableLiveData<String> Error;
    public ServerInfoViewModel(){
        Data = new MutableLiveData<>();
        Error = new MutableLiveData<>();
    }

    public void load() {
        ApiClient.getInstance().getServerInfo().enqueue(new Callback<ServerInfo>() {

            @Override
            public void onResponse(Call<ServerInfo> call, Response<ServerInfo> response) {
                LoadingIndicator.setLoading(false);
                Error.setValue(null);
                Data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<ServerInfo> call, Throwable t) {
                LoadingIndicator.setLoading(false);
                Error.setValue("An error occurred while checking the server\n["+t.getMessage()+"]");
                Util.error("ServerInfoViewModel.load",t);
            }
        });
    }
}
