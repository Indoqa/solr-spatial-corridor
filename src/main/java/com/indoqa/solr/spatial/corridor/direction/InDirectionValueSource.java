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

import com.indoqa.solr.spatial.corridor.LineStringUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import org.apache.commons.lang.ObjectUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.indoqa.solr.spatial.corridor.CorridorConstants.WGS84_TO_KILOMETERS_FACTOR;

public class InDirectionValueSource extends ValueSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(InDirectionValueSource.class);

    private List<Point> queryPoints;
    private ValueSource routeValueSource;
    private ValueSource routeHashValueSource;
    private double maxDifference;
    private boolean bidirectional;
    private double maxDifferenceAdditionalPointsCheck;
    private double pointsMaxDistanceToRoute;
    private int percentageOfPointsWithinDistance;
    private boolean alwaysCheckPointDistancePercent;

    protected InDirectionValueSource(List<Point> queryPoints, ValueSource routeValueSource, ValueSource routeHashValueSource,
            double maxDifference, boolean bidirectional, double maxDifferenceAdditionalPointsCheck, double pointsMaxDistanceToRoute,
            int percentageOfPointsWithinDistance, boolean alwaysCheckPointDistancePercent) {
        this.queryPoints = queryPoints;
        this.routeValueSource = routeValueSource;
        this.routeHashValueSource = routeHashValueSource;
        this.maxDifference = maxDifference;
        this.bidirectional = bidirectional;
        this.maxDifferenceAdditionalPointsCheck = maxDifferenceAdditionalPointsCheck;
        this.pointsMaxDistanceToRoute = pointsMaxDistanceToRoute;
        this.percentageOfPointsWithinDistance = percentageOfPointsWithinDistance;
        this.alwaysCheckPointDistancePercent = alwaysCheckPointDistancePercent;
    }

    @Override
    public String description() {
        return "inPointsDirection()";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InDirectionValueSource)) {
            return false;
        }

        InDirectionValueSource other = (InDirectionValueSource) o;

        if (ObjectUtils.notEqual(other.queryPoints, this.queryPoints)) {
            return false;
        }

        if (ObjectUtils.notEqual(other.routeValueSource, this.routeValueSource)) {
            return false;
        }

        if (ObjectUtils.notEqual(other.description(), this.description())) {
            return false;
        }

        return true;
    }

    public List<Point> getQueryPoints() {
        return this.queryPoints;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public final FunctionValues getValues(Map context, LeafReaderContext readerContext) throws IOException {
        FunctionValues locationValues = this.routeValueSource.getValues(context, readerContext);
        FunctionValues hashValues = this.routeHashValueSource.getValues(context, readerContext);
        return new InverseCorridorDocValues(this, locationValues, hashValues,
                this.maxDifference, this.bidirectional, this.maxDifferenceAdditionalPointsCheck, this.pointsMaxDistanceToRoute,
                this.alwaysCheckPointDistancePercent);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.routeValueSource == null ? 0 : this.routeValueSource.hashCode());
        result = prime * result + (this.routeHashValueSource == null ? 0 : this.routeHashValueSource.hashCode());
        result = prime * result + (this.queryPoints == null ? 0 : this.queryPoints.hashCode());
        result = prime * result + (this.description() == null ? 0 : this.description().hashCode());
        return result;
    }

    protected double getValue(LineString lineString) {
        return AngleUtils.getAngleDifference(lineString, this.queryPoints, this.pointsMaxDistanceToRoute);
    }

    private final class InverseCorridorDocValues extends DoubleDocValues {

        private FunctionValues routeValues;
        private FunctionValues hashValues;
        private double maxDifference;
        private boolean bidirectional;
        private double maxDifferenceAdditionalPointsCheck;
        private double pointsMaxDistanceToRoute;
        private boolean alwaysCheckPointDistancePercent;

        protected InverseCorridorDocValues(ValueSource vs, FunctionValues routeValues, FunctionValues hashValues,
                                           double maxDifference, boolean bidirectional,
                                           double maxDifferenceAdditionalPointsCheck, double pointsMaxDistanceToRoute,
                                           boolean alwaysCheckPointDistancePercent) {
            super(vs);

            this.routeValues = routeValues;
            this.hashValues = hashValues;
            this.maxDifference = maxDifference;
            this.bidirectional = bidirectional;
            this.maxDifferenceAdditionalPointsCheck = maxDifferenceAdditionalPointsCheck;
            this.pointsMaxDistanceToRoute = pointsMaxDistanceToRoute;
            this.alwaysCheckPointDistancePercent = alwaysCheckPointDistancePercent;
        }



        @Override
        public double doubleVal(int docId) {
            String routeAsString = this.routeValues.strVal(docId);
            String routeAsHash = this.hashValues.strVal(docId);

            try {
                if (routeAsString == null || routeAsString.isEmpty()) {
                    return -1;
                }

                LineString route = LineStringUtils.parseOrGet(routeAsString, routeAsHash);

                if (this.alwaysCheckPointDistancePercent && !(enoughPointsWithinDistance(route))) {
                    return 0;
                }

                double difference = InDirectionValueSource.this.getValue(route);

                if (checkAngleDifferenceInFirstOrSecondQuadrant(difference, maxDifference)) {
                    return 1;
                }

                if (bidirectional && checkAngleDifferenceInThirdOrFourthQuadrant(difference, maxDifference)) {
                    return 1;
                }

                if (checkAngleDifferenceInFirstOrSecondQuadrant(difference, maxDifferenceAdditionalPointsCheck)){
                    if (enoughPointsWithinDistance(route)) {
                        return 1;
                    }
                }

                if (bidirectional && checkAngleDifferenceInThirdOrFourthQuadrant(difference, maxDifferenceAdditionalPointsCheck)) {
                    if (enoughPointsWithinDistance(route)) {
                        return 1;
                    }
                }

                return 0;
            } catch(Exception e){
                LOGGER.error("Could not calculate value. | linestring={}", routeAsString, e);
            }
            return Double.MAX_VALUE;
        }

        private boolean enoughPointsWithinDistance(LineString route) {
            return percentageOfPointsWithinDistanceTo(route) >= percentageOfPointsWithinDistance;
        }

        private int percentageOfPointsWithinDistanceTo(LineString route) {
            LocationIndexedLine lineRef = new LocationIndexedLine(route);

            int result = 0;

            for (Point point : queryPoints) {
                LinearLocation loc = lineRef.project(point.getCoordinate());
                Coordinate nearestPoint = lineRef.extractPoint(loc);

                double distance = nearestPoint.distance( point.getCoordinate())* WGS84_TO_KILOMETERS_FACTOR;

                if (distance <= this.pointsMaxDistanceToRoute) {
                    result++;
                }
            }

            return Double.valueOf((double) result / queryPoints.size() * 100).intValue();
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
    }
}
