package com.example.evgen.myfirsttranslator.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Evgen on 25.04.2017.
 */

public class FavoriteDb {
    public static final String TABLE_NAME = "favorite";
    public static final String DATABASE_NAME = "favoriteDB";
    public static final int DATABASE_VERSION = 1;

    public static final String KEY_ID = "_id";
    public static final String KEY_SOURSE_TEXT = "_original";
    public static final String KEY_TRANS_TEXT = "_translate";
    public static final String KEY_SOURSE_LANG = "_sourseLang";
    public static final String KEY_TRANS_LANG = "_targetLang";


    private static final String DB_CREATE =
            "create table " + TABLE_NAME + " (" +
                    KEY_ID + " integer primary key autoincrement,"
                    + KEY_SOURSE_TEXT + " text,"
                    + KEY_TRANS_TEXT + " text,"
                    + KEY_SOURSE_LANG + " text,"
                    + KEY_TRANS_LANG + " text" +
                    ");";

    private final Context mCtx;

    private FavoriteDb.DbHelper mDbHelper;
    private SQLiteDatabase mDb;

    public FavoriteDb(Context context){
        mCtx = context;
    }

    public void open(){
        mDbHelper = new FavoriteDb.DbHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDb = mDbHelper.getWritableDatabase();
    }

    public void close(){
        if (mDbHelper != null) mDbHelper.close();
    }

    public Cursor getAllData(){
        return mDb.query(TABLE_NAME, null, null, null, null, null, null);
    }

    public long addRec(String sourseText, String transText, String codeFrom, String codeTo){
        ContentValues cv = new ContentValues();
        cv.put(FavoriteDb.KEY_SOURSE_TEXT, sourseText);
        cv.put(FavoriteDb.KEY_TRANS_TEXT, transText);
        cv.put(FavoriteDb.KEY_SOURSE_LANG, codeFrom);
        cv.put(FavoriteDb.KEY_TRANS_LANG, codeTo);
        return mDb.insert(FavoriteDb.TABLE_NAME, null, cv);
    }

    public int getCount(){
        return getAllData().getCount();
    }


    public void delRec(long id){
        mDb.delete(TABLE_NAME, KEY_ID + " = " + id, null);
    }

    public void delAll(){ mDb.delete(TABLE_NAME, null, null); }

    public static class DbHelper extends SQLiteOpenHelper {
        public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

}
