package com.indoqa.solr.spatial.corridor.query.points;

import static com.indoqa.solr.spatial.corridor.CorridorConstants.WGS84_TO_KILOMETERS_FACTOR;

import java.util.List;

import org.apache.lucene.queries.function.ValueSource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

public class PointsDistanceValueSource extends AbstractPointsQueryCorridorValueSource {

    protected PointsDistanceValueSource(List<Point> queryPoints, ValueSource routeValueSource) {
        super(queryPoints, routeValueSource);
    }

    @Override
    public String description() {
        return "pointsDistance()";
    }

    @Override
    protected double getValue(LineString lineString) {
        LocationIndexedLine lineRef = new LocationIndexedLine(lineString);

        double minDistance = Integer.MAX_VALUE;

        for (Point point : this.getQueryPoints()) {
            LinearLocation loc = lineRef.project(point.getCoordinate());
            Coordinate extractPoint = lineRef.extractPoint(loc);

            double distance = extractPoint.distance(point.getCoordinate()) * WGS84_TO_KILOMETERS_FACTOR;

            minDistance = Math.min(distance, minDistance);
        }

        return minDistance;
    }
}
