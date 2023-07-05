package com.simplepathstudios.pbr.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.pbr.ObservableCatalog;
import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.adapter.CategoryAdapter;
import com.simplepathstudios.pbr.api.model.Book;
import com.simplepathstudios.pbr.api.model.CategoryList;
import com.simplepathstudios.pbr.viewmodel.CategoryListViewModel;
import com.simplepathstudios.pbr.viewmodel.SettingsViewModel;

import java.util.ArrayList;

public class CategoryListFragment extends Fragment {
    private final String TAG = "CategoryListFragment";
    private final int COLUMNS = 8;
    private RecyclerView listElement;
    private CategoryAdapter adapter;
    private LinearLayoutManager layoutManager;
    private CategoryListViewModel viewModel;
    private SettingsViewModel settingsViewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.category_list_fragment, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listElement = view.findViewById(R.id.category_list);
        adapter = new CategoryAdapter();
        listElement.setAdapter(adapter);
        layoutManager = new GridLayoutManager(getActivity(), COLUMNS);
        listElement.setLayoutManager(layoutManager);
        viewModel = new ViewModelProvider(this).get(CategoryListViewModel.class);
        viewModel.Data.observe(getViewLifecycleOwner(), new Observer<CategoryList>() {
            @Override
            public void onChanged(CategoryList categoryList) {
                adapter.setData(categoryList.list);
                adapter.notifyDataSetChanged();
            }
        });

        this.settingsViewModel = new ViewModelProvider(MainActivity.getInstance()).get(SettingsViewModel.class);
        settingsViewModel.Data.observe(MainActivity.getInstance(), new Observer<SettingsViewModel.Settings>() {
            @Override
            public void onChanged(SettingsViewModel.Settings settings) {
                if(settings.LibraryDirectory != null){
                    if(!ObservableCatalog.getInstance().hasBooks()){
                        ObservableCatalog.getInstance().importLibrary();
                    }
                    viewModel.load();
                }
            }
        });
    }
}

