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

import static com.indoqa.solr.spatial.corridor.direction.InDirectionUtils.*;

import java.io.IOException;
import java.util.*;

import com.indoqa.solr.spatial.corridor.debug.DebugValues;
import com.indoqa.solr.spatial.corridor.debug.NoOpDebugValues;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InDirectionPointsValueSource extends ValueSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(InDirectionPointsValueSource.class);
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    private LineString lineString;
    private ValueSource locationValueSource;

    private double maxDifference;
    private double maxDifferenceAdditionalPointsCheck;
    private double pointsMaxDistanceToRoute;
    private int percentageOfPointsWithinDistance;
    private boolean alwaysCheckPointDistancePercent;

    private DebugValues debugValues;

    public InDirectionPointsValueSource(LineString lineString, ValueSource locationValueSource, double maxDifference,
        double maxDifferenceAdditionalPointsCheck, double pointsMaxDistanceToRoute, int percentageOfPointsWithinDistance,
        boolean alwaysCheckPointDistancePercent) {

        this.lineString = lineString;
        this.locationValueSource = locationValueSource;
        this.maxDifference = maxDifference;
        this.maxDifferenceAdditionalPointsCheck = maxDifferenceAdditionalPointsCheck;
        this.pointsMaxDistanceToRoute = pointsMaxDistanceToRoute;
        this.percentageOfPointsWithinDistance = percentageOfPointsWithinDistance;
        this.alwaysCheckPointDistancePercent = alwaysCheckPointDistancePercent;
        this.debugValues = NoOpDebugValues.NOOP_DEBUG_VALUES;
    }

    @Override
    public FunctionValues getValues(Map context, LeafReaderContext readerContext) throws IOException {
        FunctionValues locationValues = this.locationValueSource.getValues(context, readerContext);
        return new CorridorDocValues(this, locationValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;
        InDirectionPointsValueSource that = (InDirectionPointsValueSource) o;
        return Double.compare(that.maxDifference, this.maxDifference) == 0
            && Double.compare(that.maxDifferenceAdditionalPointsCheck, this.maxDifferenceAdditionalPointsCheck) == 0
            && Double.compare(that.pointsMaxDistanceToRoute, this.pointsMaxDistanceToRoute) == 0
            && this.percentageOfPointsWithinDistance == that.percentageOfPointsWithinDistance
            && this.alwaysCheckPointDistancePercent == that.alwaysCheckPointDistancePercent
            && Objects.equals(this.lineString, that.lineString)
            && Objects.equals(this.locationValueSource, that.locationValueSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.lineString, this.locationValueSource, this.maxDifference,
            this.maxDifferenceAdditionalPointsCheck, this.pointsMaxDistanceToRoute,
            this.percentageOfPointsWithinDistance, this.alwaysCheckPointDistancePercent);
    }

    @Override
    public String description() {
        return "inDirectionPoints()";
    }

    private final class CorridorDocValues extends DoubleDocValues {

        private FunctionValues locationValues;

        protected CorridorDocValues(ValueSource vs, FunctionValues locationValues) {
            super(vs);

            this.locationValues = locationValues;
        }

        @Override
        public double doubleVal(int docId) throws IOException {
            String pointsAsString = this.locationValues.strVal(docId);

            if (pointsAsString == null) {
                return -1;
            }

            Scanner scanner = new Scanner(pointsAsString).useDelimiter(",");
            List<Point> points = new ArrayList<>();

            boolean bidirectional = scanner.nextBoolean();

            while (scanner.hasNext()) {
                String coordinateString = scanner.next().trim();
                String[] coordinates = coordinateString.split(" ");
                points.add(geometryFactory.createPoint(new Coordinate(Double.valueOf(coordinates[0]),
                    Double.valueOf(coordinates[1]))));
            }

            return InDirectionPointsValueSource.this.getValue(points, bidirectional);
        }

    }

    private boolean enoughPointsWithinDistance(LineString route, List<Point> points) {
        return percentageOfPointsWithinDistanceTo(route, points, this.pointsMaxDistanceToRoute) >= this.percentageOfPointsWithinDistance;
    }

    private double getValue(List<Point> points, boolean bidirectional) {
        LineString route = this.lineString;

        try {
            if (this.alwaysCheckPointDistancePercent && !(enoughPointsWithinDistance(route, points))) {
                return 0;
            }

            double difference = AngleUtils.getAngleDifference(this.debugValues, route, points, this.pointsMaxDistanceToRoute);

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

            if (bidirectional && checkAngleDifferenceInThirdOrFourthQuadrant(difference, this.maxDifferenceAdditionalPointsCheck)) {
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
}
