package com.example.wgucoursemanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBConnHelper extends SQLiteOpenHelper{

    //Constants for db name and version
    private static final String DATABASE_NAME = "wgucoursemanager.db";
    private static final int DATABASE_VERSION = 1;

    //Constants for identifying Term Table
    public static final String TABLE_TERMS = "terms";
    public static final String TERM_ID = "_id";
    public static final String TERM_TITLE = "termTitle";
    public static final String TERM_START = "termStart";
    public static final String TERM_END = "termEnd";
    public static final String TERM_RANGE = "termRange";

    public static final String[] TERMS_ALL_COLUMNS =
            {TERM_ID, TERM_TITLE, TERM_START, TERM_END, TERM_RANGE};

    //Constatns for identifying CoursesInTermTable
    public static final String TABLE_COURSES_IN_TERM = "coursesInTerm";
    public static final String COURSES_IN_TERM_ID = "_id";
    public static final String FK_TERM_ID = "termID";
    public static final String FK_COURSE_ID_TERMS = "courseID";

    public static final String[] COURSES_IN_TERM_ALL_COLUMNS =
            {COURSES_IN_TERM_ID, FK_TERM_ID, FK_COURSE_ID_TERMS};

    //Constants for identifying CourseTable
    public static final String TABLE_COURSES = "courses";
    public static final String PK_COURSE_ID = "_id";
    public static final String COURSE_TITLE = "courseTitle";
    public static final String COURSE_START = "courseStart";
    public static final String COURSE_END = "courseEnd";
    public static final String COURSE_STATUS = "courseStatus";
    public static final String COURSE_MENTOR_NAME = "courseMentalName";
    public static final String COURSE_MENTOR_EMAIL = "courseMentorEmail";
    public static final String COURSE_MENTOR_PHONE = "courseMentorPhone";
    public static final String COURSE_NOTES = "courseNotes";
    public static final String COURSE_RANGE = "courseRange";

    public static final String[] COURSES_ALL_COLUMNS =
            {PK_COURSE_ID, COURSE_TITLE, COURSE_START, COURSE_END,
             COURSE_STATUS, COURSE_MENTOR_NAME, COURSE_MENTOR_EMAIL, COURSE_MENTOR_PHONE,
             COURSE_NOTES, COURSE_RANGE};

    //Constants for identifying AssessmentsInCourses table
    public static final String TABLE_ASSESSMENTS_IN_COURSES = "assessmentsInCourses";
    public static final String ASSESSMENTS_IN_COURSES_ID = "_id";
    public static final String FK_COURSE_ID_ASSESSMENTS = "courseID";
    public static final String FK_ASSESSMENTS_ID_IN_COURSES = "assessmentId";

    public static final String[] TABLE_ASSESSMENTS_IN_COURSES_ALL_COLUMNS =
            {ASSESSMENTS_IN_COURSES_ID, FK_COURSE_ID_ASSESSMENTS, FK_ASSESSMENTS_ID_IN_COURSES};

    //Constants for identifying Assessments Table
    public static final String TABLE_ASSESSMENTS = "assessments";
    public static final String PK_Assessment_ID = "_id";
    public static final String ASSESSMENT_TITLE = "assessmentTitle";
    public static final String ASSESSMENT_ISOBJECTIVE = "isObjective";
    public static final String ASSESSMENT_ISPERFORMANCE = "isPerformance";
    public static final String ASSESSMENT_GOAL_DATE = "goalDate";

    public static final String[] ASSESSMENTS_ALL_COLUMNS =
            {PK_Assessment_ID, ASSESSMENT_TITLE, ASSESSMENT_ISOBJECTIVE,
                    ASSESSMENT_ISPERFORMANCE, ASSESSMENT_GOAL_DATE };

    //Create Assessment Table
    private static final String CREATE_ASSESSMENTS_TABLE =
            "CREATE TABLE " + TABLE_ASSESSMENTS + " (" +
                    PK_Assessment_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ASSESSMENT_TITLE + " TEXT, " +
                    ASSESSMENT_ISOBJECTIVE + " BIT, " +
                    ASSESSMENT_ISPERFORMANCE + " BIT, " +
                    ASSESSMENT_GOAL_DATE + " DATETIME" + ")";

    //Create Course Table
    private static final String CREATE_COURSES_TABLE =
            "CREATE TABLE " + TABLE_COURSES + " (" +
                    PK_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COURSE_TITLE + " TEXT, " +
                    COURSE_START + " TEXT, " +
                    COURSE_END + " TEXT, " +
                    COURSE_STATUS + " TEXT, " +
                    COURSE_MENTOR_NAME + " TEXT, " +
                    COURSE_MENTOR_EMAIL + " TEXT, " +
                    COURSE_MENTOR_PHONE + " TEXT, " +
                    COURSE_NOTES + " TEXT, " +
                    COURSE_RANGE + " TEXT" + ")";

    //Create Term Table
    private static final String CREATE_TERM_TABLE =
            "CREATE TABLE " + TABLE_TERMS + " (" +
                    TERM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TERM_TITLE + " TEXT, " +
                    TERM_START + " TEXT, " +
                    TERM_END + " TEXT," +
                    TERM_RANGE + " TEXT" + ")";

    //Create CoursesInTermTable
    private static final String CREATE_COURSES_IN_TERM_TABLE =
            "CREATE TABLE " + TABLE_COURSES_IN_TERM + " (" +
                    COURSES_IN_TERM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FK_TERM_ID + " INTEGER REFERENCES " + TABLE_TERMS + "(" + TERM_ID + "), " +
                    FK_COURSE_ID_TERMS + " INTEGER REFERENCES " + TABLE_COURSES + "(" + PK_COURSE_ID + "))";

    //Create AssessmentsInCoursesTable
    private static final String CREATE_ASSESSMENTS_IN_COURSES_TABLE =
            "CREATE TABLE " + TABLE_ASSESSMENTS_IN_COURSES + " (" +
                    ASSESSMENTS_IN_COURSES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FK_COURSE_ID_ASSESSMENTS + " INTEGER REFERENCES " + TABLE_COURSES + "(" + PK_COURSE_ID + "), " +
                    FK_ASSESSMENTS_ID_IN_COURSES + " INTEGER REFERENCES " + TABLE_ASSESSMENTS + "(" + PK_Assessment_ID + "))";


    public DBConnHelper(Context context){ super(context, DATABASE_NAME, null, DATABASE_VERSION);}

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ASSESSMENTS_TABLE);
        db.execSQL(CREATE_COURSES_TABLE);
        db.execSQL(CREATE_TERM_TABLE);
        db.execSQL(CREATE_COURSES_IN_TERM_TABLE);
        db.execSQL(CREATE_ASSESSMENTS_IN_COURSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSESSMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TERMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES_IN_TERM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSESSMENTS_IN_COURSES);
        onCreate(db);
    }
}
