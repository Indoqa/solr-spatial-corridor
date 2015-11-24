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

import org.apache.solr.search.SolrCache;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.SyntaxError;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public final class LineStringUtils {

    private static WKTReader wktReader = new WKTReader();

    @SuppressWarnings("unchecked")
    public static LineString parse(String corridorLineString, SolrIndexSearcher indexSearcher) throws SyntaxError {
        if (corridorLineString == null) {
            throw new SyntaxError("Parameter corridor.route must be set and a valid LineString!");
        }

        SolrCache<String, LineString> lineStringCache = indexSearcher.getCache("corridorLineStrings");

        LineString lineString = lineStringCache.get(corridorLineString);

        if (lineString == null) {
            lineString = parseWkt(corridorLineString);
            lineStringCache.put(corridorLineString, lineString);
        }

        return lineString;
    }

    private static LineString parseWkt(String corridorLineString) throws SyntaxError {
        try {
            return (LineString) wktReader.read(corridorLineString);
        } catch (ParseException e) {
            throw new SyntaxError("corridor.route is no valid WKT LineString!");
        }
    }
}
