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
package com.indoqa.solr.spatial.corridor.query.route;

import com.indoqa.solr.spatial.corridor.geo.GeoUtils;
import org.apache.lucene.queries.function.ValueSource;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;

public class RouteDistanceValueSource extends AbstractRouteQueryValueSource {

    protected RouteDistanceValueSource(LineString lineString, ValueSource loctionValueSource) {
        super(lineString, loctionValueSource);
    }

    @Override
    public String description() {
        return "corridorDistance()";
    }

    @Override
    protected double getValue(Point point) {
        LineString lineString = this.getLineString();
        if (point == null || lineString == null) {
            return Double.MAX_VALUE;
        }

        LocationIndexedLine lineRef = new LocationIndexedLine(lineString);
        LinearLocation loc = lineRef.project(point.getCoordinate());

        Coordinate extractPoint = lineRef.extractPoint(loc);
        return GeoUtils.calculateDistanceInKilometers(extractPoint, point.getCoordinate());
    }

}
