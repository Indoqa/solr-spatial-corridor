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

import java.util.List;

import com.indoqa.solr.spatial.corridor.geo.GeoUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;

public class InDirectionUtils {

    private InDirectionUtils() {
        //hide utility class
    }

    public static int percentageOfPointsWithinDistanceTo(LineString route, List<Point> points, double pointsMaxDistanceToRoute) {
        LocationIndexedLine lineRef = new LocationIndexedLine(route);

        int result = 0;

        for (Point point : points) {
            LinearLocation loc = lineRef.project(point.getCoordinate());
            Coordinate nearestPoint = lineRef.extractPoint(loc);

            double distance = GeoUtils.calculateDistanceInKilometers(nearestPoint, point.getCoordinate());

            if (distance <= pointsMaxDistanceToRoute) {
                result++;
            }
        }

        return Double.valueOf((double) result / points.size() * 100).intValue();
    }

    public static boolean checkAngleDifferenceInThirdOrFourthQuadrant(double actualDifference, double maxDifference) {
        return actualDifference >= 180 - maxDifference && actualDifference <= 180 + maxDifference;
    }

    public static boolean checkAngleDifferenceInFirstOrSecondQuadrant(double actualDifference, double maxDifference) {
        if (actualDifference >= 360 - maxDifference && actualDifference <= 360) {
            return true;
        }

        if (actualDifference >= 0 && actualDifference <= maxDifference) {
            return true;
        }

        return false;
    }
}
