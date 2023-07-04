package com.simplepathstudios.pbr;

import androidx.lifecycle.Observer;

import com.simplepathstudios.pbr.api.model.Book;
import com.simplepathstudios.pbr.api.model.BookCategory;
import com.simplepathstudios.pbr.api.model.CategoryList;
import com.simplepathstudios.pbr.api.model.MusicFile;
import com.simplepathstudios.pbr.api.model.MusicQueue;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ObservableCatalog {
   private static ObservableCatalog __instance;
   public static ObservableCatalog getInstance(){
      if(__instance == null){
         __instance = new ObservableCatalog();
      }
      return __instance;
   }
   private ArrayList<Observer<ObservableCatalog>> observers;
   private HashMap<String, ArrayList<Book>> categoriesLookup;
   private ArrayList<BookCategory> categoriesList;

   private ObservableCatalog(){
      this.observers = new ArrayList<>();
      this.categoriesLookup = new HashMap<>();
      this.categoriesList = new ArrayList<>();
   }

   public void observe(Observer<ObservableCatalog> observer){
      observers.add(observer);
      observer.onChanged(this);
   }

   private void notifyObservers(){
      for(Observer<ObservableCatalog> observer: observers) {
         observer.onChanged(this);
      }
   }

   public void addCategory(String name, ArrayList<Book> books){
      categoriesLookup.put(name, books);
      BookCategory category = new BookCategory();
      category.Name = name;
      categoriesList.add(category);
      notifyObservers();
   }

   public CategoryList getCategories(){
      CategoryList categoryList = new CategoryList();
      categoryList.list = categoriesList;
      return categoryList;
   }

   public boolean hasBooks(){
      return ! categoriesLookup.isEmpty();
   }
}
