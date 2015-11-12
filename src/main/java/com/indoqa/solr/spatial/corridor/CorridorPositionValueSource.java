package com.indoqa.solr.spatial.corridor;

import static com.indoqa.solr.spatial.corridor.CorridorConstants.WGS84_TO_KILOMETERS_FACTOR;

import org.apache.lucene.queries.function.ValueSource;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

public class CorridorPositionValueSource extends AbstractCorridorValueSource {

    protected CorridorPositionValueSource(LineString lineString, ValueSource loctionValueSource) {
        super(lineString, loctionValueSource);
    }

    @Override
    public String description() {
        return "corridorPosition()";
    }

    @Override
    protected double getValue(LineString lineString, Point point) {
        LocationIndexedLine indexedLineString = new LocationIndexedLine(lineString);
        LinearLocation intersection = indexedLineString.project(point.getCoordinate());

        LineString routeToIntersection = (LineString) indexedLineString.extractLine(indexedLineString.getStartIndex(), intersection);

        return routeToIntersection.getLength() * WGS84_TO_KILOMETERS_FACTOR;
    }

}
