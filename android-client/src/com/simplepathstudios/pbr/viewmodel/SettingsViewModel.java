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
        settings.EnableDebugLog = true;//settings.Preferences.getBoolean("EnableDebugLog", false);
        settings.LibraryDirectory = null;
        String uriString = settings.Preferences.getString("LibraryDirectory", null);
        if(uriString != null){
            settings.LibraryDirectory = Uri.parse(uriString);
        }
        PBRSettings.EnableDebugLog = settings.EnableDebugLog;
        PBRSettings.LibraryDirectory = settings.LibraryDirectory;
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
        public SharedPreferences Preferences;
        public boolean EnableDebugLog;
        public Uri LibraryDirectory;
    }
}
