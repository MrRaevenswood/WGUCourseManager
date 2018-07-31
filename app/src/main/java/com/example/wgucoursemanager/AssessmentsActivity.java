package com.example.wgucoursemanager;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

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

    private ArrayList<String> getAssessmentData(String selectedAssessment){
        Cursor assessmentData = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                DBConnHelper.ASSESSMENTS_ALL_COLUMNS, DBConnHelper.ASSESSMENT_TITLE + " = " + "\"" + selectedAssessment + "\"", null, null);
        assessmentData.moveToFirst();

        ArrayList<String> assessmentDataFound = new ArrayList<>();
        for(int i = 0; i < DBConnHelper.ASSESSMENTS_ALL_COLUMNS.length; i++){
            assessmentDataFound.add(assessmentData.getString(i));
        }

        return assessmentDataFound;
    }

    @Override
    protected void onListItemClick(final ListView l, View v, final int position, long id){
        AlertDialog.Builder editAssessment = new AlertDialog.Builder(this);
        editAssessment.setTitle("Would you like to edit, delete, share the selected assessment?");

        editAssessment.setPositiveButton("Edit", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Cursor selectedAssessmentFromList = (Cursor)l.getItemAtPosition(position);

                String selectedAssessment = selectedAssessmentFromList.getString(selectedAssessmentFromList.getColumnIndex(DBConnHelper.ASSESSMENT_TITLE));
                Intent editAssessment = new Intent(AssessmentsActivity.this, AssessmentAddActivity.class);
                editAssessment.putExtra("Edit", 1);
                editAssessment.putStringArrayListExtra("selectedAssessment", getAssessmentData(selectedAssessment));
                startActivityForResult(editAssessment, ADD_ASSESSMENT_CODE);

                selectedAssessmentFromList.close();
            }
        });

        editAssessment.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Cursor selectedAssessmentDataToDelete = (Cursor)l.getItemAtPosition(position);

                String selectedAssessmentData = selectedAssessmentDataToDelete.getString(
                        selectedAssessmentDataToDelete.getColumnIndex(DBConnHelper.ASSESSMENT_TITLE)
                );
                String selectedAssessmentDataId = getAssessmentData(selectedAssessmentData).get(0);

                getContentResolver().delete(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                        DBConnHelper.PK_Assessment_ID  + " = " + selectedAssessmentDataId, null);
                getContentResolver().delete(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_IN_COURSES_ID),
                        DBConnHelper.FK_ASSESSMENTS_ID_IN_COURSES + " = " + selectedAssessmentDataId, null);

                selectedAssessmentDataToDelete.close();
                restartLoader();
            }
        });

        editAssessment.setNeutralButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Cursor selectedAssessmentToShare = (Cursor) l.getItemAtPosition(position);

                String assessmentType = "";
                if(selectedAssessmentToShare.getString(selectedAssessmentToShare.getColumnIndex
                        (DBConnHelper.ASSESSMENT_ISPERFORMANCE)).equals("1")){
                    assessmentType = "Performance";
                }else
                    assessmentType = "Objective" ;

                String assessmentInfo = "Assessment Title: " + selectedAssessmentToShare.getString(selectedAssessmentToShare.getColumnIndex(DBConnHelper.ASSESSMENT_TITLE))
                        + "\r\n" + "Assessment Type: " + assessmentType + "\r\n" +"Goal Date: " +
                        selectedAssessmentToShare.getString(selectedAssessmentToShare.getColumnIndex(DBConnHelper.ASSESSMENT_GOAL_DATE));

                Intent share = new Intent();

                share.setAction(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_TEXT, assessmentInfo);
                share.setType("text/plain");
                startActivity(share);
                selectedAssessmentToShare.close();
            }
        });

        editAssessment.create().show();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(ADD_ASSESSMENT_CODE, null , this);
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
                null, null, null, null);
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
