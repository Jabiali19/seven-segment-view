package com.bendaschel.sevensegmentview;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class PathUtilsTest {

    @Test
    public void canDrawARectangle() throws Exception {

        Path expected = new Path();
        expected.addRect(0, 0, 10, 10, Path.Direction.CW);

        Path actual = PathUtils.makePathFromPoints(Arrays.asList(
                new Point(0, 0), // top left
                new Point(10, 0), // top right
                new Point(10, 10), // bottom right
                new Point(0, 10) // bottom left
        ));

        // Paths cannot be compared directly, so we compared the bounding boxes
        // Since we're drawing rectangles anyways, the result is the same
        RectF expectedBounds = new RectF();
        RectF actualBounds = new RectF();
        expected.computeBounds(expectedBounds, true);
        actual.computeBounds(actualBounds, true);

        assertThat(expectedBounds, is(actualBounds));
    }
}