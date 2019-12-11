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

import org.locationtech.jts.geom.Coordinate;

public class NoOpDebugValues implements DebugValues {

    public static NoOpDebugValues NOOP_DEBUG_VALUES = new NoOpDebugValues();

    private NoOpDebugValues() {
        super();
    }

    @Override
    public void add(String key, String value) {

    }

    @Override
    public void add(String key, boolean value) {

    }

    @Override
    public void add(String key, Number value) {

    }

    @Override
    public void addAngleDifference(String key, String value) {

    }

    @Override
    public void addAngleDifference(String key, Number value) {

    }

    @Override
    public void addAngleDifference(String key, Coordinate coordinate) {

    }

    @Override
    public void addSingleAngleDifference(String distance, Number distance1) {

    }

    @Override
    public String toJson() {
        return null;
    }
}
