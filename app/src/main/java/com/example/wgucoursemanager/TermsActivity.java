package com.example.wgucoursemanager;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Toolbar;

public class TermsActivity extends ListActivity
    implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int ADD_TERM_CODE = 1000;
    SimpleCursorAdapter termAdapter;
    int[] toViews = {android.R.id.text1, android.R.id.text2};
    private static final String[] PROJECTION = new String[] {
            DBConnHelper.TERM_TITLE, DBConnHelper.TERM_RANGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_terms);

        termAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null,
                PROJECTION,toViews, 0);
        setListAdapter(termAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent openTermAdd = new Intent(TermsActivity.this, termAddActivity.class);
                Uri uri = Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.TERMS_ID);
                openTermAdd.putExtra(WGUProvider.CONTENT_ITEM_TERM,uri);
                startActivityForResult(openTermAdd, ADD_TERM_CODE);
            }
        });

        FloatingActionButton goToCourses = findViewById(R.id.goToCourses);
        goToCourses.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent goToCourses = new Intent(TermsActivity.this, CourseActivity.class);
                startActivity(goToCourses);
            }
        });

        FloatingActionButton goToAssessments = findViewById(R.id.goToAssess);
        goToAssessments.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent goToAssessments = new Intent(TermsActivity.this, AssessmentsActivity.class);
                startActivity(goToAssessments);
            }
        });

        getLoaderManager().initLoader(ADD_TERM_CODE,null,this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == ADD_TERM_CODE){
            if(resultCode == RESULT_OK){
                getLoaderManager().restartLoader(ADD_TERM_CODE, null, this);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.TERMS_ID),
                PROJECTION, null, null, null );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        termAdapter.swapCursor(data);
        termAdapter.notifyDataSetChanged();

        synchronized (getListAdapter()){
            getListAdapter().notifyAll();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        termAdapter.swapCursor(null);
    }
}
