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

import java.io.IOException;

import org.apache.solr.core.PluginInfo;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.util.plugin.PluginInfoInitialized;

public class CorridorSearchComponent extends SearchComponent implements PluginInfoInitialized {

    @Override
    public String getDescription() {
        return "indoqa-corridor";
    }

    @Override
    public String getSource() {
        return "indoqa-corridor";
    }

    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public void init(PluginInfo info) {
        System.out.println("1a");
    }

    @Override
    public void prepare(ResponseBuilder rb) throws IOException {
        // nothing to do
    }

    @Override
    public void process(ResponseBuilder responseBuilder) throws IOException {

    }
}
