package com.example.wgucoursemanager;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class TermsActivity extends AppCompatActivity {

    private static final int ADD_TERM_CODE = 1000;
    ListView termList;
    ArrayList<String> termData = new ArrayList();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        termList = findViewById(R.id.ListOfTerms);
        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_terms, termData);
        termList.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openTermAdd = new Intent(TermsActivity.this, termAddActivity.class);
                startActivityForResult(openTermAdd, ADD_TERM_CODE);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.term_edit, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == ADD_TERM_CODE){
            if(resultCode == RESULT_OK){
                Uri insertedTerm = data.getData();
                Cursor termCursor = getContentResolver().query(insertedTerm,null,null,
                        null, "DESC");
                termCursor.moveToFirst();

                termData.add(termCursor.getString(1));
                adapter.notifyDataSetChanged();

            }
        }
    }

}
