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
package com.indoqa.solr.spatial.corridor.direction;

import com.indoqa.solr.spatial.corridor.LineStringUtils;
import com.indoqa.solr.spatial.corridor.LineStringValueSource;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTReader;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

public class InDirectionPointsValueSourceParser extends ValueSourceParser {

    private final WKTReader wktReader;
    private final GeometryFactory geometryFactory;

    public InDirectionPointsValueSourceParser() {
        super();
        this.wktReader = new WKTReader();
        this.geometryFactory = new GeometryFactory();
    }

    @Override
    public ValueSource parse(FunctionQParser fp) throws SyntaxError {
        String route = fp.getParam("corridor.route");
        LineString lineString = LineStringUtils.parseOrGet(route, String.valueOf(route.hashCode()));
        ValueSource locationValueSource = new LineStringValueSource(fp.parseArg());

        double maxAngleDifference = fp.getParams().getDouble("corridor.maxAngleDifference", 0);
        double maxAngleDifferenceAdditionalPointsCheck =
            fp.getParams().getDouble("corridor.maxAngleDifferenceAdditionalPointsCheck", 0);
        double pointsMaxDistanceToRoute = fp.getParams().getDouble("corridor.pointsMaxDistanceToRoute", 0.0001);
        int percentageOfPointsWithinDistance = fp.getParams().getInt("corridor.percentageOfPointsWithinDistance", 100);
        boolean alwaysCheckPointDistancePercent = fp.getParams().getBool("corridor.alwaysCheckPointDistancePercent", false);

        return new InDirectionPointsValueSource(lineString, locationValueSource, maxAngleDifference, maxAngleDifferenceAdditionalPointsCheck,
            pointsMaxDistanceToRoute, percentageOfPointsWithinDistance, alwaysCheckPointDistancePercent);
    }

    protected String getDescription() {
        return "inDirectionPoints()";
    }
}