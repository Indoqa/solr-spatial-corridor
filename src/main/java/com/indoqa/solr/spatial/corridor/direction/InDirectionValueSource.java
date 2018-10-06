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
import com.indoqa.solr.spatial.corridor.debug.DebugValues;
import com.indoqa.solr.spatial.corridor.debug.NoOpDebugValues;
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

    protected List<Point> queryPoints;
    protected ValueSource routeValueSource;
    protected ValueSource routeHashValueSource;
    protected double maxDifference;
    protected boolean bidirectional;
    protected double maxDifferenceAdditionalPointsCheck;
    protected double pointsMaxDistanceToRoute;
    protected int percentageOfPointsWithinDistance;
    protected boolean alwaysCheckPointDistancePercent;

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
        return getFunctionValues(locationValues, hashValues);
    }

    protected FunctionValues getFunctionValues(FunctionValues locationValues, FunctionValues hashValues) {
        return new InverseCorridorDocValues(this, locationValues, hashValues,
                this.maxDifference, this.bidirectional, this.maxDifferenceAdditionalPointsCheck, this.pointsMaxDistanceToRoute,
                this.alwaysCheckPointDistancePercent);
    }

    protected double getValue(DebugValues debugValues, LineString lineString) {
        return AngleUtils.getAngleDifference(debugValues, lineString, this.queryPoints, this.pointsMaxDistanceToRoute);
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

    protected final class InverseCorridorDocValues extends DoubleDocValues {

        private FunctionValues routeValues;
        private FunctionValues hashValues;
        private double maxDifference;
        private boolean bidirectional;
        private double maxDifferenceAdditionalPointsCheck;
        private double pointsMaxDistanceToRoute;
        private boolean alwaysCheckPointDistancePercent;
        private DebugValues debugValues;

        protected InverseCorridorDocValues(ValueSource vs, FunctionValues routeValues, FunctionValues hashValues,
            double maxDifference, boolean bidirectional,
            double maxDifferenceAdditionalPointsCheck, double pointsMaxDistanceToRoute,
            boolean alwaysCheckPointDistancePercent) {
            this(vs, routeValues, hashValues, maxDifference, bidirectional, maxDifferenceAdditionalPointsCheck,
                pointsMaxDistanceToRoute, alwaysCheckPointDistancePercent, NoOpDebugValues.NOOP_DEBUG_VALUES);
        }
        protected InverseCorridorDocValues(ValueSource vs, FunctionValues routeValues, FunctionValues hashValues,
                                           double maxDifference, boolean bidirectional,
                                           double maxDifferenceAdditionalPointsCheck, double pointsMaxDistanceToRoute,
                                           boolean alwaysCheckPointDistancePercent, DebugValues debugValues) {
            super(vs);

            this.routeValues = routeValues;
            this.hashValues = hashValues;
            this.maxDifference = maxDifference;
            this.bidirectional = bidirectional;
            this.maxDifferenceAdditionalPointsCheck = maxDifferenceAdditionalPointsCheck;
            this.pointsMaxDistanceToRoute = pointsMaxDistanceToRoute;
            this.alwaysCheckPointDistancePercent = alwaysCheckPointDistancePercent;
            this.debugValues = debugValues;
        }



        @Override
        public double doubleVal(int docId) {
            String routeAsString = this.routeValues.strVal(docId);
            String routeAsHash = this.hashValues.strVal(docId);

            try {
                if (routeAsString == null || routeAsString.isEmpty()) {
                    this.debugValues.add("routeAsString", "empty");
                    return -1;
                }

                LineString route = LineStringUtils.parseOrGet(routeAsString, routeAsHash);

                if (this.alwaysCheckPointDistancePercent && !enoughPointsWithinDistance(route)) {
                    this.debugValues.add("alwaysCheckPointDistancePercent", true);
                    this.debugValues.add("enoughPointsWithinDistance", false);
                    return 0;
                }

                double difference = InDirectionValueSource.this.getValue(this.debugValues, route);
                debugValues.add("difference", difference);
                debugValues.add("maxDifference", maxDifference);

                if (checkAngleDifferenceInFirstOrSecondQuadrant(difference, maxDifference)) {
                    debugValues.add("firstOrSecondQuadrant", true);
                    return 1;
                }

                if (bidirectional && checkAngleDifferenceInThirdOrFourthQuadrant(difference, maxDifference)) {
                    debugValues.add("bidirectional", true);
                    debugValues.add("thirdOrFourthQuadrant", true);
                    return 1;
                }

                debugValues.add("maxDifferenceAdditionalPointsCheck", maxDifferenceAdditionalPointsCheck);
                if (checkAngleDifferenceInFirstOrSecondQuadrant(difference, maxDifferenceAdditionalPointsCheck)){
                    debugValues.add("firstOrSecondQuadrant", true);

                    if (enoughPointsWithinDistance(route)) {
                        this.debugValues.add("enoughPointsWithinDistance", true);
                        return 1;
                    }
                }

                if (bidirectional && checkAngleDifferenceInThirdOrFourthQuadrant(difference, maxDifferenceAdditionalPointsCheck)) {
                    debugValues.add("bidirectional", true);
                    debugValues.add("thirdOrFourthQuadrant", true);

                    if (enoughPointsWithinDistance(route)) {
                        this.debugValues.add("enoughPointsWithinDistance", true);
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
            int percentageOfPointsWithinDistance = percentageOfPointsWithinDistanceTo(route);
            this.debugValues.add("percentageOfPointsWithinDistance", percentageOfPointsWithinDistance);
            return percentageOfPointsWithinDistance >= percentageOfPointsWithinDistance;
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
