# Indoqa Solr Spatial Corridor

This plugin offers functions to filter and sort results alongside a geographical route. It solves tasks like "I want to go from Vienna to Bratislava by car, please find all fuel stations that are max. 2 kilometers away from the computed route on the highway. Additionally, sort them by driven distance on the highway, starting in Vienna." To achieve this, the plugin introduces two dynamically created properties, given a query route as WKT LineString:

  * _corridorDistance(latLonField)_: Normal (shortest) distance between the result and the route
  * _corridorPosition(latLonField)_: The distance on the route between the start point and the projection of the result (= where the normal distance line intersects the route)

![Solr spatial corridor legend](/docs/solr_spatial_corridor_legend.png)

## Example Use Cases

### Filter by corridorDistance()

"Find all results with a maximum normal distance of 2 kilometers to the route"

![Solr spatial corridor filter](/docs/solr_spatial_corridor_filter.png)

```
?q={!corridor field=latLon buffer=2}  // a shortcut for {!frange l=0 u=2}corridorDistance(lanLon)
&corridor.route=LINESTRING(16.37039615861895 48.20057128552842, 16.358677998093828 48.19719643770264, 16.339207685325686 48.188545501731866, 16.31468318513228 48.18773608789617, 16.28094963169427 48.192208688939225... 
```

### Sort by corridorDistance()

"Show all results, sort them by normal distance"

![Solr spatial distance sort](/docs/solr_spatial_corridor_sort_distance.png)

```
?q=*:*
&sort=corridorDistance(latLon) ASC
&corridor.route=LINESTRING(16.37039615861895 48.20057128552842, 16.358677998093828 48.19719643770264, 16.339207685325686 48.188545501731866, 16.31468318513228 48.18773608789617, 16.28094963169427 48.192208688939225... 
```

### Sort by corridorPosition()

"Show all results, sort them by distance from startpoint alongside the route"

![Solr spatial position sort](/docs/solr_spatial_corridor_sort_position.png)

```
?q=*:*
&sort=corridorPosition(latLon) ASC
&corridor.route=LINESTRING(16.37039615861895 48.20057128552842, 16.358677998093828 48.19719643770264, 16.339207685325686 48.188545501731866, 16.31468318513228 48.18773608789617, 16.28094963169427 48.192208688939225... 
```

## Installation

### Requirements

  * Apache Solr 4.9+ including JTS in /WEB-INF/lib (see [why](https://wiki.apache.org/solr/SolrAdaptersForLuceneSpatial4#JTS_.2F_WKT_.2F_Polygon_notes))
  * Java 7+

### Build

  * Download the latest release
  * run "maven clean install"

### Deployment

  * Copy the plugin jar from 'target/spatial-corridor-<VERSION>-jar-with-dependencies.jar' into the /lib directory of your solr core.

## Configuration

### schema.xml

Store one or more geocoordinate(s) for each result in a field of type solr.LatLonType

```xml
<fieldType name="location" class="solr.LatLonType" subFieldSuffix="_coordinate"/>
<field name="latLon" type="location" indexed="true" stored="true" multivalued="true|false"/>
```

### solrconfig.xml

Define new ValueSourceParsers for corridorDistance() and corridorPosition() and the QueryParser for the !{corridor} shortcut:

```xml
<valueSourceParser name="corridorDistance" class="com.indoqa.solr.spatial.corridor.query.route.RouteDistanceValueSourceParser" />
<valueSourceParser name="corridorPosition" class="com.indoqa.solr.spatial.corridor.query.route.RoutePositionValueSourceParser" />
  
<queryParser name="corridor" class="com.indoqa.solr.spatial.corridor.CorridorQueryParserPlugin" />
```

As WKT LineStrings are internally converted into JTS Geometry objects, a custom cache storing results of this conversion may be configured. The plugin scans for a cache named _corridorLineStrings_:

```xml
<query>
  <cache name="corridorLineStrings"
      class="solr.LRUCache"
      size="4096"
      initialSize="2048"
      autowarmCount="2048"/>
</query>
```



