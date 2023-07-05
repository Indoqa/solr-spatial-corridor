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

import static java.lang.Math.PI;

import com.indoqa.solr.spatial.corridor.debug.DebugValues;
import com.indoqa.solr.spatial.corridor.geo.GeoUtils;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;

import java.util.List;

public class AngleUtils {

    public static double angle(Coordinate c1, Coordinate c2) {
        double angleInRadians = Angle.angle(c1, c2);
        return angleInRadians * (180 / PI);
    }

    protected static double getAngleDifference(DebugValues debugValues, LineString lineString, List<Point> queryPoints, double maxDistance) {
        if (lineString == null || lineString.isEmpty()) {
            debugValues.addAngleDifference("lineString", "empty");
            return Double.MAX_VALUE;
        }

        if (queryPoints.size() < 2) {
            debugValues.addAngleDifference("querypoints", "less than 2");
            return 0;
        }

        LocationIndexedLine indexedLineString = new LocationIndexedLine(lineString);

        Coordinate queryCoordinate1 = queryPoints.get(0).getCoordinate();
        Coordinate queryCoordinate2 = queryPoints.get(1).getCoordinate();

        double angleDifference1 = getAngleDifference(debugValues, lineString, queryCoordinate1, queryCoordinate2, indexedLineString, maxDistance);

        debugValues.addAngleDifference("maxDistance", maxDistance);
        debugValues.addAngleDifference("queryCoordinate1", queryCoordinate1);
        debugValues.addAngleDifference("queryCoordinate2", queryCoordinate2);
        debugValues.addAngleDifference("angleDifference1", angleDifference1);
        if (queryPoints.size() < 3) {
            debugValues.addAngleDifference("querypoints", "less than 3");
            return angleDifference1;
        }

        Coordinate queryCoordinate3 = queryPoints.get(queryPoints.size() - 2).getCoordinate();
        Coordinate queryCoordinate4 = queryPoints.get(queryPoints.size() - 1).getCoordinate();

        double angleDifference2 = getAngleDifference(debugValues, lineString, queryCoordinate3, queryCoordinate4, indexedLineString, maxDistance);

        debugValues.addAngleDifference("queryCoordinate3", queryCoordinate3);
        debugValues.addAngleDifference("queryCoordinate4", queryCoordinate4);
        debugValues.addAngleDifference("angleDifference2", angleDifference2);

        int middlePoint = queryPoints.size() / 2;
        debugValues.addAngleDifference("middlePoint", middlePoint);

        Coordinate queryCoordinate5 = queryPoints.get(middlePoint).getCoordinate();
        Coordinate queryCoordinate6 = queryPoints.get(middlePoint + 1).getCoordinate();

        double angleDifference3 = getAngleDifference(debugValues, lineString, queryCoordinate5, queryCoordinate6, indexedLineString, maxDistance);

        debugValues.addAngleDifference("queryCoordinate5", queryCoordinate5);
        debugValues.addAngleDifference("queryCoordinate6", queryCoordinate6);
        debugValues.addAngleDifference("angleDifference3", angleDifference3);

        double minAngleDifference = Math.min(Math.min(angleDifference1, angleDifference2), angleDifference3);
        debugValues.addAngleDifference("minAngleDifference", minAngleDifference);
        return minAngleDifference;
    }

    private static double getAngleDifference(DebugValues debugValues, LineString lineString, Coordinate queryCoordinate1, Coordinate queryCoordinate2,
            LocationIndexedLine indexedLineString, double maxDistance) {
        Coordinate routeCoordinate1;
        Coordinate routeCoordinate2;

        LinearLocation intersection = indexedLineString.project(queryCoordinate1);

        double distance = GeoUtils.calculateDistanceInKilometers(intersection.getCoordinate(lineString), queryCoordinate1);
        debugValues.addSingleAngleDifference("distance", distance);

        if (distance > maxDistance) {
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

        debugValues.addSingleAngleDifference("routeAngleAtIntersection", routeAngleAtIntersection);
        debugValues.addSingleAngleDifference("queryAngle", queryAngle);
        return Math.abs(routeAngleAtIntersection - queryAngle);
    }

}
