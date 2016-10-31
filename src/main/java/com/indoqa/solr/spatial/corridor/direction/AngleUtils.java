package com.indoqa.solr.spatial.corridor.direction;

import static java.lang.Math.PI;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;

public class AngleUtils {

    public static double angle(Coordinate c1, Coordinate c2) {
        double angleInRadians = Angle.angle(c1, c2);
        return angleInRadians * (180 / PI);
    }

}
