/*
 * Licensed to the Indoqa Software Design und Beratung GmbH (Indoqa) under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Indoqa licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indoqa.solr.spatial.corridor.direction;

import static com.indoqa.solr.spatial.corridor.TestGeoPoint.geo;
import static org.junit.Assert.assertEquals;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.indoqa.solr.spatial.corridor.TestGeoPoint;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.indoqa.solr.spatial.corridor.EmbeddedSolrInfrastructureRule;

public class TestDirection {

    private static final String SOLR_FIELD_ID = "id";
    private static final String SOLR_FIELD_GEO = "geo";

    private static final String DOCUMENT_ID_1 = "route-hietzing-stadtlau";
    private static final String DOCUMENT_ID_2 = "route-wien-marchegg";
    private static final String DOCUMENT_ID_3 = "route-a22-s3-stockerau";

    @ClassRule
    public static EmbeddedSolrInfrastructureRule infrastructureRule = new EmbeddedSolrInfrastructureRule();

    @Test
    public void exactMatchBackwards() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=90}pointsDirection(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.point", "POINT(16.891858798594868 48.22143885086025)");
        query.add("corridor.point", "POINT(16.89467367522139 48.22112128651476)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_2, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void exactMatchForwards() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=90}pointsDirection(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.pointsMaxDistanceToRoute", "0.01");

        String linestring = appendPointsAsCorridorPointGetLinestring(query,
                geo(16.31468318513228, 48.35434292589007),
                geo(16.315681432114854, 48.35333891624698)
                );
        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void exactMatchBackwardsBoolean() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=1} inPointsDirection(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.point", "POINT(16.891858798594868 48.22143885086025)");
        query.add("corridor.point", "POINT(16.89467367522139 48.22112128651476)");
        query.add("corridor.maxAngleDifference", "90");
        query.add("corridor.bidirectional", "false");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_2, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void exactMatchBidirectionalBoolean() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=1} inPointsDirection(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);

        String linestring = appendPointsAsCorridorPointGetLinestring(query,
                geo(16.31468318513228, 48.35434292589007),
                geo(16.315681432114854, 48.35333891624698));
        query.add("corridor.maxAngleDifference", "90");
        query.add("corridor.bidirectional", "true");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
    }

    @Test
    public void exactMatchForwardsBoolean() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=1}inPointsDirection(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        String linestring = appendPointsAsCorridorPointGetLinestring(query,
                geo(16.31468318513228, 48.35434292589007),
                geo(16.315681432114854, 48.35333891624698));
        query.add("corridor.maxAngleDifference", "90");
        query.add("corridor.bidirectional", "false");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void matchForwardsBoolean() throws IOException, SolrServerException {
        /*SolrQuery query = new SolrQuery("{!frange l=1}inPointsDirection(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        String linestring = appendPointsAsCorridorPointGetLinestring(query,
                geo(16.218802762437235, 48.37957817543284),
                geo(16.217043233323466, 48.38016606824774),
                geo(16.21519787352122, 48.38066844400059),
                geo(16.214060616898905, 48.38093922603797),
                geo(16.213245225358378, 48.3810674907108),
                geo(16.21242983381785, 48.381210006634916),
                geo(16.211550069260966, 48.38129551599785),
                geo(16.21043427031077, 48.38135252216),
                geo(16.20933188240565, 48.381372118013566),
                geo(16.20820803682841, 48.381363210808374),
                geo(16.20598180334605, 48.38123138398915),
                geo(16.203766298699747, 48.381099556828545),
                geo(16.201507878709208, 48.38103898747814),
                geo(16.19924945871867, 48.38101048422946),
                geo(16.19694812338389, 48.38108174232117),
                geo(16.192538571763407, 48.3814665342928));
        query.addFilterQuery(String.format("{!field f=geoGeom}Intersects(%s)", linestring));
        query.add("corridor.maxAngleDifference", "5");
        query.add("corridor.maxAngleDifferenceAdditionalPointsCheck", "80");
        query.add("corridor.pointsMaxDistanceToRoute", "0.01");
        query.add("corridor.bidirectional", "false");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_3, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));*/

        SolrQuery query = new SolrQuery("{!frange l=1}inPointsDirection(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        String linestring = appendPointsAsCorridorPointGetLinestring(query,
                geo(16.22989637892283, 48.37665998879075),
                geo(16.210563016343485, 48.3813489592768),
                geo(16.190306973863017, 48.38183350911145),
                geo(16.177904439378153, 48.38779024485453),
                geo(16.178333592820536, 48.38077889474244));
        query.addFilterQuery(String.format("{!field f=geoGeom}Intersects(%s)", linestring));
        query.add("corridor.maxAngleDifference", "5");
        query.add("corridor.maxAngleDifferenceAdditionalPointsCheck", "10");
        query.add("corridor.pointsMaxDistanceToRoute", "0.0001");
        query.add("corridor.percentageOfPointsWithinDistance", "100");
        query.add("corridor.alwaysCheckPointDistancePercent", "true");
        query.add("corridor.bidirectional", "false");

        // not all points are within route distance
        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(0, response.getResults().getNumFound());

        // only 4 / 5 percent are withinDistance
        query.set("corridor.percentageOfPointsWithinDistance", "90");
        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(0, response.getResults().getNumFound());

        query.set("corridor.percentageOfPointsWithinDistance", "80");
        query.set("corridor.pointsMaxDistanceToRoute", "0.01");
        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_3, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));

        query.set("corridor.pointsMaxDistanceToRoute", "0.000000001");
        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(0, response.getResults().getNumFound());
    }

    @Test
    public void booleanWithDefaults() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=1}inPointsDirection(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.point", "POINT(16.44175 48.2103)");
        query.add("corridor.point", "POINT(16.43768 48.20753)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(0, response.getResults().getNumFound());
    }

    @Test
    public void notEnoughPoints() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=90}pointsDirection(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.point", "POINT(16.41654 48.19311)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(3, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(DOCUMENT_ID_2, response.getResults().get(1).getFieldValue(SOLR_FIELD_ID));
        assertEquals(DOCUMENT_ID_3, response.getResults().get(2).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void testContains() throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery("*:*");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115321848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115322848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115323848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115324848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115325848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115326848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115328848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115329848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115320848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115321848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.20711532248342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327348342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327448342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327548342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327648342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327748342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327948342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327048342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327818342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327828342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327838342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207135327848342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207145327858342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207155327858342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207165327858342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207175327858342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207185327858342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207195327858342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207105327858342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207125327858342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207135327858342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207145327858342 48.384746675352))");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207155327858342 48.384746675352))");

        query.setRows(Integer.MAX_VALUE);

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());

        query = new SolrQuery("*:*");
        query.addFilterQuery("{!field f=geoGeom}Contains(POINT(16.207115327848342 48.384746675352))");
        query.setRows(Integer.MAX_VALUE);
        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());

    }

    @Test
    public void intersects() throws IOException, SolrServerException {
        SolrInputDocument solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, "intersects");
        solrDocument.addField(SOLR_FIELD_GEO, "LINESTRING(14.72702 48.11615,14.8369 48.13587)");

        infrastructureRule.getSolrClient().add(solrDocument);
        infrastructureRule.getSolrClient().commit(true, true);

        SolrQuery query = new SolrQuery("*:*");
        query.addFilterQuery("{!field f=geoGeom}Intersects(LINESTRING(14.72702 48.19311, 14.8369 48.13587))");

        query.setRows(Integer.MAX_VALUE);
        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals("intersects", solrDocument.getFieldValue(SOLR_FIELD_ID).toString());
    }

    @Test
    public void intersects2() throws IOException, SolrServerException {
        SolrInputDocument solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, "intersects2");
        solrDocument.addField(SOLR_FIELD_GEO, "LINESTRING(16.38271045 48.14339643,  16.38271065 48.14410595)");

        infrastructureRule.getSolrClient().add(solrDocument);
        infrastructureRule.getSolrClient().commit(true, true);

        SolrQuery query = new SolrQuery("*:*");
        query.addField("id");
        query.addField("pointsposition");
        query.addField("pointsposition:pointsPosition(geo, geoHash)");
        query.addField("pointsdirection:pointsDirection(geo, geoHash)");
        query.addFilterQuery("{!frange l=0 u=0.01 cache=false}pointsDistance(geo, geoHash)");
        query.addFilterQuery("{!frange l=0 u=90.0 cache=false}pointsDirection(geo, geoHash)");
         query.add("corridor.point", "POINT(16.38271045 48.14339643)");
         query.add("corridor.point", "POINT(16.38271065 48.14410595)");
         query.add("corridor.point", "POINT(16.38268436 48.14416652)");
         query.add("corridor.point", "POINT(16.38269672 48.14443787)");
         query.add("corridor.point", "POINT(16.38267045 48.14449773)");
         query.add("corridor.point", "POINT(16.38267664 48.14497039)");
         query.add("corridor.point", "POINT(16.38265036 48.14503062)");
         query.add("corridor.point", "POINT(16.38266341 48.14526763)");
         query.add("corridor.point", "POINT(16.38265021 48.14556425)");
         query.add("corridor.point", "POINT(16.38263701 48.14586088)");
         query.add("corridor.point", "POINT(16.38262381 48.1461575)");
         query.add("corridor.point", "POINT(16.38259756 48.14621712)");
         query.add("corridor.point", "POINT(16.38261119 48.14640734)");
         query.add("corridor.point", "POINT(16.38258499 48.14646639)");
         query.add("corridor.point", "POINT(16.38260274 48.14651863)");
         query.add("corridor.point", "POINT(16.38257666 48.14657542)");
         query.add("corridor.point", "POINT(16.38258383 48.14676431)");
         query.add("corridor.point", "POINT(16.38255775 48.14682103)");
         query.add("corridor.point", "POINT(16.38251804 48.14762496)");
         query.add("corridor.point", "POINT(16.38249195 48.14768172)");
         query.add("corridor.point", "POINT(16.38248834 48.14801218)");
         query.add("corridor.point", "POINT(16.38246226 48.14806892)");
         query.add("corridor.point", "POINT(16.38248166 48.14838898)");
         query.add("corridor.point", "POINT(16.38247474 48.14877103)");
         query.add("corridor.point", "POINT(16.38246781 48.14915308)");
         query.add("corridor.point", "POINT(16.38246089 48.14953513)");
         query.add("corridor.point", "POINT(16.38243456 48.14959712)");
         query.add("corridor.point", "POINT(16.38245619 48.14979294)");
         query.add("corridor.point", "POINT(16.38242986 48.14985492)");
         query.add("corridor.point", "POINT(16.38243916 48.15013669)");
         query.add("corridor.point", "POINT(16.38242224 48.15047761)");
         query.add("corridor.point", "POINT(16.38240531 48.15081854)");
         query.add("corridor.point", "POINT(16.38238839 48.15115946)");
         query.add("corridor.point", "POINT(16.38238844 48.15115824)");
         query.add("corridor.point", "POINT(16.38237395 48.15156069)");
         query.add("corridor.point", "POINT(16.38237145 48.15163397)");
         query.add("corridor.point", "POINT(16.38235697 48.15205845)");
         query.add("corridor.point", "POINT(16.38232785 48.15223582)");
         query.add("corridor.point", "POINT(16.38224998 48.15250296)");
         query.add("corridor.point", "POINT(16.38224955 48.15250417)");
         query.add("corridor.point", "POINT(16.38219629 48.15265162)");
         query.add("corridor.point", "POINT(16.38215142 48.15284092)");
         query.add("corridor.point", "POINT(16.38212696 48.15288622)");
         query.add("corridor.point", "POINT(16.38212774 48.1531097)");
         query.add("corridor.point", "POINT(16.38210176 48.15316522)");
         query.add("corridor.point", "POINT(16.38210174 48.15339764)");
         query.add("corridor.point", "POINT(16.38207574 48.15368559)");
         query.add("corridor.point", "POINT(16.38204974 48.15397354)");
         query.add("corridor.point", "POINT(16.38202374 48.15426149)");
         query.add("corridor.point", "POINT(16.38199776 48.15431702)");
         query.add("corridor.point", "POINT(16.38201386 48.15459709)");
         query.add("corridor.point", "POINT(16.38201384 48.15459774)");
         query.add("corridor.point", "POINT(16.38200368 48.15487091)");
         query.add("corridor.point", "POINT(16.38200572 48.1549708)");
         query.add("corridor.point", "POINT(16.38200578 48.15497353)");
         query.add("corridor.point", "POINT(16.38205396 48.15533472)");
         query.add("corridor.point", "POINT(16.3820704 48.1554461)");
         query.add("corridor.point", "POINT(16.38211166 48.15570793)");
         query.add("corridor.point", "POINT(16.38214687 48.15593141)");
         query.add("corridor.point", "POINT(16.38219812 48.15604942)");
         query.add("corridor.point", "POINT(16.38220515 48.15607497)");
         query.add("corridor.point", "POINT(16.38222187 48.1562129)");
         query.add("corridor.point", "POINT(16.38225027 48.1563377)");
         query.add("corridor.point", "POINT(16.38236753 48.1567184)");
         query.add("corridor.point", "POINT(16.38236754 48.15671842)");
         query.add("corridor.point", "POINT(16.38237714 48.15674814)");
         query.add("corridor.point", "POINT(16.38244898 48.15697164)");
         query.add("corridor.point", "POINT(16.38252717 48.15717077)");
         query.add("corridor.point", "POINT(16.38270832 48.15762191)");
         query.add("corridor.point", "POINT(16.38288948 48.15807305)");
         query.add("corridor.point", "POINT(16.38303331 48.15843124)");
         query.add("corridor.point", "POINT(16.38306822 48.15852873)");
         query.add("corridor.point", "POINT(16.38306818 48.15852863)");
         query.add("corridor.point", "POINT(16.3831645 48.15879632)");
         query.add("corridor.point", "POINT(16.38318319 48.15885038)");
         query.add("corridor.point", "POINT(16.38329445 48.15917225)");
         query.add("corridor.point", "POINT(16.3834057 48.15949413)");
         query.add("corridor.point", "POINT(16.38351696 48.159816)");
         query.add("corridor.point", "POINT(16.3834319 48.1598454)");
         query.add("corridor.point", "POINT(16.383858 48.1599123)");
         query.add("corridor.point", "POINT(16.3840103 48.159944)");
         query.add("corridor.point", "POINT(16.3842151 48.160019)");
         query.add("corridor.point", "POINT(16.3844333 48.1601118)");
         query.add("corridor.point", "POINT(16.3845602 48.1601114)");
         query.add("corridor.point", "POINT(16.3846354 48.1601127)");
         query.add("corridor.point", "POINT(16.384699 48.1601075)");
         query.add("corridor.point", "POINT(16.3847867 48.1600939)");
         query.add("corridor.point", "POINT(16.3848782 48.1600655)");
         query.add("corridor.point", "POINT(16.3849303 48.1600468)");
         query.add("corridor.point", "POINT(16.385007 48.1600535)");
         query.add("corridor.point", "POINT(16.38494336 48.15998986)");
         query.add("corridor.point", "POINT(16.38560008 48.15964061)");
         query.add("corridor.point", "POINT(16.38557827 48.15965665)");
         query.add("corridor.point", "POINT(16.38596188 48.15944709)");
         query.add("corridor.point", "POINT(16.3863243 48.15925324)");
         query.add("corridor.point", "POINT(16.38668673 48.15905939)");
//         query.add("corridor.point", "POINT(16.38704915 48.15886554)");
        query.addFilterQuery("{!field f=geoGeom}Intersects(LINESTRING(16.38271045 48.14339643,16.38271065 48.14410595,16.38268436 48.14416652,16.38269672 48.14443787,16.38267045 48.14449773,16.38267664 48.14497039,16.38265036 48.14503062,16.38266341 48.14526763,16.38265021 48.14556425,16.38263701 48.14586088,16.38262381 48.1461575,16.38259756 48.14621712,16.38261119 48.14640734,16.38258499 48.14646639,16.38260274 48.14651863,16.38257666 48.14657542,16.38258383 48.14676431,16.38255775 48.14682103,16.38251804 48.14762496,16.38249195 48.14768172,16.38248834 48.14801218,16.38246226 48.14806892,16.38248166 48.14838898,16.38247474 48.14877103,16.38246781 48.14915308,16.38246089 48.14953513,16.38243456 48.14959712,16.38245619 48.14979294,16.38242986 48.14985492,16.38243916 48.15013669,16.38242224 48.15047761,16.38240531 48.15081854,16.38238839 48.15115946,16.38238844 48.15115824,16.38237395 48.15156069,16.38237145 48.15163397, 16.38235697 48.15205845,16.38232785 48.15223582,16.38224998 48.15250296,16.38224955 48.15250417, 16.38219629 48.15265162,16.38215142 48.15284092,16.38212696 48.15288622,16.38212774 48.1531097, 16.38210176 48.15316522,16.38210174 48.15339764,16.38207574 48.15368559,16.38204974 48.15397354,16.38202374 48.15426149,16.38199776 48.15431702,16.38201386 48.15459709,16.38201384 48.15459774,16.38200368 48.15487091, 16.38200572 48.1549708,16.38200578 48.15497353,16.38205396 48.15533472,16.3820704 48.1554461, 16.38211166 48.15570793,16.38214687 48.15593141,16.38219812 48.15604942, 16.38220515 48.15607497,16.38222187 48.1562129,16.38225027 48.1563377,16.38236753 48.1567184,16.38236754 48.15671842,16.38237714 48.15674814,16.38244898 48.15697164,16.38252717 48.15717077,16.38270832 48.15762191,16.38288948 48.15807305, 16.38303331 48.15843124,16.38306822 48.15852873,16.38306818 48.15852863,16.3831645 48.15879632,16.38318319 48.15885038, 16.38329445 48.15917225,16.3834057 48.15949413, 16.38351696 48.159816,16.3834319 48.1598454,16.383858 48.1599123,16.3840103 48.159944,16.3842151 48.160019,16.3844333 48.1601118,16.3845602 48.1601114,16.3846354 48.1601127,16.384699 48.1601075,16.3847867 48.1600939,16.3848782 48.1600655,16.3849303 48.1600468,16.385007 48.1600535,16.38494336 48.15998986,16.38560008 48.15964061,16.38557827 48.15965665,16.38596188 48.15944709,16.3863243 48.15925324,16.38668673 48.15905939,16.38704915 48.15886554))");
        
        

        query.setRows(Integer.MAX_VALUE);
        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals("intersects2", solrDocument.getFieldValue(SOLR_FIELD_ID).toString());
    }

    @Test()
    public void intersects3() throws IOException, SolrServerException {
        SolrInputDocument solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, "intersects3");
        solrDocument.addField(SOLR_FIELD_GEO, "LINESTRING(9.747609347051139 47.409453865628436,9.747684123860608 47.4116993751579,9.745624582473056 47.41106082487543,9.743565041085617 47.41262945960319,9.743520075005335 47.41239211160078,9.743520075005335 47.41177743616075,9.743520075005335 47.41148531066436,9.743520075005335 47.41122361353119,9.743394169980434 47.40990293606566,9.743349203900152 47.409142162123985,9.74330423781987 47.40885002201029,9.74330423781987 47.408588311782445,9.74325927173959 47.408472671965164,9.743214305659308 47.40788229630981,9.743178332795082 47.40762058127347,9.743088400634406 47.40712149318118,9.73991345543277 47.40476713491394,9.749051112555094 47.40701973595363,9.747609347051139 47.409453865628436)");

        infrastructureRule.getSolrClient().add(solrDocument);
        infrastructureRule.getSolrClient().commit(true, true);

        SolrQuery query = new SolrQuery("*:*");
        query.setRows(Integer.MAX_VALUE);
        query.addField("id");
        query.addField("pointsposition");
        query.addField("pointsposition:pointsPosition(geo, geoHash)");
        query.addField("pointsdirection:pointsDirection(geo, geoHash)");
        query.addFilterQuery("{!frange l=0 u=0.01 cache=false}pointsDistance(geo, geoHash)");
        query.addFilterQuery("{!frange l=0 u=90.0 cache=false}pointsDirection(geo, geoHash)");
        query.addFilterQuery("{!field f=geoGeom}Intersects(LINESTRING(9.743735912190722 47.41315283859754,9.743735912190722 47.41312240972865,9.7435650410856 47.41262945960319,9.743520075005302 47.41239211160078,9.743520075005302 47.41177743616075,9.743520075005302 47.41148531066436,9.743520075005302 47.41122361353119,9.743394169980473 47.40990293606566,9.743349203900177 47.409142162123985,9.743304237819881 47.40885002201029,9.743304237819881 47.408588311782445,9.743259271739586 47.408472671965164,9.74321430565929 47.40788229630981,9.743178332795052 47.40762058127347,9.743088400634461 47.40712149318118,9.742917529529336 47.40682934185967,9.74265672626362 47.40648241006073,9.742566794103027 47.406360679063425))");
        query.add("corridor.point", "POINT(9.743735912190722 47.41315283859754)");
        query.add("corridor.point", "POINT(9.743735912190722 47.41312240972865)");
        query.add("corridor.point", "POINT(9.7435650410856 47.41262945960319)");
        query.add("corridor.point", "POINT(9.743520075005302 47.41239211160078)");
        query.add("corridor.point", "POINT(9.743520075005302 47.41177743616075)");
        query.add("corridor.point", "POINT(9.743520075005302 47.41148531066436)");
        query.add("corridor.point", "POINT(9.743520075005302 47.41122361353119)");
        query.add("corridor.point", "POINT(9.743394169980473 47.40990293606566)");
        query.add("corridor.point", "POINT(9.743349203900177 47.409142162123985)");
        query.add("corridor.point", "POINT(9.743304237819881 47.40885002201029)");
        query.add("corridor.point", "POINT(9.743304237819881 47.408588311782445)");
        query.add("corridor.point", "POINT(9.743259271739586 47.408472671965164)");
        query.add("corridor.point", "POINT(9.74321430565929 47.40788229630981)");
        query.add("corridor.point", "POINT(9.743178332795052 47.40762058127347)");
        query.add("corridor.point", "POINT(9.743088400634461 47.40712149318118)");
        query.add("corridor.point", "POINT(9.742917529529336 47.40682934185967)");
        query.add("corridor.point", "POINT(9.74265672626362 47.40648241006073)");
        query.add("corridor.point", "POINT(9.742566794103027 47.406360679063425)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals("intersects3", solrDocument.getFieldValue(SOLR_FIELD_ID).toString());
    }

    @Before
    public void setup() throws Exception {
        SolrInputDocument solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_1);
        solrDocument.addField(SOLR_FIELD_GEO,
            "LINESTRING(16.20750203613889 48.38486015504406, 16.207115327848342 48.384746675352, 16.206989422823515 48.384686949096654, 16.20573037257523 48.384400262095404, 16.205514535389806 48.384400262095404, 16.205559501470105 48.384316644749184, 16.205685406494933 48.384316644749184, 16.20582030473582 48.384197191159146, 16.20724123287317 48.38362381002455, 16.207070361768046 48.38339684487434, 16.206989422823515 48.38319376993471, 16.206854524582628 48.382990694184855, 16.206728619557797 48.3826502418453, 16.206638687397206 48.38241729945883, 16.2066836534775 48.38221422061111, 16.206854524582628 48.38201711389583, 16.207070361768046 48.38187376307804, 16.20771787332431 48.381784168611915, 16.208194513775446 48.381784168611915, 16.209318665782845 48.38176027672766, 16.209498530104028 48.38176027672766, 16.209624435128855 48.381640817138106, 16.20966940120915 48.38155719525862, 16.209795306233982 48.38127049062888, 16.209885238394573 48.38106740720579, 16.21014604166029 48.38046412520009, 16.210191007740587 48.38038050138755, 16.210101075579995 48.38035063570694, 16.210011143419404 48.38020728019605, 16.21027194668512 48.38011768279672, 16.210406844926005 48.380237145960784, 16.212565216780213 48.38026103855994, 16.212870986126223 48.38043425956854, 16.212951925070755 48.38055372198964, 16.21313178939194 48.38063734551762, 16.213779300948204 48.38078069981737, 16.214291914263576 48.38075083437156, 16.215811767777577 48.38038050138755, 16.216288408228717 48.38026103855994, 16.21689095370468 48.38009379013027, 16.222907415248276 48.37814052671679, 16.22320419137823 48.37805092567985, 16.225326590368198 48.3773878731052, 16.226792484585847 48.37704738329207, 16.22774576548812 48.37681441526576, 16.23082144538036 48.37647392161732, 16.234364772507682 48.37647392161732, 16.234751480798224 48.376497815982745, 16.235533890595377 48.376497815982745, 16.236055497126806 48.376497815982745, 16.23726058807874 48.37652768392374, 16.237782194610173 48.37652768392374, 16.23860057727156 48.376557551847206, 16.239338020988413 48.376557551847206, 16.2416762571638 48.37664118193971, 16.243357988566867 48.376671049796585, 16.24487784208087 48.376671049796585, 16.245741190822553 48.37664118193971, 16.246523600619703 48.37661728764159, 16.248726938554203 48.37647392161732, 16.2487719046345 48.37647392161732, 16.25084034432811 48.37627081905853, 16.259752621442768 48.37495063267683, 16.270742331467094 48.37331379297968, 16.27165064628907 48.37316444367236, 16.281723048275364 48.37161715907045, 16.28683119499698 48.3708405120111, 16.28739776760871 48.37075089812662, 16.290680291470313 48.369920468628585, 16.292712758299686 48.36920354018411, 16.29379194422679 48.36879727625199, 16.29656185477302 48.367506769321885, 16.29725433240958 48.36710646654569, 16.299592568584966 48.365612771696206, 16.3023175130509 48.363658952707894, 16.302488384156025 48.36353945064335, 16.303702468324015 48.36267902750196, 16.30387333942914 48.36253562223199, 16.308369947458733 48.359374628547336, 16.308756655749278 48.35908779899783, 16.310015705997564 48.358197421983796, 16.31118482408526 48.35739069111777, 16.312479847197782 48.356386741532745, 16.31261474543867 48.35624331854845, 16.313559033124886 48.355436556738866, 16.31373889744607 48.35526325068325, 16.314431375082627 48.354605877527284, 16.31468318513228 48.35434292589007, 16.315681432114854 48.35333891624698, 16.317066387387968 48.351611734321786, 16.318622213766208 48.34919716734547, 16.31879308487133 48.348940164618725, 16.31922475924217 48.34816317174839, 16.319746365773607 48.347188925487316, 16.320178040144445 48.34644179150383, 16.320438843410162 48.34598155151842, 16.321086354966425 48.344798058185965, 16.3220396358687 48.343303709167444, 16.322687147424958 48.34244294424703, 16.32333465898122 48.34163596393267, 16.324332905963793 48.340344768856276, 16.325546990131784 48.33902365115468, 16.32567289515661 48.33888017929807, 16.327318653695443 48.337266093088395, 16.33078104187823 48.334336694994505, 16.330817014742465 48.334306802309705, 16.33427940292525 48.33154464261103, 16.335493487093242 48.330510288893656, 16.335925161464083 48.33013959054691, 16.336662605180937 48.32941612310877, 16.337741791108037 48.32835183004044, 16.338955875276028 48.32709019544213, 16.338991848140267 48.32703040155855, 16.33925265140598 48.32674338994155, 16.34137505039595 48.323956901535084, 16.34314671395961 48.321164280812695, 16.34379422551587 48.31972904152074, 16.344441737072135 48.31834758556425, 16.344702540337853 48.31776748251658, 16.34522414686928 48.31575800240464, 16.345350051894112 48.31492069566503, 16.345350051894112 48.313108477597765, 16.345269112949577 48.312420655210296, 16.344963343603567 48.30943001529935, 16.344963343603567 48.30709719449494, 16.345269112949577 48.30574530594495, 16.345484950135 48.30511122198763, 16.345655821240122 48.30456686060788, 16.346564136062103 48.30283802608004, 16.347643321989203 48.30145611277454, 16.34781419309433 48.30119886937099, 16.349891626004002 48.29898532605081, 16.35088987298657 48.29798022604757, 16.3510157780114 48.297806724639486, 16.35275146871082 48.2959340684675, 16.357077205635292 48.29049519835633, 16.357940554376974 48.289430093254616, 16.358633032013532 48.28862227401008, 16.361052207133454 48.28586362456362, 16.36209542019632 48.28470865765978, 16.362913802857705 48.283816981408066, 16.363480375469432 48.28318262506824, 16.36429875813082 48.282237060428805, 16.364514595316237 48.281973735514505, 16.364820364662254 48.28166253159293, 16.36728450586247 48.2786641043719, 16.368408657869868 48.27728153673101, 16.369577775957563 48.27587499013181, 16.3696137488218 48.27581513622608, 16.37347183851119 48.27103856849187, 16.374416126197403 48.26985932202068, 16.37472189554342 48.26948219684798, 16.37536940709968 48.26867406210637, 16.37545933926027 48.2685902548092, 16.37545933926027 48.26856032359835, 16.375504305340566 48.268506447374605, 16.37679932845309 48.2669200664409, 16.377752609355362 48.26571079205148, 16.379092598548183 48.264070443439365, 16.37991098120957 48.26303472176479, 16.38055849276583 48.262226485070464, 16.380603458846124 48.26216661517731, 16.381385868643275 48.26125059706821, 16.38289672894122 48.25940653697426, 16.384668392504878 48.25735883386326, 16.385495768382324 48.25646668024886, 16.385927442753164 48.25595173886196, 16.38774407239712 48.25393383716681, 16.387914943502246 48.25370030630983, 16.38808581460737 48.253532642472685, 16.388256685712495 48.25338893017471, 16.388346617873086 48.253269169617845, 16.3886523872191 48.25292785049187, 16.391593168870454 48.24984389896133, 16.391674107814985 48.24973011855129, 16.39384147288525 48.2476820278811, 16.394794753787526 48.24675976144002, 16.39496562489265 48.24661603010714, 16.39504656383718 48.2465321866431, 16.396044810819753 48.245609899466984, 16.39617970906064 48.245466164902666, 16.397123996746853 48.24457380377756, 16.397519698253458 48.24419649204158, 16.397771508303116 48.243968905839246, 16.39803231156883 48.243711372862656, 16.400973093220184 48.241141961235925, 16.402097245227583 48.24019561860591, 16.40257388567872 48.23976436805647, 16.402789722864142 48.23959066889147, 16.403095492210152 48.23930316552932, 16.404606352508097 48.23794948215251, 16.405082992959233 48.23751222278879, 16.40564956557096 48.236997090524454, 16.40634204320752 48.236332202841304, 16.406422982152055 48.23627829265047, 16.406818683658656 48.235960820374366, 16.407682032400338 48.235122204887524, 16.40867128616685 48.23425961461805, 16.409930336415137 48.23322328902265, 16.41144119671308 48.23198326491877, 16.41243944369565 48.23117453735702, 16.412700246961364 48.2309468932196, 16.413347758517627 48.23045565768318, 16.413563595703046 48.23028192691352, 16.413608561783345 48.230251973272914, 16.413734466808172 48.23013814927879, 16.416504377354403 48.22800540026002, 16.41736772609608 48.227316431443164, 16.417502624336972 48.22720260091976, 16.41841093915895 48.22650763012771, 16.420659243173745 48.2248061100776, 16.420875080359167 48.22466231705872, 16.421954266286267 48.22385946527914, 16.42329425547909 48.22293676932309, 16.42415760422077 48.22230165724742, 16.424553305727375 48.22201405673423, 16.426621745420988 48.22054607060885, 16.430174065764366 48.218269110885544, 16.430731645160037 48.21789160514454, 16.43146908887689 48.21734631415985, 16.43254827480399 48.216537355296715, 16.43553402253564 48.21446396533048, 16.436703140623337 48.213654960925055, 16.43708984891388 48.21342723886116, 16.437746353686205 48.213019733692754, 16.43912231574326 48.21201294231416, 16.4399496916207 48.21126382874234, 16.440552237096668 48.21065853697166, 16.44076807428209 48.210227037479896, 16.440687135337555 48.21002327256715, 16.44064216925726 48.20984947244205, 16.43985975946011 48.2091602592421, 16.439733854435282 48.209070361184345, 16.439383119008973 48.20881265254422, 16.43899641071843 48.208554942607236, 16.43873560745271 48.208381137498904, 16.437827292630736 48.20774584484326, 16.437134814994177 48.207284353727324, 16.43553402253564 48.206193540013544, 16.434544768769133 48.20549828398546, 16.434409870528242 48.20541437313692, 16.434283965503415 48.20532446850379, 16.43380732505228 48.20500680420285, 16.43354652178656 48.20480901222775, 16.432728139125174 48.204257588143, 16.430434869030083 48.202705177588676, 16.429481588127807 48.20206981452704, 16.429094879837265 48.201806076602004, 16.42857327330583 48.20143444358466, 16.427709924564148 48.20088897733775, 16.42571343059901 48.19950430616394, 16.424553305727375 48.1986950654227, 16.423770895930225 48.19817354794938, 16.422988486133075 48.197628046980576, 16.422907547188544 48.197568101464974, 16.422340974576812 48.19719643770264, 16.421864334125676 48.19684874980665, 16.42143265975484 48.19659097967103, 16.421261788649712 48.19644710740349, 16.420353473827735 48.19584164051667, 16.419750928351768 48.19538004214274, 16.41806020373264 48.194199050749305, 16.41707094996613 48.193503631916215, 16.416765180620118 48.19330579552376, 16.41663028237923 48.193245844951, 16.416504377354403 48.1931619140313, 16.41628854016898 48.192988056688876, 16.41607270298356 48.19284417430422, 16.41585686579814 48.19270029151554, 16.415685994693014 48.192580388883016, 16.41464278163015 48.19186096719603, 16.41408520223448 48.191459285693426, 16.413563595703046 48.19114153540864, 16.412700246961364 48.19056598270639, 16.41208870826934 48.19015829563121, 16.411486162793377 48.1897266234282, 16.410362010785978 48.18894720607416, 16.409066987673455 48.18805385929661, 16.405865402756383 48.18577545473249, 16.405478694465838 48.1854336853119, 16.405172925119825 48.185085917597235, 16.4050020540147 48.184852072462384, 16.40482218969352 48.18456426160099, 16.404525413563565 48.18404859643268, 16.404309576378143 48.183640857510596, 16.40412971205696 48.183329054969924, 16.40404877311243 48.18309520181972, 16.40391387487154 48.18274741823855, 16.40378796984671 48.182429613937295, 16.403221397234983 48.1815961178249, 16.402618851759016 48.18113439112366, 16.40257388567872 48.18113439112366, 16.401665570856743 48.180930510477424, 16.40136879472679 48.180930510477424, 16.40058638492964 48.18110440872654, 16.400415513824516 48.18116437350325, 16.39967807010766 48.181799995823916, 16.39946223292224 48.18211780402869, 16.399030558551402 48.18272343307701, 16.39894062639081 48.18283736249437, 16.398859687446276 48.18298127297542, 16.39850895201997 48.18355691085931, 16.397996338704594 48.184102561635214, 16.396998091722022 48.184942013025115, 16.396692322376012 48.18531376567683, 16.396215681924875 48.18589537328734, 16.395873939714626 48.18620716021685, 16.395352333183194 48.18661487872436, 16.394102276150967 48.18733437406076, 16.393319866353817 48.18765214795198, 16.392240680426717 48.18793994147396, 16.391593168870454 48.18802388094679, 16.39128739952444 48.18805385929661, 16.390900691233895 48.1880838376289, 16.390549955807586 48.18811381594367, 16.390208213597337 48.18814379424091, 16.3897765392265 48.18817377252059, 16.3886523872191 48.188227733379826, 16.38743830305111 48.18828768982349, 16.38665589325396 48.188371628726756, 16.386485022148836 48.188371628726756, 16.385145032956014 48.1884615631134, 16.384236718134037 48.188485545589856, 16.382375122409787 48.188485545589856, 16.38186250909441 48.188485545589856, 16.381601705828697 48.1884615631134, 16.381295936482683 48.188401606873164, 16.38120600432209 48.188401606873164, 16.380864262111842 48.188317668018996, 16.380603458846124 48.188227733379826, 16.37991098120957 48.18805385929661, 16.379263469653306 48.18787998462349, 16.37913756462848 48.18785600186366, 16.378615958097047 48.18771210506901, 16.37727596890423 48.18727441650185, 16.37735690784876 48.187190475801536, 16.37792348046049 48.186327077761625, 16.378139317645907 48.18594933654548, 16.378265222670738 48.1855476087053, 16.3782292498065 48.185517628889464, 16.378139317645907 48.18519984176365, 16.37809435156561 48.18511589766561, 16.37809435156561 48.184995977287016, 16.378049385485316 48.1848820526675, 16.37792348046049 48.18451029688458, 16.3777885822196 48.184162522904714, 16.37766267719477 48.184222484104076, 16.37679932845309 48.18430642966532)");
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_2);
        solrDocument.addField(SOLR_FIELD_GEO,
            "LINESTRING(16.38298666110181 48.23391217831798, 16.383499274417186 48.23351082655188, 16.38402088094862 48.233019615628535, 16.38518999903631 48.23198326491877, 16.38518999903631 48.23198326491877, 16.386269184963414 48.23100679966973, 16.386871730439378 48.230515564708405, 16.387132533705095 48.230689294684915, 16.387699106316823 48.2309468932196, 16.387825011341654 48.23103076223017, 16.388607421138804 48.2314381240404, 16.389254932695064 48.23177958658882, 16.38964164098561 48.23198326491877, 16.389902444251327 48.23212703736942, 16.39007331535645 48.23221689494597, 16.39050498972729 48.23244453343363, 16.391332365604736 48.232875845685726, 16.391593168870454 48.233019615628535, 16.39171907389528 48.23307951965212, 16.392546449772727 48.23351082655188, 16.39353570353924 48.23403198420518, 16.394138249015203 48.23437342944475, 16.39396737791008 48.23457709745009, 16.393319866353817 48.23515215567731, 16.392150748266122 48.23618844220615, 16.39206980932159 48.236505913069855, 16.392150748266122 48.236619722899796, 16.39271732087785 48.23705698989146, 16.39448898444151 48.237973441461705, 16.395658102529204 48.23855445127434, 16.396215681924875 48.23884195884421, 16.396692322376012 48.23903961936124, 16.397339833932275 48.23930316552932, 16.397996338704594 48.23956072069997, 16.39838304699514 48.23962061706545, 16.399201429656525 48.23976436805647, 16.40028061558363 48.23978832651569, 16.40080222211506 48.23978832651569, 16.402277109548766 48.23959066889147, 16.40257388567872 48.23944691741237, 16.40257388567872 48.23944691741237, 16.402753749999903 48.23930316552932, 16.40300556004956 48.239129464798346, 16.40369803768612 48.23852450247621, 16.404606352508097 48.23794948215251, 16.405082992959233 48.23751222278879, 16.40564956557096 48.236997090524454, 16.40634204320752 48.236332202841304, 16.406422982152055 48.23627829265047, 16.406818683658656 48.235960820374366, 16.407682032400338 48.235122204887524, 16.40867128616685 48.23425961461805, 16.409930336415137 48.23322328902265, 16.41144119671308 48.23198326491877, 16.41243944369565 48.231174537357, 16.412700246961364 48.2309468932196, 16.413347758517627 48.23045565768321, 16.413563595703046 48.23028192691352, 16.413608561783345 48.230251973272914, 16.413734466808172 48.23013814927879, 16.416504377354403 48.22800540026002, 16.41736772609608 48.227316431443136, 16.417502624336972 48.22720260091976, 16.41841093915895 48.22650763012771, 16.420659243173745 48.2248061100776, 16.420875080359167 48.22466231705875, 16.421954266286267 48.22385946527914, 16.42329425547909 48.22293676932309, 16.42415760422077 48.22230165724742, 16.424553305727375 48.22201405673423, 16.426621745420988 48.22054607060885, 16.430174065764366 48.218269110885544, 16.430731645160037 48.21789160514454, 16.43146908887689 48.21734631415985, 16.43254827480399 48.216537355296715, 16.43553402253564 48.21446396533048, 16.436703140623337 48.213654960925055, 16.43708984891388 48.21342723886116, 16.437746353686205 48.213019733692754, 16.43912231574326 48.21201294231416, 16.439688888354986 48.21155148965004, 16.439904725540405 48.211377694711516, 16.44085800644268 48.21077240428675, 16.44193719236978 48.21002327256715, 16.442242961715795 48.20982549996473, 16.44297141221659 48.20956180197336, 16.443537984828318 48.20967567172712, 16.44410455744005 48.21002327256715, 16.44483300794084 48.21048473900256, 16.445138777286854 48.21074243922838, 16.445831254923412 48.21117992733986, 16.44652373255997 48.211665354979566, 16.446910440850512 48.21198297798169, 16.447297149141058 48.21239049140088, 16.44738708130165 48.21255829010144, 16.447692850647663 48.21342723886116, 16.44773781672796 48.21382874813458, 16.447647884567367 48.21451789850304, 16.447557952406775 48.21483550381128, 16.44717124411623 48.21642350080102, 16.44717124411623 48.21696880161438, 16.44717124411623 48.21734631415985, 16.44717124411623 48.21792156601933, 16.447216210196526 48.219131970697894, 16.44726117627682 48.22048615191362, 16.447647884567367 48.22140889204388, 16.448079558938208 48.222331615541286, 16.44838532828422 48.222966727245236, 16.44868210441417 48.22365575461566, 16.448817002655062 48.223943345904935, 16.448942907679893 48.22429084989026, 16.449248677025903 48.22466231705875, 16.449680351396744 48.22524347802694, 16.451065306669857 48.226537585959925, 16.4514969810407 48.22711273453825, 16.451883689331243 48.22777774202483, 16.45214449259696 48.22838283141872, 16.452792004153224 48.229880546672966, 16.45326864460436 48.2309468932196, 16.453655352894902 48.23189939746932, 16.453655352894902 48.23198326491877, 16.453871190080324 48.232360666740185, 16.454743532038066 48.23368454635944, 16.455436009674624 48.23440338067284, 16.455948622989997 48.234864627371586, 16.456299358416306 48.23515215567731, 16.456856937811974 48.235553494568954, 16.457504449368237 48.23627829265047, 16.457765252633955 48.2365658130119, 16.458071021979965 48.23708094961863, 16.458286859165387 48.237398414944195, 16.45945597725308 48.23930316552932, 16.459932617704215 48.24005186882671, 16.46010348880934 48.240309420227966, 16.460229393834172 48.24053702271259, 16.460274359914468 48.24062686551988, 16.460921871470727 48.24154924254882, 16.461920118453296 48.24272914216485, 16.462522663929263 48.24333405476473, 16.463565876992128 48.24425638297918, 16.4652476083952 48.245669788749574, 16.466119950352937 48.246358510458904, 16.46641672648289 48.24661603010714, 16.467244102360336 48.24728078413565, 16.46823335612685 48.24811321168054, 16.468494159392563 48.248316824988855, 16.46870999657798 48.24849049452296, 16.46983414858538 48.24967023402328, 16.471003266673076 48.250796051416664, 16.471479907124213 48.25137092733461, 16.471785676470226 48.25174219793024, 16.4722982897856 48.25177213898985, 16.473125665663044 48.25180208003192, 16.477316504346625 48.251454762866025, 16.479483869416892 48.251454762866025, 16.480176347053447 48.251454762866025, 16.480347218158574 48.251454762866025, 16.48233471890765 48.25148470409394, 16.48302719654421 48.251454762866025, 16.483243033729632 48.251454762866025, 16.483935511366187 48.251454762866025, 16.484717921163337 48.251424821620596, 16.484843826188165 48.251424821620596, 16.485104629453883 48.251424821620596, 16.486183815380983 48.25140086861162, 16.486318713621873 48.25140086861162, 16.486615489751827 48.25140086861162, 16.48856701763667 48.25137092733461, 16.488737888741795 48.25137092733461, 16.488908759846918 48.251340986040105, 16.489646203563773 48.25125715032216, 16.49046458622516 48.25122720896101, 16.492461080190296 48.25111343162878, 16.492631951295422 48.25111343162878, 16.492802822400545 48.251137384772385, 16.4935402661174 48.251137384772385, 16.493666171142227 48.251137384772385, 16.493801069383117 48.251137384772385, 16.49609433947821 48.25116732618612, 16.497254464349844 48.251197267582334, 16.497605199776153 48.25122720896101, 16.497946941986402 48.25122720896101, 16.500114307056666 48.25122720896101, 16.500240212081493 48.25122720896101, 16.500420076402676 48.25122720896101, 16.50114852690347 48.25122720896101, 16.501454296249484 48.25122720896101, 16.501454296249484 48.25122720896101, 16.50184100454003 48.251197267582334, 16.506553449755042 48.25108349018348, 16.50716498844707 48.25108349018348, 16.507380825632488 48.25105354872066, 16.510843213815278 48.2508499463085, 16.511535691451833 48.2508499463085, 16.51244400627381 48.25082000470894, 16.51399983265205 48.250796051416664, 16.514557412047722 48.25073616813686, 16.514953113554323 48.25067628478695, 16.515294855764573 48.25059244797927, 16.516374041691677 48.25041878558327, 16.517066519328235 48.25041878558327, 16.518757243947363 48.25044872741774, 16.52342472308208 48.25001756330964, 16.524252098959522 48.24992773699622, 16.524593841169775 48.24990378328599, 16.524764712274898 48.24990378328599, 16.525933830362593 48.249760060789, 16.52710294845029 48.24958639556622, 16.528577835883993 48.249442672177246, 16.528703740908824 48.249412729753686, 16.528874612013947 48.249382787312626, 16.53052037055278 48.24926900587664, 16.53064627557761 48.2492390633514, 16.530826139898792 48.2492390633514, 16.53147365145505 48.249179178248376, 16.531554590399583 48.249179178248376, 16.531770427585005 48.249179178248376, 16.531905325825893 48.249179178248376, 16.532337000196733 48.249179178248376, 16.532552837382156 48.249179178248376, 16.532678742406983 48.249179178248376, 16.532984511752996 48.249179178248376, 16.533245315018714 48.249155224187504, 16.53372195546985 48.24912528159565, 16.533982758735565 48.24912528159565, 16.534153629840688 48.24912528159565, 16.534414433106406 48.24906539635938, 16.534675236372124 48.2490354537149, 16.534846107477247 48.24900551105294, 16.534972012502077 48.24898155691074, 16.53570945621893 48.24880788904424, 16.53584435445982 48.24874800343626, 16.5377868891286 48.24828688190611, 16.53921681048201 48.24802937067092, 16.5411143790705 48.24814315486496, 16.54124927731139 48.24820304118121, 16.541680951682228 48.24834676805404, 16.544747638358412 48.249179178248376, 16.545395149914672 48.24926900587664, 16.54608762755123 48.249382787312626, 16.546483329057835 48.249382787312626, 16.54687003734838 48.24935284485401, 16.547823318250654 48.24935284485401, 16.550026656185157 48.24926900587664, 16.55561144335791 48.248777946249014, 16.556384859939 48.24869410632916, 16.55850725892897 48.248460551541925, 16.560108051387505 48.24828688190611, 16.560584691838642 48.24823298431301, 16.5612771694752 48.24814315486496, 16.561708843846038 48.2480892571204, 16.563093799119155 48.247711971317976, 16.563561446354232 48.24756824266112, 16.56491042876311 48.24690349236897, 16.565378075998186 48.246645974168146, 16.565512974239077 48.24661603010714, 16.56689792951219 48.24613093387655, 16.567374569963327 48.246071045133945, 16.567545441068454 48.24604110073636, 16.567932149359 48.2459572563298, 16.568759525236445 48.245609899466984, 16.570612127744635 48.24451391321166, 16.57299533000032 48.24247160294623, 16.575243634015116 48.24056697033255, 16.575720274466253 48.24016567076855, 16.576673555368526 48.23932712420454, 16.57671852144882 48.23930316552932, 16.57732106692479 48.23875211290219, 16.577923612400756 48.23823699312225, 16.578319313907357 48.23800339058242, 16.578355286771593 48.237973441461705, 16.58030681465644 48.2372007480861, 16.581772708874087 48.236619722899796, 16.582941826961783 48.23615849202299, 16.584758456605737 48.23558344510628, 16.58497429379116 48.235409731745754, 16.585019259871455 48.23538577123596, 16.585190130976578 48.23529591922425, 16.5853610020817 48.23529591922425, 16.58562180534742 48.23529591922425, 16.585882608613133 48.23521205720429, 16.589344996795923 48.23419971197601, 16.5902533116179 48.233942129816086, 16.59756479627402 48.23284589356346, 16.59933645983768 48.232558352297694, 16.60033470682025 48.232360666740185, 16.602277241489034 48.23198326491877, 16.60396796610816 48.231665765992524, 16.60573962967182 48.23134826509632, 16.609327922879437 48.2309468932196, 16.61131542362852 48.23059943442587, 16.613698625884204 48.230168102985985, 16.616117801004123 48.22970681395083, 16.61858194220434 48.22924552075759, 16.62005682963805 48.22927547498728, 16.620965144460023 48.22930542919945, 16.622305133652844 48.22919160309998, 16.62645999947219 48.22875426888902, 16.63285417609027 48.22771783179463, 16.63328585046111 48.227687876653206, 16.635884889902215 48.22752012753733, 16.636145693167933 48.22746021700554, 16.636451462513946 48.22737634214325, 16.639086474819287 48.22627996522998, 16.641982290390345 48.22584859738336, 16.64358308284888 48.22575872862437, 16.64427556048544 48.22575872862437, 16.6449230720417 48.22575872862437, 16.64989632052243 48.22556101679912, 16.653925281316948 48.22518356483014, 16.655265270509766 48.22497985943643, 16.65720780517855 48.22466231705875, 16.658592760451665 48.22443464395269, 16.661362670997892 48.22385946527914, 16.664042649383532 48.22273305498744, 16.6650408963661 48.222475415107745, 16.66624598731803 48.222361573817615, 16.66772087475174 48.22221777393131, 16.668458318468595 48.22218781557084, 16.672999892578485 48.22178038012745, 16.6765881857861 48.22140889204388, 16.67723569734236 48.22140889204388, 16.67723569734236 48.22140889204388, 16.68048224833973 48.22161261164936, 16.680743051605443 48.22166653729157, 16.681084793815693 48.22178038012745, 16.68156143426683 48.221930172946834, 16.683288131750192 48.22250537329993, 16.684502215918183 48.222876853426186, 16.684628120943014 48.22293676932309, 16.68666058777239 48.22348200076205, 16.686795486013278 48.22348200076205, 16.687487963649833 48.22336816171054, 16.687703800835255 48.223308246318574, 16.68800957018127 48.22316444909175, 16.688135475206096 48.223140482847995, 16.688135475206096 48.222966727245236, 16.688225407366687 48.222966727245236, 16.68839627847181 48.222966727245236, 16.688441244552106 48.22308056718954, 16.688351312391514 48.223140482847995, 16.688441244552106 48.223194406880644, 16.688567149576937 48.223338204023314, 16.689043790028073 48.22368571211703, 16.689475464398914 48.224057183677225, 16.690078009874878 48.22451852363592, 16.690473711381486 48.22466231705875, 16.691247127962573 48.22500981616282, 16.69284792042111 48.22556101679912, 16.69427784177452 48.22590251856302, 16.694448712879645 48.225932474749385, 16.69458361112053 48.22596243091824, 16.695878634233054 48.22616613240134, 16.69595957317759 48.22627996522998, 16.696184403579068 48.22625000924701, 16.69704775232075 48.22645370958556, 16.698126938247853 48.22645370958556, 16.69894532090924 48.22636384188906, 16.699206124174953 48.22636384188906, 16.701373489245217 48.22636384188906, 16.70656257491137 48.226795205393124, 16.709935030933565 48.22720260091976, 16.710195834199283 48.22720260091976, 16.711275020126383 48.22720260091976, 16.71179662665782 48.22720260091976, 16.71641913971224 48.226567541774585, 16.71784906106565 48.22642375370427, 16.718370667597082 48.22650763012771, 16.72018729724104 48.226795205393124, 16.720789842717004 48.226825161057, 16.72117655100755 48.2267712408494, 16.722129831909825 48.22671132944097, 16.722957207787267 48.226537585959925, 16.725978928383157 48.22556101679912, 16.729099574355693 48.22466231705875, 16.73113204118507 48.22408714094364, 16.732166261031878 48.22359583956036, 16.732472030377888 48.22345204314144, 16.73411778891672 48.22273305498744, 16.735709588159196 48.222361573817615, 16.739046071317155 48.22207397364105, 16.741681083622495 48.221468809659065, 16.743021072815313 48.22120517162768, 16.7431559710562 48.22120517162768, 16.745620112256418 48.221031409455534, 16.748174185617227 48.22115124549944, 16.749334310488862 48.221295048381826, 16.751978316010266 48.22161261164936, 16.75413668786447 48.22187025587168, 16.760845627044624 48.22267913046876, 16.76438895417194 48.222792971052655, 16.765126397888796 48.22282292905897, 16.768112145620446 48.22290681138338, 16.76863375215188 48.222966727245236, 16.77083709008638 48.22336816171054, 16.774812091584543 48.22408714094364, 16.776116107913126 48.22429084989026, 16.77680858554968 48.22437472980911, 16.778841052379057 48.22466231705875, 16.783724368699197 48.22532735638464, 16.785846767689165 48.22535731290764, 16.78709682472139 48.22524347802694, 16.78822097672879 48.2251236515632, 16.79034337571876 48.22466231705875, 16.79233087646784 48.22423093557836, 16.79708828776315 48.22325432240586, 16.798086534745718 48.222966727245236, 16.79994813046997 48.22238554042605, 16.80236730558989 48.2213250072648, 16.80461560960469 48.22071983442845, 16.806342307088055 48.2205999973746, 16.809067251553987 48.22094752405787, 16.810668044012523 48.22115124549944, 16.812268836471056 48.221265089481314, 16.812403734711943 48.221265089481314, 16.812484673656478 48.22123513056325, 16.812745476922196 48.22120517162768, 16.814391235461027 48.2205999973746, 16.81542545530783 48.22034234675889, 16.816036993999855 48.220887605832516, 16.816594573395527 48.221265089481314, 16.816945308821836 48.221468809659065, 16.8175478542978 48.221810338726385, 16.817808657563518 48.22195413975726, 16.81833026409495 48.22230165724742, 16.819238578916927 48.223140482847995, 16.820011995498017 48.22385946527914, 16.820056961578313 48.22388942266128, 16.82062353419004 48.2242009783961, 16.821612787956553 48.22466231705875, 16.82230526559311 48.22497985943643, 16.82308767539026 48.22556101679912, 16.823168614334794 48.225614938281765, 16.824121895237067 48.22599238706952, 16.82472444071303 48.22601635197794, 16.826244294227035 48.22578868489489, 16.829140109798093 48.22526744328604, 16.83104667160264 48.22524347802694, 16.832080891449447 48.22524347802694, 16.833591751747388 48.22515360820546, 16.835327442446815 48.22495589404271, 16.837099106010474 48.22466231705875, 16.83770165148644 48.22457843761119, 16.83956324721069 48.22432080701991, 16.840858270323213 48.2242009783961, 16.84133491077435 48.22435076413203, 16.841550747959772 48.22451852363592, 16.841595714040068 48.22454848063232, 16.84176658514519 48.22466231705875, 16.84202738841091 48.22486602371624, 16.842243225596327 48.22500981616282, 16.843196506498604 48.2250996862368, 16.844536495691422 48.22495589404271, 16.846011383125127 48.2248061100776, 16.846182254230254 48.2247821446025, 16.84730640623765 48.22466231705875, 16.84739633839824 48.22466231705875, 16.849599676332744 48.22435076413203, 16.852666363008925 48.22391338855438, 16.853017098435235 48.22388942266128, 16.854662856974066 48.22379955046228, 16.856776262747974 48.22357187351858, 16.859851942640216 48.222990693570345, 16.862531921025855 48.22241549867081, 16.86400680845956 48.222361573817615, 16.865085994386664 48.22230165724742, 16.868629321513986 48.22250537329993, 16.87300002451875 48.222649172378276, 16.87498752526783 48.22241549867081, 16.878575818475444 48.22201405673423, 16.88363899911677 48.22187025587168, 16.88562649986585 48.22201405673423, 16.885977235292156 48.221984098254524, 16.88736219056527 48.2218402973078, 16.889439623474946 48.22143885086025, 16.891858798594868 48.22143885086025, 16.89467367522139 48.22112128651476, 16.896957952100426 48.220456192539686, 16.897956199082994 48.220168581657965, 16.898217002348712 48.22011465443775, 16.898477805614426 48.22005473523755, 16.89886451390497 48.21999481596721, 16.90089698073435 48.21970720249332, 16.900941946814644 48.219797081877495, 16.90111281791977 48.220024775611165, 16.902021132741748 48.22137893320999, 16.90245280711259 48.2220440151964, 16.902713610378303 48.22250537329993, 16.903190250829443 48.223428077032345, 16.90323521690974 48.22362579709679, 16.90323521690974 48.223973303238, 16.90310031866885 48.22460839457252, 16.90310031866885 48.22466231705875, 16.90297441364402 48.22497985943643, 16.90284850861919 48.22532735638464, 16.9027585764586 48.22553106039534, 16.902668644298007 48.22575872862437, 16.90219200384687 48.22702885910648, 16.9016793905315 48.22884413238813, 16.9016793905315 48.229766721852684, 16.901976166661452 48.23059943442587, 16.90262367821771 48.23140817107659, 16.90310031866885 48.23183949206415, 16.90323521690974 48.23198326491877, 16.90405359957112 48.23279197969921, 16.90483600936827 48.23359469136022, 16.904880975448567 48.233738459283245, 16.905051846553693 48.234343478199094, 16.905006880473398 48.234864627371586, 16.905006880473398 48.235068293421705, 16.904791043287975 48.23736846546935, 16.904961914393102 48.23930316552932, 16.905132785498225 48.24080056116653, 16.905267683739112 48.24310646472499, 16.905609425949365 48.24494512372097, 16.905573453085125 48.24661603010714, 16.905573453085125 48.24748440075809, 16.906131032480797 48.25019122706819, 16.90622096464139 48.250388843731216, 16.906436801826807 48.25128110339843, 16.90651774077134 48.251628421743845, 16.906913442277947 48.25286796970852, 16.90708431338307 48.2532392294348, 16.90730015056849 48.25358653448029, 16.90742605559332 48.25370030630982, 16.907641892778738 48.25393383716681, 16.90782175709992 48.254107487625426, 16.90794766212475 48.2542212582958, 16.909242685237274 48.25571821722142, 16.90963838674388 48.25626309939023, 16.910546701565856 48.25778993607102, 16.910888443776106 48.25885570092668, 16.9109334098564 48.26004118853991, 16.9109334098564 48.26015494600671, 16.910717572670983 48.26116079048769, 16.910717572670983 48.26125059706821, 16.910582674430092 48.261537977065615, 16.908981881971556 48.264615236590814, 16.90885597694673 48.264848717592415, 16.908595173681015 48.264848717592415, 16.908811010866433 48.264615236590814, 16.909377583478165 48.2635495918101)");

        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
                solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_3);
                solrDocument.addField(SOLR_FIELD_GEO, "LINESTRING(16.235939027349787 48.37663444618723,16.23318976310952 48.37658634278413,16.230504871885614 48.37659881404117,16.229271055738764 48.37671640002863,16.227919222395258 48.37688743370746,16.226996542494135 48.37707271954446,16.226030947248773 48.37730788905878,16.22486150411828 48.37764282558259,16.223552586119013 48.37804902223988,16.222179295103388 48.37850509883475,16.220462681333856 48.37903243230421,16.219003559629755 48.37953125623426,16.217823387663202 48.3798946820197,16.215999485533075 48.38045050467305,16.215216280500726 48.3806785327239,16.214293600599603 48.3808709305973,16.212931038420038 48.3811132083664,16.211897835899208 48.381215440181734,16.210980520416115 48.381311638194695,16.209929094482277 48.38136508145665,16.20874900963679 48.38138289586484,16.20746691372767 48.38134370415858,16.206597878006846 48.38127957221058,16.20533821675963 48.381211877288884,16.203600145317978 48.38111567908741,16.202859855629868 48.38108005007777,16.201368547417587 48.381037295233284,16.200445867516464 48.38101235489075,16.199410534836716 48.38101235489075,16.19854686353392 48.38102304361048,16.19777975175566 48.381062235563604,16.19708774182982 48.38108005007777,16.19626698587126 48.381137056481236,16.19531748387999 48.38119762571497,16.194244600274033 48.381290260874195,16.193338013627 48.38138645874572,16.192452884652084 48.38151115941972,16.19159457776732 48.38161091973902,16.190700718812195 48.38173961258219,16.190024802140442 48.38185807740789,16.189413258485047 48.38195872775745,16.188078859500138 48.38220857134901,16.187422388843743 48.38236912081718,16.18672300284311 48.382522544177014,16.185351052931992 48.38287570562525,16.183979103020874 48.38331437059466,16.18261117642328 48.38386926523154,16.181388089112488 48.38452747109023,16.180641764454094 48.38500041212846,16.179959812812058 48.38550184952169,16.179385149530617 48.38604247009315,16.178810486249176 48.38657595988747,16.178221741357675 48.387353473396466,16.17770809831859 48.38817372391305,16.17729637922207 48.38904027171544,16.17712807060002 48.38945216619071,16.17689002454358 48.39015260178917)");
        infrastructureRule.getSolrClient().add(solrDocument);


        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, "wrong");
        solrDocument.addField(SOLR_FIELD_GEO,"LINESTRING()");

        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, "id-no-geo");
        infrastructureRule.getSolrClient().add(solrDocument);
        infrastructureRule.getSolrClient().commit(true, true);
    }

    @After
    public void tearDown() throws Exception {
        infrastructureRule.getSolrClient().deleteByQuery("*:*");
    }


    private String appendPointsAsCorridorPointGetLinestring(SolrQuery query, TestGeoPoint...geoPoints) {
        if (geoPoints.length == 1) {
            String pointString = "POINT(" + geoPoints[0] + ")";
            query.add("corridor.point", pointString);
            return pointString;
        }

        StringBuilder lineString = new StringBuilder("LINESTRING(");

        for (TestGeoPoint geoPoint : geoPoints) {
            String geoPointString = geoPoint.toString();
            query.add("corridor.point", "POINT("+ geoPointString +")");
            lineString.append(geoPointString);
            lineString.append(',');
        }
        lineString.setCharAt(lineString.length()-1, ')');

        return lineString.toString();
    }
}
