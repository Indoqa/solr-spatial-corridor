# Indoqa Solr Spatial Corridor

This plugin offers functions to filter and sort results alongside a geographical route. It solves tasks like "I want to go from Vienna to Bratislava by car, please find all fuel stations that are max. 2 kilometers away from the computed route on the highway. Additionally, sort them by driven distance on the highway, starting in Vienna." To achieve this, the plugin introduces two dynamically created properties, given a query route as WKT LineString:

  * corridorDistance(latLonField): Normal (shortest) distance between the result and the route
  * corridorPosition(latLonField): The distance on the route between the start point and the projection of the result (= where the normal distance line intersects the route)

![Solr spatial corridor legend](/docs/corridor_legend.png)

## Example Use Cases

### Filter by corridorDistance()

"Find all results with a maximum normal distance of 2 kilometers to the route"

![Solr spation corridor filter](/docs/corridor_filter.png)

```
?q={!corridor field=latLon buffer=2}  // a shortcut for {!frange l=0 u=2}lanLon
&corridor.route=LINESTRING(16.37039615861895 48.20057128552842, 16.358677998093828 48.19719643770264, 16.339207685325686 48.188545501731866, 16.31468318513228 48.18773608789617, 16.28094963169427 48.192208688939225... 
```

### Sort by corridorDistance()

"Show all results, sort them by normal distance"

![Solr spatial distance sort](/docs/corridor_sort_distance.png)

```
?q=*:*
&sort=corridorDistance(latLon) ASC
&corridor.route=LINESTRING(16.37039615861895 48.20057128552842, 16.358677998093828 48.19719643770264, 16.339207685325686 48.188545501731866, 16.31468318513228 48.18773608789617, 16.28094963169427 48.192208688939225... 
```

### Sort by corridorPosition()

"Show all results, sort them by distance from startpoint alongside the route"

![Solr spatial position sort](/docs/corridor_sort_position.png)

```
?q=*:*
&sort=corridorPosition(latLon) ASC
&corridor.route=LINESTRING(16.37039615861895 48.20057128552842, 16.358677998093828 48.19719643770264, 16.339207685325686 48.188545501731866, 16.31468318513228 48.18773608789617, 16.28094963169427 48.192208688939225... 
```
