/*
 * Licensed to the Indoqa Software Design und Beratung GmbH (Indoqa) under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Indoqa licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indoqa.solr.spatial.corridor.direction;

import static com.indoqa.solr.spatial.corridor.CorridorConstants.WGS84_TO_KILOMETERS_FACTOR;
import static java.lang.Math.PI;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class AngleUtils {

    public static double angle(Coordinate c1, Coordinate c2) {
        double angleInRadians = Angle.angle(c1, c2);
        return angleInRadians * (180 / PI);
    }

    protected static double getAngleDifference(LineString lineString, List<Point> queryPoints) {
        if (lineString == null || lineString.isEmpty()) {
            return Double.MAX_VALUE;
        }

        if (queryPoints.size() < 2) {
            return 0;
        }

        LocationIndexedLine indexedLineString = new LocationIndexedLine(lineString);

        Coordinate queryCoordinate1 = queryPoints.get(0).getCoordinate();
        Coordinate queryCoordinate2 = queryPoints.get(1).getCoordinate();

        double angleDifference1 = getAngleDifference(lineString, queryCoordinate1, queryCoordinate2, indexedLineString);

        if (queryPoints.size() < 3) {
            return angleDifference1;
        }

        Coordinate queryCoordinate3 = queryPoints.get(queryPoints.size() - 2).getCoordinate();
        Coordinate queryCoordinate4 = queryPoints.get(queryPoints.size() - 1).getCoordinate();

        double angleDifference2 = getAngleDifference(lineString, queryCoordinate3, queryCoordinate4, indexedLineString);

        int middlePoint = queryPoints.size() / 2;

        Coordinate queryCoordinate5 = queryPoints.get(middlePoint).getCoordinate();
        Coordinate queryCoordinate6 = queryPoints.get(middlePoint + 1).getCoordinate();

        double angleDifference3 = getAngleDifference(lineString, queryCoordinate5, queryCoordinate6, indexedLineString);

        return Math.min(Math.min(angleDifference1, angleDifference2), angleDifference3);
    }

    private static double getAngleDifference(LineString lineString, Coordinate queryCoordinate1, Coordinate queryCoordinate2,
            LocationIndexedLine indexedLineString) {
        Coordinate routeCoordinate1;
        Coordinate routeCoordinate2;

        LinearLocation intersection = indexedLineString.project(queryCoordinate1);

        double distance = intersection.getCoordinate(lineString).distance(queryCoordinate1) * WGS84_TO_KILOMETERS_FACTOR;

        if (distance > 0.01) {
            return Double.MAX_VALUE;
        }

        int intersectionIndex = intersection.getSegmentIndex();

        if (!intersection.isEndpoint(lineString)) {
            routeCoordinate1 = intersection.getCoordinate(lineString);
            routeCoordinate2 = lineString.getCoordinateN(intersectionIndex + 1);
        } else {
            routeCoordinate1 = lineString.getCoordinateN(intersectionIndex - 1);
            routeCoordinate2 = intersection.getCoordinate(lineString);
        }

        double routeAngleAtIntersection = AngleUtils.angle(routeCoordinate1, routeCoordinate2);
        double queryAngle = AngleUtils.angle(queryCoordinate1, queryCoordinate2);

        return Math.abs(routeAngleAtIntersection - queryAngle);
    }

}
