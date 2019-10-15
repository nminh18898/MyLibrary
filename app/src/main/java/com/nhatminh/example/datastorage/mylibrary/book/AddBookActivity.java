package com.nhatminh.example.datastorage.mylibrary.book;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import com.nhatminh.example.datastorage.mylibrary.R;
import com.nhatminh.example.datastorage.mylibrary.constant.Constant;
import com.nhatminh.example.datastorage.mylibrary.database.LibraryContract;
import com.nhatminh.example.datastorage.mylibrary.database.LibraryDatabaseHelper;

public class AddBookActivity extends AppCompatActivity {

    private static final int REQUEST_IMPORT_PHOTO = 2;
    Button btnChooseImage, btnConfirm, btnReset, btnTest;
    EditText etThumbnail, etName, etAuthor, etContent;
    LibraryDatabaseHelper database = new LibraryDatabaseHelper(this);
    Book bookUpdated = new Book();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        setupView();

        if(getIntent().getExtras().getString("action").equals("add")){
            createData();
        }
        else {
            passData();
            btnConfirm.setText("Update");
        }


    }

    void passData(){
        int bookId = getIntent().getExtras().getInt(LibraryContract.BookEntry.COLUMN_BOOK_ID);

        bookUpdated = database.getBookById(bookId);

        etName.setText(bookUpdated.getName());
        etAuthor.setText(bookUpdated.getAuthor());
        etContent.setText(bookUpdated.getContent());

        ThumbnailPath thumbnailPath = database.getThumbnailPathById(bookUpdated.getThumbnailPathId());

        if(thumbnailPath.getDatabasePath()!= null){
            etThumbnail.setText(thumbnailPath.getDatabasePath());
        }
        else {
            etThumbnail.setText(thumbnailPath.getOriginalPath());
        }

    }

    void addBook(){
        Book book = new Book();
        book.setName(etName.getText().toString());
        book.setAuthor(etAuthor.getText().toString());
        book.setContent(etContent.getText().toString());
        book.setCategoryId(getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));

        int thumbnailPathId = database.insertThumbnailOriginalPath(etThumbnail.getText().toString());
        book.setThumbnailPathId(thumbnailPathId);

        int newBookId = database.insertBook(book);
        Intent intent = new Intent();
        intent.putExtra(Constant.Database.BOOK_INSERTED_ID, newBookId);
        finishActivityWithResultOk(intent);
    }

    void updateBook(){
        bookUpdated.setName(etName.getText().toString());
        bookUpdated.setAuthor(etAuthor.getText().toString());
        bookUpdated.setContent(etContent.getText().toString());

        String userThumbnailPath = etThumbnail.getText().toString();
        ThumbnailPath thumbnailPath = database.getThumbnailPathById(bookUpdated.getThumbnailPathId());

        if(!thumbnailPath.getOriginalPath().equals(userThumbnailPath) &&
            !thumbnailPath.getDatabasePath().equals(userThumbnailPath)){
            thumbnailPath.setOriginalPath(userThumbnailPath);
            thumbnailPath.setDatabasePath(null);
            database.updateThumbnailPath(thumbnailPath);
        }

        database.updateBook(bookUpdated);

        Intent intent = new Intent();
        finishActivityWithResultOk(intent);
    }

    void finishActivityWithResultOk(Intent returnIntent){
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    void setupView(){
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchImage();
            }
        });

        etThumbnail = findViewById(R.id.etThumbnail);
        etName = findViewById(R.id.etName);
        etAuthor = findViewById(R.id.etAuthor);
        etContent = findViewById(R.id.etContent);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getIntent().getExtras().getString("action").equals("add") ){
                    addBook();
                }
                else {
                   updateBook();
                }
            }
        });

        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetView();
            }
        });

        btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTestData();
            }
        });
    }

    void createTestData(){
        // Book #0
        Book book = new Book();
        book.setName("Think and Grow Rich: The Original, an Official Publication of The Napoleon Hill Foundation");
        book.setAuthor("Napoleon Hill ");
        book.setContent("This edition of Napoleon Hill's Classic Think and Grow Rich is a reproduction of Napoleon Hill's personal copy of the first edition, the ONLY original version recommended by The Napoleon Hill Foundation, originally printed in March of 1937.");
        book.setCategoryId(getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));

        int thumbnailPathId = database.insertThumbnailOriginalPath("https://images-na.ssl-images-amazon.com/images/I/51Yv0xlmb8L._SY346_.jpg");
        book.setThumbnailPathId(thumbnailPathId);

        database.insertBook(book);

        // Book #1
        book = new Book();
        book.setName("The 4-Hour Workweek, Expanded and Updated: Expanded and Updated, With Over 100 New Pages of Cutting-Edge Content.");
        book.setAuthor("Timothy Ferriss ");
        book.setContent("Forget the old concept of retirement and the rest of the deferred-life plan–there is no need to wait and every reason not to, especially in unpredictable economic times. Whether your dream is escaping the rat race, experiencing high-end world travel, or earning a monthly five-figure income with zero management, The 4-Hour Workweek is the blueprint.");
        book.setCategoryId(getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));

        thumbnailPathId = database.insertThumbnailOriginalPath("https://images-na.ssl-images-amazon.com/images/I/51pPn2LkI8L.jpg");
        book.setThumbnailPathId(thumbnailPathId);
        database.insertBook(book);

        // Book #2
        book = new Book();
        book.setName("Tribe of Mentors: Short Life Advice from the Best in the World");
        book.setAuthor("Timothy Ferriss");
        book.setContent("This book contains their answers—practical and tactical advice from mentors who have found solutions. Whether you want to 10x your results, get unstuck, or reinvent yourself, someone else has traveled a similar path and taken notes.");
        book.setCategoryId(getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));

        thumbnailPathId = database.insertThumbnailOriginalPath("https://images-na.ssl-images-amazon.com/images/I/51jkW-GzFiL.jpg");
        book.setThumbnailPathId(thumbnailPathId);
        database.insertBook(book);

        // Book #3
        book = new Book();
        book.setName("Stillness Is the Key");
        book.setAuthor("Ryan Holiday");
        book.setContent("All great leaders, thinkers, artists, athletes, and visionaries share one indelible quality. It enables them to conquer their tempers. To avoid distraction and discover great insights. To achieve happiness and do the right thing. Ryan Holiday calls it stillness--to be steady while the world spins around you.");
        book.setCategoryId(getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));

        thumbnailPathId = database.insertThumbnailOriginalPath("https://images-na.ssl-images-amazon.com/images/I/41lasz04WVL.jpg");
        book.setThumbnailPathId(thumbnailPathId);
        database.insertBook(book);

        // Book #4
        book = new Book();
        book.setName("The Obstacle is the Way: The Ancient Art of Turning Adversity to Advantage");
        book.setAuthor("Ryan Holiday");
        book.setContent("We give up too easily. With a simple change of attitude, what seem like insurmountable obstacles become once-in-a-lifetime opportunities. Ryan Holiday, who dropped out of college at nineteen to serve as an apprentice to bestselling 'modern Machiavelli' Robert Greene and is now a media consultant for billion-dollar brands, draws on the philosophy of the Stoics to guide you in every situation, showing that what blocks our path actually opens one that is new and better.");
        book.setCategoryId(getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));

        thumbnailPathId = database.insertThumbnailOriginalPath("https://images-na.ssl-images-amazon.com/images/I/517Zp0Ul6OL.jpg");
        book.setThumbnailPathId(thumbnailPathId);
        database.insertBook(book);

        // Book #5
        book = new Book();
        book.setName("Perennial Seller: The Art of Making and Marketing Work that Lasts");
        book.setAuthor("Ryan Holiday");
        book.setContent("Bestselling author and marketer Ryan Holiday calls such works and artists perennial sellers. How do they endure and thrive while most books, movies, songs, video games, and pieces of art disappear quickly after initial success? How can we create and market creative works that achieve longevity?");
        book.setCategoryId(getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));

        thumbnailPathId = database.insertThumbnailOriginalPath("https://images-na.ssl-images-amazon.com/images/I/51TVamX9bsL.jpg");
        book.setThumbnailPathId(thumbnailPathId);
        database.insertBook(book);

        // Book #6
        book = new Book();
        book.setName("Principles for Success");
        book.setAuthor("Ray Dalio");
        book.setContent("Principles for Success distills Ray Dalio’s 600-page bestseller, Principles: Life & Work, down to an easy-to-read and entertaining format that’s acces\u00ADsible to readers of all ages. It contains the key elements of the unconven\u00ADtional principles that helped Dalio become one of the world’s most suc\u00ADcessful people—and that have now been read and shared by millions worldwide—including how to set goals, learn from mistakes, and collaborate with others to produce exceptional results. Whether you’re already a fan of the ideas in Princi\u00ADples or are discovering them for the first time, this illustrated guide will help you achieve success in having the life that you want to have.");
        book.setCategoryId(getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));

        thumbnailPathId = database.insertThumbnailOriginalPath("https://images-na.ssl-images-amazon.com/images/I/41mD%2Bn0RquL.jpg");
        book.setThumbnailPathId(thumbnailPathId);
        database.insertBook(book);

        // Book #7
        book = new Book();
        book.setName("The Rule: How I Beat the Odds in the Markets and in Life—and How You Can Too");
        book.setAuthor("Larry Hite ");
        book.setContent("In The Rule, legendary trader and hedge fund pioneer Larry Hite recounts his working-class upbringing in Brooklyn as a dyslexic, partially blind kid who was anything but a model student—and how he went on to found and run Mint Investment Management Company, one of the most profitable and largest quantitative hedge funds in the world.");
        book.setCategoryId(getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));

        thumbnailPathId = database.insertThumbnailOriginalPath("https://images-na.ssl-images-amazon.com/images/I/41N9eyn9yYL._SX329_BO1,204,203,200_.jpg");
        book.setThumbnailPathId(thumbnailPathId);
        database.insertBook(book);

        // Book #8
        book = new Book();
        book.setName("Successful Traders Size Their Positions - Why and How?");
        book.setAuthor("Tom Basso ");
        book.setContent("Starting with purchases of mutual funds as a newspaper delivery boy at 12, through a brief chemical engineering career and a stock portfolio, then through 28 years as a professional money manager with securities, futures and currencies, and currently as an individual investor of our retirement funds, I have seen a lot of things across the world of trading investments. I’ve seen academics and money managers make the investment process mysterious and complicated, intimidating many individuals attempting to manage their own portfolio. It need not be overwhelming to get started.\n");
        book.setCategoryId(getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));

        thumbnailPathId = database.insertThumbnailOriginalPath("https://images-na.ssl-images-amazon.com/images/I/41nhxrYUW1L.jpg");
        book.setThumbnailPathId(thumbnailPathId);
        database.insertBook(book);

        // Book #9
        book = new Book();
        book.setName("How to Turn $ 5,000 into a Million");
        book.setAuthor("Ashi Trader, Heikin");
        book.setContent("Can you become a millionaire on the stock market? The question of how to grow a small account undoubtedly occupies every trader’s mind. How do you manage to make a fortune out of a small amount? And preferably really fast?\n" +
                "Just as it is possible to build a real estate empire without a dollar of equity, so it is also possible to achieve high profits on the stock market with a small amount of starting capital (USD 5000 or less).");
        book.setCategoryId(getIntent().getExtras().getInt(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID));

        thumbnailPathId = database.insertThumbnailOriginalPath("https://images-na.ssl-images-amazon.com/images/I/41BIable-VL.jpg");
        book.setThumbnailPathId(thumbnailPathId);
        database.insertBook(book);

    }

    boolean isNetworkPath(String path){
        if(path.contains("http") || path.contains("https")){
            return true;
        }
        return false;
    }

    void resetView(){
        etName.getText().clear();
        etAuthor.getText().clear();
        etContent.getText().clear();
        etThumbnail.getText().clear();
    }

    public void searchImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMPORT_PHOTO);
    }

    public void createData(){
        etName.setText("Harry Potter and the Cursed Child");
        etAuthor.setText("JK Rowling");
        etContent.setText("It was always difficult being Harry Potter and it isn't much easier now that he is an overworked employee of the Ministry of Magic, a husband, and father of three school-age children.\n" +
                "\n" +
                "While Harry grapples with a past that refuses to stay where it belongs, his youngest son Albus must struggle with the weight of a family legacy he never wanted. As past and present fuse ominously, both father and son learn the uncomfortable truth: sometimes, darkness comes from unexpected places.\n" +
                "\n" +
                "The playscript for Harry Potter and the Cursed Child was originally released as a 'special rehearsal edition' alongside the opening of Jack Thorne's play in London's West End in summer 2016. Based on an original story by J.K. Rowling, John Tiffany and Jack Thorne, the play opened to rapturous reviews from theatregoers and critics alike, while the official playscript became an immediate global bestseller.\n" +
                "\n" +
                "This definitive and final playscript updates the 'special rehearsal edition' with the conclusive and final dialogue from the play, which has subtly changed since its rehearsals, as well as a conversation piece between director John Tiffany and writer Jack Thorne, who share stories and insights about reading playscripts. This edition also includes useful background information1");

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;

        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case REQUEST_IMPORT_PHOTO:
                    if (data != null) {
                        Uri uri = data.getData();
                        etThumbnail.setText(getPathFromURI(uri));

                    }
                    break;
            }
        }

    }

    @SuppressLint("ObsoleteSdkInt")
    public String getPathFromURI(Uri uri){
        String realPath="";
        // SDK < API11
        if (Build.VERSION.SDK_INT < 11) {
            String[] proj = { MediaStore.Images.Media.DATA };
            @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
            int column_index = 0;
            String result="";
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                realPath=cursor.getString(column_index);
            }
        }
        // SDK >= 11 && SDK < 19
        else if (Build.VERSION.SDK_INT < 19){
            String[] proj = { MediaStore.Images.Media.DATA };
            CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            if(cursor != null){
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                realPath = cursor.getString(column_index);
            }
        }
        // SDK > 19 (Android 4.4)
        else{
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            String[] column = { MediaStore.Images.Media.DATA };
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{ id }, null);
            int columnIndex = 0;
            if (cursor != null) {
                columnIndex = cursor.getColumnIndex(column[0]);
                if (cursor.moveToFirst()) {
                    realPath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        }
        return realPath;
    }
}
