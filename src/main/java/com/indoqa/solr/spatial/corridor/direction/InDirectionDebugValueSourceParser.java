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

import java.util.List;

import com.indoqa.solr.spatial.corridor.LineStringValueSource;
import org.locationtech.jts.geom.Point;
import org.apache.lucene.queries.function.ValueSource;

public class InDirectionDebugValueSourceParser extends InDirectionValueSourceParser {

    @Override
    protected ValueSource createValueSource(List<Point> queryPoints, LineStringValueSource routeValueSource,
        ValueSource routeHashValueSource, double maxAngleDifference, boolean bidirectional,
        double maxAngleDifferenceAdditionalPointsCheck, double pointsMaxDistanceToRoute, int percentageOfPointsWithinDistance,
        boolean alwaysCheckPointDistancePercent) {
        return new InDirectionDebugValueSource(queryPoints, routeValueSource, routeHashValueSource, maxAngleDifference, bidirectional,
            maxAngleDifferenceAdditionalPointsCheck, pointsMaxDistanceToRoute, percentageOfPointsWithinDistance,
            alwaysCheckPointDistancePercent);
    }

    protected String getDescription() {
        return "inPointsDirectionDebug()";
    }

}
