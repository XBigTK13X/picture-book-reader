package com.simplepathstudios.pbr.api.model;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;

public class BookView {
   public Uri TreeUi;
   public String Name;
   public String CategoryName;
   public HashMap<String, Bitmap> Pages;
   public ArrayList<String> PageIds;
   public HashMap<String, String> Info;
   public int CurrentPageIndex = 0;

   public BookView(){
      Pages = new HashMap<>();
      Info = new HashMap<>();
      PageIds = new ArrayList<>();
   }

   public Bitmap gotoPage(int pageIndex){
      CurrentPageIndex = pageIndex;
      return Pages.get(PageIds.get(pageIndex));
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
