package services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.puchoInc.diya.DataBase.DBHelper;
import com.puchoInc.diya.R;
import com.puchoInc.diya.SpeakActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import helper.MySharedPreferences;

public class RecordingService extends Service {

    private static final String LOG_TAG = "RecordingService";
    private static int count = 0;
    private String mFileName = null;
    private String mFilePath = null;
    SharedPreferences splogin;
    private MediaRecorder mRecorder = null;
    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private int mElapsedSeconds = 0;
    Uri file;
    private OnTimerChangedListener onTimerChangedListener = null;
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    SharedPreferences spLogin=getSharedPreferences("isLogin",0);

    private Timer mTimer = null;
    private TimerTask mIncrementTimerTask = null;
    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // get the Firebase  storage reference
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
       // mRecorder.release();
        if (mRecorder != null) {
            stopRecording();
        }

        super.onDestroy();
    }

    public void startRecording() {
        setFileNameAndPath();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        if (MySharedPreferences.getPrefHighQuality(this)) {
            mRecorder.setAudioSamplingRate(44100);
            mRecorder.setAudioEncodingBitRate(192000);
        }

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();

            //startTimer();
            //startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void setFileNameAndPath(){

        File f;

        do{
            count++;
            Date d =  new Date();
            //mFileName ="recorder_"+d.getTime()+ "_.mp4";
//            mFileName ="recorder_temp"+d.getTime()+ "_.mp4";
            int count=splogin.getInt("NumberOfRecordings",0);
            SharedPreferences.Editor editor=spLogin.edit();
            count++;
            editor.putInt("NumberOfRecordings",count);
            editor.commit();
            mFileName ="recorder_temp"+count +"_.mp4";
//            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/SoundRecorder/" + mFileName;

            f = new File(mFilePath);
        }while (f.exists() && !f.isDirectory());
    }

    public void stopRecording() {
//        mRecorder.release();
//        mRecorder.stop();
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
      mRecorder.release();
        Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_LONG).show();

        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }
        uploadImage(mFileName);
        DBHelper dbHelper=new DBHelper(getApplicationContext());
        try {
            dbHelper.addRecording(mFileName, mFilePath, mElapsedMillis);

        } catch (Exception e){
            Log.e(LOG_TAG, "exception", e);
        }
        mRecorder = null;
    }

    private void startTimer() {
        mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                if (onTimerChangedListener != null)
                    onTimerChangedListener.onTimerChanged(mElapsedSeconds);
                NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mgr.notify(1, createNotification());
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    //TODO:
    private Notification createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_mic_white_36dp)
                        .setContentTitle(getString(R.string.notification_recording))
                        .setContentText(mTimerFormat.format(mElapsedSeconds * 1000))
                        .setOngoing(true);

        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
                new Intent[]{new Intent(getApplicationContext(), SpeakActivity.class)}, 0));

        return mBuilder.build();
    }
    // UploadImage method
    private void uploadImage(String filename)
    {
         splogin=getSharedPreferences("isLogin",0);
//        mFileName =filename;
//        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        mFilePath += "/SoundRecorder/" + mFileName;

//        if (mFilePath != null) {

//            file = Uri.fromFile(new File(filename));
            StorageReference riversRef = storageReference.child(splogin.getString("PhoneNumber","1234")+"/"+filename);
            StorageTask<UploadTask.TaskSnapshot> uploadTask = riversRef.putFile(Uri.parse(filename));

// Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast
                            .makeText(RecordingService.this,
                                    "File upload fail!!",
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Toast
                            .makeText(RecordingService.this,
                                    "File uploaded!!",
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            });
//        }
    }
//    private void getUrlAsync (){
//        // Points to the root reference
//        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//        final StorageReference dateRef = storageRef.child(splogin.getString("PhoneNumber","1234")+"/"+file.getLastPathSegment());
//        dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
//        {
//            @Override
//            public void onSuccess(Uri downloadUrl)
//            {
//                DBHelper dbHelper=new DBHelper(getApplicationContext());
//                try {
//                    dbHelper.addRecording(mFileName, mFilePath, mElapsedMillis,downloadUrl.toString());
//
//                } catch (Exception e){
//                    Log.e(LOG_TAG, "exception", e);
//                }
//            }
//        });
//    }
}
