package com.simplepathstudios.pbr.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.PBRSettings;
import com.simplepathstudios.pbr.api.ApiClient;
import com.simplepathstudios.pbr.api.model.ServerInfo;
import com.simplepathstudios.pbr.viewmodel.ObservableMusicQueue;
import com.simplepathstudios.pbr.viewmodel.ServerInfoViewModel;
import com.simplepathstudios.pbr.viewmodel.SettingsViewModel;

public class OptionsFragment extends Fragment {
    private static final String TAG = "OptionsFragment";
    private SettingsViewModel settingsViewModel;
    private ServerInfoViewModel serverInfoViewModel;
    private TextView versionText;
    private TextView errorText;
    private TextView userText;
    private Button debugLogToggle;
    private TextView debugLogStatus;
    private Button updatePBRButton;
    private Button clearLibraryButton;
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

        serverInfoViewModel = new ViewModelProvider(getActivity()).get(ServerInfoViewModel.class);
        serverInfoViewModel.Data.observe(getViewLifecycleOwner(), new Observer<ServerInfo>() {
            @Override
            public void onChanged(ServerInfo serverInfo) {
                Log.d(TAG, "Loaded serverInfo");
                versionText.setText(String.format(
                        "Client Version: %s\nServer Version: %s\nClient Built: %s\nServer Built: %s",
                        PBRSettings.ClientVersion,
                        serverInfo.version,
                        PBRSettings.BuildDate,
                        serverInfo.buildDate
                ));
            }
        });
        serverInfoViewModel.Error.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String error) {
                if(error != null){
                    Log.d(TAG, "An error occurred while loading "+error);
                    errorText.setText(error);
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

        String versionInfo = String.format("Client Version: %s\nServer Version: %s\nClient Built: %s\nServer Built: %s",PBRSettings.ClientVersion, "???",PBRSettings.BuildDate,"???");
        versionText = view.findViewById(R.id.version_text);
        versionText.setText(versionInfo);
        errorText = view.findViewById(R.id.error_text);

        userText = view.findViewById(R.id.user_text);
        userText.setText(String.format("Logged in as %s.", ApiClient.getInstance().getCurrentUser()));

        serverInfoViewModel.load();

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
