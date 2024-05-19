package com.simplepathstudios.pbr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.Observer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simplepathstudios.pbr.api.model.Book;
import com.simplepathstudios.pbr.api.model.BookCategory;
import com.simplepathstudios.pbr.api.model.BookView;
import com.simplepathstudios.pbr.api.model.CategoryList;
import com.simplepathstudios.pbr.api.model.CategoryView;
import com.simplepathstudios.pbr.api.model.SearchResults;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
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
   private HashMap<String, ArrayList<Book>> categoriesLookup;
   private ArrayList<BookCategory> categoriesList;
   private HashMap<String, Book> bookLookup;
   private ArrayList<Book> bookList;
   private File cachedCatalogFile;
   private HashMap<String, String> bookThumbnailLookup;
   private String thumbnailReport;
   private CentralCatalog(){
      this.categoriesLookup = new HashMap<>();
      this.categoriesList = new ArrayList<>();
      this.bookList = new ArrayList<>();
      this.bookLookup = new HashMap<>();
      this.bookThumbnailLookup = new HashMap<>();
      String filesRoot = MainActivity.getInstance().getFilesDir().getAbsolutePath();
      this.cachedCatalogFile = new File(Paths.get(filesRoot,"data/","catalog.json").toString());
   }

   public Observable<Boolean> importLibrary(boolean cleanScan){
      if(cleanScan){
         LoadingIndicator.setLoadingMessage("Cleaning up leftover folders");
         Util.clean("data/");
      }
      else {
         try {
            if (cachedCatalogFile.exists()) {
               LoadingIndicator.setLoadingMessage("Reading cached library");
               BufferedReader fileReader = new BufferedReader(new FileReader(cachedCatalogFile));
               Gson gson = new Gson();
               CentralCatalog cachedCatalog = gson.fromJson(fileReader, CentralCatalog.class);
               categoriesList = cachedCatalog.categoriesList;
               categoriesLookup = cachedCatalog.categoriesLookup;
               bookLookup = cachedCatalog.bookLookup;
               bookList = cachedCatalog.bookList;
               bookThumbnailLookup = cachedCatalog.bookThumbnailLookup;
               for(BookCategory category : categoriesList){
                  CategoryView categoryView = getBooks(category.Name);
                  category.ThumbnailIndex = new Random().nextInt(categoryView.Books.size());
               }
               LoadingIndicator.setLoading(false);
               return Observable.fromCallable(()-> true);
            }
         }
         catch(Throwable t){
            Util.error(TAG, t);
         }
      }
      Util.log(TAG,"Attempting to read ["+PBRSettings.LibraryDirectory+"]");
      DocumentFile libraryRoot = DocumentFile.fromTreeUri(MainActivity.getInstance(), PBRSettings.LibraryDirectory);
      try {
         MainActivity.getInstance().getContentResolver().takePersistableUriPermission(PBRSettings.LibraryDirectory, Intent.FLAG_GRANT_READ_URI_PERMISSION);
      }
      catch(Exception e){
         Util.log(TAG, "Unable to import the library");
         Util.error(TAG, e);
      }
      LoadingIndicator.setLoadingMessage("Starting a clean import");
      this.categoriesLookup = new HashMap<>();
      this.categoriesList = new ArrayList<>();
      this.bookLookup = new HashMap<>();
      this.bookList = new ArrayList<>();
      this.bookThumbnailLookup = new HashMap<>();
      return Observable.fromCallable(() -> {
                 int bookIndex = 0;
                 int bookCount = 0;
                 LoadingIndicator.setLoadingMessage("Indexing all thumbnails");
                 DocumentFile[] categories = libraryRoot.listFiles();
                 // Top level is folders (categories)}
                 for (DocumentFile category : categories) {
                    if (category.getName().equals(".thumbnails")) {
                       for (DocumentFile thumb : category.listFiles()) {
                          if (thumb.isFile()) {
                             bookThumbnailLookup.put(thumb.getName().replace(".jpg", ""), thumb.getUri().toString());
                          }
                       }
                    } else {
                       if(category.getName().charAt(0) == '.'){
                          continue;
                       }
                       // Next level is files (books)
                       for (DocumentFile book : category.listFiles()) {
                          bookCount++;
                       }
                    }
                 }
                 for (DocumentFile category : categories) {
                    if (category.getName().equals(".thumbnails")) {
                       continue;
                    }
                    if(category.getName().charAt(0) == '.'){
                       continue;
                    }
                    DocumentFile[] books = category.listFiles();
                    ArrayList<Book> parsedBooks = new ArrayList<Book>();
                    for (DocumentFile bookFile : books) {
                       String loadingMessage = "(" + (++bookIndex) + "/" + bookCount + ") Indexing pages for\n[" + bookFile.getName() + "]";
                       LoadingIndicator.setLoadingMessage(loadingMessage);
                       Book book = new Book();
                       book.TreeUri = bookFile.getUri().toString();
                       book.Name = bookFile.getName();
                       book.CategoryName = category.getName();
                       book.SearchSlug = book.CategoryName.toLowerCase() + "-" + book.Name.toLowerCase();
                       book.CompareSlug = book.Name.toLowerCase();
                       book.View = new BookView();
                       book.View.Name = book.Name;
                       DocumentFile[] pages = bookFile.listFiles();
                       for(DocumentFile pageFile: pages){
                          if(pageFile.isDirectory()){
                             continue;
                          }
                          String entryName = pageFile.getName();
                          if (entryName.endsWith(".xml") || entryName.endsWith(".txt")) {
                             //TODO Parse text files for metadata
                             book.View.Info.put(entryName, null);
                          } else {
                             book.View.Pages.put(entryName, pageFile.getUri().toString());
                             book.View.PageIds.add(entryName);
                          }
                       }
                       book.View.sortPages();
                       parsedBooks.add(book);
                    }

                    CentralCatalog.getInstance().addCategory(category.getName(), parsedBooks);
                 }
                 cachedCatalogFile.getParentFile().mkdirs();
                 Gson gson = new GsonBuilder().setPrettyPrinting().create();
                 FileOutputStream writeStream = new FileOutputStream(cachedCatalogFile);
                 BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(writeStream));
                 gson.toJson(this, CentralCatalog.class, fileWriter);
                 fileWriter.close();
                 Util.toast("Library import complete. Found " + bookCount + " books");
                 return true;
              })
              .subscribeOn(Schedulers.newThread())
              .observeOn(AndroidSchedulers.mainThread());
   }

   public byte[] getBookThumbnail(String category, String book) {
      try{
         InputStream thumbStream = MainActivity.getInstance().getContentResolver().openInputStream(Uri.parse(bookThumbnailLookup.get(book.replace(".cbz",""))));
         return Util.readAllBytes(thumbStream);
      }
      catch(Throwable e){
         Util.error(TAG, e);
      }
      return null;
   }

   public byte[] getCategoryThumbnail(String category, Integer thumbnailIndex){
      ArrayList<Book> books = getBooks(category).Books;
      Book firstBook = books.get(thumbnailIndex);
      return getBookThumbnail(category, firstBook.Name);
   }

   public String getBookKey(String categoryName, String bookName){
      return categoryName + "-=-" + bookName;
   }

   public void addCategory(String name, ArrayList<Book> books){
      if(!books.isEmpty()){
         books.sort(new Comparator<Book>() {
            @Override
            public int compare(Book o1, Book o2) {
               return o1.CompareSlug.compareTo(o2.CompareSlug);
            }
         });
         categoriesLookup.put(name, books);
         bookList.addAll(books);
         BookCategory category = new BookCategory();
         category.Name = name;
         category.ThumbnailIndex = new Random().nextInt(books.size());
         categoriesList.add(category);
         for(Book book : books){
            bookLookup.put(getBookKey(name, book.Name), book);
         }
      }
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

   public Book getRandomBook(){
      BookCategory category = categoriesList.get(new Random().nextInt(categoriesList.size()));
      ArrayList<Book> books = categoriesLookup.get(category.Name);
      return books.get(new Random().nextInt(books.size()));
   }

   public Book getBook(String categoryName, String bookName){
      return bookLookup.get(getBookKey(categoryName, bookName));
   }

   public SearchResults search(String needle){
      SearchResults results = new SearchResults();
      needle = needle.toLowerCase();
      for(Book book : bookList){
         if(book.SearchSlug.contains(needle)){
            results.Books.add(book);
         }
      }
      results.Books.sort(new Comparator<Book>() {
         @Override
         public int compare(Book o1, Book o2) {
            return o1.CompareSlug.compareTo(o2.CompareSlug);
         }
      });
      return results;
   }

   public String getThumbnailReport(){
      if(thumbnailReport != null){
         return thumbnailReport;
      }
      String report = "Books missing thumbnails:";
      boolean allMatched = true;
      for(Book book:bookList){
         if(!bookThumbnailLookup.containsKey(book.Name)){
            allMatched = false;
            report += "\n\t[" + book.CategoryName + "] " + book.Name;
         }
      }
      if(allMatched){
         report += "\n\tAll books matched to thumbnails.";
      }
      thumbnailReport = report;
      return report;
   }
}
