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
package com.indoqa.solr.spatial.corridor.query.points;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.indoqa.solr.spatial.corridor.EmbeddedSolrInfrastructureRule;

public class TestPointsDistance {

    private static final String SOLR_FIELD_ID = "id";
    private static final String SOLR_FIELD_POINT_POSITION = "position";

    private static final String DOCUMENT_ID_1 = "id-1";

    @ClassRule
    public static EmbeddedSolrInfrastructureRule infrastructureRule = new EmbeddedSolrInfrastructureRule();

    @Test
    public void pointsExactMatch() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.00001}pointsDistance(geo)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.point", "POINT(16.41654 48.19311)");
        query.addField(SOLR_FIELD_ID);

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void pointsFarAwayBroadDistance() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=2}pointsDistance(geo)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.point", "POINT(16.41 48.19)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void pointsFarAwayDistance() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.2}pointsDistance(geo)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.point", "POINT(16.41 48.19)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void pointsFarAwaySmallDistance() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.002}pointsDistance(geo)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.point", "POINT(16.41 48.19)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(0, response.getResults().getNumFound());
    }

    @Test
    public void pointsPositionMatch() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.01}pointsDistance(geo)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.point", "POINT(16.41618 48.19288)");
        query.addField(SOLR_FIELD_ID);
        query.addField(SOLR_FIELD_POINT_POSITION + ":pointsPosition(geo)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(0.0475, (double) response.getResults().get(0).getFieldValue(SOLR_FIELD_POINT_POSITION), 0.00009);
    }

    @Before
    public void setup() throws Exception {
        SolrInputDocument solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_1);
        solrDocument.addField("geo", "LINESTRING(16.41654 48.19311,16.40812 48.18743)");
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
}
