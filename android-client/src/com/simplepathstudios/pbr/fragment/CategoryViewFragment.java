package com.simplepathstudios.pbr.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.pbr.MainActivity;
import com.simplepathstudios.pbr.R;
import com.simplepathstudios.pbr.Util;
import com.simplepathstudios.pbr.adapter.BookAdapter;
import com.simplepathstudios.pbr.adapter.SongAdapter;
import com.simplepathstudios.pbr.api.model.AlbumView;
import com.simplepathstudios.pbr.api.model.CategoryView;
import com.simplepathstudios.pbr.api.model.MusicAlbum;
import com.simplepathstudios.pbr.api.model.MusicFile;
import com.simplepathstudios.pbr.viewmodel.AlbumViewViewModel;
import com.simplepathstudios.pbr.viewmodel.CategoryViewViewModel;
import com.simplepathstudios.pbr.viewmodel.ObservableMusicQueue;

public class CategoryViewFragment extends Fragment {
   private final String TAG = "CategoryViewFragment";

   private ObservableMusicQueue observableMusicQueue;
   private CategoryViewViewModel categoryViewModel;
   private String categoryName;
   private RecyclerView listElement;
   private BookAdapter adapter;
   private LinearLayoutManager layoutManager;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      categoryName = getArguments().getString("CategoryName");
      MainActivity.getInstance().setActionBarTitle(categoryName);
      MainActivity.getInstance().setActionBarSubtitle("Category");
      return inflater.inflate(R.layout.album_view_fragment, container, false);
   }

   @Override
   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      observableMusicQueue = ObservableMusicQueue.getInstance();
      listElement = view.findViewById(R.id.album_songs);
      adapter = new BookAdapter();
      listElement.setAdapter(adapter);
      layoutManager = new LinearLayoutManager(getActivity());
      listElement.setLayoutManager(layoutManager);
      categoryViewModel = new ViewModelProvider(this).get(CategoryViewViewModel.class);
      categoryViewModel.Data.observe(getViewLifecycleOwner(), new Observer<CategoryView>() {
         @Override
         public void onChanged(CategoryView categoryView) {
            adapter.setData(categoryView.Books);
            adapter.notifyDataSetChanged();
         }
      });
      categoryViewModel.load(null);
   }
}
