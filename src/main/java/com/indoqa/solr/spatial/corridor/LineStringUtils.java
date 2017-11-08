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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.indoqa.solr.spatial.corridor.wkt.WktUtils;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.concurrent.TimeUnit;

public final class LineStringUtils {

    static {
        cache = Caffeine.newBuilder()
                .maximumSize(100000)
                .expireAfterAccess(24, TimeUnit.HOURS)
                .build();
    }

    private static Cache<String, LineString> cache;

    public static LineString parseOrGet(String value)  {
        if (value == null) {
            return null;
        }
        if(value.indexOf("hash-") != -1){
            return cache.getIfPresent(value);
        }

        String key = calculateHash(value);
        return cache.get(key, s -> parseWktLinestring(value));
    }

    public static HashGeometry cacheLineStringGetHashGeometry(String linestring, int radiusInMeters){
        if(linestring == null){
            return null;
        }
        String key = calculateHash(linestring);
        LineString parsedLineString = parseWktLinestring(linestring);

        cache.put(key, parsedLineString);
        HashGeometry result = new HashGeometry();
        // meters /((PI/180)x6378137)
        result.setGeometry(parsedLineString.buffer(radiusInMeters / (Math.PI / 180 * 6378137)).toText());
        result.setHash(key);
        return result;
    }

    private static LineString parseWktLinestring(String corridorLineString) {
        return WktUtils.parseLineString(corridorLineString);
    }

    private static String calculateHash(String linestring) {
        if (linestring == null) {
            return null;
        }

        StringBuilder result = new StringBuilder("hash-");
        result.append(Hex.encodeHex(DigestUtils.md5(linestring)));
        return result.toString();
    }
}
