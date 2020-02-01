package com.puchoInc.diya.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.puchoInc.diya.R;
import com.puchoInc.diya.SpeakActivity;
import com.puchoInc.diya.SpeakActivityPager;
import com.puchoInc.diya.SpeakViewPagerItemFragment;
import com.puchoInc.diya.TextSpeakData.SpeakData;

import java.io.File;
import java.util.ArrayList;

import services.RecordingService;


public class SpeakFragment extends Fragment {

    private ViewPager mMyViewPager;
    Intent intent;
    Button record_list;
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private TabLayout mTabLayout;
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    public static final int RECORD_AUDIO = 0;
    public static final int SAVE_AUDIO = 1;
    private String file_exts[] = {AUDIO_RECORDER_FILE_EXT_MP4,
            AUDIO_RECORDER_FILE_EXT_3GP};


    //new implementation
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "position";

    private int position;

    //Recording controls
    private Button mRecordButton = null;
    private boolean mStartRecording = true;
    public SpeakFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static SpeakFragment newInstance(int position) {
        SpeakFragment fragment = new SpeakFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_speak, container, false);
        mTabLayout =  v.findViewById(R.id.tab_speak);
        mMyViewPager = v.findViewById(R.id.viewpager);
        mRecordButton = (Button) v.findViewById(R.id.btn_play);
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/recorded_audio.3gp";
//
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO},
                            RECORD_AUDIO);

                } else {
                    onRecord(mStartRecording);
                    mStartRecording = !mStartRecording;
                }
            }
        });

        init();
        return v;
    }

    private void init() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        SpeakData[] speakData = Resources.speakData.getSpeakDatas();
        for (SpeakData speakData1 : speakData) {
            SpeakViewPagerItemFragment fragment = SpeakViewPagerItemFragment.getInstance(speakData1);
            fragments.add(fragment);
        }
        SpeakActivityPager pagerAdapter = new SpeakActivityPager(getFragmentManager(), fragments);
        mMyViewPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mMyViewPager, true);
    }

    private void onRecord(boolean start) {

        intent = new Intent(getActivity(), RecordingService.class);

        if (start) {
            // start recording
            mRecordButton.setBackgroundResource(R.drawable.stop_audio_button);
            //mPauseButton.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), R.string.toast_recording_start, Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                //folder /SoundRecorder doesn't exist, create the folder
                folder.mkdir();
            }

            //start RecordingService
            getActivity().startService(intent);
            //keep screen on while recording
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        } else {
            //stop recording
            mRecordButton.setBackgroundResource(R.drawable.circular_play_btn);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        SAVE_AUDIO);

            } else {
                getActivity().stopService(intent);
                //allow the screen to turn off again once recording is finished
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;
            } else {
                //User denied Permission.
                Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode==SAVE_AUDIO)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
                getActivity().stopService(intent);
                //allow the screen to turn off again once recording is finished
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                //User denied Permission.
                Toast.makeText(getActivity(), "Storage Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

}
