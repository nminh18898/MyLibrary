package com.nhatminh.example.datastorage.mylibrary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nhatminh.example.datastorage.mylibrary.book.Book;
import com.nhatminh.example.datastorage.mylibrary.book.ThumbnailPath;
import com.nhatminh.example.datastorage.mylibrary.category.Category;

import java.util.ArrayList;

public class LibraryDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "Database";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Library.db";
    Context context;


    public LibraryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String sqlCreateCategoryEntry = "create table " + LibraryContract.CategoryEntry.TABLE_NAME +
                "( " +
                LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID + " integer primary key autoincrement, " +
                LibraryContract.CategoryEntry.COLUMN_CATEGORY_NAME + " text)";

        String sqlCreateBookEntry = "create table " + LibraryContract.BookEntry.TABLE_NAME +
                "( " +
                LibraryContract.BookEntry.COLUMN_BOOK_ID + " integer primary key autoincrement, " +
                LibraryContract.BookEntry.COLUMN_BOOK_NAME + " text, " +
                LibraryContract.BookEntry.COLUMN_BOOK_AUTHOR + " text, " +
                LibraryContract.BookEntry.COLUMN_BOOK_CONTENT + " text, " +
                LibraryContract.BookEntry.COLUMN_BOOK_THUMBNAIL + " text, " +
                LibraryContract.BookEntry.COLUMN_BOOK_CATEGORY + " text)";

        String sqlCreateThumbnailPathEntry = "create table " + LibraryContract.ThumbnailPathEntry.TABLE_NAME +
                "( " +
                LibraryContract.ThumbnailPathEntry.COLUMN_THUMBNAIL_ID + " integer primary key autoincrement, " +
                LibraryContract.ThumbnailPathEntry.COLUMN_ORIGINAL_PATH + " text, " +
                LibraryContract.ThumbnailPathEntry.COLUMN_DATABASE_PATH + " text) ";

        sqLiteDatabase.execSQL(sqlCreateCategoryEntry);
        sqLiteDatabase.execSQL(sqlCreateBookEntry);
        sqLiteDatabase.execSQL(sqlCreateThumbnailPathEntry);
    }


    public int insertCategory(String categoryName){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LibraryContract.CategoryEntry.COLUMN_CATEGORY_NAME, categoryName);
        return (int) db.insert(LibraryContract.CategoryEntry.TABLE_NAME, null, values);
    }

    public int insertBook(Book book){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LibraryContract.BookEntry.COLUMN_BOOK_NAME, book.getName());
        values.put(LibraryContract.BookEntry.COLUMN_BOOK_AUTHOR, book.getAuthor());
        values.put(LibraryContract.BookEntry.COLUMN_BOOK_CONTENT, book.getContent());
        values.put(LibraryContract.BookEntry.COLUMN_BOOK_THUMBNAIL, book.getThumbnailPathId());
        values.put(LibraryContract.BookEntry.COLUMN_BOOK_CATEGORY, book.getCategoryId());

        //insertThumbnailPath(book.getThumbnailPath());

        return (int) db.insert(LibraryContract.BookEntry.TABLE_NAME, null, values);

    }

    public int insertThumbnailOriginalPath(String originalPath){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LibraryContract.ThumbnailPathEntry.COLUMN_ORIGINAL_PATH, originalPath);

        return (int) db.insert(LibraryContract.ThumbnailPathEntry.TABLE_NAME, LibraryContract.ThumbnailPathEntry.COLUMN_DATABASE_PATH, values);

    }

    public ThumbnailPath getThumbnailPathById(int thumbnailId){
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = LibraryContract.ThumbnailPathEntry.COLUMN_THUMBNAIL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(thumbnailId) };

        Cursor cursor = db.query(LibraryContract.ThumbnailPathEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);

        ThumbnailPath thumbnailPath = new ThumbnailPath();

        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndexOrThrow(LibraryContract.ThumbnailPathEntry.COLUMN_THUMBNAIL_ID);
            int indexOriginalPath = cursor.getColumnIndexOrThrow(LibraryContract.ThumbnailPathEntry.COLUMN_ORIGINAL_PATH);
            int indexDatabasePath = cursor.getColumnIndexOrThrow(LibraryContract.ThumbnailPathEntry.COLUMN_DATABASE_PATH);

            thumbnailPath.setThumbnailPathId(cursor.getInt(indexId));
            thumbnailPath.setOriginalPath(cursor.getString(indexOriginalPath));
            thumbnailPath.setDatabasePath(cursor.getString(indexDatabasePath));
        }

        return thumbnailPath;
    }

    public int updateThumbnailPath(ThumbnailPath thumbnailPath){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LibraryContract.ThumbnailPathEntry.COLUMN_ORIGINAL_PATH, thumbnailPath.getOriginalPath());

        if(thumbnailPath.getDatabasePath() == null){
            values.putNull(LibraryContract.ThumbnailPathEntry.COLUMN_DATABASE_PATH);
        }
        else {
            values.put(LibraryContract.ThumbnailPathEntry.COLUMN_DATABASE_PATH, thumbnailPath.getDatabasePath());
        }

        String selection = LibraryContract.ThumbnailPathEntry.COLUMN_THUMBNAIL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(thumbnailPath.getThumbnailPathId())};

        return (int) db.update(LibraryContract.ThumbnailPathEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private boolean isNetworkPath(String path){
        if(path.contains("http") || path.contains("https")){
            return true;
        }
        return false;
    }



    public int updateBook(Book newBookInfo){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LibraryContract.BookEntry.COLUMN_BOOK_NAME, newBookInfo.getName());
        values.put(LibraryContract.BookEntry.COLUMN_BOOK_AUTHOR, newBookInfo.getAuthor());
        values.put(LibraryContract.BookEntry.COLUMN_BOOK_CONTENT, newBookInfo.getContent());
        values.put(LibraryContract.BookEntry.COLUMN_BOOK_THUMBNAIL, newBookInfo.getThumbnailPathId());
        values.put(LibraryContract.BookEntry.COLUMN_BOOK_CATEGORY, newBookInfo.getCategoryId());


        String selection = LibraryContract.BookEntry.COLUMN_BOOK_ID + " = ?";
        String[] selectionArgs = { String.valueOf(newBookInfo.getBookId())};

        return db.update(LibraryContract.BookEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public ArrayList<Book> getAllBookByCategory(int categoryID){
        ArrayList<Book> books = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String selection = LibraryContract.BookEntry.COLUMN_BOOK_CATEGORY + " = ?";
        String[] selectionArgs = { String.valueOf(categoryID) };

        Cursor cursor = db.query(LibraryContract.BookEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndexOrThrow(LibraryContract.BookEntry.COLUMN_BOOK_ID);
            int indexName = cursor.getColumnIndexOrThrow(LibraryContract.BookEntry.COLUMN_BOOK_NAME);
            int indexAuthor = cursor.getColumnIndexOrThrow(LibraryContract.BookEntry.COLUMN_BOOK_AUTHOR);
            int indexContent = cursor.getColumnIndexOrThrow(LibraryContract.BookEntry.COLUMN_BOOK_CONTENT);
            int indexThumbnailPath = cursor.getColumnIndexOrThrow(LibraryContract.BookEntry.COLUMN_BOOK_THUMBNAIL);
            int indexCategoryId = cursor.getColumnIndexOrThrow(LibraryContract.BookEntry.COLUMN_BOOK_CATEGORY);

            do {
                Book book = new Book();
                book.setBookId(cursor.getInt(indexId));
                book.setName(cursor.getString(indexName));
                book.setAuthor(cursor.getString(indexAuthor));
                book.setContent(cursor.getString(indexContent));
                book.setThumbnailPathId(cursor.getInt(indexThumbnailPath));
                book.setCategoryId(cursor.getInt(indexCategoryId));

                books.add(book);

            } while (cursor.moveToNext());
        }

        return books;
    }

    public Book getBookById(int bookId){
        Book book = new Book();

        SQLiteDatabase db = this.getReadableDatabase();

        String selection = LibraryContract.BookEntry.COLUMN_BOOK_ID + " = ?";
        String[] selectionArgs = { String.valueOf(bookId) };

        Cursor cursor = db.query(LibraryContract.BookEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndexOrThrow(LibraryContract.BookEntry.COLUMN_BOOK_ID);
            int indexName = cursor.getColumnIndexOrThrow(LibraryContract.BookEntry.COLUMN_BOOK_NAME);
            int indexAuthor = cursor.getColumnIndexOrThrow(LibraryContract.BookEntry.COLUMN_BOOK_AUTHOR);
            int indexContent = cursor.getColumnIndexOrThrow(LibraryContract.BookEntry.COLUMN_BOOK_CONTENT);
            int indexThumbnailPath = cursor.getColumnIndexOrThrow(LibraryContract.BookEntry.COLUMN_BOOK_THUMBNAIL);
            int indexCategoryId = cursor.getColumnIndexOrThrow(LibraryContract.BookEntry.COLUMN_BOOK_CATEGORY);


            book.setBookId(cursor.getInt(indexId));
            book.setName(cursor.getString(indexName));
            book.setAuthor(cursor.getString(indexAuthor));
            book.setContent(cursor.getString(indexContent));
            book.setThumbnailPathId(cursor.getInt(indexThumbnailPath));
            book.setCategoryId(cursor.getInt(indexCategoryId));

        }
        return book;

    }

    public int deleteBookById(int bookId){
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = LibraryContract.BookEntry.COLUMN_BOOK_ID + " = ?";
        String[] selectionArgs = { String.valueOf(bookId) };


        return db.delete(LibraryContract.BookEntry.TABLE_NAME, selection, selectionArgs);
    }

    public int deleteAllBookByCategory(int categoryId){
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = LibraryContract.BookEntry.COLUMN_BOOK_CATEGORY + " = ?";
        String[] selectionArgs = { String.valueOf(categoryId) };

        return db.delete(LibraryContract.BookEntry.TABLE_NAME, selection, selectionArgs);
    }

    public int deleteThumbnailPathById(int thumbnailPathId){
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = LibraryContract.ThumbnailPathEntry.COLUMN_THUMBNAIL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(thumbnailPathId) };

        return db.delete(LibraryContract.ThumbnailPathEntry.TABLE_NAME, selection, selectionArgs);
    }




    public ArrayList<Category> getAllCategory(){
        ArrayList<Category> categories = new ArrayList<>();
        String[] projection = {
                LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID,
                LibraryContract.CategoryEntry.COLUMN_CATEGORY_NAME
        };

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                LibraryContract.CategoryEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndexOrThrow(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID);
            int indexName = cursor.getColumnIndexOrThrow(LibraryContract.CategoryEntry.COLUMN_CATEGORY_NAME);
            do {
                Category category = new Category();
                category.setCategoryId(cursor.getInt(indexId));
                category.setCategoryName(cursor.getString(indexName));
                categories.add(category);

            } while (cursor.moveToNext());
        }

        return categories;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
