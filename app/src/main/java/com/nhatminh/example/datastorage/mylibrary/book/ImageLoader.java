package com.nhatminh.example.datastorage.mylibrary.book;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.nhatminh.example.datastorage.mylibrary.database.LibraryDatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.DOWNLOAD_SERVICE;


public class ImageLoader{

    private static final String TAG = "ImageLoader";

    private Context context;
    private OnBitmapLoadingCompletedListener callback;
    private BroadcastReceiver downloadCompleteReceiver;
    private HashMap<Long, RequestInfo> requestIdentifier;

    private boolean isRunning = false;

    // skip load image until receive request #startLoadImageId
    private boolean isSkipMode = false;
    private int loadImageFromId;
    private int loadImageToId;
    private final int range = 4;


    public ImageLoader(Context context) {
        this.context = context;
        this.requestIdentifier = new HashMap<>();
        this.isRunning = true;

        this.downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(!isRunning){
                    return;
                }

                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                RequestInfo requestInfo = requestIdentifier.get(downloadId);

                decodeBitmap(createDatabasePathFromSourcePath(requestInfo.filePath), requestInfo.requestId, requestInfo.reqWidth, requestInfo.reqHeight);
            }
        };
        context.registerReceiver(downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void cancelLoading(){
        isRunning = false;
    }

    public void unregisterBroadcastReceiver(){
        if(downloadCompleteReceiver != null){
            context.unregisterReceiver(downloadCompleteReceiver);
        }
    }


    public void getImage(String url, int id, int reqWidth, int reqHeight){

        String filePathInDatabase = createDatabasePathFromSourcePath(url);

        if(isFileExist(filePathInDatabase)){
            decodeBitmap(filePathInDatabase, id, reqWidth, reqHeight);
        }
        else {
            RequestInfo requestInfo = new RequestInfo(id, reqHeight, reqWidth, url);

            if (isNetworkPath(url)){
                long downloadId = downloadWithDownloadManager(url);
                requestIdentifier.put(downloadId, requestInfo);
            }
            else {
                new CopyImageToDatabase(requestInfo).execute();
            }


        }
    }

    private boolean isNetworkPath(String filePath){
        if(filePath.contains("http") || filePath.contains("https")){
            return true;
        }
        return false;
    }


    boolean isFileExist(String filePath){
        File file = new File(filePath);
        return file.exists();
    }

    private static String createFilePathFromHashPath(String hashString){
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Thumbnail/";
        return path + hashString +  ".jpeg";
    }

    private static String createDatabasePathFromSourcePath(String sourcePath){
        String hashPath = hashWithMd5(sourcePath);
        return createFilePathFromHashPath(hashPath);
    }


    public static String hashWithMd5(String input){
        String hashResult = null;
        try {
                MessageDigest md = MessageDigest.getInstance("MD5");

                byte[] hash = md.digest(input.getBytes("UTF-8"));

                StringBuilder sb = new StringBuilder(2*hash.length);
                for(byte b:hash){
                    sb.append(String.format("%02x", b&0xff));
                }

            hashResult = sb.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return hashResult;
    }

    public void triggerSkipMode(int startLoadImageId, int endLoadImageId){
        this.loadImageFromId = startLoadImageId;
        this.loadImageToId = endLoadImageId;
        isSkipMode = true;
    }


    private void decodeBitmap(String path, int requestId, int reqWidth, int reqHeight){
        // get bitmap size without loading it to memory
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);


        // calculate sample size for scaling bitmap
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // decode with sample size
        options.inJustDecodeBounds = false;

        new DecodeImage(path, options, requestId).execute();
    }

    public class CopyImageToDatabase extends AsyncTask<Void, Void, Void>{

        RequestInfo requestInfo;
        String filePathInDatabase;

        public CopyImageToDatabase(RequestInfo requestInfo){
            this.requestInfo = requestInfo;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            filePathInDatabase = copyFile(context, requestInfo.filePath);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!isRunning){
                return;
            }

            super.onPostExecute(aVoid);
            decodeBitmap(filePathInDatabase, requestInfo.requestId, requestInfo.reqWidth, requestInfo.reqHeight);
        }

        public String copyFile(Context context, String inputPath)
        {
            InputStream inFile;
            OutputStream outFile;
            try
            {
                File file = new File(createDatabasePathFromSourcePath(inputPath));

                inFile = new FileInputStream(inputPath);
                outFile = new FileOutputStream(file);

                byte buffer[] = new byte[1024];
                int currentRead;
                while ((currentRead = inFile.read(buffer)) != -1) {
                    outFile.write(buffer,0,currentRead);
                }

                inFile.close();
                inFile = null;

                outFile.flush();
                outFile.close();
                outFile = null;

                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                return file.getAbsolutePath();
            }
            catch (FileNotFoundException e) {
                Log.e("tag",e.getMessage());
            }
            catch (IOException e) {
                Log.e("tag",e.getMessage());
            }

            return null;
        }

    }

    public class DecodeImage extends AsyncTask<Void, Void, Void>{

        String path;
        BitmapFactory.Options options;
        int requestId;
        Bitmap bitmap;


        public DecodeImage(String path, BitmapFactory.Options options, int requestId){
            this.path = path;
            this.options = options;
            this.requestId = requestId;
            this.bitmap = null;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            if(isSkipMode) {
                if (requestId < loadImageFromId - range || requestId > loadImageToId + range) {
                    Log.e(TAG, "Skip decode bitmap #" + requestId);
                    return null;
                }
                else {
                    isSkipMode = false;
                }
            }


            bitmap = BitmapFactory.decodeFile(path, options);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!isRunning){
                return;
            }
            super.onPostExecute(aVoid);
            callback.onBitmapLoadingCompleted(bitmap, requestId);
        }


    }

   private static int calculateInSampleSize(
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

    private long downloadWithDownloadManager(String downloadUrl){
        File file = new File(createDatabasePathFromSourcePath(downloadUrl));

        DownloadManager.Request request= new DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle(file.getName())
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(file));

        DownloadManager downloadManager= (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

        long downloadId = downloadManager.enqueue(request);
        return downloadId;
    }


    public interface OnBitmapLoadingCompletedListener {
        void onBitmapLoadingCompleted(Bitmap bitmap, int id);
    }

    public void setOnBitmapLoadingCompletedListener(OnBitmapLoadingCompletedListener onBitmapLoadingCompletedListener){
        callback = onBitmapLoadingCompletedListener;
    }

    private class RequestInfo{
        public int requestId;
        public int reqHeight;
        public int reqWidth;
        public String filePath;

        public RequestInfo(int requestId, int reqHeight, int reqWidth, String filePath) {
            this.requestId = requestId;
            this.reqHeight = reqHeight;
            this.reqWidth = reqWidth;
            this.filePath = filePath;
        }
    }

    public static void deleteImageInDatabase(String path){
        String filePath = createDatabasePathFromSourcePath(path);

        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }
    }

}


/**
public class ImageLoader {

    Context context;
    BroadcastReceiver completeReceiver;

    HashMap<Long, String> fileDownloadedPath;
    HashMap<Long, Integer> bookThumbnailId;
    HashMap<Long, Integer> itemPos;

    ArrayList<Book> books;
    ArrayList<ThumbnailPath> thumbnailPaths;

    public ImageLoader(Context context, final ArrayList<Book> books, final ArrayList<ThumbnailPath> thumbnailPaths){
        this.context = context;
        fileDownloadedPath = new HashMap<>();
        bookThumbnailId = new HashMap<>();
        itemPos = new HashMap<>();
        
        this.books = books;
        this.thumbnailPaths = thumbnailPaths;

        completeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                String path = fileDownloadedPath.get(id);

                int bookId = bookThumbnailId.get(id);
                int bookPos = itemPos.get(id);

                LibraryDatabaseHelper database = new LibraryDatabaseHelper(context);
                Book newBook = database.getBookById(bookId);

                ThumbnailPath thumbnailPath = database.getThumbnailPathById(newBook.getThumbnailPathId());
                thumbnailPath.setDatabasePath(path);

                database.updateThumbnailPath(thumbnailPath);
                books.set(bookPos, newBook);
                thumbnailPaths.set(bookPos, thumbnailPath);

                fileDownloadedPath.remove(id);
                bookThumbnailId.remove(id);
                itemPos.remove(id);

            }
        };

        context.registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    boolean isDatabasePath(String path){
        if(path.contains("/DCIM/Thumbnail/")){
            return true;
        }
        return false;
    }

    public Bitmap decodeBitmap(String path, int reqWidth, int reqHeight){

        // get bitmap size without loading it to memory
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path);

        // calculate sample size for scaling bitmap
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // decode with sample size
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public void loadImageToView(String imagePath, ImageView view){
        Bitmap bitmap = decodeBitmap(imagePath, view.getWidth(), view.getHeight());
        view.setImageBitmap(bitmap);
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

    boolean isFileExist(String filePath){
        File file = new File(filePath);
        return file.exists();
    }


    public void downloadImage(ThumbnailPath thumbnailPath, Book book, int position){
        long downloadId = downloadWithDownloadManager(thumbnailPath.getOriginalPath());
        bookThumbnailId.put(downloadId, book.getBookId());
        itemPos.put(downloadId, position);
    }

    private long downloadWithDownloadManager(String downloadUrl){
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Thumbnail/";
        String timeStamp = String.valueOf(System.currentTimeMillis());

        File file = new File(path + timeStamp +  ".jpeg");


        DownloadManager.Request request= new DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle(file.getName())
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(file));

        DownloadManager downloadManager= (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

        long downloadId = downloadManager.enqueue(request);

        fileDownloadedPath.put(downloadId, file.getAbsolutePath());

        return downloadId;
    }
    
    void copyImageFileToDatabase(String srcPath, int bookId, int position){
        CopyImage copyImage = new CopyImage(srcPath, bookId, position);
        copyImage.execute();
    }

    public class CopyImage extends AsyncTask<Void, Void, Void>{
        int bookId, position;
        String destinationPath, srcPath;
      

        public CopyImage(String srcPath, int bookId, int position){
            this.bookId = bookId;
            this.position = position;
            this.srcPath = srcPath;
        }
        
        @Override
        protected Void doInBackground(Void... voids) {
            destinationPath = BookShelf.copyFile(context, srcPath);
            
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            LibraryDatabaseHelper database = new LibraryDatabaseHelper(context);
            Book newBook = database.getBookById(bookId);

            ThumbnailPath thumbnailPath = database.getThumbnailPathById(newBook.getThumbnailPathId());
            thumbnailPath.setDatabasePath(destinationPath);

            database.updateThumbnailPath(thumbnailPath);
            books.set(position, newBook);
            thumbnailPaths.set(position, thumbnailPath);
        }
    }

}
*/