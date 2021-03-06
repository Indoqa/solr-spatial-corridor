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

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRouteQueryValueSource extends ValueSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRouteQueryValueSource.class);

    private LineString lineString;
    private ValueSource loctionValueSource;

    protected AbstractRouteQueryValueSource(LineString lineString, ValueSource loctionValueSource) {
        this.lineString = lineString;
        this.loctionValueSource = loctionValueSource;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractRouteQueryValueSource)) {
            return false;
        }

        AbstractRouteQueryValueSource other = (AbstractRouteQueryValueSource) o;

        if (ObjectUtils.notEqual(other.lineString, this.lineString)) {
            return false;
        }

        if (ObjectUtils.notEqual(other.loctionValueSource, this.loctionValueSource)) {
            return false;
        }

        if (ObjectUtils.notEqual(other.description(), this.description())) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public final FunctionValues getValues(Map context, LeafReaderContext readerContext) throws IOException {
        FunctionValues locationValues = this.loctionValueSource.getValues(context, readerContext);
        return new CorridorDocValues(this, locationValues);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.loctionValueSource == null ? 0 : this.loctionValueSource.hashCode());
        result = prime * result + (this.lineString == null ? 0 : this.lineString.hashCode());
        result = prime * result + (this.description() == null ? 0 : this.description().hashCode());
        return result;
    }

    protected LineString getLineString() {
        return this.lineString;
    }

    protected abstract double getValue(Point point);

    private final class CorridorDocValues extends DoubleDocValues {

        private FunctionValues locationValues;

        protected CorridorDocValues(ValueSource vs, FunctionValues locationValues) {
            super(vs);

            this.locationValues = locationValues;
        }

        @Override
        public double doubleVal(int docId) {
            try{
                double[] values = new double[2];

                this.locationValues.doubleVal(docId, values);

                Point point = GeometryFactory.createPointFromInternalCoord(new Coordinate(values[1], values[0]),
                        AbstractRouteQueryValueSource.this.getLineString());
                return AbstractRouteQueryValueSource.this.getValue(point);
            }catch (Exception e){
                LOGGER.error("Could not calculate value.", e);
            }
            return Double.MAX_VALUE;
        }

    }
}
