package com.indoqa.solr.spatial.corridor;

import static org.apache.solr.core.CoreDescriptor.CORE_DATADIR;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.NodeConfig;
import org.apache.solr.core.NodeConfig.NodeConfigBuilder;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.junit.rules.ExternalResource;

public class EmbeddedSolrInfrastructureRule extends ExternalResource {

    private static String instanceDir = "src/test/resources/solr/test";

    private EmbeddedSolrServer embeddedSolrServer;
    private boolean initialized;

    private static File getCanonicalFile(File file) {
        if (file == null) {
            return null;
        }

        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return file;
        }
    }

    private static String getNormalizedPath(String path) {
        File file = new File(path);
        file = getCanonicalFile(file);
        return file.getAbsolutePath();
    }

    public SolrClient getSolrClient() {
        return this.embeddedSolrServer;
    }

    @Override
    protected void after() {
        try {
            this.embeddedSolrServer.close();
        } catch (Exception e) {
            // ignore
        }

        this.embeddedSolrServer.getCoreContainer().shutdown();
        this.embeddedSolrServer = null;
    }

    @Override
    protected void before() throws Throwable {
        if (this.initialized) {
            return;
        }
        SolrResourceLoader loader = new SolrResourceLoader(getNormalizedPath("."));
        NodeConfig nodeConfig = new NodeConfigBuilder(null, loader).build();

        CoreContainer container = new CoreContainer(nodeConfig);
        container.load();

        Properties properties = new Properties();
        properties.setProperty(CORE_DATADIR, getNormalizedPath("./target/test-core"));

        properties.setProperty(CoreDescriptor.CORE_CONFIG, instanceDir + "/conf/solrconfig.xml");
        properties.setProperty(CoreDescriptor.CORE_SCHEMA, instanceDir + "/conf/schema.xml");

        CoreDescriptor coreDescriptor = new CoreDescriptor(container, "Embedded Core", instanceDir, properties);
        SolrCore core = container.create(coreDescriptor);

        this.embeddedSolrServer = new EmbeddedSolrServer(container, core.getName());
        this.initialized = true;
    }

}
