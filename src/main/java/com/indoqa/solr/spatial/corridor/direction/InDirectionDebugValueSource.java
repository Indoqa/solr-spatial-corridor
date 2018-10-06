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

import com.vividsolutions.jts.geom.Point;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;

public class InDirectionDebugValueSource extends InDirectionValueSource {

    protected InDirectionDebugValueSource(List<Point> queryPoints, ValueSource routeValueSource, ValueSource routeHashValueSource,
            double maxDifference, boolean bidirectional, double maxDifferenceAdditionalPointsCheck, double pointsMaxDistanceToRoute,
            int percentageOfPointsWithinDistance, boolean alwaysCheckPointDistancePercent) {
        super(queryPoints, routeValueSource, routeHashValueSource, maxDifference, bidirectional, maxDifferenceAdditionalPointsCheck, pointsMaxDistanceToRoute,
            percentageOfPointsWithinDistance, alwaysCheckPointDistancePercent);
    }

    @Override
    public String description() {
        return "inPointsDirectionDebug()";
    }

    @Override
    protected FunctionValues getFunctionValues(FunctionValues locationValues, FunctionValues hashValues) {
        return new InverseCorridorDebugDocValues(this, locationValues, hashValues,
            this.maxDifference, this.bidirectional, this.maxDifferenceAdditionalPointsCheck, this.pointsMaxDistanceToRoute,
            this.alwaysCheckPointDistancePercent);
    }

    private final class InverseCorridorDebugDocValues extends DebugStrDocValues {

        private InDirectionValueSource.InverseCorridorDocValues corridorDocValues;

        protected InverseCorridorDebugDocValues(ValueSource vs, FunctionValues routeValues, FunctionValues hashValues,
            double maxDifference, boolean bidirectional,
            double maxDifferenceAdditionalPointsCheck, double pointsMaxDistanceToRoute,
            boolean alwaysCheckPointDistancePercent) {
            super(vs);
            this.corridorDocValues = new InDirectionValueSource.InverseCorridorDocValues(vs, routeValues, hashValues, maxDifference, bidirectional,
                maxDifferenceAdditionalPointsCheck, pointsMaxDistanceToRoute, alwaysCheckPointDistancePercent, this.getDebugValues());
        }

        @Override
        protected Number callFunctionValues(int doc) {
            return this.corridorDocValues.doubleVal(doc);
        }
    }
}
