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

import static com.indoqa.solr.spatial.corridor.CorridorConstants.WGS84_TO_KILOMETERS_FACTOR;

import java.util.List;

import org.apache.lucene.queries.function.ValueSource;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

public class PointsPositionValueSource extends AbstractPointsQueryCorridorValueSource {

    public PointsPositionValueSource(List<Point> queryPoints, ValueSource routeValueSource) {
        super(queryPoints, routeValueSource);
    }

    @Override
    public String description() {
        return "pointsPosition()";
    }

    @Override
    protected double getValue(LineString lineString) {
        try{
            LocationIndexedLine indexedLineString = new LocationIndexedLine(lineString);

            LinearLocation intersection = indexedLineString.project(this.getPoint().getCoordinate());
            LineString routeToIntersection = (LineString) indexedLineString.extractLine(indexedLineString.getStartIndex(), intersection);

            return routeToIntersection.getLength() * WGS84_TO_KILOMETERS_FACTOR;
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("IllegalArgument for linestring: " + lineString.toText(), e);
        }
    }

    private Point getPoint() {
        return this.getQueryPoints().get(0);
    }
}
