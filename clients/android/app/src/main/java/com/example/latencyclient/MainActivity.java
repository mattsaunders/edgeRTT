package com.example.latencyclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    String[] endpoints = {
            "https://j42js4m2c1.execute-api.us-east-1.amazonaws.com/dev/ping",
            "https://www.washingtonpost.com/",
            "https://www.nytimes.com/"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** Called when the user touches the button */
    public void runTest(View view) {
        // Do something in response to button click
        try {
            new ExperimentRunner().run(endpoints);

        } catch (Exception e) {
            // This will catch any exception, because they are all descended from Exception
            System.out.println("Error " + e.getMessage());
        }
    }

}
