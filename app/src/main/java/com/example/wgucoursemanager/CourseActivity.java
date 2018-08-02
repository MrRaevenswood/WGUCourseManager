package com.example.wgucoursemanager;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class CourseActivity extends ListActivity
    implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int ADD_COURSE_CODE = 2000;
    SimpleCursorAdapter courseAdapter;
    int[] toViews = {android.R.id.text1, android.R.id.text2};
    private static final String[] PROJECTION = new String[] {
            DBConnHelper.COURSE_TITLE, DBConnHelper.COURSE_START
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

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

        FloatingActionButton goToTerms = findViewById(R.id.goToTerms);
        goToTerms.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent goToTerms = new Intent(CourseActivity.this, TermsActivity.class);
                startActivity(goToTerms);
            }
        });

        FloatingActionButton goToAssessments = findViewById(R.id.goToAssess);
        goToAssessments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToAssessments = new Intent(CourseActivity.this, AssessmentsActivity.class);
                startActivity(goToAssessments);
            }
        });

        getLoaderManager().initLoader(ADD_COURSE_CODE, null, this);
   }

   private ArrayList<String> getCourseData(String selectedCourse){
        Cursor courseData = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSE_ID),
                DBConnHelper.COURSES_ALL_COLUMNS, DBConnHelper.COURSE_TITLE + " = " + "\"" + selectedCourse + "\"", null, null);

        courseData.moveToFirst();

        ArrayList<String> courseDataFound = new ArrayList<>();
        for(int i = 0; i < DBConnHelper.COURSES_ALL_COLUMNS.length; i++){
            courseDataFound.add(courseData.getString(i));
        }

        return courseDataFound;
   }

   @Override
   protected void onListItemClick(final ListView l, View v, final int position, long id){
       AlertDialog.Builder editCourse = new AlertDialog.Builder(this);
       editCourse.setTitle("Would you like to edit or delete the selected course?");

       editCourse.setPositiveButton("Edit", new DialogInterface.OnClickListener(){

           @Override
           public void onClick(DialogInterface dialog, int which) {
               Cursor selectedCourseFromList = (Cursor)l.getItemAtPosition(position);

               String selectedCourse = selectedCourseFromList.getString(1);
               Intent editCourse = new Intent(CourseActivity.this, CourseAddActivity.class);
               editCourse.putExtra( "Edit", 1);
               editCourse.putStringArrayListExtra("selectedCourse", getCourseData(selectedCourse));
               startActivityForResult(editCourse, ADD_COURSE_CODE);
           }
       });

       editCourse.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               Cursor selectedCourseDataToDelete = (Cursor)l.getItemAtPosition(position);

               String selectedCourseData = selectedCourseDataToDelete.getString(1);
               String selectedCourseDataId = getCourseData(selectedCourseData).get(0);

               getContentResolver().delete(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSE_ID),
                       DBConnHelper.PK_COURSE_ID + " = " + selectedCourseDataId, null);
               getContentResolver().delete(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSES_IN_TERM_ID),
                       DBConnHelper.FK_COURSE_ID_TERMS + " = " + selectedCourseDataId, null);
               getContentResolver().delete(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSES_WITH_ASSESSMENTS_ID),
                       DBConnHelper.FK_COURSE_ID_ASSESSMENTS + " = " + selectedCourseDataId, null);

               SharedPreferences prefs = getSharedPreferences("Alarms", Context.MODE_PRIVATE);
               SharedPreferences.Editor editor = prefs.edit();

               editor.remove(selectedCourseData + "-Start");
               editor.remove(selectedCourseData + "-End");
               editor.commit();

               restartLoader();
           }
       });

       editCourse.setNeutralButton("Share", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               Cursor selectedCourseToShare = (Cursor) l.getItemAtPosition(position);


               String courseInfo = "Course Title: " + selectedCourseToShare.getString(selectedCourseToShare.getColumnIndex(DBConnHelper.COURSE_TITLE))
                       + "\r\n" + "Course Start: " + selectedCourseToShare.getString(selectedCourseToShare.getColumnIndex(DBConnHelper.COURSE_START)) + "\r\n"
                       +"Course End: " + selectedCourseToShare.getString(selectedCourseToShare.getColumnIndex(DBConnHelper.COURSE_END)) + "\r\n" +
                       selectedCourseToShare.getString(selectedCourseToShare.getColumnIndex(DBConnHelper.COURSE_STATUS)) + "\r\n" + "Mentor Name: " +
                       selectedCourseToShare.getString(selectedCourseToShare.getColumnIndex(DBConnHelper.COURSE_MENTOR_NAME)) + "\r\n" + "Mentor Email: " +
                       selectedCourseToShare.getString(selectedCourseToShare.getColumnIndex(DBConnHelper.COURSE_MENTOR_EMAIL)) + "\r\n" + "Mentor Phone: " +
                       selectedCourseToShare.getString(selectedCourseToShare.getColumnIndex(DBConnHelper.COURSE_MENTOR_PHONE)) + "\r\n" + "Course Notes: " +
                       selectedCourseToShare.getString(selectedCourseToShare.getColumnIndex(DBConnHelper.COURSE_NOTES));

               Intent share = new Intent();

               share.setAction(Intent.ACTION_SEND);
               share.putExtra(Intent.EXTRA_TEXT, courseInfo);
               share.setType("text/plain");
               startActivity(share);
               selectedCourseToShare.close();
           }
       });

       editCourse.create().show();
   }

    private void restartLoader() {
        getLoaderManager().restartLoader(ADD_COURSE_CODE, null, this);
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

        return false;
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
                null, null, null, null);
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
