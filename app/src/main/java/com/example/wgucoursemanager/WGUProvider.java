package com.example.wgucoursemanager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.AutoScrollHelper;

public class WGUProvider extends ContentProvider {

    private static final String AUTHORITY = "com.example.wgucoursemanager.wguprovider";
    private static final String BASE_PATH = "wgucoursemanager";
    public  static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final int TERMS = 1;
    private static final int TERMS_ID = 2;
    private static final int COURSES = 3;
    private static final int COURSE_ID = 4;
    private static final int ASSESSMENTS = 5;
    private static final int ASSESSMENTS_ID = 6;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TERM = "Term";
    public static final String CONTENT_ITEM_COURSE = "Course";
    public static final String CONTENT_ITEM_ASSESSMENT = "Assessment";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, TERMS );
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TERMS_ID);
        uriMatcher.addURI(AUTHORITY, BASE_PATH, COURSES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", COURSE_ID);
        uriMatcher.addURI(AUTHORITY, BASE_PATH, ASSESSMENTS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ASSESSMENTS_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DBConnHelper helper = new DBConnHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        switch (uriMatcher.match(uri)) {
            case TERMS_ID:
                selection = DBConnHelper.TERM_ID + "=" + uri.getLastPathSegment();
                return database.query(DBConnHelper.TABLE_TERMS, DBConnHelper.TERMS_ALL_COLUMNS,
                        selection, null, null, null, DBConnHelper.TERM_START + " DESC");
            case COURSE_ID:
                selection = DBConnHelper.PK_COURSE_ID + "=" + uri.getLastPathSegment();
                return database.query(DBConnHelper.TABLE_COURSES, DBConnHelper.COURSES_ALL_COLUMNS,
                        selection, null, null, null, DBConnHelper.COURSE_START + " DESC");
            case ASSESSMENTS_ID:
                selection = DBConnHelper.PK_Assessment_ID + "=" + uri.getLastPathSegment();
                return database.query(DBConnHelper.TABLE_ASSESSMENTS, DBConnHelper.ASSESSMENTS_ALL_COLUMNS,
                        selection, null, null, null, DBConnHelper.ASSESSMENT_GOAL_DATE + " DESC");
        }

        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        long id = 0;

        switch(uriMatcher.match(uri)){
            case TERMS_ID:
                id = database.insert(DBConnHelper.TABLE_TERMS, null, values);
                break;
            case COURSE_ID:
                id = database.insert(DBConnHelper.TABLE_COURSES, null, values);
                break;
            case ASSESSMENTS_ID:
                id = database.insert(DBConnHelper.TABLE_ASSESSMENTS, null, values);
                break;
        }

        return Uri.parse(BASE_PATH + "/" + id);

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch(uriMatcher.match(uri)){
            case TERMS_ID:
                return database.delete(DBConnHelper.TABLE_TERMS, selection, selectionArgs);
            case COURSE_ID:
                return database.delete(DBConnHelper.TABLE_COURSES, selection, selectionArgs);
            case ASSESSMENTS_ID:
                return database.delete(DBConnHelper.TABLE_ASSESSMENTS, selection, selectionArgs);
        }

        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch(uriMatcher.match(uri)){
            case TERMS_ID:
                return database.update(DBConnHelper.TABLE_TERMS, values, selection, selectionArgs);
            case COURSE_ID:
                return database.update(DBConnHelper.TABLE_COURSES, values, selection, selectionArgs);
            case ASSESSMENTS_ID:
                return database.update(DBConnHelper.TABLE_ASSESSMENTS, values, selection, selectionArgs);
        }

        return 0;
    }
}
