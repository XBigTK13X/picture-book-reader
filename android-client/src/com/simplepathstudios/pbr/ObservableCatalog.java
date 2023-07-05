package com.simplepathstudios.pbr;

import android.content.Context;
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

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
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

   public void importLibrary(){
      DocumentFile libraryRoot = DocumentFile.fromTreeUri(MainActivity.getInstance(), PBRSettings.LibraryDirectory);
      Util.log(TAG, libraryRoot.listFiles().toString());
      // Top level is folders (categories)
      for(DocumentFile category : libraryRoot.listFiles()){
         // Next level is files (books)
         ArrayList<Book> books = new ArrayList<Book>();
         for(DocumentFile bookFile : category.listFiles()) {
            Book book = new Book();
            book.TreeUri = bookFile.getUri();
            book.Name = bookFile.getName();
            book.CategoryName = category.getName();
            books.add(book);
            generateThumbnail(getBookKey(book.CategoryName, book.Name), book.TreeUri);
         }
         ObservableCatalog.getInstance().addCategory(category.getName(), books);
      }
   }

   public Bitmap getBookThumbnail(String category, String book) {
      try{
         FileInputStream thumbnailStream = MainActivity.getInstance().openFileInput(getThumbnailPath(getBookKey(category, book)));
         Bitmap bitmap = BitmapFactory.decodeStream(thumbnailStream);
         thumbnailStream.close();
         return bitmap;
      } catch(IOException e){
         Util.error(TAG, e);
      }
      return null;
   }

   private String getThumbnailPath(String bookKey){
      return "thumbnails." + bookKey + ".jpeg";
   }

   public void generateThumbnail(String bookKey, Uri archiveUri){
      File file = MainActivity.getInstance().getFileStreamPath(getThumbnailPath(bookKey));
      if(file != null && file.exists()) {
         return;
      }
      try {
         Util.log(TAG,"Parse book " + archiveUri);
         InputStream safInputStream = MainActivity.getInstance().getContentResolver().openInputStream(archiveUri);
         byte[] zipBytes = IOUtils.toByteArray(safInputStream);
         SeekableInMemoryByteChannel byteChannel = new SeekableInMemoryByteChannel(zipBytes);
         ZipFile bookArchive = new ZipFile(byteChannel);
         Enumeration<ZipArchiveEntry> entries = bookArchive.getEntries();
         while (entries.hasMoreElements()) {
            ZipArchiveEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
               continue;
            }
            String entryName = entry.getName();
            if (!entryName.endsWith(".xml") && !entryName.endsWith(".txt")) {
               InputStream zipStream = bookArchive.getInputStream(entry);
               byte[] rawImage = new byte[(int) entry.getSize()];
               int ii = 0;
               while (ii < rawImage.length) {
                  ii += zipStream.read(rawImage, ii, rawImage.length - ii);
               }
               Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
               Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap,500,500,true);
               FileOutputStream thumbnailStream = MainActivity.getInstance().openFileOutput(getThumbnailPath(bookKey), Context.MODE_PRIVATE);
               thumbnail.compress(Bitmap.CompressFormat.JPEG, 95, thumbnailStream);
               safInputStream.close();
               thumbnail.recycle();
               bitmap.recycle();
               return;
            }
         }
         safInputStream.close();
         byteChannel.close();
         bookArchive.close();
      }
      catch (IOException e) {
         Util.error(TAG, e);
      }
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
      bookView.PageIds.sort(new Comparator<String>() {
         @Override
         public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
         }
      });
      return bookView;
   }
}
