package com.puchoInc.diya;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.puchoInc.diya.TextSpeakData.SpeakData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ListenActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener{

    private static final String TAG = "listen_activity";
    private MediaPlayer mMediaplayer;
    Button ListenRecord;
    FirebaseStorage storage;
    StorageReference storageRef;
    StorageReference pathReference ;
    StorageReference gsReference;
    StorageReference httpsReference;
    Button yes;
    Button no;
    private TabLayout mTabLayout;
    private ViewPager mMyViewPager;
    Uri file;
    private String mFileName = null;
    private String mFilePath = null;
    public static final String STORAGE_PATH = "gs://diya-fc8cd.appspot.com";

    private String audioOne, audioTwo, audioThree, audioFour, audioFive, audioSix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen);
        init();

    }

    private void init() {
    ListenRecord=(Button) findViewById(R.id.btn_validate);
    yes=(Button) findViewById(R.id.btn_yes);
    no=(Button) findViewById(R.id.btn_no);
        mTabLayout =  findViewById(R.id.tab_listen);
        mMyViewPager =findViewById(R.id.pager_listen);
        initPager();
    yes.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    });

        Date d =  new Date();
        SharedPreferences splogin=getSharedPreferences("isLogin",0);
        mFileName ="recorder_temp"+splogin.getInt("NumberOfRecordings",0)+ "_.mp4";
        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + mFileName;
//        Date d =  new Date();
//        mFileName =mFileName;
//        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        mFilePath += "/SoundRecorder/" + mFileName;
        file = Uri.fromFile(new File(mFilePath));
        //mFileName ="recorder_"+d.getTime()+ "_.mp4";

       // SharedPreferences splogin=getSharedPreferences("isLogin",0);
        storageRef = FirebaseStorage.getInstance().getReference();
        pathReference = storageRef.child(file.getPath());
//        gsReference = storage.getReferenceFromUrl("gs://bucket/images/stars.jpg");
//        httpsReference = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/b/bucket/o/images%20stars.jpg");

        fetchData();

    }
    private void initPager() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        SpeakData[] speakData = Resources.speakData.getSpeakDatas();
        for (SpeakData speakData1 : speakData) {
            SpeakViewPagerItemFragment fragment = SpeakViewPagerItemFragment.getInstance(speakData1);
            fragments.add(fragment);
        }
        SpeakActivityPager pagerAdapter = new SpeakActivityPager(getSupportFragmentManager(), fragments);
        mMyViewPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mMyViewPager, true);
    }

    // Retrieve audio file url's from Fire_base Storage & pass it to StoreData Method !!!
    private void fetchData(){
        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String url = uri.toString();
                Log.d(TAG, "onSuccess: "+uri.toString());
                storeData(url, null,null,null,null,null);
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.d(TAG, "onCanceled: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: "+e.toString());
            }
        });
    }


    private void storeData(String audioFileOne, String audioFileTwo, String audioFileThree,
                           String audioFileFour, String audioFileFive, String audioFileSix){

        // Download audio files in sequence and pass here !!!
        // Connect drawable image with respective audio file !!!


        Log.d(TAG, "AudioFileOne "+audioFileOne);
        Log.d(TAG, "AudioFileTwo "+audioFileTwo);
        Log.d(TAG, "AudioFileThree "+audioFileThree);
        Log.d(TAG, "AudioFileFour "+audioFileFour);
        Log.d(TAG, "AudioFileFive "+audioFileFive);
        Log.d(TAG, "AudioFileSix "+audioFileSix);

        audioOne = String.valueOf(this.getResources().getDrawable(R.drawable.audio1));
        audioTwo = String.valueOf(this.getResources().getDrawable(R.drawable.audio2));
        audioThree = String.valueOf(this.getResources().getDrawable(R.drawable.audio3));
        audioFour = String.valueOf(this.getResources().getDrawable(R.drawable.audio4));
        audioFive = String.valueOf(this.getResources().getDrawable(R.drawable.audio5));
        audioSix = String.valueOf(this.getResources().getDrawable(R.drawable.audio6));


        HashMap<String, String> audioataHashMap = new HashMap<>();
        audioataHashMap.put(audioOne, audioFileOne);
        audioataHashMap.put(audioTwo, audioFileTwo);
        audioataHashMap.put(audioThree, audioFileThree);
        audioataHashMap.put(audioFour, audioFileFour);
        audioataHashMap.put(audioFive, audioFileFive);
        audioataHashMap.put(audioSix, audioFileSix);

        retriveData(audioataHashMap);
    }


    // For playing audio according to the image file !!!
    private void retriveData(HashMap<String, String> stringHashMap){

        String one = stringHashMap.get(audioOne);
        String two = stringHashMap.get(audioOne);
        String three = stringHashMap.get(audioOne);
        String four = stringHashMap.get(audioOne);
        String five = stringHashMap.get(audioOne);
        String six = stringHashMap.get(audioOne);

        // Now play the audio according to the Image (Key in HashMap) !!!

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaplayer.start();
    }
}
