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
import java.util.Collections;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LineStringFunctionValues extends FunctionValues {

    private static final Logger LOGGER = LoggerFactory.getLogger(LineStringFunctionValues.class);

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
            LOGGER.error("Could not extract string value. | fieldName={}", this.fieldName, e);
        }

        return null;
    }
}
