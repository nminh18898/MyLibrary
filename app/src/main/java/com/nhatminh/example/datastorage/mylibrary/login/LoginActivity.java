package com.nhatminh.example.datastorage.mylibrary.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nhatminh.example.datastorage.mylibrary.R;
import com.nhatminh.example.datastorage.mylibrary.category.MainActivity;
import com.nhatminh.example.datastorage.mylibrary.constant.Constant;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin;
    EditText etUsername;
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btnLogin);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticateUser();
            }
        });

    }



    private void authenticateUser() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if(username.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Empty", Toast.LENGTH_SHORT).show();
            return;
        }


        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int databasePassword = sharedPref.getInt(username, Constant.Role.DEFAULT);

        if(databasePassword == Constant.Role.DEFAULT){
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if(Integer.valueOf(password) == databasePassword){
            Intent startMainActivity = new Intent(LoginActivity.this, MainActivity.class);
            startMainActivity.putExtra(Constant.Role.ROLE, Integer.valueOf(password));
            startActivity(startMainActivity);
            finish();
        }
        else {
            Toast.makeText(this, "Password not correct", Toast.LENGTH_SHORT).show();
        }


    }


    public void createUser(){

        User admin = new User("1", Constant.Role.ADMIN);
        saveUser(admin);


        User visitor = new User("2",  Constant.Role.VISITOR);
        saveUser(visitor);

        User manager = new User("3", Constant.Role.MANAGER);
        saveUser(manager);

    }

    public void saveUser(User user){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(user.getUserId(), user.getRole());
        editor.commit();
    }
}
