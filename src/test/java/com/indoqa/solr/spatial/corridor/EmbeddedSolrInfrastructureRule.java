package com.indoqa.solr.spatial.corridor;

import static org.apache.solr.core.CoreDescriptor.CORE_CONFIG;
import static org.apache.solr.core.CoreDescriptor.CORE_DATADIR;
import static org.apache.solr.core.CoreDescriptor.CORE_SCHEMA;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
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

        SolrResourceLoader loader = new SolrResourceLoader(Paths.get(getNormalizedPath(".")));
        NodeConfig nodeConfig = new NodeConfigBuilder(null, loader).build();

        CoreContainer container = new CoreContainer(nodeConfig);
        container.load();

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(CORE_DATADIR, getNormalizedPath("./target/test-core"));
        properties.put(CORE_CONFIG, instanceDir + "/conf/solrconfig.xml");
        properties.put(CORE_SCHEMA, instanceDir + "/conf/schema.xml");

        SolrCore core = container.create("Embedded-Core", loader.getInstancePath(), properties);

        this.embeddedSolrServer = new EmbeddedSolrServer(container, core.getName());
        this.initialized = true;
    }
}
