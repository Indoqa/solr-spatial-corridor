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
package com.indoqa.solr.spatial.corridor.geo;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.locationtech.jts.geom.Coordinate;

public class GeoUtils {

    private GeoUtils() {
        // hide utility class constructor
    }

    public static double calculateDistanceInMeters(Coordinate a, Coordinate b){
        return calculateDistanceInMeters(a.getX(), a.getY(), b.getX(), b.getY());
    }
    public static double calculateDistanceInKilometers(Coordinate a, Coordinate b){
        return calculateDistanceInMeters(a.getX(), a.getY(), b.getX(), b.getY()) / 1000;
    }

    private static double calculateDistanceInMeters(double lon1, double lat1, double lon2, double lat2) {
        GeodesicData g = Geodesic.WGS84.Inverse(lat1, lon1, lat2, lon2);
        return g.s12;
    }
}
