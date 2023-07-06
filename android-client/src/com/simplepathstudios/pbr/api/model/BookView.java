package com.simplepathstudios.pbr.api.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class BookView {
   public Uri TreeUi;
   public String Name;
   public HashMap<String, File> Pages;
   public ArrayList<String> PageIds;
   public HashMap<String, String> Info;
   public int CurrentPageIndex = 0;
   private Bitmap currentPage;

   public BookView(){
      Pages = new HashMap<>();
      Info = new HashMap<>();
      PageIds = new ArrayList<>();
   }

   public Bitmap gotoPage(int pageIndex){
      CurrentPageIndex = pageIndex;
      File pageFile = Pages.get(PageIds.get(pageIndex));
      if(currentPage != null){
         currentPage.recycle();
      }
      currentPage = BitmapFactory.decodeFile(pageFile.getAbsolutePath());
      return currentPage;
   }

   public Bitmap nextPage(){
      if(CurrentPageIndex >= PageIds.size() - 1){
         return null;
      }
      return gotoPage(CurrentPageIndex + 1);
   }

   public Bitmap previousPage(){
      if(CurrentPageIndex < 0){
         return null;
      }
      return gotoPage(CurrentPageIndex - 1);
   }

   public int getPageCount(){
      return PageIds.size();
   }

   public Bitmap getCurrentPage(){
      return gotoPage(CurrentPageIndex);
   }
}
