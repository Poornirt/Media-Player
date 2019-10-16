package activity;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediaplayer.R;
import com.facebook.stetho.Stetho;

import java.util.ArrayList;

import adapter.RecyclerviewAdapter;
import database.DatabaseOperation;
import jdo.MediaJdo;
import listener.RecyclerListener;
import service.JobSchedulerservice;

import static constants.Constants.AUDIO_PATH;
import static constants.Constants.DB_COLUMN_DURATION;
import static constants.Constants.DB_COLUMN_PATH;
import static constants.Constants.DB_COLUMN_TITLE;
import static constants.Constants.IS_DATA_FETCHED;
import static constants.Constants.IS_FETCHED;
import static constants.Constants.RECYCLER_POSITION;
import static constants.Constants.SERVICE_TYPE;
import static constants.Constants.TYPES_OF_SERVICE;

public class MediaList extends AppCompatActivity {

    private RecyclerView mRecyclerview;
    private RecyclerviewAdapter mRecyclerviewAdapter;
    private static ArrayList<MediaJdo> mMediaJdoArrayList;
    private int REQUEST_CODE_FOR_PERMISSION = 123;
    private MediaJdo mMediaJdo;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private DatabaseOperation mDatabaseOperation;
    private boolean mIsFetched;
    private SharedPreferences mSharedPreferences;
    private String mChosenOption;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_list);
        mRecyclerview = findViewById(R.id.recycler_view);
        mProgressBar = findViewById(R.id.progress_circular);
        Stetho.initializeWithDefaults(this);
        mDatabaseOperation = new DatabaseOperation(MediaList.this);
        mMediaJdoArrayList = new ArrayList<>();
        mChosenOption = getIntent().getExtras().getString(SERVICE_TYPE);
        mLinearLayoutManager = new LinearLayoutManager(mRecyclerview.getContext());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mSharedPreferences = getSharedPreferences(IS_DATA_FETCHED, MODE_PRIVATE);
        mIsFetched = mSharedPreferences.getBoolean(IS_FETCHED, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissionToAccessAudio();
        }


        mRecyclerview.addOnItemTouchListener(new RecyclerListener(MediaList.this, new
                RecyclerListener.onItemClickListener() {
                    @Override
                    public void onClick(View view, int Position) {
                        Intent lIntent = null;
                        if (mChosenOption.equals(TYPES_OF_SERVICE[0])) {
                            lIntent = new Intent(MediaList.this, MediaPlayerActivity.class);
                        } else if (mChosenOption.equals(TYPES_OF_SERVICE[1])) {
                            //check service running
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                JobScheduler lJobScheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
                                ComponentName lComponentName = new ComponentName(MediaList.this, JobSchedulerservice.class);
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    PersistableBundle lPersistableBundle = new PersistableBundle();
                                    lPersistableBundle.putString(AUDIO_PATH, mMediaJdoArrayList.get(Position).getmPath());
                                    JobInfo lJobInfoObj = new JobInfo.Builder(1, lComponentName).setExtras(lPersistableBundle)
                                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_CELLULAR)
                                            .setPeriodic(3000)
                                            .setRequiresBatteryNotLow(true).build();
                                    lJobScheduler.schedule(lJobInfoObj);
                                }
                            }
                            lIntent = new Intent(MediaList.this, MediaPlayerServiceActivity.class);
                        } else if (mChosenOption.equals(TYPES_OF_SERVICE[2])) {
                            lIntent = new Intent(MediaList.this, MediaPlayerBoundActivity.class);
                        } else if (mChosenOption.equals(TYPES_OF_SERVICE[3])) {
                            lIntent = new Intent(MediaList.this, MediaPlayerIntentServiceActivity.class);
                        } else if (mChosenOption.equals(TYPES_OF_SERVICE[4])) {
                            lIntent = new Intent(MediaList.this, MediaPlayerForegroundActivity.class);
                        }
                        lIntent.putExtra(RECYCLER_POSITION, Position);
                        startActivity(lIntent);

                    }
                }));
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissionToAccessAudio() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (mIsFetched) {
                new FetchFromDatabase().execute();
            } else {
                new FetchAudioFromContentProvider().execute();
            }
        } else {
            ActivityCompat.requestPermissions(MediaList.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_FOR_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mIsFetched) {
                new FetchFromDatabase().execute();
            } else {

                new FetchAudioFromContentProvider().execute();
            }
        } else {

        }

    }


    public void setmRecyclerviewAdapter() {
        if (mRecyclerviewAdapter == null) {
            mRecyclerviewAdapter = new RecyclerviewAdapter(MediaList.this, mMediaJdoArrayList);
            mRecyclerview.setLayoutManager(mLinearLayoutManager);
            mRecyclerview.setAdapter(mRecyclerviewAdapter);
        } else {
            mRecyclerviewAdapter.notifyDataSetChanged();
        }
    }

    class FetchAudioFromContentProvider extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected String doInBackground(String... strings) {
            String lAlbumId;
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Uri lImageuri;
            Cursor lAudioCursor = null, lImageCursor = null;
            try {
                lAudioCursor = MediaList.this.getContentResolver().query(uri,
                        new String[]{MediaStore.MediaColumns.TITLE, MediaStore.MediaColumns.DATA, MediaStore.Audio.Albums.ALBUM_ID, MediaStore.Audio.AudioColumns.DURATION}, null,
                        null, MediaStore.MediaColumns.TITLE + " COLLATE NOCASE");
                if (lAudioCursor != null && lAudioCursor.moveToFirst()) {
                    do {
                        mMediaJdo = new MediaJdo();
                        lAlbumId = lAudioCursor.getString(lAudioCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID));
                        mMediaJdo.setmAudioname(lAudioCursor.getString(lAudioCursor.getColumnIndex(DB_COLUMN_TITLE)));
                        mMediaJdo.setmPath(lAudioCursor.getString(lAudioCursor.getColumnIndex(DB_COLUMN_PATH)));
                        mMediaJdo.setmDuration(lAudioCursor.getLong(lAudioCursor.getColumnIndex(DB_COLUMN_DURATION)));

                        lImageuri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
                        lImageCursor = MediaList.this.getContentResolver().query(lImageuri,
                                new String[]{MediaStore.Audio.AlbumColumns.ALBUM_ART, MediaStore.Audio.Media._ID},
                                MediaStore.Audio.Media._ID + " = ?",
                                new String[]{lAlbumId}, null);
                        if (lImageCursor != null && lImageCursor.moveToFirst()) {
                            String imageUrl = lImageCursor.getString(lImageCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART));
                            if (imageUrl != null) {
                                mMediaJdo.setmImgUrl(imageUrl);
                            }
                        }
                        mMediaJdoArrayList.add(mMediaJdo);
                    } while (lAudioCursor.moveToNext());
                    mDatabaseOperation.insertValues(mMediaJdoArrayList);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lAudioCursor.close();
                lImageCursor.close();
            }
            mIsFetched = true;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(IS_FETCHED, mIsFetched);
            editor.apply();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressBar.setVisibility(View.GONE);
            setmRecyclerviewAdapter();
        }
    }

    class FetchFromDatabase extends AsyncTask<String, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... strings) {
            mMediaJdoArrayList = mDatabaseOperation.fetchFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            mProgressBar.setVisibility(View.GONE);
            setmRecyclerviewAdapter();
        }
    }

    /**
     * This method is used to get the media list
     */
    public static ArrayList<MediaJdo> getmMediaJdoArrayList() {
        return new ArrayList<>(mMediaJdoArrayList);
    }

}
