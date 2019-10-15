package com.nhatminh.example.datastorage.mylibrary.book;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nhatminh.example.datastorage.mylibrary.R;
import com.nhatminh.example.datastorage.mylibrary.constant.Constant;
import com.nhatminh.example.datastorage.mylibrary.database.LibraryContract;
import com.nhatminh.example.datastorage.mylibrary.database.LibraryDatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class BookShelf extends AppCompatActivity {

    private static String TAG = "BookShelf";
    private static final int ADD_BOOK_REQUEST = 808;
    private static final int UPDATE_BOOK_REQUEST = 809;
    private static final int DELETE_BOOK_REQUEST = 810;

    FloatingActionButton fabInsert;
    RecyclerView rvBookShelf;
    BookShelfAdapter adapter;
    ArrayList<Book> bookArrayList;
    ArrayList<ThumbnailPath> thumbnailPathArrayList;

    LibraryDatabaseHelper database = new LibraryDatabaseHelper(this);

    int positionChosen;
    int userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);
        setupView();

        userRole = getIntent().getExtras().getInt(Constant.Role.ROLE, Constant.Role.DEFAULT);
        if(userRole != Constant.Role.ADMIN){
            fabInsert.setEnabled(false);
        }

    }

    void setupView(){
        fabInsert = findViewById(R.id.fabInsert);
        fabInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookShelf.this, AddBookActivity.class);
                intent.putExtra(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID,
                        getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));
                intent.putExtra("action", "add");
                startActivityForResult(intent, ADD_BOOK_REQUEST);
            }
        });

        bookArrayList = database.getAllBookByCategory(getIntent()
                .getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));
        thumbnailPathArrayList = new ArrayList<>();

        for(int i=0;i<bookArrayList.size();i++){
            ThumbnailPath thumbnailPath = database.getThumbnailPathById(bookArrayList.get(i).getThumbnailPathId());
            thumbnailPathArrayList.add(thumbnailPath);
        }

        rvBookShelf = findViewById(R.id.rvBookShelf);
        rvBookShelf.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BookShelfAdapter(this, bookArrayList, thumbnailPathArrayList);
        rvBookShelf.setAdapter(adapter);




        rvBookShelf.addOnItemTouchListener(new BookShelfItemClickListener(this, rvBookShelf, new BookShelfItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(bookArrayList.size() > 0){
                    callUpdateBookActivity(position);
                }
                else {
                    Toast.makeText(BookShelf.this, "Empty List", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if(bookArrayList.size() > 0){
                    positionChosen = position;
                }
                else {
                    Toast.makeText(BookShelf.this, "Empty List", Toast.LENGTH_SHORT).show();
                }


            }
        }));

        positionChosen = -1;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case ADD_BOOK_REQUEST:
                if(resultCode == Activity.RESULT_OK){
                    int id = data.getExtras().getInt(Constant.Database.BOOK_INSERTED_ID);
                    insertBookSuccessfully(id);
                    Toast.makeText(this, "Book inserted", Toast.LENGTH_SHORT).show();
                }
                break;

            case  UPDATE_BOOK_REQUEST:
                if(resultCode == Activity.RESULT_OK){
                    updateBookSuccessfully();
                    Toast.makeText(this, "Book updated", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    private void deleteBookSuccessfully(){
        bookArrayList.remove(positionChosen);
        thumbnailPathArrayList.remove(positionChosen);

        adapter.notifyDataSetChanged();
        positionChosen = -1;
    }

    private void updateBookSuccessfully(){
        Book newBook = database.getBookById(bookArrayList.get(positionChosen).getBookId());
        ThumbnailPath newThumbnailPath = database.getThumbnailPathById(newBook.getThumbnailPathId());

        bookArrayList.set(positionChosen, newBook);
        thumbnailPathArrayList.set(positionChosen, newThumbnailPath);

        adapter.notifyDataSetChanged();
    }

    private void insertBookSuccessfully(int id){
        Book newBook = database.getBookById(id);
        ThumbnailPath newThumbnailPath = database.getThumbnailPathById(newBook.getThumbnailPathId());

        bookArrayList.add(newBook);
        thumbnailPathArrayList.add(newThumbnailPath);
        adapter.notifyDataSetChanged();
    }

    void callUpdateBookActivity(int position){
        Intent intent = new Intent(BookShelf.this, AddBookActivity.class);
        intent.putExtra(LibraryContract.BookEntry.COLUMN_BOOK_ID, bookArrayList.get(position).getBookId());
        intent.putExtra("action", "update");


        startActivityForResult(intent, UPDATE_BOOK_REQUEST);
        positionChosen = position;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update:
                callUpdateBookActivity(positionChosen);
                break;

            case R.id.action_delete:
                if (deleteBooKById(bookArrayList.get(positionChosen).getBookId()) > 0){
                    deleteBookSuccessfully();
                }
                break;

            case R.id.action_delete_all:
                database.deleteAllBookByCategory(
                        getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));
                deleteBookThumbnail();
                bookArrayList.clear();
                thumbnailPathArrayList.clear();
                adapter.notifyDataSetChanged();
                break;

            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }



    void deleteBookThumbnail(){
        for(int i=0;i<bookArrayList.size();i++){
            ThumbnailPath thumbnailPath = database.getThumbnailPathById(bookArrayList.get(i).getThumbnailPathId());
            deleteImage(thumbnailPath.getDatabasePath());
            database.deleteThumbnailPathById(thumbnailPath.getThumbnailPathId());
        }
    }

    int deleteBooKById(int bookId){
        Book book = database.getBookById(bookId);
        ThumbnailPath thumbnailPath = database.getThumbnailPathById(book.getThumbnailPathId());
        deleteImage(thumbnailPath.getDatabasePath());
        return database.deleteBookById(bookId);
    }

    private void deleteImage(String imagePath)
    {
        File originalFile =  new File(imagePath);


        if (!originalFile.exists()) {
            Log.e(TAG, "Image file not exist");
        }
        else {
            originalFile.delete();
            Log.e(TAG, "Image file not deleted");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG, "On Resumed - List size: " + bookArrayList.size());
        adapter.notifyDataSetChanged();


        if(positionChosen >= 0 && positionChosen < bookArrayList.size()) {
            adapter.notifyItemChanged(positionChosen);
        }

    }






    /**
     * ===========================================================================
     */

    public void addImageToDCIM(){
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.book01);
        saveImageToExternalStorage(this, bm);

        Bitmap bm2 = BitmapFactory.decodeResource(getResources(), R.drawable.book02);
        saveImageToExternalStorage(this, bm2);

        Bitmap bm3 = BitmapFactory.decodeResource(getResources(), R.drawable.book03);
        saveImageToExternalStorage(this, bm3);

    }

    public static boolean saveImageToExternalStorage(Context context, Bitmap image) {
        try
        {
            OutputStream fOut = null;

            File file = createImageFile();

            fOut = new FileOutputStream(file);

            image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            return true;

        }
        catch (Exception e)
        {
            Log.e("saveToExternalStorage()", e.getMessage());
            return false;
        }

    }

    public static String copyFile(Context context, String inputPath)
    {
        InputStream inFile;
        OutputStream outFile;
        try
        {
            File file =  createImageFile();

            //Check if file doesnt exist
            if (!file.exists())
            {
                file.mkdir(); //Make a new file directiory
            }


            inFile = new FileInputStream(inputPath);
            outFile = new FileOutputStream(file);


            byte buffer[] = new byte[1024];
            int currentRead;
            while ((currentRead = inFile.read(buffer)) != -1)
            {
                outFile.write(buffer,0,currentRead);
            }

            inFile.close();
            inFile = null;


            //File copied
            outFile.flush();
            outFile.close();
            outFile = null;

            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            return file.getAbsolutePath();
        }
        catch (FileNotFoundException e)
        {
            Log.e("tag",e.getMessage());
        }
        catch (IOException e)
        {
            Log.e("tag",e.getMessage());
        }


        return null;
    }

    public static File createFile(String fileType, String path) throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(System.currentTimeMillis());

        File storageDir = new File(path);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File file = File.createTempFile(
                timeStamp,
                "." + fileType,
                storageDir
        );
        return file;
    }



    public static File createImageFile() throws IOException {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Thumbnail/";
        return createFile("jpeg", path);
    }


}
