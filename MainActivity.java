package com.example.androidaudiorecorder;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    public Button start;


    Context ctx = MainActivity.this;
    @Override


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        start =  ( Button) findViewById(R.id.button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


             Intent intent = new Intent(MainActivity.this, WavRecorderActivity.class);

                startActivity(intent);

            }
        });

    }

}