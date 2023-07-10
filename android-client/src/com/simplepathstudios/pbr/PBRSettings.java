package com.simplepathstudios.pbr;


import android.net.Uri;

public class PBRSettings {
    public static final String BuildDate = "July 09, 2023";
    public static final String ClientVersion = "1.0.3";
    public static boolean EnableDebugLog = false;
    public static Uri UpdatePBRUrl = Uri.parse("http://9914.us/software/android/picture-book-reader.apk");
    public static boolean DebugResourceLeaks = false;
    public static Uri LibraryDirectory = null;
    public static int SwipeThresholdX = 200;
    public static int SwipeThresholdY = 300;
    public static int ShowToolbarMilliseconds = 2000;
}
