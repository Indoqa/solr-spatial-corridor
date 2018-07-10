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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import static com.indoqa.solr.spatial.corridor.CorridorConstants.WGS84_TO_KILOMETERS_FACTOR;

public class InDirectionValueSource extends ValueSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(InDirectionValueSource.class);
    private static final GeometryFactory geometryFactory = new GeometryFactory();


    private LineString lineString;
    private ValueSource loctionValueSource;

    private double maxDifference;
    private double maxDifferenceAdditionalPointsCheck;
    private double pointsMaxDistanceToRoute;
    private int percentageOfPointsWithinDistance;
    private boolean alwaysCheckPointDistancePercent;

    public InDirectionValueSource(LineString lineString, ValueSource loctionValueSource, double maxDifference,
            double maxDifferenceAdditionalPointsCheck, double pointsMaxDistanceToRoute, int percentageOfPointsWithinDistance,
            boolean alwaysCheckPointDistancePercent) {

        this.lineString = lineString;
        this.loctionValueSource = loctionValueSource;
        this.maxDifference = maxDifference;
        this.maxDifferenceAdditionalPointsCheck = maxDifferenceAdditionalPointsCheck;
        this.pointsMaxDistanceToRoute = pointsMaxDistanceToRoute;
        this.percentageOfPointsWithinDistance = percentageOfPointsWithinDistance;
        this.alwaysCheckPointDistancePercent = alwaysCheckPointDistancePercent;
    }

    protected InDirectionValueSource(LineString lineString, ValueSource loctionValueSource) {
        this.lineString = lineString;
        this.loctionValueSource = loctionValueSource;
    }

    @Override
    public FunctionValues getValues(Map context, AtomicReaderContext atomicReaderContext) throws IOException {
        FunctionValues locationValues = this.loctionValueSource.getValues(context, atomicReaderContext);
        return new CorridorDocValues(this, locationValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;
        InDirectionValueSource that = (InDirectionValueSource) o;
        return Double.compare(that.maxDifference, this.maxDifference) == 0
                && Double.compare(that.maxDifferenceAdditionalPointsCheck, this.maxDifferenceAdditionalPointsCheck) == 0
                && Double.compare(that.pointsMaxDistanceToRoute, this.pointsMaxDistanceToRoute) == 0
                && this.percentageOfPointsWithinDistance == that.percentageOfPointsWithinDistance
                && this.alwaysCheckPointDistancePercent == that.alwaysCheckPointDistancePercent && Objects.equals(
                this.lineString,
                that.lineString) && Objects.equals(this.loctionValueSource, that.loctionValueSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.lineString, this.loctionValueSource, this.maxDifference,
                this.maxDifferenceAdditionalPointsCheck, this.pointsMaxDistanceToRoute, this.percentageOfPointsWithinDistance, this.alwaysCheckPointDistancePercent);
    }

    @Override
    public String description() {
        return "inRouteDirection()";
    }

    protected double getValue(List<Point> points, boolean bidirectional) {
        LineString route = this.lineString;

        try {
            if (this.alwaysCheckPointDistancePercent && !(enoughPointsWithinDistance(route, points))) {
                return 0;
            }

            double difference = AngleUtils.getAngleDifference(route, points, this.pointsMaxDistanceToRoute);

            if (checkAngleDifferenceInFirstOrSecondQuadrant(difference, this.maxDifference)) {
                return 1;
            }

            if (bidirectional && checkAngleDifferenceInThirdOrFourthQuadrant(difference, this.maxDifference)) {
                return 1;
            }

            if (checkAngleDifferenceInFirstOrSecondQuadrant(difference, this.maxDifferenceAdditionalPointsCheck)) {
                if (enoughPointsWithinDistance(route, points)) {
                    return 1;
                }
            }

            if (bidirectional && checkAngleDifferenceInThirdOrFourthQuadrant(
                    difference,
                    this.maxDifferenceAdditionalPointsCheck)) {
                if (enoughPointsWithinDistance(route, points)) {
                    return 1;
                }
            }

            return 0;
        } catch (Exception e) {
            LOGGER.error("Could not calculate value. | linestring={}", route.toText(), e);
        }
        return Double.MAX_VALUE;
    }

    private boolean enoughPointsWithinDistance(LineString route, List<Point> points) {
        return percentageOfPointsWithinDistanceTo(route, points) >= this.percentageOfPointsWithinDistance;
    }

    private int percentageOfPointsWithinDistanceTo(LineString route, List<Point> points) {
        LocationIndexedLine lineRef = new LocationIndexedLine(route);

        int result = 0;

        for (Point point : points) {
            LinearLocation loc = lineRef.project(point.getCoordinate());
            Coordinate nearestPoint = lineRef.extractPoint(loc);

            double distance = nearestPoint.distance(point.getCoordinate()) * WGS84_TO_KILOMETERS_FACTOR;

            if (distance <= this.pointsMaxDistanceToRoute) {
                result++;
            }
        }

        return Double.valueOf((double) result / points.size() * 100).intValue();
    }

    private boolean checkAngleDifferenceInThirdOrFourthQuadrant(double actualDifference, double maxDifference) {
        return actualDifference >= 180 - maxDifference && actualDifference <= 180 + maxDifference;
    }

    private boolean checkAngleDifferenceInFirstOrSecondQuadrant(double actualDifference, double maxDifference) {
        if (actualDifference >= 360 - maxDifference && actualDifference <= 360) {
            return true;
        }

        if (actualDifference >= 0 && actualDifference <= maxDifference) {
            return true;
        }

        return false;
    }

    private final class CorridorDocValues extends DoubleDocValues {

        private FunctionValues locationValues;

        protected CorridorDocValues(ValueSource vs, FunctionValues locationValues) {
            super(vs);

            this.locationValues = locationValues;
        }

        @Override
        public double doubleVal(int docId) {
            String pointsAsString = this.locationValues.strVal(docId);

            if (pointsAsString == null) {
                return -1;
            }

            Scanner scanner = new Scanner(pointsAsString).useDelimiter(",");
            List<Point> points = new ArrayList<>();

            boolean bidirectional = scanner.nextBoolean();

            while(scanner.hasNext()) {
                String coordinateString = scanner.next().trim();
                String[] coordinates = coordinateString.split(" ");
                points.add(geometryFactory.createPoint(new Coordinate(
                        Double.valueOf(coordinates[0]),
                        Double.valueOf(coordinates[1])
                )));
            }

            return InDirectionValueSource.this.getValue(points, bidirectional);
        }

    }
}
