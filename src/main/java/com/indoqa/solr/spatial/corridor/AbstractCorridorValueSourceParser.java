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
package com.indoqa.solr.spatial.corridor;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SolrCache;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public abstract class AbstractCorridorValueSourceParser extends ValueSourceParser {

    private WKTReader wktReader = new WKTReader();

    @Override
    @SuppressWarnings("unchecked")
    public ValueSource parse(FunctionQParser fp) throws SyntaxError {
        String corridorLineString = fp.getParam("corridor.route");

        if (corridorLineString == null) {
            throw new SyntaxError(this.getDescription() + ": Parameter corridor.route must be set and a valid LineString!");
        }

        SolrCache<String, LineString> lineStringCache = fp.getReq().getSearcher().getCache("corridorLineStrings");

        LineString lineString = lineStringCache.get(corridorLineString);

        if (lineString == null) {
            lineString = this.parseWkt(corridorLineString);
            lineStringCache.put(corridorLineString, lineString);
        }

        return new CorridorValueSource(lineString, fp.parseValueSource());
    }

    protected abstract String getDescription();

    protected abstract double getValue(LineString lineString, Point point);

    private LineString parseWkt(String corridorLineString) throws SyntaxError {
        try {
            return (LineString) this.wktReader.read(corridorLineString);
        } catch (ParseException e) {
            throw new SyntaxError("corridor.route is no valid WKT LineString!");
        }
    }

    private final class CorridorDistanceDocValues extends DoubleDocValues {

        private LineString lineString;
        private FunctionValues locationValues;

        protected CorridorDistanceDocValues(ValueSource vs, LineString lineString, FunctionValues locationValues) {
            super(vs);

            this.lineString = lineString;
            this.locationValues = locationValues;
        }

        @Override
        public double doubleVal(int docId) {
            double[] values = new double[2];

            this.locationValues.doubleVal(docId, values);

            Point point = GeometryFactory.createPointFromInternalCoord(new Coordinate(values[1], values[0]), this.lineString);
            return AbstractCorridorValueSourceParser.this.getValue(this.lineString, point);
        }

    }

    private class CorridorValueSource extends ValueSource {

        private LineString lineString;
        private ValueSource loctionValueSource;

        protected CorridorValueSource(LineString lineString, ValueSource loctionValueSource) {
            this.lineString = lineString;
            this.loctionValueSource = loctionValueSource;
        }

        @Override
        public String description() {
            return AbstractCorridorValueSourceParser.this.getDescription();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CorridorValueSource)) {
                return false;
            }

            CorridorValueSource other = (CorridorValueSource) o;

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

        @Override
        public FunctionValues getValues(@SuppressWarnings("rawtypes") Map context, AtomicReaderContext readerContext)
                throws IOException {
            FunctionValues locationValues = this.loctionValueSource.getValues(context, readerContext);
            return new CorridorDistanceDocValues(this, this.lineString, locationValues);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.loctionValueSource == null) ? 0 : this.loctionValueSource.hashCode());
            result = prime * result + ((this.lineString == null) ? 0 : this.lineString.hashCode());
            result = prime * result + ((this.description() == null) ? 0 : this.description().hashCode());
            return result;
        }
    }

}
