package com.simplepathstudios.pbr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.lifecycle.Observer;

import com.google.android.gms.common.util.IOUtils;
import com.simplepathstudios.pbr.api.model.Book;
import com.simplepathstudios.pbr.api.model.BookCategory;
import com.simplepathstudios.pbr.api.model.BookView;
import com.simplepathstudios.pbr.api.model.CategoryList;
import com.simplepathstudios.pbr.api.model.CategoryView;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;

public class ObservableCatalog {
   public final String TAG = "ObservableCatalog";

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
   private HashMap<String, Book> bookLookup;

   private ObservableCatalog(){
      this.observers = new ArrayList<>();
      this.categoriesLookup = new HashMap<>();
      this.categoriesList = new ArrayList<>();
      this.bookLookup = new HashMap<>();
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

   public String getBookKey(String categoryName, String bookName){
      return categoryName + "::" + bookName;
   }

   public void addCategory(String name, ArrayList<Book> books){
      categoriesLookup.put(name, books);
      BookCategory category = new BookCategory();
      category.Name = name;
      categoriesList.add(category);
      for(Book book : books){
         bookLookup.put(getBookKey(name, book.Name), book);
      }
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

   public CategoryView getBooks(String categoryName){
      CategoryView categoryView = new CategoryView();
      categoryView.Books = categoriesLookup.get(categoryName);
      return categoryView;
   }

   public BookView getBook(String categoryName, String bookName){
      Book book = bookLookup.get(getBookKey(categoryName,bookName));
      BookView bookView = new BookView();
      bookView.Name = book.Name;
      bookView.TreeUi = book.TreeUri;
      //TODO Add some progress bar or something when opening the book
      try {
         InputStream safInputStream = MainActivity.getInstance().getContentResolver().openInputStream(book.TreeUri);
         byte[] zipBytes = IOUtils.toByteArray(safInputStream);
         ZipFile bookArchive = new ZipFile(new SeekableInMemoryByteChannel(zipBytes));
         Enumeration<ZipArchiveEntry> entries = bookArchive.getEntries();
         while(entries.hasMoreElements()){
            ZipArchiveEntry entry = entries.nextElement();
            if(entry.isDirectory()){
               continue;
            }
            String entryName = entry.getName();
            if(entryName.endsWith(".xml") || entryName.endsWith(".txt")){
               //TODO Parse text
               bookView.Info.put(entryName, null);
            }
            else {
               InputStream zipStream = bookArchive.getInputStream(entry);
               byte[] rawImage = new byte[(int) entry.getSize()];
               int ii = 0;
               while (ii < rawImage.length) {
                  ii += zipStream.read(rawImage, ii, rawImage.length - ii);
               }
               Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage,0,rawImage.length);
               bookView.Pages.put(entryName, bitmap);
               bookView.PageIds.add(entryName);
            }
         }
      }
      catch (IOException e) {
         Util.error(TAG, e);
      }
      return bookView;
   }
}
