package com.simplepathstudios.pbr.api.model;

import android.graphics.Bitmap;
import android.media.Image;
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

   public Bitmap GotoPage(int pageIndex){
      CurrentPageIndex = pageIndex;
      return Pages.get(PageIds.get(pageIndex));
   }

   public Bitmap NextPage(){
      if(CurrentPageIndex >= PageIds.size() - 1){
         return null;
      }
      return GotoPage(CurrentPageIndex + 1);
   }

   public Bitmap PreviousPage(){
      if(CurrentPageIndex < 0){
         return null;
      }
      return GotoPage(CurrentPageIndex - 1);
   }

   public int GetPageCount(){
      return PageIds.size();
   }

   public Bitmap GetCurrentPage(){
      return GotoPage(CurrentPageIndex);
   }
}
