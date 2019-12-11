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
package com.indoqa.solr.spatial.corridor.debug;

import java.util.LinkedHashMap;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;

public class DebugValuesImpl implements DebugValues {

    public static final String ANGLE_DIFFERENCE_KEY = "angleDifference";
    private Map<String, Object> values = new LinkedHashMap<>();

    @Override
    public void add(String key, String value) {
        this.values.put(key, value);
    }

    @Override
    public void add(String key, boolean value) {
        this.values.put(key, value);
    }

    @Override
    public void add(String key, Number value) {
        this.values.put(key, value);
    }

    private void putAngleDifference(String key, Object value) {
        Object angleDifference = this.values.get(ANGLE_DIFFERENCE_KEY);
        if (angleDifference == null) {
            angleDifference = new LinkedHashMap<>();
        }

        if (angleDifference instanceof Map) {
            Map map = (Map) angleDifference;
            String uniqueKey = createUniqueKey(map, key);
            map.put(uniqueKey, value);
        }
        this.values.put("angleDifference", angleDifference);
    }

    private String findNextUniqueKey(Map<String, Object> map, String key) {
        String newKey = this.appendIndex(key);
        while (map.containsKey(newKey)) {
            newKey = this.appendIndex(newKey);
        }
        return newKey;
    }

    private String createUniqueKey(Map<String, Object> map, String key) {
        if (!map.containsKey(key)) {
            return key;
        }
        Object value = map.get(key);
        if (value == null) {
            return this.findNextUniqueKey(map, key);
        }

        map.put(key, null);
        String newKey = this.appendIndex(key);
        map.put(newKey, value);
        return this.findNextUniqueKey(map, newKey);
    }

    private String appendIndex(String key) {
        int indexOfSeparator = key.indexOf('_');
        int i = 1;
        if (indexOfSeparator > -1) {
            i = i + Integer.parseInt(key.substring(indexOfSeparator + 1));
        } else {
            return key + "_1";
        }
        return key.substring(0, indexOfSeparator) + "_" + i;
    }

    @Override
    public void addAngleDifference(String key, String value) {
        this.putAngleDifference(key, value);
    }

    @Override
    public void addAngleDifference(String key, Number value) {
        this.putAngleDifference(key, value);
    }

    @Override
    public void addAngleDifference(String key, Coordinate coordinate) {
        this.putAngleDifference(key, "(" + coordinate.x + "," + coordinate.y + ")");
    }

    @Override
    public void addSingleAngleDifference(String key, Number value) {
        this.putAngleDifference(key, value);
    }

    @Override
    public String toJson() {
        return toJson(this.values);
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder result = new StringBuilder();
        result.append('{');
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            result.append('"');
            result.append(entry.getKey());
            result.append("\":");
            if (value instanceof Map) {
                result.append(toJson((Map<String, Object>) value));
                result.append(',');
            } else if (value instanceof  Number) {
                result.append(value);
                result.append(',');
            } else {
                result.append('"');
                result.append(value);
                result.append("\",");
            }
        }
        result.setLength(result.length() - 1);
        result.append('}');
        return result.toString();
    }
}
