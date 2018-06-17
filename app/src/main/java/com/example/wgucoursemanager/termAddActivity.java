package com.example.wgucoursemanager;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import java.util.Calendar;

public class termAddActivity extends AppCompatActivity {

    private static TextView termTitle;
    private static TextView termStart;
    private static TextView termEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        termTitle = findViewById(R.id.termTitle);
        termStart = findViewById(R.id.termStartDate);
        termEnd = findViewById(R.id.termEndDate);

        Toolbar toolbar = findViewById(R.id.addTermToolBar);
        toolbar.setTitle(R.string.Add_Edit_Term);
        setSupportActionBar(toolbar);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.newmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.save:
                String title = termTitle.getText().toString();
                String start = termStart.getText().toString();
                String end = termEnd.getText().toString();
                Term newTerm = new Term(title,start,end);
                saveTerm(newTerm);
                break;
            case R.id.cancel:
                Intent returnToTermsView = new Intent(termAddActivity.this, TermsActivity.class);
                startActivity(returnToTermsView);
                break;
        }

        return true;
    }

    private void saveTerm(Term newTerm) {

        ContentValues values = new ContentValues();

        values.put(DBConnHelper.TERM_TITLE, newTerm.getTermTitle());
        values.put(DBConnHelper.TERM_START, newTerm.getStartDate());
        values.put(DBConnHelper.TERM_END, newTerm.getEndDate());

        getContentResolver().insert(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.TERMS_ID), values);

        setResult(RESULT_OK);
        finish();

    }

    public void showStartDatePickerDialog(View view){
        Bundle startBundle = new Bundle();
        startBundle.putString("startDate","startDate");
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setArguments(startBundle);

        startDatePicker.show(getFragmentManager(), "datePicker");
    }

    public void showEndDatePickerDialog(View view) {
        Bundle endBundle = new Bundle();
        endBundle.putString("endDate", "endDate");

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setArguments(endBundle);

        endDatePicker.show(getFragmentManager(), "datePicker");
    }

    public static class DatePicker extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public int year;
        public int month;
        public int day;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {

            if(this.getArguments().containsKey("startDate")){
                termStart.setText(String.valueOf(month) + "/" + String.valueOf(dayOfMonth) + "/"
                        + String.valueOf(year));
            }else if(this.getArguments().containsKey("endDate")){
                termEnd.setText(String.valueOf(month) + "/" + String.valueOf(dayOfMonth) + "/"
                        + String.valueOf(year));
            };

        }

    }

}
