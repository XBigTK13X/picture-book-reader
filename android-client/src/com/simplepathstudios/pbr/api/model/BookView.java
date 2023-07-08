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

   public BookView(){
      Pages = new HashMap<>();
      Info = new HashMap<>();
      PageIds = new ArrayList<>();
   }

   public File gotoPage(int pageIndex){
      CurrentPageIndex = pageIndex;
      return Pages.get(PageIds.get(pageIndex));
   }

   public File getCurrentPage(){
      return gotoPage(CurrentPageIndex);
   }

   public File nextPage(){
      if(CurrentPageIndex >= PageIds.size() - 1){
         return null;
      }
      return gotoPage(CurrentPageIndex + 1);
   }

   public File previousPage(){
      if(CurrentPageIndex < 0){
         return null;
      }
      return gotoPage(CurrentPageIndex - 1);
   }

   public int getPageCount(){
      return PageIds.size();
   }
}
