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

import org.apache.commons.lang.ObjectUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

public class DirectionValueSource extends ValueSource {

    private List<Point> queryPoints;
    private ValueSource routeValueSource;

    protected DirectionValueSource(List<Point> queryPoints, ValueSource routeValueSource) {
        this.queryPoints = queryPoints;
        this.routeValueSource = routeValueSource;
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
        return new InverseCorridorDocValues(this, locationValues);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.routeValueSource == null ? 0 : this.routeValueSource.hashCode());
        result = prime * result + (this.queryPoints == null ? 0 : this.queryPoints.hashCode());
        result = prime * result + (this.description() == null ? 0 : this.description().hashCode());
        return result;
    }

    protected double getValue(LineString lineString) {
        if (this.getQueryPoints().size() < 2) {
            return 0;
        }

        Coordinate queryCoordinate1 = this.getQueryPoints().get(0).getCoordinate();
        Coordinate queryCoordinate2 = this.getQueryPoints().get(1).getCoordinate();

        Coordinate routeCoordinate1;
        Coordinate routeCoordinate2;

        LocationIndexedLine indexedLineString = new LocationIndexedLine(lineString);
        LinearLocation intersection = indexedLineString.project(queryCoordinate1);
        int intersectionIndex = intersection.getSegmentIndex();

        if (!intersection.isEndpoint(lineString)) {
            routeCoordinate1 = intersection.getCoordinate(lineString);
            routeCoordinate2 = lineString.getCoordinateN(intersectionIndex + 1);
        } else {
            routeCoordinate1 = lineString.getCoordinateN(intersectionIndex - 1);
            routeCoordinate2 = intersection.getCoordinate(lineString);
        }

        double routeAngleAtIntersection = AngleUtils.angle(routeCoordinate1, routeCoordinate2);
        double queryAngle = AngleUtils.angle(queryCoordinate1, queryCoordinate2);

        return Math.abs(routeAngleAtIntersection - queryAngle);
    }

    private final class InverseCorridorDocValues extends DoubleDocValues {

        private FunctionValues routeValues;

        protected InverseCorridorDocValues(ValueSource vs, FunctionValues routeValues) {
            super(vs);

            this.routeValues = routeValues;
        }

        @Override
        public double doubleVal(int docId) {
            String routeAsString = this.routeValues.strVal(docId);

            if (routeAsString == null || routeAsString.isEmpty()) {
                return -1;
            }

            LineString route = this.parseLineString(routeAsString);
            return DirectionValueSource.this.getValue(route);
        }

        private LineString parseLineString(String routeAsString) {
            try {
                return (LineString) new WKTReader().read(routeAsString);
            } catch (ParseException e) {
                throw new IllegalStateException(e);
            }
        }

    }
}
