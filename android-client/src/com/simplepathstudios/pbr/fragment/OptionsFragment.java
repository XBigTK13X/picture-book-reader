package com.simplepathstudios.pbr.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.Random;

public class OptionsFragment extends Fragment {
    private static final String TAG = "OptionsFragment";
    private SettingsViewModel settingsViewModel;
    private TextView versionText;
    private Button debugLogToggle;
    private TextView debugLogStatus;
    private Button updatePBRButton;
    private Button clearLibraryButton;
    private Button rescanLibraryButton;
    private Button enableFullScreenButton;
    private TextView parentalUnlockWarning;
    private EditText parentalUnlockAnswer;
    private TextView libraryText;

    private boolean parentalLock;
    private int parentalCorrectAnswer;
    private int questionX;
    private int questionY;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.options_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parentalLock = true;
        parentalUnlockWarning = view.findViewById(R.id.parental_warning);
        parentalUnlockAnswer = view.findViewById(R.id.parental_answer);
        parentalCorrectAnswer = new Random().nextInt(50) + 50;
        questionX = new Random().nextInt(20) + 15;
        questionY = parentalCorrectAnswer - questionX;
        parentalUnlockWarning.setText("Enter the answer below to unlock these options. " + questionX + " + "+questionY+" = ?");


        parentalUnlockAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try{
                    String answerText = charSequence.toString();
                    Integer answer = Integer.parseInt(answerText);
                    parentalLock = !(parentalCorrectAnswer == answer);
                    if(!parentalLock){
                        Util.toast("Parental lock disabled.");
                        parentalUnlockAnswer.setVisibility(View.GONE);
                        parentalUnlockWarning.setVisibility(View.GONE);
                        debugLogToggle.setEnabled(true);
                        enableFullScreenButton.setEnabled(true);
                        rescanLibraryButton.setEnabled(true);
                        clearLibraryButton.setEnabled(true);
                        updatePBRButton.setEnabled(true);
                        MainActivity.getInstance().hideKeyboard();
                    }
                } catch(Exception e){
                    Util.toast("Invalid entry for parental unlock.");
                    Util.error(TAG, e);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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

        rescanLibraryButton = view.findViewById(R.id.rescan_library_button);
        rescanLibraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().toolbarHide();
                LoadingIndicator.setLoading(true);
                CentralCatalog.getInstance().importLibrary(true).doOnComplete(()->{
                    MainActivity.getInstance().toolbarShow();
                    LoadingIndicator.setLoading(false);
                }).subscribe();
            }
        });

        String versionInfo = String.format("Client Version: %s\nClient Built: %s",PBRSettings.ClientVersion, PBRSettings.BuildDate);
        versionText = view.findViewById(R.id.version_text);
        versionText.setText(versionInfo);

        enableFullScreenButton = view.findViewById(R.id.enable_fullscreen_button);
        enableFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.enableFullscreen();
            }
        });

        debugLogStatus = view.findViewById(R.id.debug_log_status);
        debugLogStatus.setText("Debug logging is "+(PBRSettings.EnableDebugLog ? "enabled" : "disabled"));

        debugLogToggle = view.findViewById(R.id.debug_log_toggle);
        debugLogToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setDebugLog(!PBRSettings.EnableDebugLog);
            }
        });

        debugLogToggle.setEnabled(false);
        enableFullScreenButton.setEnabled(false);
        rescanLibraryButton.setEnabled(false);
        clearLibraryButton.setEnabled(false);
        updatePBRButton.setEnabled(false);
    }
}
