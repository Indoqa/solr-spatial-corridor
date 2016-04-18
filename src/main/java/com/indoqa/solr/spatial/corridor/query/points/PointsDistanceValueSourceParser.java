package com.indoqa.solr.spatial.corridor.query.points;

import java.util.List;

import org.apache.lucene.queries.function.ValueSource;

import com.vividsolutions.jts.geom.Point;

public class PointsDistanceValueSourceParser extends AbstractPointsQueryCorridorValueSourceParser {

    @Override
    protected ValueSource createValueSource(List<Point> queryPoints, ValueSource routeValueSource) {
        return new PointsDistanceValueSource(queryPoints, routeValueSource);
    }

    @Override
    protected String getDescription() {
        return "pointsDistance()";
    }

}
