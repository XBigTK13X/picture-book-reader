package com.simplepathstudios.pbr.viewmodel;

import android.content.SharedPreferences;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.PBRSettings;

import java.net.URLEncoder;

public class SettingsViewModel extends ViewModel {
    public MutableLiveData<Settings> Data;
    public SettingsViewModel(){
        Data = new MutableLiveData<>();
    }

    public void initialize(SharedPreferences preferences){
        Settings settings = new Settings();
        settings.Preferences = preferences;
        settings.Username = settings.Preferences.getString("Username",null);
        settings.ServerUrl = settings.Preferences.getString("ServerUrl", PBRSettings.ProdServerUrl);
        settings.EnableDebugLog = settings.Preferences.getBoolean("EnableDebugLog", false);
        settings.InternalMediaVolume = settings.Preferences.getFloat("InternalMediaVolume", 1.0f);
        settings.EnableSimpleUIMode = settings.Preferences.getBoolean("EnableSimpleUIMode", false);
        settings.LibraryDirectory = null;
        String uriString = settings.Preferences.getString("LibraryDirectory", null);
        if(uriString != null){
            settings.LibraryDirectory = Uri.parse(uriString);
        }
        PBRSettings.EnableDebugLog = settings.EnableDebugLog;
        PBRSettings.InternalMediaVolume = settings.InternalMediaVolume;
        Data.setValue(settings);
    }

    public void setDebugLog(boolean enabled){
        Settings settings = Data.getValue();
        settings.EnableDebugLog = enabled;
        SharedPreferences.Editor editor = settings.Preferences.edit();
        editor.putBoolean("EnableDebugLog", enabled);
        editor.commit();
        Data.setValue(settings);
        PBRSettings.EnableDebugLog = settings.EnableDebugLog;
    }

    public void setLibraryDirectory(Uri directory){
        Settings settings = Data.getValue();
        settings.LibraryDirectory = directory;
        SharedPreferences.Editor editor = settings.Preferences.edit();
        if(directory == null){
            editor.putString("LibraryDirectory", null);
        }
        else {
            editor.putString("LibraryDirectory", settings.LibraryDirectory.toString());
        }
        editor.commit();
        Data.setValue(settings);
        PBRSettings.LibraryDirectory = settings.LibraryDirectory;
    }

    public class Settings {
        public String Username;
        public String ServerUrl;
        public SharedPreferences Preferences;
        public boolean EnableDebugLog;
        public double InternalMediaVolume;
        public boolean EnableSimpleUIMode;
        public Uri LibraryDirectory;
    }
}
