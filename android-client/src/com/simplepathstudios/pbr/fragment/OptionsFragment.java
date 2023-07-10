package com.simplepathstudios.pbr.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.simplepathstudios.pbr.CentralCatalog;
import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.PBRSettings;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.viewmodel.SettingsViewModel;

public class OptionsFragment extends Fragment {
    private static final String TAG = "OptionsFragment";
    private SettingsViewModel settingsViewModel;
    private TextView versionText;
    private Button debugLogToggle;
    private TextView debugLogStatus;
    private Button updatePBRButton;
    private Button clearLibraryButton;
    private Button scanLibraryButton;
    private Button rescanLibraryButton;
    private TextView libraryText;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.options_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updatePBRButton = view.findViewById(R.id.download_update_button);
        updatePBRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, PBRSettings.UpdatePBRUrl);
                startActivity(intent);
            }
        });

        libraryText = view.findViewById(R.id.library_text);

        settingsViewModel = new ViewModelProvider(getActivity()).get(SettingsViewModel.class);
        settingsViewModel.Data.observe(getViewLifecycleOwner(), new Observer<SettingsViewModel.Settings>() {
            @Override
            public void onChanged(SettingsViewModel.Settings settings) {
                PBRSettings.EnableDebugLog = settings.EnableDebugLog;
                debugLogStatus.setText("Debug logging is "+(PBRSettings.EnableDebugLog ? "enabled" : "disabled"));
                if(settings.LibraryDirectory != null){
                    libraryText.setText(settings.LibraryDirectory.toString());
                } else {
                    libraryText.setText("No library chosen");
                }

            }
        });

        clearLibraryButton = view.findViewById(R.id.clear_library_button);
        clearLibraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsViewModel.setLibraryDirectory(null);
            }
        });

        scanLibraryButton = view.findViewById(R.id.scan_library_button);
        scanLibraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadingIndicator.setLoading(true);
                MainActivity.getInstance().toolbarHide();
                CentralCatalog.getInstance().importLibrary(false, true).doOnComplete(()->{
                    MainActivity.getInstance().toolbarShow();
                    LoadingIndicator.setLoading(false);
                }).subscribe();;
            }
        });

        rescanLibraryButton = view.findViewById(R.id.rescan_library_button);
        rescanLibraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().toolbarHide();
                LoadingIndicator.setLoading(true);
                CentralCatalog.getInstance().importLibrary(true, false).doOnComplete(()->{
                    MainActivity.getInstance().toolbarShow();
                    LoadingIndicator.setLoading(false);
                }).subscribe();
            }
        });

        String versionInfo = String.format("Client Version: %s\nClient Built: %s",PBRSettings.ClientVersion, PBRSettings.BuildDate);
        versionText = view.findViewById(R.id.version_text);
        versionText.setText(versionInfo);

        debugLogStatus = view.findViewById(R.id.debug_log_status);
        debugLogStatus.setText("Debug logging is "+(PBRSettings.EnableDebugLog ? "enabled" : "disabled"));

        debugLogToggle = view.findViewById(R.id.debug_log_toggle);
        debugLogToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setDebugLog(!PBRSettings.EnableDebugLog);
            }
        });
    }
}
