package com.indoqa.solr.spatial.corridor;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;

public class LineStringValueSource extends ValueSource {

    private String fieldName;

    public LineStringValueSource(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String description() {
        return "retrieve raw linestring even for tokenized fields";
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public FunctionValues getValues(Map context, LeafReaderContext readerContext) throws IOException {
        return new LineStringFunctionValues(this.fieldName, readerContext);
    }

    @Override
    public int hashCode() {
        return ("linestring" + this.fieldName).hashCode();
    }

}