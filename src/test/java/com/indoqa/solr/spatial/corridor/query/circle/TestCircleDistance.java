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
    public void circle10Meters() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.2}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.388190090656263 48.21037932900157)");
        query.add("corridor.circle.radius", "0.01");

        query.addField(SOLR_FIELD_ID);
        query.addField(SOLR_FIELD_CIRCLE_DISTANCE + ":circleDistance(geo, geoHash)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_2, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(0.1271, (double) response.getResults().get(0).getFieldValue(SOLR_FIELD_CIRCLE_DISTANCE), 0.00009);
    }

    @Test
    public void circle100Meters() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.1}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.388190090656263 48.21037932900157)");
        query.add("corridor.circle.radius", "0.1");

        query.addField(SOLR_FIELD_ID);
        query.addField(SOLR_FIELD_CIRCLE_DISTANCE + ":circleDistance(geo, geoHash)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_2, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(0.03727863, (double) response.getResults().get(0).getFieldValue(SOLR_FIELD_CIRCLE_DISTANCE), 0.00009);
    }

    @Test
    public void circle200Meters() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.1}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.388190090656263 48.21037932900157)");
        query.add("corridor.circle.radius", "0.2");

        query.addField(SOLR_FIELD_ID);
        query.addField(SOLR_FIELD_CIRCLE_DISTANCE + ":circleDistance(geo, geoHash)");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(1, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_2, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(0.0, (double) response.getResults().get(0).getFieldValue(SOLR_FIELD_CIRCLE_DISTANCE), 0.00009);
    }

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
        assertEquals(2, response.getResults().getNumFound());
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
    public void circleFarAwayBroadCircleBroadDistance() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=2}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41 48.19)");
        query.add("corridor.circle.radius", "0.01");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(2, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
    }

    @Test
    public void circleFarAwayBigCircleBroadDistance() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=2}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41 48.19)");
        query.add("corridor.circle.radius", "10");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(3, response.getResults().getNumFound());
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
        assertEquals(2, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(0.00113, (double) response.getResults().get(0).getFieldValue(SOLR_FIELD_CIRCLE_DISTANCE), 0.00009);

        query = new SolrQuery("{!frange l=0 u=0.01}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41618 48.19288)");
        query.add("corridor.circle.radius", "0.000005");

        query.addField(SOLR_FIELD_ID);
        query.addField(SOLR_FIELD_CIRCLE_DISTANCE + ":circleDistance(geo, geoHash)");

        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(2, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(0.00118, (double) response.getResults().get(0).getFieldValue(SOLR_FIELD_CIRCLE_DISTANCE), 0.00009);

        query = new SolrQuery("{!frange l=0 u=0.01}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.41618 48.19288)");
        query.add("corridor.circle.radius", "0.00005");

        query.addField(SOLR_FIELD_ID);
        query.addField(SOLR_FIELD_CIRCLE_DISTANCE + ":circleDistance(geo, geoHash)");

        response = infrastructureRule.getSolrClient().query(query);
        assertEquals(2, response.getResults().getNumFound());
        assertEquals(DOCUMENT_ID_1, response.getResults().get(0).getFieldValue(SOLR_FIELD_ID));
        assertEquals(0.00113, (double) response.getResults().get(0).getFieldValue(SOLR_FIELD_CIRCLE_DISTANCE), 0.00009);
    }

    @Test
    public void live() throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.1}circleDistance(geo, geoHash)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.circle.point", "POINT(16.260685126351778 47.939900608464335)");
        query.add("corridor.circle.radius", "0.1");

        QueryResponse response = infrastructureRule.getSolrClient().query(query);
        assertEquals(0, response.getResults().getNumFound());
    }

    @Test
    public void circleCacheCleanup() throws SolrServerException, IOException {
        circleExactMatch();

        LineStringUtils.purgeCache();

        circleExactMatch();
    }

    @Before
    public void setup() throws Exception {
        SolrInputDocument solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_1);
        solrDocument.addField("geo", "LINESTRING(16.41654 48.19311,16.40812 48.18743)");
        infrastructureRule.getSolrClient().add(solrDocument);

        String lineString = "LINESTRING(16.38711452484129 48.2119111225894,16.387050151824933 48.21112467652911,16.386942863464338 48.21035251773728,16.386835575103742 48.209523149029216,16.38664245605467 48.20845066957865,16.38659954071043 48.20746396864767)";

        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, DOCUMENT_ID_2);
        solrDocument.addField("geo", lineString);
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField(SOLR_FIELD_ID, "id-no-geo");
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField("geo", "LINESTRING()");
        solrDocument.addField(SOLR_FIELD_ID, "id-empty-geo");
        infrastructureRule.getSolrClient().add(solrDocument);

        solrDocument = new SolrInputDocument();
        solrDocument.addField("geo", "LINESTRING(16.357338008901007 48.309717124336174,"
            + "16.361528847584587 48.30715701174508,16.36399298878481 48.30609225420982,16.364343724211114 48.30591878037218,"
            + "16.39327490027352 48.260645896380645,16.392240680426717 48.25992144356453,16.391593168870454 48.25966399091767,"
            + "16.389515735960778 48.258855700926695,16.38808581460737 48.25842460770592,16.38786997742195 48.258394670541726,"
            + "16.39604481081975 48.24560989946699,16.39617970906064 48.245466164902666,16.397123996746853 48.244573803777556,"
            + "16.402789722864142 48.23959066889147,16.403095492210152 48.23930316552932,16.404606352508097 48.23794948215251,"
            + "16.437827292630736 48.207745844843245,16.43713481499418 48.207284353727324,16.43553402253564 48.206193540013544,"
            + "16.425713430599007 48.19950430616394,16.424553305727375 48.1986950654227,16.423770895930225 48.19817354794938,"
            + "16.405478694465835 48.18543368531189,16.405172925119825 48.185085917597235,16.4050020540147 48.18485207246238,"
            + "16.404822189693515 48.18456426160099,16.404525413563565 48.18404859643269,16.404309576378143 48.183640857510596)");
        solrDocument.addField(SOLR_FIELD_ID, "my-id");
        infrastructureRule.getSolrClient().add(solrDocument);
        infrastructureRule.getSolrClient().commit(true, true);
    }

    @After
    public void tearDown() throws Exception {
        infrastructureRule.getSolrClient().deleteByQuery("*:*");
    }
}
