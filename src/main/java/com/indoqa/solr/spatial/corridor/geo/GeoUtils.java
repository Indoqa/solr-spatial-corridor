package com.indoqa.solr.spatial.corridor.geo;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.locationtech.jts.geom.Coordinate;

public class GeoUtils {

    private GeoUtils() {
        // hide utility class constructor
    }

    public static double calculateDistanceInMeters(Coordinate a, Coordinate b){
        return calculateDistanceInMeters(a.getX(), a.getY(), b.getX(), b.getY());
    }
    public static double calculateDistanceInKilometers(Coordinate a, Coordinate b){
        return calculateDistanceInMeters(a.getX(), a.getY(), b.getX(), b.getY()) / 1000;
    }

    private static double calculateDistanceInMeters(double lon1, double lat1, double lon2, double lat2) {
        GeodesicData g = Geodesic.WGS84.Inverse(lat1, lon1, lat2, lon2);
        return g.s12;
    }

}
