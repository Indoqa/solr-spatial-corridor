package com.indoqa.solr.spatial.corridor;

import org.apache.solr.search.SolrCache;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.SyntaxError;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public final class LineStringUtils {

    private static WKTReader wktReader = new WKTReader();

    @SuppressWarnings("unchecked")
    public static LineString parse(String corridorLineString, SolrIndexSearcher indexSearcher) throws SyntaxError {
        if (corridorLineString == null) {
            throw new SyntaxError("Parameter corridor.route must be set and a valid LineString!");
        }

        SolrCache<String, LineString> lineStringCache = indexSearcher.getCache("corridorLineStrings");

        LineString lineString = lineStringCache.get(corridorLineString);

        if (lineString == null) {
            lineString = parseWkt(corridorLineString);
            lineStringCache.put(corridorLineString, lineString);
        }

        return lineString;
    }

    private static LineString parseWkt(String corridorLineString) throws SyntaxError {
        try {
            return (LineString) wktReader.read(corridorLineString);
        } catch (ParseException e) {
            throw new SyntaxError("corridor.route is no valid WKT LineString!");
        }
    }
}
