package com.example.androidaudiorecorder;




import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


/**
 Application: DogAudioMonitor
 * Component: Audio Monitor
 *
 *
 * This class is an Activity That Monitors live audio and compares its sturcture to the Wav Files tagged
 * in the EventTaggingActivity. This Does this by using
 *
 *
 *
 *
 * @author Emma Cox
 *
 */
public class UserSettingsActivity extends AppCompatActivity {

    //Instance Variables
    private Context context= this;
    private String name, email , phoneNo;
    private String value;
    private static ArrayList<File> taggedWavsInput;
    private static ArrayList<File> taggedWavsOutput;

    //Button to submit all details
    public Button submit;

    //Text prompts used to submit details
    EditText nameInput;
    EditText emailInput;
    EditText phoneNoInput;

    //Activity Context
    Context ctx = UserSettingsActivity.this;

    /**
     * Default Constructor that sets all instance variables to 0/null
     */
    public UserSettingsActivity(){

    }

// Activity Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static ArrayList<File> getTaggedWavsInput() {
        return taggedWavsInput;
    }

    public static void setTaggedWavsInput(ArrayList<File> taggedWavsInput) {
        UserSettingsActivity.taggedWavsInput = taggedWavsInput;
    }

    public static ArrayList<File> getTaggedWavsOutput() {
        return taggedWavsOutput;
    }

    public static void setTaggedWavsOutput(ArrayList<File> taggedWavsOutput) {
        UserSettingsActivity.taggedWavsOutput = taggedWavsOutput;
    }

    /**
     * When Activity is Activated during application run
     *    @Override
     */
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_settings);

        nameInput = (EditText)findViewById(R.id.nameInput);
        emailInput = (EditText)findViewById(R.id.emailInput);
        phoneNoInput = (EditText)findViewById(R.id.phoneInput);

        //TaggedWav Arraylist recieved from EventTaggingActivity
        taggedWavsInput = (ArrayList<File>)getIntent().getSerializableExtra("taggedwavs");

        //TaggedWav Arraylist to be passed to  AudioMontitorActivity
        taggedWavsOutput = taggedWavsInput;


        // RadioGroup allows user to choose from five radio buttons to set an accuracy level.
        final RadioGroup accuracyGroup = (RadioGroup) findViewById(R.id.radioAccuracy);
        RadioButton one = (RadioButton) findViewById(R.id.radio1);
        RadioButton two = (RadioButton) findViewById(R.id.radio2);
        RadioButton three = (RadioButton) findViewById(R.id.radio3);
        RadioButton four = (RadioButton) findViewById(R.id.radio4);
        RadioButton five = (RadioButton) findViewById(R.id.radio5);



        //Submit button determines the accuracy level chosen based on switch result
        submit =  ( Button) findViewById(R.id.button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                accuracyGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        if (i == R.id.radio1) {
                            value = "1";
                        } else if (i == R.id.radio2) {
                            value = "2";
                        } else if (i == R.id.radio3) {
                            value = "3";
                        } else if (i == R.id.radio4) {
                            value = "4";
                        } else if (i == R.id.radio5) {
                            value = "5";
                        }
                    }
                });

                //Each EditText sets the value of each of the user detail variables based on keyboard input
                name = nameInput.getText().toString();
                email = emailInput.getText().toString();
                phoneNo = phoneNoInput.getText().toString();


                //If any of the user details are empty, the user will not be able to proceed.
                if (name.isEmpty() || email.isEmpty()  || phoneNo.isEmpty()   ) {
                    Toast.makeText(ctx, "Please Enter all fields", Toast.LENGTH_SHORT).show();


                }else{
                    //The user setting details are passed to the AudioMontior Activity.
                    Intent intent = new Intent(UserSettingsActivity.this, AudioMonitorActivity.class);
                    intent.putExtra("Name", name);
                    intent.putExtra("email", email);
                    intent.putExtra("phone", phoneNo);
                    intent.putExtra("wavs", taggedWavsOutput);
                    intent.putExtra("accuracy", value);

                    startActivity(intent);
                }
            }
        });

    }

}