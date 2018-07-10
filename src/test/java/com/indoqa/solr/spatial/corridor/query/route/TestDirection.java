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
package com.indoqa.solr.spatial.corridor.query.route;

import static com.indoqa.solr.spatial.corridor.query.route.TestGeoPoint.geo;
import static org.junit.Assert.assertEquals;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class TestDirection {

    private static final String SOLR_FIELD_ID = "id";
    private static final String SOLR_FIELD_GEO_DIRECTION = "geo_direction";

    private static final String DOCUMENT_ID_1 = "traffic-bidirectional";
    private static final String DOCUMENT_ID_2 = "traffic-forwards";
    private static final String DOCUMENT_ID_3 = "traffic-backwards";

    @ClassRule
    public static EmbeddedSolrInfrastructureRule infrastructureRule = new EmbeddedSolrInfrastructureRule();

    @Test
    public void exactMatchBidirectional() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=1} inRouteDirection(geo_direction)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.route", "LINESTRING(16.891858798594868 48.22143885086025, 16.89467367522138 48.22112128651477)");
        query.add("corridor.maxAngleDifference", "15");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(2, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(DOCUMENT_ID_2, response.getResults().get(1).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void exactMatchForwards() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=1} inRouteDirection(geo_direction)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.route", "LINESTRING(16.891858798594868 48.22143885086025, 16.89467367522138 48.22112128651477)");
        query.add("corridor.maxAngleDifference", "15");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(2, response.getResults().getNumFound());

        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(DOCUMENT_ID_2, response.getResults().get(1).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void exactMatchBackwards() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=1} inRouteDirection(geo_direction)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.route", "LINESTRING(16.89467367522138 48.22112128651477, 16.891858798594868 48.22143885086025)");
        query.add("corridor.maxAngleDifference", "15");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(2, response.getResults().getNumFound());

        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(DOCUMENT_ID_3, response.getResults().get(1).getFieldValue(SOLR_FIELD_ID));
    }

    @Before
    public void setup() throws Exception {
        SolrInputDocument solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_1);
        solrDocument.addField(SOLR_FIELD_GEO_DIRECTION, "true,16.891858798594868 48.22143885086025,16.89467367522139 48.22112128651476");
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_2);
        solrDocument.addField(SOLR_FIELD_GEO_DIRECTION, "false,16.891858798594868 48.22143885086025,16.89467367522139 48.22112128651476");
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_3);
        solrDocument.addField(SOLR_FIELD_GEO_DIRECTION, "false,16.89467367522139 48.22112128651476,16.891858798594868 48.22143885086025");
        infrastructureRule.getSolrClient().add(solrDocument);

        infrastructureRule.getSolrClient().commit(true, true);
    }

    @After
    public void tearDown() throws Exception {
        infrastructureRule.getSolrClient().deleteByQuery("*:*");
    }
}
