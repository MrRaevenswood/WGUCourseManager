package com.example.wgucoursemanager;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;



public class TermsActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int ADD_TERM_CODE = 1000;
    ListView termList;
    private CursorAdapter termAdapter;
    WGUProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        termAdapter = new TermCursorAdapter(this, null);
        termList = findViewById(R.id.assignedTerms);
        termList.setAdapter(termAdapter);


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
                System.out.println("READY!!!!!!!!");

                restartLoader();
            }
        }
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0,null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, WGUProvider.CONTENT_URI
                , DBConnHelper.TERMS_ALL_COLUMNS
            , null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToFirst();
        termAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        termAdapter.changeCursor(null);
    }

    private class TermCursorAdapter extends CursorAdapter{

        public TermCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(
                    R.layout.activity_terms, parent,false
            );
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String termText = cursor.getString(
                    cursor.getColumnIndex(DBConnHelper.TERM_TITLE)
            );

            int position = termText.indexOf(10);
            if(position != -1){
                termText = termText.substring(0, position) + "...";
            }
            System.out.println("Term text is: " + termText);
            TextView termView = view.findViewById(R.id.assignedTerms);
            termView.setText(termText);
        }
    }

}
