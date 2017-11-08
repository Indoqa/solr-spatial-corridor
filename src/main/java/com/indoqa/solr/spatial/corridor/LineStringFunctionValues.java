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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;

import java.io.IOException;
import java.util.Collections;

public class LineStringFunctionValues extends FunctionValues {

    private String hashFieldName;
    private LeafReaderContext readerContext;
    private String linestringFieldName;

    public LineStringFunctionValues(String linestringFieldName, String hashFieldName, LeafReaderContext readerContext) {
        this.linestringFieldName = linestringFieldName;
        this.hashFieldName = hashFieldName;
        this.readerContext = readerContext;
    }

    @Override
    public String strVal(int doc) {
        return this.toString(doc);
    }

    @Override
    public void strVal(int doc, String[] vals) {
        try{
            Document document = getDocumentWithField(doc, this.linestringFieldName);
            IndexableField field = document.getField(this.linestringFieldName);
            vals[0]= field.stringValue();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean hasField(String fieldname) throws IOException {
        Fields fields = this.readerContext.reader().fields();
        for (String field : fields) {
            if (field.equals(fieldname)) {
                return true;
            }
        }
        return false;
    }

    private Document getDocumentWithField(int doc, String fieldname) throws IOException {
        return this.readerContext.reader().document(doc, Collections.singleton(fieldname));
    }

    @Override
    public String toString(int doc) {
        Document document;

        try {
            if (hasField(this.hashFieldName)) {
                document = getDocumentWithField(doc, this.hashFieldName);
                IndexableField field = document.getField(this.hashFieldName);
                if(field != null){
                    return field.stringValue();
                }
            }
            if(!hasField(linestringFieldName)){
                return null;
            }

            document = getDocumentWithField(doc, this.linestringFieldName);
            IndexableField field = document.getField(this.linestringFieldName);

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
