package com.simplepathstudios.pbr;


import android.net.Uri;

public class PBRSettings {
    public static final String BuildDate = "July 21, 2023";
    public static final String ClientVersion = "1.1.0";
    public static boolean EnableDebugLog = false;
    public static final Uri UpdatePBRUrl = Uri.parse("http://9914.us/software/android/picture-book-reader.apk");
    public static final boolean DebugResourceLeaks = false;
    public static Uri LibraryDirectory = null;
    public static final int SwipeThresholdX = 200;
    public static final int SwipeThresholdY = 300;
    public static final int ShowToolbarMilliseconds = 2000;
    public static final float PageTurnZoomThreshold = 1.5f;
    public static final float DoubleTapThreshold = 300;
    public static final float PinchReleaseThreshold = 300;
    public static final float TapBorderThresholdPercent = 0.05f;
    public static final int SearchDelayMilliseconds = 300;
    public static final int PageTurnDebounceMilliseconds = 150;
}
