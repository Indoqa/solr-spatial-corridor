package com.indoqa.solr.spatial.corridor;

import java.io.IOException;
import java.util.Collections;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;

public class LineStringFunctionValues extends FunctionValues {

    private LeafReaderContext readerContext;
    private String fieldName;

    public LineStringFunctionValues(String fieldName, LeafReaderContext readerContext) {
        this.fieldName = fieldName;
        this.readerContext = readerContext;
    }

    @Override
    public String strVal(int doc) {
        return this.toString(doc);
    }

    @Override
    public String toString(int doc) {
        Document document;

        try {
            document = this.readerContext.reader().document(doc, Collections.singleton(this.fieldName));

            if (document == null) {
                return null;
            }

            IndexableField field = document.getField(this.fieldName);

            if (field == null) {
                return null;
            }

            return field.stringValue();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}