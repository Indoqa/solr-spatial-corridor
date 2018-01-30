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

package com.indoqa.solr.spatial.corridor;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.NodeConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

public final class EmbeddedSolrClientBuilder {
    private static final String CORE_NAME = "Embedded-Core";

    private EmbeddedSolrClientBuilder() {
    }

    public static SolrClient build(String url, String embeddedSolrConfigurationPath) {
        SolrResourceLoader loader;
        NodeConfig nodeConfig;
        CoreContainer container;
        HashMap properties;
        SolrCore core;
        if ((new File(embeddedSolrConfigurationPath)).exists()) {
            deleteOldCoreProperties(embeddedSolrConfigurationPath);
            loader = new SolrResourceLoader(getNormalizedPath(embeddedSolrConfigurationPath));
            nodeConfig = (new NodeConfig.NodeConfigBuilder((String) null, loader)).build();
            container = new CoreContainer(nodeConfig);
            container.load();
            properties = new HashMap();
            properties.put("dataDir", getNormalizedPath(getDataDir(url)).toString());
            core = container.create("Embedded-Core", loader.getInstancePath(), properties, false);
            return new EmbeddedSolrServer(core);
        } else {
            loader = new SolrResourceLoader(createTempDirectory());
            nodeConfig = (new NodeConfig.NodeConfigBuilder((String) null, loader)).build();
            container = new CoreContainer(nodeConfig);
            container.load();
            properties = new HashMap();
            properties.put("dataDir", getNormalizedPath(getDataDir(url)).toString());
            properties.put("config", embeddedSolrConfigurationPath + "/conf/solrconfig.xml");
            properties.put("schema", embeddedSolrConfigurationPath + "/conf/schema.xml");
            core = container.create("Embedded-Core", loader.getInstancePath(), properties, false);
            return new EmbeddedSolrServer(core);
        }
    }

    public static File getCanonicalFile(File file) {
        if (file == null) {
            return null;
        } else {
            try {
                return file.getCanonicalFile();
            } catch (IOException var2) {
                return file;
            }
        }
    }

    private static Path createTempDirectory() {
        try {
            Path tempDirectory = Files.createTempDirectory("embedded-solr-client");
            Runtime.getRuntime().addShutdownHook(new EmbeddedSolrClientBuilder.CleanupThread(tempDirectory));
            return tempDirectory;
        } catch (IOException var1) {
            throw new RuntimeException("Failed to create temporary directory", var1);
        }
    }

    private static void deleteOldCoreProperties(String path) {
        Path corePropertiesPath = Paths.get(path, "core.properties");
        if (Files.exists(corePropertiesPath, new LinkOption[0])) {
            try {
                Files.delete(corePropertiesPath);
            } catch (IOException var3) {
                ;
            }

        }
    }

    private static Path getNormalizedPath(String path) {
        File file = new File(path);
        file = getCanonicalFile(file);
        return file.toPath();
    }

    private static class CleanupThread extends Thread {
        private Path directory;

        public CleanupThread(Path directory) {
            this.directory = directory;
        }

        public void run() {
            try {
                Files.walkFileTree(this.directory, new SimpleFileVisitor<Path>() {
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return super.postVisitDirectory(dir, exc);
                    }

                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return super.visitFile(file, attrs);
                    }
                });
            } catch (IOException var2) {
                ;
            }

        }
    }

    public static String getDataDir(String url) {
        String path = url.substring("file:".length());
        return path.startsWith("//") ? path.substring(2) : path;
    }
}
