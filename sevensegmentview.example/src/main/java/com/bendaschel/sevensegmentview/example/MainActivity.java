package com.bendaschel.sevensegmentview.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bendaschel.example.R;
import com.bendaschel.sevensegmentview.SevenSegmentView;


public class MainActivity extends AppCompatActivity {

    private SevenSegmentView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sv = (SevenSegmentView) findViewById(R.id.display);

        sv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = (sv.getCurrentValue() + 1) % 10;
                sv.setCurrentValue(next);
            }
        });
    }
}
