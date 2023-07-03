package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.api.model.ArtistView;

public class InterDestinationViewModel extends ViewModel {
    public MutableLiveData<InterDestinationModel> Data;
    public InterDestinationViewModel(){
        Data = new MutableLiveData<InterDestinationModel>();
        Data.setValue(new InterDestinationModel());
    }
    public void setArtist(String artist){
        InterDestinationModel model = Data.getValue();
        model.Artist = artist;
        Data.setValue(model);
    }
    private class InterDestinationModel {
        public String Artist;
    }
}
