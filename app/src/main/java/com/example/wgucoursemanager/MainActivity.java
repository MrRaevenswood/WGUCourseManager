package com.example.wgucoursemanager;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openTermsWindow(View view) {

        DBConnHelper db = new DBConnHelper(MainActivity.this);
        Intent intent = new Intent(MainActivity.this, TermsActivity.class);
        intent.setData(WGUProvider.CONTENT_URI);
        startActivity(intent);
    }
}
