package com.example.wgucoursemanager;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;

public class CourseActivity extends ListActivity
    implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int ADD_COURSE_CODE = 2000;
    SimpleCursorAdapter courseAdapter;
    int[] toViews = {android.R.id.text1, android.R.id.text2};
    private static final String[] PROJECTION = new String[] {
            DBConnHelper.COURSE_TITLE, DBConnHelper.COURSE_RANGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        courseAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null,
                PROJECTION, toViews, 0);

        setListAdapter(courseAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openCourseAdd = new Intent(CourseActivity.this, CourseAddActivity.class);
                Uri uri = Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSE_ID);
                openCourseAdd.putExtra(WGUProvider.CONTENT_ITEM_COURSE, uri);
                startActivityForResult(openCourseAdd, ADD_COURSE_CODE);
            }
        });

        getLoaderManager().initLoader(ADD_COURSE_CODE, null, this);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.course_activity_menu, menu);
        return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            case R.id.goToAssess:
                Intent goToAssess = new Intent(CourseActivity.this, AssessmentsActivity.class);
                startActivity(goToAssess);
                break;
            case R.id.goToTerms:
                Intent goToTerms = new Intent(CourseActivity.this, TermsActivity.class);
                startActivity(goToTerms);
                break;
        }
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == ADD_COURSE_CODE){
            if(resultCode == RESULT_OK){
                getLoaderManager().restartLoader(ADD_COURSE_CODE, null, this);
            }
        }
   }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSE_ID),
                PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        courseAdapter.swapCursor(data);
        courseAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        courseAdapter.swapCursor(null);
    }
}
