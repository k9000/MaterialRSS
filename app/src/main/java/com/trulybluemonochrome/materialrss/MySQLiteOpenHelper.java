package com.trulybluemonochrome.materialrss;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String DB = "RSS_URI.db";
    public static final int DB_VERSION = 1;
    public static final String CREATE_CATEGORY_TABLE = "create table folder ( _id integer primary key autoincrement, category integer not null );";
    public static final String INSERT_DEFAULT_VALUE = "insert into folder values(null, 'Default');";
    public static final String CREATE_FEEDS_TABLE = "create table feeds ( _id integer primary key autoincrement, category category not null, title integer not null, url integer not null );";
    public static final String DROP_TABLE = "drop table mytable;";

    public MySQLiteOpenHelper(Context c) {
        super(c, DB, null, DB_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CATEGORY_TABLE);
        db.execSQL(INSERT_DEFAULT_VALUE);
        db.execSQL(CREATE_FEEDS_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
}
