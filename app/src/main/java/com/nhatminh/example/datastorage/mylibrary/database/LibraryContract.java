package com.nhatminh.example.datastorage.mylibrary.database;

import android.provider.BaseColumns;

public final class LibraryContract {
    private LibraryContract() {

    }

    public static class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME  = "category";
        public static final String COLUMN_CATEGORY_ID = "_id";
        public static final String COLUMN_CATEGORY_NAME = "name";
    }


    public static class BookEntry implements BaseColumns {
        public static final String TABLE_NAME  = "book";
        public static final String COLUMN_BOOK_ID = "_id";
        public static final String COLUMN_BOOK_NAME = "name";
        public static final String COLUMN_BOOK_AUTHOR = "author";
        public static final String COLUMN_BOOK_CONTENT = "content";
        public static final String COLUMN_BOOK_THUMBNAIL = "thumbnail";
        public static final String COLUMN_BOOK_CATEGORY = "category";
    }


}
