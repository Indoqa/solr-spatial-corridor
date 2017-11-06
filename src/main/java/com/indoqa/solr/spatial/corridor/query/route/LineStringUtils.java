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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.solr.search.SolrCache;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.SyntaxError;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.util.concurrent.TimeUnit;

public final class LineStringUtils {

    private static WKTReader wktReader = new WKTReader();

    static {
        cache = Caffeine.newBuilder()
                .maximumSize(100000)
                .expireAfterAccess(24, TimeUnit.HOURS)
                .build(key -> parseWkt(key));
    }

    private static LoadingCache<String, LineString> cache;

    @SuppressWarnings("unchecked")
    public static LineString parse(String corridorLineString)  {
        if (corridorLineString == null) {
            return null;
        }

        LineString lineString = cache.get(corridorLineString);

        if (lineString == null) {
            lineString = parseWkt(corridorLineString);
            cache.put(corridorLineString, lineString);
        }

        return lineString;
    }

    private static LineString parseWkt(String corridorLineString) {
        try {
            return (LineString) wktReader.read(corridorLineString);
        } catch (ParseException e) {
            throw new IllegalStateException("corridor.route is no valid WKT LineString!");
        }
    }
}
