package com.simplepathstudios.pbr.api.model;

import android.net.Uri;

public class Book {
   public String CategoryName;
   public String Name;
   public String TreeUri;

   public String getDisplayName(){
      return Name.substring(0,Name.lastIndexOf(" - "));
   }
}
