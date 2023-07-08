package com.simplepathstudios.pbr;

import android.net.Uri;

import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ZipUtil {
   public static final String TAG = "ZipUtil";
   public static ArrayList<File> extract(Uri documentTreeUri, String outputDir) {
      ArrayList<File> extractedFiles = new ArrayList<>();
      return Observable.fromCallable(() -> {
            try {
               InputStream archiveStream = MainActivity.getInstance().getContentResolver().openInputStream(documentTreeUri);
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
}
