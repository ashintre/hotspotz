package com.buzzters.hotspotz.dao;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class hotspotzDbAdapter {

    public static final String KEY_TYPE = "type";
    public static final String KEY_PLACE = "place";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "hotspotzDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
 
  
    private static final String DATABASE_CREATE =
            "create table places (_id integer primary key autoincrement, "
                    + "type text not null, place text not null, latitude float not null, longitude float not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "places";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    
    public hotspotzDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    public hotspotzDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }


    
    public long insertPlaces(String type, String place, Float latitude, Float longitude) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_PLACE, place);
        initialValues.put(KEY_LATITUDE, latitude);
        initialValues.put(KEY_LONGITUDE, longitude);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    
    public boolean deleteRow(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    
    public Cursor fetchAllPlaces() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TYPE,
                KEY_PLACE, KEY_LATITUDE, KEY_LONGITUDE}, null, null, null, null, null);
    }

    
    public Cursor fetchPlace(String type) throws SQLException {

        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_TYPE, KEY_PLACE, KEY_LATITUDE, KEY_LONGITUDE}, KEY_TYPE + "=" + type, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    
    public boolean updatePlace(long rowId, String type, String place, Float latitude, Float longitude) {
        ContentValues args = new ContentValues();
        args.put(KEY_TYPE, type);
        args.put(KEY_PLACE, place);
        args.put(KEY_LATITUDE, latitude);
        args.put(KEY_LONGITUDE, longitude);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
