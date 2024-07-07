package com.simplepathstudios.pbr.api.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.simplepathstudios.pbr.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class BookView {
   public static String TAG = "BookView";
   public String TreeUi;
   public String Name;
   public HashMap<String, String> Pages;
   public ArrayList<String> PageIds;
   public HashMap<String, String> Info;
   public int CurrentPageIndex = 0;

   public BookView(){
      Pages = new HashMap<>();
      Info = new HashMap<>();
      PageIds = new ArrayList<>();
   }

   public String gotoPage(int pageIndex){
      CurrentPageIndex = pageIndex;
      if(Pages.size() <= CurrentPageIndex){
         return null;
      }
      return Pages.get(PageIds.get(pageIndex));
   }

   public String getCurrentPage(){
      return gotoPage(CurrentPageIndex);
   }

   public String nextPage(){
      if(CurrentPageIndex >= PageIds.size() - 1){
         return null;
      }
      return gotoPage(CurrentPageIndex + 1);
   }

   public String previousPage(){
      if(CurrentPageIndex < 0){
         return null;
      }
      return gotoPage(CurrentPageIndex - 1);
   }

   public int getPageCount(){
      return PageIds.size();
   }

   public void sortPages(){
      this.PageIds.sort(new Comparator<String>() {
         @Override
         public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
         }
      });
   }
}
