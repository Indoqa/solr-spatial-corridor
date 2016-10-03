package com.indoqa.solr.spatial.corridor;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.junit.rules.ExternalResource;

import com.indoqa.solr.spring.client.EmbeddedSolrServerBuilder;

public class EmbeddedSolrInfrastructureRule extends ExternalResource {

    private SolrClient solrClient;
    private boolean initialized;

    public SolrClient getSolrClient() {
        return this.solrClient;
    }

    @Override
    protected void after() {
        try {
            this.solrClient.close();
        } catch (Exception e) {
            // ignore
        }

        if (this.solrClient instanceof EmbeddedSolrServer) {
            EmbeddedSolrServer embeddedSolrServer = (EmbeddedSolrServer) this.solrClient;
            embeddedSolrServer.getCoreContainer().shutdown();
        }
    }

    @Override
    protected void before() throws Throwable {
        if (this.initialized) {
            return;
        }

        this.solrClient = EmbeddedSolrServerBuilder.build("file://./target/test-core", "solr/test");

        this.initialized = true;
    }
}
