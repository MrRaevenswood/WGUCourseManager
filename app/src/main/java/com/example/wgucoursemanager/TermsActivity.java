package com.example.wgucoursemanager;

import android.app.ActionBar;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toolbar;

import java.util.ArrayList;

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

    private ArrayList<String> getTermData(String selectedTerm) {

        Cursor termData = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.TERMS_ID), DBConnHelper.TERMS_ALL_COLUMNS,
                DBConnHelper.TERM_TITLE + " = " + "\"" + selectedTerm + "\"",null, null);

        termData.moveToFirst();

        ArrayList<String> termDataFound = new ArrayList<>();
        for(int i = 0; i < DBConnHelper.TERMS_ALL_COLUMNS.length; i++){
            termDataFound.add(termData.getString(i));
        }

        return termDataFound;

    }

    @Override
    protected void onListItemClick(final ListView l, View v, final int position, long id){
        super.onListItemClick(l,v,position,id);

        AlertDialog.Builder editTerm = new AlertDialog.Builder(this);
        editTerm.setTitle("Would you like to edit or delete the selected term");

        editTerm.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Cursor selectedTermFromList = (Cursor)l.getItemAtPosition(position);

                String selectedTerm = selectedTermFromList.getString(1);

                Intent editTerm = new Intent(TermsActivity.this, termAddActivity.class);
                editTerm.putExtra("Edit", 1);
                editTerm.putStringArrayListExtra("selectedTerm", getTermData(selectedTerm));
                startActivityForResult(editTerm, ADD_TERM_CODE);
            }
        });

        editTerm.create().show();
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
                null, null, null, null );
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
