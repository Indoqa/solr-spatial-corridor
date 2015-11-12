package com.indoqa.solr.spatial.corridor;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public abstract class AbstractCorridorValueSource extends ValueSource {

    private LineString lineString;
    private ValueSource loctionValueSource;

    protected AbstractCorridorValueSource(LineString lineString, ValueSource loctionValueSource) {
        this.lineString = lineString;
        this.loctionValueSource = loctionValueSource;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractCorridorValueSource)) {
            return false;
        }

        AbstractCorridorValueSource other = (AbstractCorridorValueSource) o;

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
    public final FunctionValues getValues(Map context, AtomicReaderContext readerContext) throws IOException {
        FunctionValues locationValues = this.loctionValueSource.getValues(context, readerContext);
        return new CorridorDocValues(this, this.lineString, locationValues);
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

    protected abstract double getValue(LineString lineString, Point point);

    private final class CorridorDocValues extends DoubleDocValues {

        private LineString lineString;
        private FunctionValues locationValues;

        protected CorridorDocValues(ValueSource vs, LineString lineString, FunctionValues locationValues) {
            super(vs);

            this.lineString = lineString;
            this.locationValues = locationValues;
        }

        @Override
        public double doubleVal(int docId) {
            double[] values = new double[2];

            this.locationValues.doubleVal(docId, values);

            Point point = GeometryFactory.createPointFromInternalCoord(new Coordinate(values[1], values[0]), this.lineString);
            return AbstractCorridorValueSource.this.getValue(this.lineString, point);
        }

    }
}
