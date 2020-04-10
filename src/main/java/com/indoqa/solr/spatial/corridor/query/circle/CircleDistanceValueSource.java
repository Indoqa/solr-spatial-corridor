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
package com.indoqa.solr.spatial.corridor.query.circle;

import static com.indoqa.solr.spatial.corridor.CorridorConstants.WGS84_TO_KILOMETERS_FACTOR;

import java.util.List;

import com.indoqa.solr.spatial.corridor.query.points.AbstractPointsQueryCorridorValueSource;
import org.apache.lucene.queries.function.ValueSource;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class CircleDistanceValueSource extends AbstractPointsQueryCorridorValueSource {

    private final Double radius;

    protected CircleDistanceValueSource(Double radius, List<Point> queryPoints, ValueSource routeValueSource,
        ValueSource routeHashValueSource) {
        super(queryPoints, routeValueSource, routeHashValueSource);
        this.radius = radius;
    }

    @Override
    public String description() {
        return "circleDistance()";
    }

    @Override
    protected double getValue(LineString lineString) {
        if (lineString.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        double minDistance = Integer.MAX_VALUE;

        double distance;
        for (Point point : this.getQueryPoints()) {
            distance = lineString.distance(point.buffer(radius)) * WGS84_TO_KILOMETERS_FACTOR;
            minDistance = Math.min(distance, minDistance);
        }

        return minDistance;
    }
}
