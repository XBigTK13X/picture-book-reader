package com.simplepathstudios.pbr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MotionEventCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.simplepathstudios.pbr.viewmodel.BookViewViewModel;
import com.simplepathstudios.pbr.viewmodel.SettingsViewModel;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    public static final int OPEN_LIBRARY_DIR_CODE = 123456;

    private static MainActivity __instance;

    public static MainActivity getInstance() {
        return __instance;
    }

    private NavController navController;
    private NavigationView navigationView;
    private LinearLayout mainLayout;
    private NavDestination currentLocation;

    private SettingsViewModel settingsViewModel;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ProgressBar loadingProgress;
    private TextView loadingText;


    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public boolean toolbarIsVisible(){
        return toolbar.getVisibility() == View.VISIBLE;
    }

    public void toolbarShow(){
        toolbar.setVisibility(View.VISIBLE);
    }

    public void toolbarHide(){
        toolbar.setVisibility(View.GONE);
    }

    public void navigateUp(){
        navController.navigateUp();
    }

    public boolean isCurrentLocation(String locationName){
        return currentLocation.getLabel().equals(locationName);
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        __instance = this;
        Util.setGlobalContext(this);

        Util.registerGlobalExceptionHandler();

        this.settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        this.settingsViewModel.initialize(this.getSharedPreferences("PBR", Context.MODE_PRIVATE));

        setContentView(R.layout.main_activity);

        SettingsViewModel.Settings settings = settingsViewModel.Data.getValue();
        settingsViewModel.Data.observe(MainActivity.getInstance(), new Observer<SettingsViewModel.Settings>() {
            @Override
            public void onChanged(SettingsViewModel.Settings settings) {
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
        Util.log(TAG, "====== Starting new app instance ======");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadingProgress = findViewById(R.id.loading_indicator);
        LoadingIndicator.setProgressBar(loadingProgress);
        loadingText = findViewById(R.id.loading_message);
        LoadingIndicator.setLoadingTextView(loadingText);

        drawerLayout = findViewById(R.id.main_activity_drawer);
        mainLayout = findViewById(R.id.main_activity_layout);
        navigationView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        // Pages that show full nav, not just the back button
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.category_list_fragment,
                R.id.options_fragment)
                .setDrawerLayout(drawerLayout)
                .build();
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                getSupportActionBar().setSubtitle("");
                CharSequence name = destination.getLabel();
                currentLocation = destination;
                if(name.toString().equals("Book")){
                    toolbar.setVisibility(View.GONE);
                } else {
                    toolbar.setVisibility(View.VISIBLE);
                }
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

        drawerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide the keyboard if touch event outside keyboard (better search experience)
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

        mainLayout.setVisibility(View.VISIBLE);
        navigationView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        Util.log(TAG, "Resuming with intent " + intent.getAction());
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
    }
}