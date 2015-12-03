# Indoqa Solr Spatial Corridor

This plugin offers functions to filter and sort results alongside a geographical route. It solves tasks like "I want to go from Vienna to Bratislava by car, please find all fuel stations that are max. 2 kilometers away from the computed route on the highway. Additionally, sort them by driven distance on the highway, starting in Vienna." To achieve this, the plugin introduces two dynamically created properties, given a query route as WKT LineString:

  * corridorDistance(latLonField): Normal (shortest) distance between the result and the route
  * corridorPosition(latLonField): The distance on between the start point and the 

![Solr spation corridor legend](/docs/corridor_legend.png)
