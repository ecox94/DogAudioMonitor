package com.example.androidaudiorecorder;


import android.content.Context;

import android.media.AudioFormat;

import android.media.AudioRecord;

import android.media.MediaRecorder;

import android.os.AsyncTask;


  import android.os.SystemClock;

import android.support.v7.app.AppCompatActivity;

 import android.util.Log;

import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import java.io.FileOutputStream;

import java.io.IOException;

import java.io.OutputStream;

import java.io.RandomAccessFile;

import java.nio.ByteBuffer;

import java.nio.ByteOrder;


import java.util.Locale;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Application DogAudioMonitor
 * Component: Wav File Recorder
 *
 * This class is used by the Android Monitor Activity to continously record a wav File every 30 seconds through a Timer class.
 * Uses an Instance of the RecordWavTask to execute its recording.
 * Code is reused from WavRecordActivity
  *
 * @author Kevin Mark  Copyright 2016
 *https://gist.github.com/kmark/d8b1b01fb0d2febf5770
 */
public class WavRecord extends AppCompatActivity {

    //Contextis passed from AndoridMonitorActivity through object constructor
    public Context context;


    //fileQueue is passed from AndoridMonitorActivity through object constructor
    private BlockingQueue<String> fileQueue = new LinkedBlockingQueue<>();

    public File wavFile;


    public RecordWavTask recordTask = null;


    private String filePath;

    public static File file;


    public WavRecord(BlockingQueue fileQueue) {

        //fileQueue is passed from AndroidMonitorActivity to be modified by class instance
        this.fileQueue = fileQueue;

        //New Record Task upon initialisation of class instance

        recordTask = (RecordWavTask) getLastCustomNonConfigurationInstance();

        if (recordTask == null) {

            recordTask = new RecordWavTask(context);

        } else {

            recordTask.setContext(context);

        }


    }


    /**
     * Gets Current Recording Status
     * Creates wav File for monitor and saves to Application File Directory.
     * Triggers RecordTask class instance to execute recording with enrollment wav File
     * If already recording will finish and start a new task
     */
    public void launchTask(Context context) {


        try {


            switch (recordTask.getStatus()) {

                case RUNNING:


                    recordTask.cancel(false);


                case FINISHED:

                    recordTask = new RecordWavTask(context);

                    break;

                case PENDING:

                    if (recordTask.isCancelled()) {

                        recordTask = new RecordWavTask(context);

                    }


            }

            wavFile = new File("/data/data/com.example.androidaudiorecorder/files" + System.currentTimeMillis() / 1000 + "monitor.wav");
            filePath = wavFile.getAbsolutePath();

            fileQueue.add(filePath);


            recordTask.execute(wavFile);
        } catch (Exception e) {
            System.err.println(e);
        }


    }


    @Override

    public Object onRetainCustomNonConfigurationInstance() {

        recordTask.setContext(context);

        return recordTask;

    }


}
