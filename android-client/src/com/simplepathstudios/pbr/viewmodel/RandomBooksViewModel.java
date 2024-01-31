package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.CentralCatalog;
import com.simplepathstudios.pbr.api.model.Book;
import com.simplepathstudios.pbr.api.model.CategoryList;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class RandomBooksViewModel extends ViewModel {
   public MutableLiveData<ArrayList<Book>> Data;
   public RandomBooksViewModel(){
      Data = new MutableLiveData<>();
      Data.setValue(new ArrayList<>());
   }

   public void addBook(Book book){
      ArrayList<Book> books = Data.getValue();
      books.add(0,book);
      if(books.size() > 100){
         books.remove(books.size() - 1);
      }
      Data.setValue(books);
   }
}
