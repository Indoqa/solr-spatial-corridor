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

import java.util.ArrayList;
import java.util.List;

import com.indoqa.solr.spatial.corridor.wkt.WktUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.schema.StrFieldSource;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

import com.indoqa.solr.spatial.corridor.LineStringValueSource;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class DirectionValueSourceParser extends ValueSourceParser {

    private final WKTReader wktReader;
    private final GeometryFactory geometryFactory;

    public DirectionValueSourceParser(){
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

        double pointsMaxDistanceToRoute = fp.getParams().getDouble("corridor.pointsMaxDistanceToRoute", 0.01);

        return this.createValueSource(queryPoints, new LineStringValueSource(fp.parseArg()), fp.parseValueSource(), pointsMaxDistanceToRoute);
    }

    protected ValueSource createValueSource(List<Point> queryPoints, ValueSource routeValueSource, ValueSource routeHashValueSource,
                                            double pointsMaxDistanceToRoute) {
        return new DirectionValueSource(queryPoints, routeValueSource, routeHashValueSource, pointsMaxDistanceToRoute);
    }

    protected String getDescription() {
        return "pointsDirection()";
    }

}
