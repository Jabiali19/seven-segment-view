package com.bendaschel.sevensegmentview.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout

import com.bendaschel.example.R
import com.bendaschel.sevensegmentview.SevenSegmentView


class MainActivity : AppCompatActivity() {

    private var sv: SevenSegmentView? = null

    private var colorPicker: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sv = findViewById(R.id.display) as SevenSegmentView

        sv!!.setOnClickListener {
            val next = (sv!!.currentValue + 1) % 10
            sv!!.currentValue = next
        }

        colorPicker = findViewById(R.id.color_picker) as LinearLayout

    }
}
