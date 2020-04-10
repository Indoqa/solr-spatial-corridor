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
package com.indoqa.solr.spatial.corridor.query.circle;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import com.indoqa.solr.spatial.corridor.EmbeddedSolrInfrastructureRule;
import com.indoqa.solr.spatial.corridor.LineStringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class TestCircleDistance {

    private static final String SOLR_FIELD_ID = "id";
    private static final String SOLR_FIELD_CIRCLE_DISTANCE = "circleDistance";

    private static final String DOCUMENT_ID_1 = "id-1";
    private static final String DOCUMENT_ID_2 = "id-2";

    @ClassRule
    public static EmbeddedSolrInfrastructureRule infrastructureRule = new EmbeddedSolrInfrastructureRule();

    @Test
    public void circleExactMatch() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.00001}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41654 48.19311)");
        query.add("corridor.circle.radius", "0.00001");
        query.addField(SOLR_FIELD_ID);

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void circleFarAwaySmallCircleBroadDistance() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=2}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41 48.19)");
        query.add("corridor.circle.radius", "0.00001");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void circleFarAwaySmallCircleSmallDistance() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.00001}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41 48.19)");
        query.add("corridor.circle.radius", "0.00001");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(0, response.getResults().getNumFound());
    }

    @Test
    public void circleFarAwayBroadCircleSmallDistance() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.00001}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41 48.19)");
        query.add("corridor.circle.radius", "0.001");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(0, response.getResults().getNumFound());
    }

    @Test
    public void circleFarAwayBigCircleSmallDistance() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.00001}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41 48.19)");
        query.add("corridor.circle.radius", "0.01");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void circleFarAwayBroadCircleBroadDistance() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=2}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41 48.19)");
        query.add("corridor.circle.radius", "0.01");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void circleFarAwayBigCircleBroadDistance() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=2}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41 48.19)");
        query.add("corridor.circle.radius", "10");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void circleDistanceMatch() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.01}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41618 48.19288)");
        query.add("corridor.circle.radius", "0.0000005");

        query.addField(SOLR_FIELD_ID);
        query.addField(SOLR_FIELD_CIRCLE_DISTANCE + ":circleDistance(geo, geoHash)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(0.00113, (double) response.getResults().get(0).getFieldValue(SOLR_FIELD_CIRCLE_DISTANCE), 0.00009);

        query = new SolrQuery("{!frange l=0 u=0.01}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41618 48.19288)");
        query.add("corridor.circle.radius", "0.000005");

        query.addField(SOLR_FIELD_ID);
        query.addField(SOLR_FIELD_CIRCLE_DISTANCE + ":circleDistance(geo, geoHash)");

        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(0.0006, (double) response.getResults().get(0).getFieldValue(SOLR_FIELD_CIRCLE_DISTANCE), 0.00009);

        query = new SolrQuery("{!frange l=0 u=0.01}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41618 48.19288)");
        query.add("corridor.circle.radius", "0.00005");

        query.addField(SOLR_FIELD_ID);
        query.addField(SOLR_FIELD_CIRCLE_DISTANCE + ":circleDistance(geo, geoHash)");

        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(0.0, (double) response.getResults().get(0).getFieldValue(SOLR_FIELD_CIRCLE_DISTANCE), 0.00009);
    }

    @Test
    public void circleCacheCleanup() throws SolrServerException, IOException {
        circleExactMatch();

        LineStringUtils.purgeCache();

        circleExactMatch();
    }

    @Before
    public void setup() throws Exception {
        String lineString = "LINESTRING(16.41654 48.19311,16.40812 48.18743)";

        SolrInputDocument solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_1);
        solrDocument.addField("geo", lineString);
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, "id-no-geo");
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField("geo", "LINESTRING()");
        solrDocument.addField(SOLR_FIELD_ID, "id-empty-geo");
        infrastructureRule.getSolrClient().add(solrDocument);
        infrastructureRule.getSolrClient().commit(true, true);
    }

    @After
    public void tearDown() throws Exception {
        infrastructureRule.getSolrClient().deleteByQuery("*:*");
    }
}
