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
   public BookView(){
      Pages = new HashMap<>();
      Info = new HashMap<>();
      PageIds = new ArrayList<>();
   }
}
