package com.example.androidaudiorecorder;




//Imports used for the Activity functionality.

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;


import android.os.AsyncTask;


import android.os.Bundle;

import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;

import android.support.v4.content.ContextCompat;

import android.support.v7.app.AppCompatActivity;

import android.view.View;

import android.widget.Button;
import android.widget.Toast;

import java.io.File;


/**
 * Application DogAudioMonitor
 * Component: Wav File Recorder
 *
 * This class is an Activity that allows the user to Record An Enrollment Wav File of their Dogs Vocalization.
 * This class initiates Enrollment wavFile And uses an Instance of the RecordWavTask to execute its recording.
 * The user can start Recording and stop anytime before moving on the the WavEventFilter Activity.
 *
 * @author Kevin Mark  Copyright 2016
 *https://gist.github.com/kmark/d8b1b01fb0d2febf5770
 */
public class WavRecorderActivity extends AppCompatActivity {


    // Instance Variables used in class

        private File enrollmentWavFile;
        private static final int PERMISSION_RECORD_AUDIO = 0;
        private RecordWavTask recordTask = null;


        //Button used to access next activity
        private Button next;


    /**
     * Default Constructor
     */
        public WavRecorderActivity(){

}



    @Override


    /**
     * When Activity is Activated during application run
     *
     */
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Sets Activities layout through assigned xml file
        setContentView(R.layout.activity_wavrecorder);




        next= (Button) findViewById(R.id.nextPage);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity();
            }
        });



        //noinspection ConstantConditions

        /**
         * Start button when pressed triggers Wav file to record if permisions are given
         */
        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(WavRecorderActivity.this, Manifest.permission.RECORD_AUDIO)

                        != PackageManager.PERMISSION_GRANTED) {

                    // Request permission

                    ActivityCompat.requestPermissions(WavRecorderActivity.this,

                            new String[] { Manifest.permission.RECORD_AUDIO },

                            PERMISSION_RECORD_AUDIO);

                    return;

                }

                // Permission already available

                launchTask();

            }

        });




         /**
         * Stop button when pressed ends wav File recording.
          *
         */

        findViewById(R.id.btnStop).setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                if (!recordTask.isCancelled() && recordTask.getStatus() == AsyncTask.Status.RUNNING) {

                    recordTask.cancel(false);
                    Toast.makeText(WavRecorderActivity.this, "Recording Finished", Toast.LENGTH_SHORT).show();


                } else {

                    Toast.makeText(WavRecorderActivity.this, "Task not running.", Toast.LENGTH_SHORT).show();

                }

            }

        });


        // Restore the previous task or create a new one if necessary

        recordTask = (RecordWavTask) getLastCustomNonConfigurationInstance();

        if (recordTask == null) {

            recordTask = new RecordWavTask(this);

        } else {

            recordTask.setContext(this);

        }




    }



    @Override

    /**
     * When App is first installed and initiated, upon accessing the Activty the user will
     *be prompted for permission to Record Audio.
     * @param
     *
     */

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case PERMISSION_RECORD_AUDIO:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission granted

                    launchTask();

                } else {

                    // Permission denied

                    Toast.makeText(this, "\uD83D\uDE41", Toast.LENGTH_SHORT).show();

                }

                break;

        }

    }


    /**
     * Gets Current Recording Status
     * Creates enrollment wav File and saves to Application File Directory.
     * Triggers RecordTask class instance to execute recording with enrollment wav File
     * If already recording will inform user,
     */

    private void launchTask() {

        switch (recordTask.getStatus()) {

            case RUNNING:

                Toast.makeText(this, "Task already running...", Toast.LENGTH_SHORT).show();

                return;



            case FINISHED:

                recordTask = new RecordWavTask(this);

                Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show();


                break;

            case PENDING:

                if (recordTask.isCancelled()) {

                    recordTask = new RecordWavTask(this);

                }

        }


        //Enrollment Wav File creation
        enrollmentWavFile = new File(getFilesDir(), "recording_DOG.wav");




        recordTask.execute(enrollmentWavFile);

    }




    /**
     * Starts WavEventFilter Activity
     */
    public void startActivity() {
File file = new File(getFilesDir(), "recording_DOG.wav");
if (file.exists()){
    Intent intent  = new Intent(this,  WavFileEventFilterActivity.class);
    startActivity(intent);
}else{
    Toast.makeText(this, "Please create a recording of your dog barking to begin ", Toast.LENGTH_SHORT).show();
}



    }

}