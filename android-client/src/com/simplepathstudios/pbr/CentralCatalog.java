package com.simplepathstudios.pbr;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.Observer;

import com.simplepathstudios.pbr.api.model.Book;
import com.simplepathstudios.pbr.api.model.BookCategory;
import com.simplepathstudios.pbr.api.model.BookView;
import com.simplepathstudios.pbr.api.model.CategoryList;
import com.simplepathstudios.pbr.api.model.CategoryView;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CentralCatalog {
   public final String TAG = "ObservableCatalog";

   private static CentralCatalog __instance;
   public static CentralCatalog getInstance(){
      if(__instance == null){
         __instance = new CentralCatalog();
      }
      return __instance;
   }
   private ArrayList<Observer<CentralCatalog>> observers;
   private HashMap<String, ArrayList<Book>> categoriesLookup;
   private ArrayList<BookCategory> categoriesList;
   private HashMap<String, Book> bookLookup;

   private CentralCatalog(){
      this.observers = new ArrayList<>();
      this.categoriesLookup = new HashMap<>();
      this.categoriesList = new ArrayList<>();
      this.bookLookup = new HashMap<>();
   }

   public void observe(Observer<CentralCatalog> observer){
      observers.add(observer);
      observer.onChanged(this);
   }

   private void notifyObservers(){
      for(Observer<CentralCatalog> observer: observers) {
         observer.onChanged(this);
      }
   }

   public Observable<Boolean> importLibrary(boolean force){
      if(force){
         Util.clean("thumbnails/");
         Util.clean("extract-thumbnails/");
         Util.clean("extract-book/");
      }
      DocumentFile libraryRoot = DocumentFile.fromTreeUri(MainActivity.getInstance(), PBRSettings.LibraryDirectory);

      return Observable.fromCallable(()->{
         int bookIndex = 0;
         int bookCount = 0;
         DocumentFile[] categories = libraryRoot.listFiles();
         // Top level is folders (categories)}
         for(DocumentFile category : categories){
            // Next level is files (books)
            for(DocumentFile book : category.listFiles()){
               bookCount++;
            }
         }
         for(DocumentFile category : categories){
            DocumentFile[] books = category.listFiles();
            ArrayList<Book> parsedBooks = new ArrayList<Book>();
            for(DocumentFile bookFile : books){
               String loadingMessage = "(" + (++bookIndex) + "/" + bookCount + ") Generating thumbnail for [" + bookFile.getName() + "]";
                 LoadingIndicator.setLoadingMessage(loadingMessage);
                 Book book = new Book();
                 book.TreeUri = bookFile.getUri();
                 book.Name = bookFile.getName();
                 book.CategoryName = category.getName();
                 parsedBooks.add(book);
                 generateThumbnail(getBookKey(book.CategoryName, book.Name), book.TreeUri);
            };
            CentralCatalog.getInstance().addCategory(category.getName(), parsedBooks);
           }
         return true;
      })
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread());
   }

   public File getBookThumbnail(String category, String book) {
      return getThumbnail(getBookKey(category, book));
   }

   private File getThumbnail(String bookKey){
      return new File(Paths.get(MainActivity.getInstance().getFilesDir().getAbsolutePath(), "thumbnails", bookKey+".jpeg").toString());
   }

   public File generateThumbnail(String bookKey, Uri archiveUri){
      File file = getThumbnail(bookKey);
      if(file != null && file.exists()) {
         return file;
      }
      ArrayList<File> files = ZipUtil.extract(archiveUri, "extract-thumbnails/", false);
      try {
         for (File extractedFile : files) {
            if (extractedFile.isDirectory()) {
               continue;
            }
            String entryName = extractedFile.getName();
            if (!entryName.endsWith(".xml") && !entryName.endsWith(".txt")) {
               Bitmap bitmap = BitmapFactory.decodeFile(extractedFile.getAbsolutePath());
               if (bitmap != null) {
                  Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, 500, 500, true);
                  File thumbnailFile = getThumbnail(bookKey);
                  thumbnailFile.getParentFile().mkdirs();
                  FileOutputStream thumbnailStream = new FileOutputStream(thumbnailFile);
                  thumbnail.compress(Bitmap.CompressFormat.JPEG, 95, thumbnailStream);
                  thumbnail.recycle();
                  bitmap.recycle();
                  Util.clean("extract-thumbnails/");
                  return thumbnailFile;
               }
            }
         }

      } catch(Throwable e){
         Util.error(TAG, e);
      }
      return null;
   }

   public String getBookKey(String categoryName, String bookName){
      return categoryName + "-=-" + bookName;
   }

   public void addCategory(String name, ArrayList<Book> books){
      books.sort(new Comparator<Book>() {
         @Override
         public int compare(Book o1, Book o2) {
            return o1.Name.toLowerCase().compareTo(o2.Name.toLowerCase());
         }
      });
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

   public Observable<BookView> getBook(String categoryName, String bookName){
      return Observable.fromCallable(()-> {
         Book book = bookLookup.get(getBookKey(categoryName, bookName));
         Util.clean("extract-book/");
         ArrayList<File> extractedFiles = ZipUtil.extract(book.TreeUri, "extract-book/", true);
         BookView bookView = new BookView();
         bookView.Name = book.Name;
         bookView.TreeUi = book.TreeUri;
         try {
            for (File file : extractedFiles) {
               if (file.isDirectory()) {
                  continue;
               }
               String entryName = file.getName();
               if (entryName.endsWith(".xml") || entryName.endsWith(".txt")) {
                  //TODO Parse text files for metadata
                  bookView.Info.put(entryName, null);
               } else {
                  bookView.Pages.put(entryName, file);
                  bookView.PageIds.add(entryName);
               }
            }
            bookView.PageIds.sort(new Comparator<String>() {
               @Override
               public int compare(String o1, String o2) {
                  return o1.toLowerCase().compareTo(o2.toLowerCase());
               }
            });
         } catch (Exception e) {
            Util.error(TAG, e);
         }
         return bookView;
      })
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread());
   }
}
