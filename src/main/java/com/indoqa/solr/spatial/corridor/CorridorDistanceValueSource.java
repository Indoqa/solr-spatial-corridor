package com.indoqa.solr.spatial.corridor;

import static com.indoqa.solr.spatial.corridor.CorridorConstants.WGS84_TO_KILOMETERS_FACTOR;

import org.apache.lucene.queries.function.ValueSource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

public class CorridorDistanceValueSource extends AbstractCorridorValueSource {

    protected CorridorDistanceValueSource(LineString lineString, ValueSource loctionValueSource) {
        super(lineString, loctionValueSource);
    }

    @Override
    public String description() {
        return "corridorDistance()";
    }

    @Override
    protected double getValue(LineString lineString, Point point) {
        LocationIndexedLine lineRef = new LocationIndexedLine(lineString);
        LinearLocation loc = lineRef.project(point.getCoordinate());

        Coordinate extractPoint = lineRef.extractPoint(loc);
        return extractPoint.distance(point.getCoordinate()) * WGS84_TO_KILOMETERS_FACTOR;
    }

}
