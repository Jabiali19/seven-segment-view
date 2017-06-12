package com.bendaschel.sevensegmentview;

import android.graphics.Path;
import android.graphics.Point;

import java.util.List;

class PathUtils {
    static Path makePathFromPoints(List<Point> points){
        Path path = new Path();
        Point firstPoint = points.get(0);
        List<Point> middlePoints = points.subList(1, points.size());
        path.moveTo(firstPoint.x, firstPoint.y);

        for(Point p: middlePoints){
            path.lineTo(p.x, p.y);
        }
        path.close();
        return path;
    }
}
