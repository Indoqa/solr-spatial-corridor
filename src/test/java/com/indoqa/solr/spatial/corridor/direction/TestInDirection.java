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

import com.indoqa.solr.spatial.corridor.EmbeddedSolrInfrastructureRule;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestInDirection {

    private static final String SOLR_FIELD_ID = "id";
    private static final String SOLR_FIELD_GEO = "geo";
    private static final String SOLR_FIELD_GEO_DIRECTION = "geoDirection";

    private static final String DOCUMENT_ID_WTE_LONG = "route-west-to-east-long";
    private static final String DOCUMENT_ID_ETW_LONG = "route-east-to-west-long";
    private static final String DOCUMENT_ID_WTE_SHORT = "route-west-to-east-short";
    private static final String DOCUMENT_ID_ETW_SHORT = "route-east-to-west-short";

    private static final String DOCUMENT_ID_NTS_SHORT = "route-north-to-south-short";

    @ClassRule
    public static EmbeddedSolrInfrastructureRule infrastructureRule = new EmbeddedSolrInfrastructureRule();


    @Test
    public void onlyInDirections() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=1} inDirectionPoints(geoDirection)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.route", "LINESTRING(13.789743483066559 47.76935515104816,13.790076076984409 47.76938760067327,13.790565580129623 47.76940292410029)");
        query.add("corridor.pointsMaxDistanceToRoute", "0.01");
        query.add("corridor.maxAngleDifference", "15");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_WTE_LONG, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));

        // other direction
        query = new SolrQuery("{!frange l=1} inDirectionPoints(geoDirection)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.route", "LINESTRING(13.790565580129623 47.76940292410029, 13.790076076984409 47.76938760067327, 13.789743483066559 47.76935515104816)");
        query.add("corridor.pointsMaxDistanceToRoute", "0.1");
        query.add("corridor.maxAngleDifference", "10");

        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_ETW_LONG, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));

        // both directions
        query = new SolrQuery("{!frange l=1} inDirectionPoints(geoDirection)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.route", "LINESTRING(13.790565580129623 47.76940292410029, 13.790076076984409 47.76938760067327, 13.789743483066559 47.76935515104816)");
        query.add("corridor.pointsMaxDistanceToRoute", "0.05");
        query.add("corridor.maxAngleDifference", "180");
        query.add("corridor.alwaysCheckPointDistancePercent", "true");
        query.add("corridor.percentageOfPointsWithinDistance", "100");

        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(2, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_WTE_LONG, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(DOCUMENT_ID_ETW_LONG, response.getResults().get(1).getFieldValue(SOLR_FIELD_ID));
    }

    @Before
    public void setup() throws Exception {
        SolrInputDocument solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_WTE_LONG);
        String points = "13.789342492818836 47.76931098347035, 13.78989636898041 47.76938038964718, 13.790542781352997 47.76940833236759, 13.791073858737947 47.769311884849856";
        solrDocument.addField(SOLR_FIELD_GEO, "LINESTRING(" + points + ")");
        solrDocument.addField(SOLR_FIELD_GEO_DIRECTION, "false," + points);
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_ETW_LONG);
        points = "13.791073858737947 47.769311884849856, 13.790542781352997 47.76940833236759, 13.78989636898041 47.76938038964718, 13.789342492818836 47.76931098347035";
        solrDocument.addField(SOLR_FIELD_GEO, "LINESTRING(" + points + ")");
        solrDocument.addField(SOLR_FIELD_GEO_DIRECTION, "false," + points);
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_WTE_SHORT);
        points = "13.783518075942991 47.769802232989576, 13.784515857696531 47.76987434262023, 13.783984780311583 47.769845498779944";
        solrDocument.addField(SOLR_FIELD_GEO, "LINESTRING(" + points + ")");
        solrDocument.addField(SOLR_FIELD_GEO_DIRECTION, "false," + points);
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_ETW_SHORT);
        points = "13.784515857696531 47.76987434262023, 13.783984780311583 47.769845498779944, 13.783518075942991 47.769802232989576";
        solrDocument.addField(SOLR_FIELD_GEO, "LINESTRING(" + points + ")");
        solrDocument.addField(SOLR_FIELD_GEO_DIRECTION, "false," + points);
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_NTS_SHORT);
        points = "13.789719343185425 47.77007264358892,13.789687156677246 47.769315490367745,13.789762258529663 47.768579959540716";
        solrDocument.addField(SOLR_FIELD_GEO, "LINESTRING(" + points + ")");
        solrDocument.addField(SOLR_FIELD_GEO_DIRECTION, "false," + points);
        infrastructureRule.getSolrClient().add(solrDocument);

        infrastructureRule.getSolrClient().commit(true, true, true);
    }

    @After
    public void tearDown() throws Exception {
        infrastructureRule.getSolrClient().deleteByQuery("*:*");
        infrastructureRule.getSolrClient().commit(true, true);
    }
}
