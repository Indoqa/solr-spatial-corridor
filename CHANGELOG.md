# v6.6.2-11 | 2020-04-17
* Add circleDistance ValueSource

# v8.3.1-0 | 2019-12-11
* Support Solr 8.3 (include JTS 1.16)

# v6.6.2-10 | 2018-12-03
* Add inDirectionPoints ValueSource

# v6.6.2-9 | 2018-10-16
* expose angle distance of first point

# v6.6.2-8 | 2018-03-28
* always use pointsMaxDistanceToRoute as corridor in direction, introduce alwaysCheckPointDistancePercent

# v6.6.2-7 | 2018-03-08
* check distance before angle calculation

# v6.6.2-6 | 2018-03-07
* fix single coordinates, use middle for distance too, fix bidirectional traffic

# v6.6.2-5 | 2018-03-06
* added parameter to check that a percentage of points is wihtin distance to the route

# v6.6.2-4 | 2018-02-22
* added test for boolean defaults
* introduce inPointDirection() to return boolean match

# v6.6.2-3 | 2018-02-21
* safeguard for empty linestrings in angle calculation

# v6.6.2-2 | 2018-02-20
* gracefully handle empty linestrings
* use last point for distance calculation too

# v6.6.2 | 2018-02-01
* use the minimal Solr version as version number for this project
* upgrade to Solr 6.2.2
* fix null cache hits
* Improved error handling and logging of these
* Also calculate geometry to enable geo spatial filter queries for surroundings of the linestring (contains, intersects, ...)
* use caffeine cache for all wkt linestring parsing

# v2.0.2 | 2017-06-08
* use custom valuesource to retrieve original linestring even for text fields
* added function to extract the position of the first query point on a stored geo linestring

# v2.0.1 | 2016-11-07
* Added missing license headers

# v2.0.0 | 2016-11-07
* initial release
