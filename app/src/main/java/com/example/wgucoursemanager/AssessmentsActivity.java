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
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;

public class AssessmentsActivity extends ListActivity
    implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int ADD_ASSESSMENT_CODE = 3000;
    SimpleCursorAdapter assessmentAdapter;
    int[] toViews = {android.R.id.text1, android.R.id.text2};
    private static final String[] PROJECTION = new String[]{
        DBConnHelper.ASSESSMENT_TITLE, DBConnHelper.ASSESSMENT_GOAL_DATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessments);

        assessmentAdapter = new SimpleCursorAdapter( this,
                android.R.layout.simple_list_item_2, null,
                PROJECTION, toViews, 0);

        setListAdapter(assessmentAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openAssessmentAdd = new Intent(AssessmentsActivity.this, AssessmentAddActivity.class);
                Uri uri = Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID);
                openAssessmentAdd.putExtra(WGUProvider.CONTENT_ITEM_ASSESSMENT, uri);
                startActivityForResult(openAssessmentAdd, ADD_ASSESSMENT_CODE);
            }
        });

        FloatingActionButton goToTerms = findViewById(R.id.goToTerms);
        goToTerms.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent goToTerms = new Intent(AssessmentsActivity.this, TermsActivity.class);
                startActivity(goToTerms);
            }
        });

        FloatingActionButton goToCourses = findViewById(R.id.goToCourses);
        goToCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToCourses = new Intent(AssessmentsActivity.this, CourseActivity.class);
                startActivity(goToCourses);
            }
        });

        getLoaderManager().initLoader(ADD_ASSESSMENT_CODE, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == ADD_ASSESSMENT_CODE && resultCode == RESULT_OK){

            getLoaderManager().restartLoader(ADD_ASSESSMENT_CODE, null , this);

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        assessmentAdapter.swapCursor(data);
        assessmentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        assessmentAdapter.swapCursor(null);
    }
}
