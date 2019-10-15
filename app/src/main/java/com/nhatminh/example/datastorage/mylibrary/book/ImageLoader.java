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
import android.widget.ImageView;

import com.nhatminh.example.datastorage.mylibrary.database.LibraryDatabaseHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.DOWNLOAD_SERVICE;

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
