package com.simplepathstudios.pbr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.material.navigation.NavigationView;
import com.simplepathstudios.pbr.api.ApiClient;
import com.simplepathstudios.pbr.api.model.MusicPlaylistListItem;
import com.simplepathstudios.pbr.api.model.MusicQueue;
import com.simplepathstudios.pbr.api.model.PlaylistList;
import com.simplepathstudios.pbr.audio.AudioPlayer;
import com.simplepathstudios.pbr.viewmodel.ObservableMusicQueue;
import com.simplepathstudios.pbr.viewmodel.PlaylistListViewModel;
import com.simplepathstudios.pbr.viewmodel.SettingsViewModel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private final int SEEK_BAR_UPDATE_MILLISECONDS = 350;
    public static final int OPEN_LIBRARY_DIR_CODE = 123456;

    private static MainActivity __instance;

    public static MainActivity getInstance() {
        return __instance;
    }

    private NavController navController;
    private NavigationView navigationView;
    private LinearLayout mainLayout;
    private LinearLayout simpleMainLayout;

    private SettingsViewModel settingsViewModel;
    private ObservableMusicQueue observableMusicQueue;
    private MusicQueue queue;
    private PlaylistListViewModel playlistListViewModel;
    private PlaylistList playlistListData;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ProgressBar loadingView;
    private ImageButton simpleUiMusicButton;
    private AudioPlayer audioPlayer;
    private NavDestination currentLocation;

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public void setActionBarSubtitle(String subtitle) {
        getSupportActionBar().setSubtitle(subtitle);
    }

    public ArrayList<MusicPlaylistListItem> getPlaylists(){
        return playlistListData.list;
    }

    public void refreshPlaylists(){
        playlistListViewModel.load();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == OPEN_LIBRARY_DIR_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                settingsViewModel.setLibraryDirectory(uri);
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        __instance = this;


        Util.registerGlobalExceptionHandler();

        this.settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        this.settingsViewModel.initialize(this.getSharedPreferences("PBR", Context.MODE_PRIVATE));

        setContentView(R.layout.main_activity);

        SettingsViewModel.Settings settings = settingsViewModel.Data.getValue();
        settingsViewModel.Data.observe(MainActivity.getInstance(), new Observer<SettingsViewModel.Settings>() {
            @Override
            public void onChanged(SettingsViewModel.Settings settings) {
                Util.log(TAG, "Definitely changed some settings");
                if(settings.LibraryDirectory == null){
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, MainActivity.OPEN_LIBRARY_DIR_CODE);
                }
            }
        });
        if(PBRSettings.DebugResourceLeaks) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                    .detectLeakedClosableObjects()
                    .build());
        }
        ApiClient.retarget(settings.ServerUrl, settings.Username);
        Util.log(TAG, "====== Starting new app instance ======");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadingView = findViewById(R.id.loading_indicator);
        LoadingIndicator.setProgressBar(loadingView);

        observableMusicQueue = ObservableMusicQueue.getInstance();

        drawerLayout = findViewById(R.id.main_activity_drawer);
        mainLayout = findViewById(R.id.main_activity_layout);
        simpleMainLayout = findViewById(R.id.simple_ui_main_activity_layout);
        navigationView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.category_list_fragment,
                R.id.queue_fragment,
                R.id.album_list_fragment,
                R.id.artist_list_fragment,
                R.id.search_fragment,
                R.id.options_fragment,
                R.id.artist_view_fragment,
                R.id.album_view_fragment,
                R.id.playlist_view_fragment,
                R.id.playlist_list_fragment,
                R.id.random_list_fragment,
                R.id.now_playing_fragment)
                .setDrawerLayout(drawerLayout)
                .build();
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                getSupportActionBar().setSubtitle("");
                CharSequence name = destination.getLabel();
                currentLocation = destination;
                if (arguments != null && arguments.size() > 0) {
                    String category = arguments.getString("Category");
                    if (category != null) {
                        getSupportActionBar().setTitle(category);
                    } else {
                        getSupportActionBar().setTitle(name);
                    }
                } else {
                    getSupportActionBar().setTitle(name);
                }

            }
        });
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                navController.navigate(menuItem.getItemId());
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Hide the keyboard if touch event outside keyboard (better search experience)
        findViewById(R.id.main_activity_drawer).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    View focus = getCurrentFocus();
                    if (focus != null) {
                        imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });

        simpleUiMusicButton = findViewById(R.id.simple_ui_music_button);
        simpleUiMusicButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AudioPlayer audioPlayer = AudioPlayer.getInstance();
                if(audioPlayer.isPlaying()){
                    audioPlayer.pause();
                } else {
                    audioPlayer.play();
                }
            }
        });

        if (settings.EnableSimpleUIMode) {
            mainLayout.setVisibility(View.GONE);
            navigationView.setVisibility(View.GONE);
            simpleMainLayout.setVisibility(View.VISIBLE);
            ObservableMusicQueue.getInstance().setRepeatMode(ObservableMusicQueue.RepeatMode.All);
        } else {
            mainLayout.setVisibility(View.VISIBLE);
            navigationView.setVisibility(View.VISIBLE);
            simpleMainLayout.setVisibility(View.GONE);
        }


        playlistListViewModel = new ViewModelProvider(MainActivity.getInstance()).get(PlaylistListViewModel.class);
        playlistListViewModel.Data.observe(MainActivity.getInstance(), new Observer<PlaylistList>() {
            @Override
            public void onChanged(PlaylistList playlistList) {
                playlistListData = playlistList;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        // If this happens before cast context discovery is complete, then the menu button won't appear
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        Util.log(TAG, "Resuming with intent " + intent.getAction());
        audioPlayer = AudioPlayer.getInstance();
        //New SDK logic ObservableCastContext.getInstance().reconnect();
    }

    @Override
    public void onPause() {
        super.onPause();
        Util.log(TAG, "Pausing");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Util.log(TAG, "Destroying");
        audioPlayer.destroy();
        audioPlayer = null;
    }
}