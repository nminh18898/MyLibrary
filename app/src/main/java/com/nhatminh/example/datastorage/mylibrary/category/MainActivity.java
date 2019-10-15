package com.nhatminh.example.datastorage.mylibrary.category;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhatminh.example.datastorage.mylibrary.R;
import com.nhatminh.example.datastorage.mylibrary.book.BookShelf;
import com.nhatminh.example.datastorage.mylibrary.constant.Constant;
import com.nhatminh.example.datastorage.mylibrary.database.LibraryContract;
import com.nhatminh.example.datastorage.mylibrary.database.LibraryDatabaseHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //permission
    private static final int REQUEST_SCAN_ACTIVITY_CODE = 100;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };
    private static final int REQUEST_PERMISSION_CODE = 200;

    private static boolean isPermissionGranted = false;



    int userRole;
    LibraryDatabaseHelper database = new LibraryDatabaseHelper(this);

    ArrayList<Category> categories;
    CategoryAdapter adapter;

    RecyclerView rvCategory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userRole = getIntent().getExtras().getInt(Constant.Role.ROLE, Constant.Role.DEFAULT);


        rvCategory = findViewById(R.id.rvCategory);

        categories = database.getAllCategory();
        adapter = new CategoryAdapter(this, categories);
        rvCategory.setLayoutManager(new LinearLayoutManager(this));

        rvCategory.setAdapter(adapter);

        rvCategory.addOnItemTouchListener(new CategoryListClickListener(this, rvCategory, new CategoryListClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MainActivity.this, BookShelf.class);
                intent.putExtra(LibraryContract.CategoryEntry.COLUMN_CATEGORY_ID, categories.get(position).getCategoryId());
                intent.putExtra(Constant.Role.ROLE, userRole);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {



            }
        }));

        checkPermission();


    }

    private void createCategory(){
        database.insertCategory("Math");
        database.insertCategory("English");
        database.insertCategory("Physics");
        database.insertCategory("Programing");
    }

    boolean hasPermission(String... permissions){
        if(permissions != null){
            for(String permission:permissions){
                if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    void checkPermission(){
        isPermissionGranted = hasPermission(PERMISSIONS);
        if(isPermissionGranted){

        }
        else {
            askPermission();
        }
    }

    void askPermission(){
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        isPermissionGranted = true;
                    }
                    else {
                        isPermissionGranted = false;
                    }
                }
            }
        }
    }
}
