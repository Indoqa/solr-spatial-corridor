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
package com.indoqa.solr.spatial.corridor.query.points;

import java.util.List;

import com.indoqa.solr.spatial.corridor.geo.GeoUtils;
import org.apache.lucene.queries.function.ValueSource;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;

public class PointsDistanceValueSource extends AbstractPointsQueryCorridorValueSource {

    protected PointsDistanceValueSource(List<Point> queryPoints, ValueSource routeValueSource, ValueSource routeHashValueSource) {
        super(queryPoints, routeValueSource, routeHashValueSource);
    }

    @Override
    public String description() {
        return "pointsDistance()";
    }

    @Override
    protected double getValue(LineString lineString) {
        if (lineString.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        LocationIndexedLine lineRef = new LocationIndexedLine(lineString);

        double minDistance = Integer.MAX_VALUE;

        for (Point point : this.getQueryPoints()) {
            LinearLocation loc = lineRef.project(point.getCoordinate());
            Coordinate extractPoint = lineRef.extractPoint(loc);

            double distance = GeoUtils.calculateDistanceInKilometers(extractPoint, point.getCoordinate());

            minDistance = Math.min(distance, minDistance);
        }

        return minDistance;
    }
}
