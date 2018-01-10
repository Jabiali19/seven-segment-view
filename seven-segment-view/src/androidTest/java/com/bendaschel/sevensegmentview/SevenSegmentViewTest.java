package com.bendaschel.sevensegmentview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.RawRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SevenSegmentViewTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
    }

    /**
     * Compares the output of the view against bitmaps of the known good output.
     * @throws Exception
     */
    @Test
    public void testDraw() throws Exception {
        Bitmap actualBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(actualBitmap);
        SevenSegmentView sv = new SevenSegmentView(context);
        sv.draw(canvas);

        Bitmap expectedBitmap = decodeRawBitmap(R.raw.test_off, actualBitmap.getDensity());

        assertTrue(expectedBitmap.sameAs(actualBitmap));

        int[] testCases = {
                R.raw.test_0,
                R.raw.test_1,
                R.raw.test_2,
                R.raw.test_3,
                R.raw.test_4,
                R.raw.test_5,
                R.raw.test_6,
                R.raw.test_7,
                R.raw.test_8,
                R.raw.test_9
        };

        for (int i = 0; i < testCases.length; i++) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
            @RawRes int resId = testCases[i];
            sv.setCurrentValue(i);
            sv.draw(canvas);
            expectedBitmap = decodeRawBitmap(resId, actualBitmap.getDensity());
            assertTrue(expectedBitmap.sameAs(actualBitmap));
        }
    }

    private Bitmap decodeRawBitmap(@RawRes int resId, int density) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        options.inDensity = density;
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }
}