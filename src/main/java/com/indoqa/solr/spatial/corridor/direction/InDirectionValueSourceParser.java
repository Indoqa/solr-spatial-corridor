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

import com.indoqa.solr.spatial.corridor.LineStringValueSource;
import com.indoqa.solr.spatial.corridor.wkt.WktUtils;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

import java.util.ArrayList;
import java.util.List;

public class InDirectionValueSourceParser extends ValueSourceParser {

    private final WKTReader wktReader;
    private final GeometryFactory geometryFactory;

    public InDirectionValueSourceParser(){
        super();
        this.wktReader = new WKTReader();
        this.geometryFactory = new GeometryFactory();
    }

    @Override
    public ValueSource parse(FunctionQParser fp) throws SyntaxError {
        List<Point> queryPoints = new ArrayList<>();
        String[] queryPointParameters = fp.getParams().getParams("corridor.point");

        for (String queryPointParameter : queryPointParameters) {
            queryPoints.add(WktUtils.parsePoint(queryPointParameter));
        }

        LineStringValueSource routeValueSource = new LineStringValueSource(fp.parseArg());
        ValueSource routeHashValueSource = fp.parseValueSource();

        double maxAngleDifference =  fp.getParams().getDouble("corridor.maxAngleDifference", 0);
        boolean bidirectional =  fp.getParams().getBool("corridor.bidirectional", false);
        double maxAngleDifferenceAdditionalPointsCheck = fp.getParams().getDouble("corridor.maxAngleDifferenceAdditionalPointsCheck", 0);
        double pointsMaxDistanceToRoute = fp.getParams().getDouble("corridor.pointsMaxDistanceToRoute", 0.0001);
        int percentageOfPointsWithinDistance = fp.getParams().getInt("corridor.percentageOfPointsWithinDistance", 100);
        boolean alwaysCheckPointDistancePercent = fp.getParams().getBool("corridor.alwaysCheckPointDistancePercent", false);

        return createValueSource(queryPoints, routeValueSource, routeHashValueSource, maxAngleDifference, bidirectional,
            maxAngleDifferenceAdditionalPointsCheck, pointsMaxDistanceToRoute, percentageOfPointsWithinDistance,
            alwaysCheckPointDistancePercent);
    }

    protected ValueSource createValueSource(List<Point> queryPoints, LineStringValueSource routeValueSource,
        ValueSource routeHashValueSource, double maxAngleDifference, boolean bidirectional,
        double maxAngleDifferenceAdditionalPointsCheck, double pointsMaxDistanceToRoute, int percentageOfPointsWithinDistance,
        boolean alwaysCheckPointDistancePercent) {
        return new InDirectionValueSource(queryPoints, routeValueSource, routeHashValueSource, maxAngleDifference, bidirectional,
                maxAngleDifferenceAdditionalPointsCheck, pointsMaxDistanceToRoute, percentageOfPointsWithinDistance,
                alwaysCheckPointDistancePercent);
    }

    protected String getDescription() {
        return "inPointsDirection()";
    }

}
