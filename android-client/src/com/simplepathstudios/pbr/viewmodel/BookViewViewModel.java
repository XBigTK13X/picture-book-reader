package com.simplepathstudios.pbr.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.pbr.LoadingIndicator;
import com.simplepathstudios.pbr.CentralCatalog;
import com.simplepathstudios.pbr.api.model.Book;
import com.simplepathstudios.pbr.api.model.BookView;

public class BookViewViewModel extends ViewModel {
   public MutableLiveData<BookView> Data;
   public BookViewViewModel(){
      Data = new MutableLiveData<BookView>();
   }

   public void load(String categoryName, String bookName){
      LoadingIndicator.setLoading(true);
      Book book = CentralCatalog.getInstance().getBook(categoryName, bookName);
      book.View.CurrentPageIndex = 0;
      Data.setValue(book.View);
      LoadingIndicator.setLoading(false);
   }

   public void nextPage(){
      BookView bookView = Data.getValue();
      bookView.nextPage();
      Data.setValue(bookView);
   }

   public void previousPage(){
      BookView bookView = Data.getValue();
      bookView.previousPage();
      Data.setValue(bookView);
   }

   public boolean isFirstPage(){
      BookView bookView = Data.getValue();
      return bookView.CurrentPageIndex <= 0;
   }

   public boolean isLastPage() {
      BookView bookView = Data.getValue();
      return bookView.CurrentPageIndex >= bookView.Pages.size() - 1;
   }

   public void gotoPage(int pageIndex){
      BookView bookView = Data.getValue();
      bookView.gotoPage(pageIndex);
      Data.setValue(bookView);
   }
}
