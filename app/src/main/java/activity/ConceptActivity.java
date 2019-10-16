package activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mediaplayer.R;

import java.util.List;

import service.BoundServiceToPlayAudio;
import service.IntentServiceToPlayAudio;
import service.JobSchedulerservice;

import static constants.Constants.SERVICE_TYPE;
import static constants.Constants.TYPES_OF_SERVICE;

public class ConceptActivity extends AppCompatActivity {


    private AlertDialog mAlertDialog;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concept);
        mSharedPreferences = getSharedPreferences(SERVICE_TYPE, MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        Intent intent = new Intent(ConceptActivity.this, MediaList.class);
        if (isServiceRunning(JobSchedulerservice.class)) {
            intent.putExtra(SERVICE_TYPE, mSharedPreferences.getString(SERVICE_TYPE, null));
            startActivity(intent);
        } else if (isServiceRunning(BoundServiceToPlayAudio.class)) {
            intent.putExtra(SERVICE_TYPE, mSharedPreferences.getString(SERVICE_TYPE, null));
            startActivity(intent);
        } else if (isServiceRunning(IntentServiceToPlayAudio.class)) {
            intent.putExtra(SERVICE_TYPE, mSharedPreferences.getString(SERVICE_TYPE, null));
            startActivity(intent);
        } else {
            showConceptTypes();
        }
    }

    public boolean isServiceRunning(Class serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : services) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void showConceptTypes() {
        AlertDialog.Builder lBuilder = new AlertDialog.Builder(ConceptActivity.this);
        lBuilder.setSingleChoiceItems(TYPES_OF_SERVICE, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pPosition) {
                Intent intent = new Intent(ConceptActivity.this, MediaList.class);
                intent.putExtra(SERVICE_TYPE, TYPES_OF_SERVICE[pPosition]);
                mEditor.putString(SERVICE_TYPE, TYPES_OF_SERVICE[pPosition]);
                mEditor.commit();
                startActivity(intent);
                mAlertDialog.hide();
            }
        });
        mAlertDialog = lBuilder.create();
        mAlertDialog.show();
        mAlertDialog.setCanceledOnTouchOutside(false);
    }

}
