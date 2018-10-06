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

public class DirectionDebugValueSource extends DirectionValueSource {

    public DirectionDebugValueSource(List<Point> queryPoints, ValueSource routeValueSource, ValueSource routeHashValueSource,
        double pointsMaxDistanceToRoute) {
        super(queryPoints, routeValueSource, routeHashValueSource, pointsMaxDistanceToRoute);
    }

    @Override
    protected FunctionValues getFunctionValues(FunctionValues locationValues, FunctionValues hashValues) {
        return new InverseCorridorDebugDocValues(this, locationValues, hashValues);
    }

    @Override
    public String description() {
        return "pointsDirectionDebug";
    }

    private final class InverseCorridorDebugDocValues extends DebugStrDocValues {

        private InverseCorridorDocValues inverseCorridorDocValues;

        public InverseCorridorDebugDocValues(ValueSource vs, FunctionValues routeValues, FunctionValues hashValues) {
            super(vs);
            this.inverseCorridorDocValues = new InverseCorridorDocValues(vs, routeValues, hashValues,
                this.getDebugValues());
        }

        @Override
        protected Number callFunctionValues(int doc) {
            return this.inverseCorridorDocValues.doubleVal(doc);
        }
    }
}
