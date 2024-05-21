package com.simplepathstudios.pbr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    private static final String TAG = "Util";

    private static Context __context;
    private static Thread.UncaughtExceptionHandler __androidExceptionHandler;

    public static void setGlobalContext(Context context){
        __context = context;
    }

    public static Context getGlobalContext(){
        if(__context == null){
            Log.d(TAG,"Global context is null, it must be set before it is read");
        }
        return __context;
    }

    private static int MILLISECONDS_PER_HOUR = 1000 * 60 * 60;
    private static int MILLISECONDS_PER_MINUTE = 1000 * 60;
    public static String millisecondsToTimestamp(int milliseconds){
        if(milliseconds >= MILLISECONDS_PER_HOUR){
            int hours = (milliseconds / (MILLISECONDS_PER_HOUR));
            int minutes = (milliseconds / (MILLISECONDS_PER_MINUTE)) % 60;
            int seconds = (milliseconds / 1000) % 60;
            return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
        }
        int minutes = (milliseconds / (MILLISECONDS_PER_MINUTE)) % 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02dm %02ds", minutes, seconds);
    }

    public static void error(String tag, Throwable e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        Util.log(tag, "Message: "+e.getMessage()+"\n StackTrace: " + sw.toString());
    }

    public static void log(String tag, String message){
        Util.log(tag, message, false);
    }

    public static void log(String tag, String message, boolean force){
        try{
            if(!PBRSettings.EnableDebugLog &&!force){
                return;
            }
            String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
            String logEntry = String.format("[PBR] - %s - %s - %s : %s",System.currentTimeMillis(), timestamp,tag,message);
            Log.d(tag, logEntry);
        } catch(Exception e){
            Log.d(TAG, "An error occurred while logging",e);
        }

    }

    private static Toast lastToast;
    public static void toast(String message){
        if(lastToast != null){
            lastToast.cancel();
        }
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                lastToast = Toast.makeText(getGlobalContext(), message, Toast.LENGTH_SHORT);
                lastToast.show();
            }
        });
    }

    public static void registerGlobalExceptionHandler() {
        if(__androidExceptionHandler == null){
            __androidExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

            Thread.setDefaultUncaughtExceptionHandler(
                    new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(
                                Thread paramThread,
                                Throwable paramThrowable
                        ) {
                            StringWriter stringWriter = new StringWriter();
                            PrintWriter printWriter = new PrintWriter(stringWriter);
                            paramThrowable.printStackTrace(printWriter);
                            String stackTrace = stringWriter.toString();
                            Util.log(TAG, "An error occurred " +paramThrowable.getMessage() +" => "+stackTrace, true);
                            if (__androidExceptionHandler != null)
                                __androidExceptionHandler.uncaughtException(
                                        paramThread,
                                        paramThrowable
                                ); //Delegates to Android's error handling
                            else
                                System.exit(2); //Prevents the service/app from freezing
                        }
                    });
        }
    }

    public static void clean(String outputDir){
        Path absolutePath = Paths.get(MainActivity.getInstance().getFilesDir().getAbsolutePath(),outputDir);
        File extractDirectory = new File(absolutePath.toString());
        try {
            FileUtils.deleteDirectory(extractDirectory);
        }
        catch(Exception e){
            Util.error(TAG, e);
        }
    }

    public static void enableFullscreen(){
        Window window = MainActivity.getInstance().getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat windowInsetsControllerCompat = new WindowInsetsControllerCompat(window, window.getDecorView());
        windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars());
        windowInsetsControllerCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        final int bufLen = 4 * 0x400; // 4KB
        byte[] buf = new byte[bufLen];
        int readLen;
        IOException exception = null;

        try {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                    outputStream.write(buf, 0, readLen);

                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            exception = e;
            throw e;
        } finally {
            if (exception == null) inputStream.close();
            else try {
                inputStream.close();
            } catch (IOException e) {
                exception.addSuppressed(e);
            }
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static Integer maxImageHeight;
    private static Integer maxImageWidth;

    public static Bitmap subsample(Uri resourceUri) {
        if(maxImageHeight == null){
            DisplayMetrics displayMetrics = new DisplayMetrics();
            MainActivity.getInstance().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            maxImageHeight = displayMetrics.heightPixels;
            maxImageWidth = displayMetrics.widthPixels;
        }

        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            final InputStream measureStream = MainActivity.getInstance().getContentResolver().openInputStream(resourceUri);
            BitmapFactory.decodeStream(measureStream, null, options);
            options.inSampleSize = calculateInSampleSize(options, maxImageWidth, maxImageHeight);
            options.inJustDecodeBounds = false;
            final InputStream imageStream = MainActivity.getInstance().getContentResolver().openInputStream(resourceUri);
            return BitmapFactory.decodeStream(imageStream, null, options);
        }
        catch(Exception e){
            Util.error(TAG,e);
            return null;
        }
    }

    public static Bitmap fullImage(Uri imagePath){
        try {
            final InputStream imageStream = MainActivity.getInstance().getContentResolver().openInputStream(imagePath);
            return BitmapFactory.decodeStream(imageStream);
        }
        catch(Exception e){
            return null;
        }
    }

}