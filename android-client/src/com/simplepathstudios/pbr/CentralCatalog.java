package com.simplepathstudios.pbr;

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

   private CentralCatalog(){
      this.categoriesLookup = new HashMap<>();
      this.categoriesList = new ArrayList<>();
      this.bookList = new ArrayList<>();
      this.bookLookup = new HashMap<>();
      this.bookThumbnailLookup = new HashMap<>();
      this.cachedCatalogFile = new File(Paths.get(MainActivity.getInstance().getFilesDir().getAbsolutePath(),"data/","catalog.json").toString());
   }

   public Observable<Boolean> importLibrary(boolean cleanScan){
      if(cleanScan){
         Util.clean("thumbnails/");
         Util.clean("extract-thumbnails/");
         Util.clean("extract-book/");
         Util.clean("data/");
      }
      else {
         try {
            if (cachedCatalogFile.exists()) {
               LoadingIndicator.setLoading(false);
               BufferedReader fileReader = new BufferedReader(new FileReader(cachedCatalogFile));
               Gson gson = new Gson();
               CentralCatalog cachedCatalog = gson.fromJson(fileReader, CentralCatalog.class);
               categoriesList = cachedCatalog.categoriesList;
               categoriesLookup = cachedCatalog.categoriesLookup;
               bookLookup = cachedCatalog.bookLookup;
               bookList = cachedCatalog.bookList;
               bookThumbnailLookup = cachedCatalog.bookThumbnailLookup;
               return Observable.fromCallable(()-> true);
            }
         }
         catch(Throwable t){
            Util.error(TAG, t);
         }
      }
      DocumentFile libraryRoot = DocumentFile.fromTreeUri(MainActivity.getInstance(), PBRSettings.LibraryDirectory);
      this.categoriesLookup = new HashMap<>();
      this.categoriesList = new ArrayList<>();
      this.bookLookup = new HashMap<>();
      this.bookList = new ArrayList<>();
      this.bookThumbnailLookup = new HashMap<>();
      return Observable.fromCallable(()->{
         int bookIndex = 0;
         int bookCount = 0;
         DocumentFile[] categories = libraryRoot.listFiles();
         // Top level is folders (categories)}
         for(DocumentFile category : categories){
            if(category.getName().equals(".thumbnails")){
               for(DocumentFile thumb : category.listFiles()){
                  if(thumb.isFile()){
                     bookThumbnailLookup.put(thumb.getName().replace(".jpg",""), thumb.getUri().toString());
                  }
               }
            }
            else {
               // Next level is files (books)
               for(DocumentFile book : category.listFiles()){
                  bookCount++;
               }
            }
         }
         for(DocumentFile category : categories){
            if(category.getName().equals(".thumbnails")){
               continue;
            }
            DocumentFile[] books = category.listFiles();
            ArrayList<Book> parsedBooks = new ArrayList<Book>();
            for(DocumentFile bookFile : books){
                 String loadingMessage = "(" + (++bookIndex) + "/" + bookCount + ") Generating thumbnail for\n[" + bookFile.getName() + "]";
                 LoadingIndicator.setLoadingMessage(loadingMessage);
                 Book book = new Book();
                 book.TreeUri = bookFile.getUri().toString();
                 book.Name = bookFile.getName();
                 book.CategoryName = category.getName();
                 book.SearchSlug = book.CategoryName.toLowerCase() + "-" + book.Name.toLowerCase();
                 book.CompareSlug = book.Name.toLowerCase();
                 book.ThumbnailUri = bookThumbnailLookup.get(book.Name.replace(".cbz", ""));
                 parsedBooks.add(book);
            };
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

   public byte[] getCategoryThumbnail(String category){
      ArrayList<Book> books = getBooks(category).Books;
      Book firstBook = books.get(new Random().nextInt(books.size()));
      return getBookThumbnail(category, firstBook.Name);
   }

   public String getBookKey(String categoryName, String bookName){
      return categoryName + "-=-" + bookName;
   }

   public void addCategory(String name, ArrayList<Book> books){
      if(books.size() > 0){
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

   public Observable<BookView> getBook(String categoryName, String bookName){
      return Observable.fromCallable(()-> {
         Book book = bookLookup.get(getBookKey(categoryName, bookName));
         Util.clean("extract-book/");
         ArrayList<File> extractedFiles = Util.extractArchive(book.TreeUri, "extract-book/", true);
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

   public SearchResults search(String needle){
      SearchResults results = new SearchResults();
      needle = needle.toLowerCase();
      for(Book book : bookList){
         if(book.SearchSlug.contains(needle)){
            results.Books.add(book);
         }
      }
      return results;
   }
}
