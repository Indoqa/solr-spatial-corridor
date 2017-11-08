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

import com.indoqa.solr.spatial.corridor.LineStringUtils;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.FunctionRangeQuery;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.function.ValueSourceRangeFilter;

import com.vividsolutions.jts.geom.LineString;

public class RouteQueryParser extends QParser {

    public RouteQueryParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        super(qstr, localParams, params, req);
    }

    @Override
    public Query parse() throws SyntaxError {
        ValueSource locationValueSource = this.parseLocationValueSource();

        LineString lineString = this.parseLineString();
        String buffer = this.parseBuffer();

        RouteDistanceValueSource corridorDistanceValueSource = new RouteDistanceValueSource(lineString, locationValueSource);
        ValueSourceRangeFilter filter = new ValueSourceRangeFilter(corridorDistanceValueSource, "0", buffer, true, true);
        return new FunctionRangeQuery(filter);
    }

    private String parseBuffer() {
        String buffer = this.getParam("buffer");

        if (buffer == null) {
            return "5";
        }

        return buffer;
    }

    private LineString parseLineString() throws SyntaxError {
        return LineStringUtils.parseOrGet(this.getParam("corridor.route"));
    }

    private ValueSource parseLocationValueSource() {
        SchemaField locationField = this.req.getSchema().getField(this.getParam("field"));
        ValueSource locationValueSource = locationField.getType().getValueSource(locationField, this);
        return locationValueSource;
    }

}
