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

    private static final String DOCUMENT_ID_1 = "id-1";

    @ClassRule
    public static EmbeddedSolrInfrastructureRule infrastructureRule = new EmbeddedSolrInfrastructureRule();

    @Test
    public void pointsExactMatch() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("{!frange l=0 u=0.00001}pointsDistance(geo)");
        query.setRows(Integer.MAX_VALUE);
        query.add("corridor.point", "POINT(16.41654 48.19311)");

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
