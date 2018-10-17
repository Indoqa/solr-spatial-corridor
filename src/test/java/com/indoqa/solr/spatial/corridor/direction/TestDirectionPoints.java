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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.indoqa.solr.spatial.corridor.EmbeddedSolrInfrastructureRule;
import com.indoqa.solr.spatial.corridor.TestGeoPoint;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class TestDirectionPoints {

    private static final String SOLR_FIELD_ID = "id";
    private static final String SOLR_FIELD_GEO_POINTS = "geoPoints";
    private static final String SOLR_FIELD_GEO_DIRECTION = "geoDirection";

    private static final String SOLR_FIELD_LATLON = "latLon";
    private static final String SOLR_FIELD_LATLON_0 = "latLon_0_coordinate";
    private static final String SOLR_FIELD_LATLON_1 = "latLon_1_coordinate";

    private static final String DOCUMENT_ID_1 = "route1";
    private static final String DOCUMENT_ID_2 = "route2";

    @ClassRule
    public static EmbeddedSolrInfrastructureRule infrastructureRule = new EmbeddedSolrInfrastructureRule();

    @Test
    public void corridorMatch() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!corridor field=latLon buffer=0.1}");
        query.setRows(Integer.MAX_VALUE);
        query.addField("*,calculatedDistance_:corridorDistance(latLon),calculatedPosition_:corridorPosition(latLon)");
        query.add("corridor.route", "LINESTRING(16.336764316407653 48.20299341070389,16.326292972413512 48.203050616269806)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(0, response.getResults().getNumFound());

        query.setQuery("{!corridor field=latLon buffer=0.5}");
        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_2, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));

        query.setQuery("{!corridor field=latLon buffer=1.0}");
        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(2, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(DOCUMENT_ID_2, response.getResults().get(1).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void corridorDirectionMatch() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!corridor field=latLon buffer=0.5}");
        query.setRows(Integer.MAX_VALUE);
        query.addField("*,calculatedDistance_:corridorDistance(latLon),calculatedPosition_:corridorPosition(latLon)");
        query.add("corridor.route", "LINESTRING(16.336764316407653 48.20299341070389,16.326292972413512 48.203050616269806)");

        query.set("corridor.maxAngleDifference", String.valueOf(10));
        query.set("corridor.pointsMaxDistanceToRoute", String.valueOf(0.5));
        query.setFilterQueries("{!frange l=1} inDirectionPoints("+SOLR_FIELD_GEO_DIRECTION+")");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(0, response.getResults().getNumFound());

        query.set("corridor.maxAngleDifference", String.valueOf(10));
        query.set("corridor.pointsMaxDistanceToRoute", String.valueOf(1));
        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(0, response.getResults().getNumFound());

        query.set("corridor.maxAngleDifference", String.valueOf(170));
        query.set("corridor.pointsMaxDistanceToRoute", String.valueOf(1));
        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(0, response.getResults().getNumFound());

        query.set("corridor.maxAngleDifference", String.valueOf(180));
        query.set("corridor.pointsMaxDistanceToRoute", String.valueOf(1));
        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
    }

    @Before
    public void setup() throws Exception {
        double[][] points = new double[][] { {48.19864644653537, 16.3249440851298}, {48.20080609559021, 16.332625931748453} };
        SolrInputDocument solrDocument = createSolrDocument(DOCUMENT_ID_1, points);
        infrastructureRule.getSolrClient().add(solrDocument);


        points = new double[][] {{48.20555159479074, 16.32369148515852}, {48.204235914853555, 16.332617876760082}};
        solrDocument = createSolrDocument(DOCUMENT_ID_2, points);
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, "id-no-geo");
        infrastructureRule.getSolrClient().add(solrDocument);
        infrastructureRule.getSolrClient().commit(true, true);
    }

    private SolrInputDocument createSolrDocument(String id, double[][] points) {
        SolrInputDocument solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, id);
        solrDocument.addField(SOLR_FIELD_LATLON_0, points[0][0]);
        solrDocument.addField(SOLR_FIELD_LATLON_1, points[0][1]);

        StringBuilder geoDirection = new StringBuilder("false,");
        for (double[] coordinates : points) {
            String point = coordinates[1] + " " + coordinates[0];
            geoDirection.append(point);
            geoDirection.append(',');
            solrDocument.addField(SOLR_FIELD_GEO_POINTS,point);
        }
        geoDirection.setLength(geoDirection.length()-1);
        solrDocument.setField(SOLR_FIELD_GEO_DIRECTION, geoDirection.toString());

        solrDocument.setField("geo", "LINESTRING()");
        return solrDocument;
    }

    @After
    public void tearDown() throws Exception {
        infrastructureRule.getSolrClient().deleteByQuery("*:*");
    }
}
