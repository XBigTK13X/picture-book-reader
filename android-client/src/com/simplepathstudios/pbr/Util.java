package com.simplepathstudios.pbr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
        lastToast = Toast.makeText(getGlobalContext(), message, Toast.LENGTH_SHORT);
        lastToast.show();
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

    public static void confirmMenuAction(MenuItem menuItem, String message, DialogInterface.OnClickListener confirmListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());
        builder.setMessage(message);
        builder.setPositiveButton("Yes", confirmListener);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        });
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

    public static ArrayList<File> extractArchive(String documentTreeUri, String outputDir, boolean showProgress) {
        ArrayList<File> extractedFiles = new ArrayList<>();
        if (showProgress){
            LoadingIndicator.setLoadingMessage("Opening book...");
        }
        return Observable.fromCallable(() -> {
                    try {
                        InputStream archiveStream = MainActivity.getInstance().getContentResolver().openInputStream(Uri.parse(documentTreeUri));
                        LocalFileHeader localFileHeader;
                        int readLen;
                        byte[] readBuffer = new byte[4096];

                        ZipInputStream zipInputStream = new ZipInputStream(archiveStream);
                        while ((localFileHeader = zipInputStream.getNextEntry()) != null) {
                            Path absolutePath = Paths.get(MainActivity.getInstance().getFilesDir().getAbsolutePath(),outputDir,localFileHeader.getFileName());
                            File extractedFile = new File(absolutePath.toString());
                            extractedFile.getParentFile().mkdirs();
                            OutputStream outputStream = new FileOutputStream(extractedFile);
                            while ((readLen = zipInputStream.read(readBuffer)) != -1) {
                                outputStream.write(readBuffer, 0, readLen);
                            }
                            extractedFiles.add(extractedFile);
                            outputStream.close();
                        }
                        archiveStream.close();
                        zipInputStream.close();
                    } catch (Exception e) {
                        Util.error(TAG, e);
                    }
                    extractedFiles.sort(new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                        }
                    });
                    return extractedFiles;
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .blockingFirst();
    }

    public static double getDistance(float x1, float y1, float x2, float y2){
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }
}