package com.bendaschel.sevensegmentview.example

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import com.bendaschel.example.R
import com.bendaschel.sevensegmentview.SevenSegmentView


class MainActivity : AppCompatActivity() {

    private lateinit var sv: SevenSegmentView

    private lateinit var colorPicker: LinearLayout

    private val colors = listOf<Int>(Color.GREEN, Color.BLUE, Color.RED, Color.MAGENTA, Color.YELLOW)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sv = findViewById(R.id.display) as SevenSegmentView

        sv.setOnClickListener {
            val next = (sv.currentValue + 1) % 10
            sv.currentValue = next
        }

        colorPicker = findViewById(R.id.color_picker) as LinearLayout
        colors.forEach { color ->
            val button = layoutInflater.inflate(R.layout.color_picker_item, colorPicker, false)
            button.setOnClickListener {
                sv.onColor = color
                sv.offColor = Color.argb(50, Color.red(color), Color.green(color), Color.blue(color))
            }
            button.setBackgroundColor(color)
            colorPicker.addView(button)
        }
    }
}
