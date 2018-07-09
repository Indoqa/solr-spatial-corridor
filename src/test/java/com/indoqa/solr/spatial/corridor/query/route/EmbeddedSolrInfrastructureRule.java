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


import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.junit.rules.ExternalResource;

public class EmbeddedSolrInfrastructureRule extends ExternalResource {

    private SolrServer solrClient;
    private boolean initialized;

    public SolrServer getSolrClient() {
        return this.solrClient;
    }

    @Override
    protected void after() {
        EmbeddedSolrServer embeddedSolrServer = (EmbeddedSolrServer) this.solrClient;
        embeddedSolrServer.getCoreContainer().shutdown();
    }

    @Override
    protected void before() throws Throwable {
        if (this.initialized) {
            return;
        }

        String dataDir = "target/solr-data";
        String coreName = "test";

        CoreContainer coreContainer = new CoreContainer(".");
        coreContainer.load();

        CoreDescriptor coreDescriptor = new CoreDescriptor(coreContainer, "test-container", ".");

        SolrConfig solrConfig = new SolrConfig("./src/test/resources/solr", SolrConfig.DEFAULT_CONF_FILE, null);
        SolrCore singlecore = new SolrCore(coreName, dataDir, solrConfig, null, coreDescriptor);

        coreContainer.register(coreName, singlecore, false);

        this.solrClient = new EmbeddedSolrServer(coreContainer, coreName);
        this.initialized = true;
    }
}
