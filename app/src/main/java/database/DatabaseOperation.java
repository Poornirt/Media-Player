package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.util.ArrayList;

import jdo.MediaJdo;

public class DatabaseOperation extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "media_db";
    private static final int DATABASE_VERSION = 1;
    private final String TABLE_NAME = "AUDIO_FILES";
    private final String AUDIO_NAME = "audio_name";
    private final String AUDIO_DURATION = "audio_duration";
    private final String AUDIO_PATH = "audio_path";
    private final String AUDIO_IMAGE = "audio_image";
    private SQLiteDatabase mSqLiteDatabase;


    public DatabaseOperation(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + "(" +
                AUDIO_NAME + " TEXT," +
                AUDIO_PATH + " TEXT," +
                AUDIO_DURATION + " TEXT," +
                AUDIO_IMAGE + " TEXT)");
    }


    public void insertValues(ArrayList<MediaJdo> mediaJdoArrayList) {
        mSqLiteDatabase = this.getWritableDatabase();
        try {
            mSqLiteDatabase.beginTransaction();
            ContentValues contentValues = new ContentValues();
            for (MediaJdo mediaJdo : mediaJdoArrayList) {
                contentValues.put(AUDIO_NAME, mediaJdo.getmAudioname());
                contentValues.put(AUDIO_DURATION, mediaJdo.getmDuration());
                contentValues.put(AUDIO_IMAGE, mediaJdo.getmImgUrl());
                contentValues.put(AUDIO_PATH, mediaJdo.getmPath());
                mSqLiteDatabase.insert(TABLE_NAME, null, contentValues);
            }
            mSqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mSqLiteDatabase.endTransaction();
        }
    }


    public ArrayList<MediaJdo> fetchFromDatabase() {
        ArrayList<MediaJdo> lMediaJdoArrayList = new ArrayList<>();
        MediaJdo lMediaJdo;
        Cursor lCursor = null;
        mSqLiteDatabase = this.getReadableDatabase();
        try {
            String lDBquery = "SELECT * FROM " + TABLE_NAME;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                lCursor = mSqLiteDatabase.rawQuery(lDBquery, null, null);
            }
            else {
                lCursor=mSqLiteDatabase.query(lDBquery,null,null,null,null,null,null);
            }
            if (lCursor != null && lCursor.moveToFirst()) {
                do {
                    lMediaJdo = new MediaJdo();
                    String lAudioname = lCursor.getString(lCursor.getColumnIndex("audio_name"));
                    String lAudiopath = lCursor.getString(lCursor.getColumnIndex("audio_path"));
                    long lAudioduration = lCursor.getLong(lCursor.getColumnIndex("audio_duration"));
                    String lAudioimageURL = lCursor.getString(lCursor.getColumnIndex("audio_image"));
                    lMediaJdo.setmAudioname(lAudioname);
                    lMediaJdo.setmPath(lAudiopath);
                    lMediaJdo.setmDuration(lAudioduration);
                    lMediaJdo.setmImgUrl(lAudioimageURL);
                    lMediaJdoArrayList.add(lMediaJdo);
                } while (lCursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            lCursor.close();   
        }
        return lMediaJdoArrayList;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
