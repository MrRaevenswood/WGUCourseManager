package com.example.wgucoursemanager;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private DBConnHelper appDataConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appDataConn = new DBConnHelper(getApplicationContext());
        DBConnHelper db = new DBConnHelper(MainActivity.this);


    }

    public void openTermsWindow(View view) {

        Intent intent = new Intent(MainActivity.this, TermsActivity.class);
        intent.setData(WGUProvider.CONTENT_URI);
        startActivity(intent);
    }

    public void openCoursesWindow(View view){
        Intent intent = new Intent(MainActivity.this, CourseActivity.class);
        intent.setData(WGUProvider.CONTENT_URI);
        startActivity(intent);
    }

    public void openAssessmentWindow(View view){
        Intent intent = new Intent(MainActivity.this,AssessmentsActivity.class);
        intent.setData(WGUProvider.CONTENT_URI);
        startActivity(intent);
    }
}
