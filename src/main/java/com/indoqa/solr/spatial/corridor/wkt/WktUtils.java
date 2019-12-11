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
package com.indoqa.solr.spatial.corridor.wkt;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class WktUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(WktUtils.class);

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public static LineString parseLineString(String corridorLineString) {
        String rawCoordinates = StringUtils.substringBetween(corridorLineString, "LINESTRING(", ")");
        if (rawCoordinates == null || rawCoordinates.trim().isEmpty()) {
            LOGGER.warn("Linestring without coordinates found: '{}'", corridorLineString);
            return geometryFactory.createLineString((Coordinate[]) null);
        }
        String[] coordinates = rawCoordinates.trim().split(",");

        Coordinate[] wktCoordinates = Arrays.stream(coordinates).map(WktUtils::createCoordinate).toArray(Coordinate[]::new);
        return geometryFactory.createLineString(wktCoordinates);
    }

    public static Point parsePoint(String pointString) {
        String rawPoints = StringUtils.substringBetween(pointString, "POINT(", ")");
        if (rawPoints == null) {
            throw new IllegalArgumentException("Parameter must be a valid WKT Point!");
        }

        Coordinate coordinate = createCoordinate(rawPoints);
        return geometryFactory.createPoint(coordinate);
    }

    private static Coordinate createCoordinate(String rawCoordinates) {
        String[] points = rawCoordinates.trim().split(" ");

        Coordinate coordinate = new Coordinate();
        coordinate.x = Double.parseDouble(points[0]);
        coordinate.y = Double.parseDouble(points[1]);
        geometryFactory.getPrecisionModel().makePrecise(coordinate);
        return coordinate;
    }
}
