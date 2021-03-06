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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.indoqa.solr.spatial.corridor.LineStringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;

import com.indoqa.solr.spatial.corridor.debug.DebugValues;
import com.indoqa.solr.spatial.corridor.debug.NoOpDebugValues;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectionValueSource extends ValueSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectionValueSource.class);

    private List<Point> queryPoints;
    private ValueSource routeValueSource;
    private ValueSource routeHashValueSource;
    private double pointsMaxDistanceToRoute;

    protected DirectionValueSource(List<Point> queryPoints, ValueSource routeValueSource, ValueSource routeHashValueSource,
                                   double pointsMaxDistanceToRoute) {
        this.queryPoints = queryPoints;
        this.routeValueSource = routeValueSource;
        this.routeHashValueSource = routeHashValueSource;
        this.pointsMaxDistanceToRoute = pointsMaxDistanceToRoute;
    }

    @Override
    public String description() {
        return "pointsDirection()";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DirectionValueSource)) {
            return false;
        }

        DirectionValueSource other = (DirectionValueSource) o;

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
        return this.getFunctionValues(locationValues, hashValues);
    }

    protected FunctionValues getFunctionValues(FunctionValues locationValues, FunctionValues hashValues) {
        return new InverseCorridorDocValues(this, locationValues, hashValues);
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

    protected double getValue(DebugValues debugValues, LineString lineString) {
        return AngleUtils.getAngleDifference(debugValues, lineString, this.queryPoints, this.pointsMaxDistanceToRoute);
    }

    protected final class InverseCorridorDocValues extends DoubleDocValues {

        private FunctionValues routeValues;
        private FunctionValues hashValues;
        private DebugValues debugValues;

        protected InverseCorridorDocValues(ValueSource vs, FunctionValues routeValues, FunctionValues hashValues) {
            this(vs, routeValues, hashValues, NoOpDebugValues.NOOP_DEBUG_VALUES);
        }
        protected InverseCorridorDocValues(ValueSource vs, FunctionValues routeValues, FunctionValues hashValues,
            DebugValues debugValues) {
            super(vs);

            this.routeValues = routeValues;
            this.hashValues = hashValues;
            this.debugValues = debugValues;
        }

        @Override
        public double doubleVal(int docId) throws IOException {
            String routeAsString = this.routeValues.strVal(docId);
            String routeAsHash = this.hashValues.strVal(docId);

            try {
                if (routeAsString == null || routeAsString.isEmpty()) {
                    this.debugValues.add("routeAsString", "empty");
                    return -1;
                }

                LineString route = LineStringUtils.parseOrGet(routeAsString, routeAsHash);

                return DirectionValueSource.this.getValue(this.debugValues, route);
            } catch(Exception e){
                LOGGER.error("Could not calculate value. | linestring={}", routeAsString, e);
            }
            return Double.MAX_VALUE;
        }
    }
}
