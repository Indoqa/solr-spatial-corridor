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
package com.indoqa.solr.spatial.corridor.processor;

import com.indoqa.solr.spatial.corridor.LineStringUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

import java.io.IOException;

import static org.apache.solr.common.SolrException.ErrorCode.SERVER_ERROR;

public class LinestringHashUpdateProcessorFactory extends UpdateRequestProcessorFactory {

    private String linestringFieldName = null;
    private String hashFieldName = null;

    @Override
    public void init(NamedList args) {

        Object obj = args.remove("linestringFieldName");
        if (null == obj && null == linestringFieldName) {
            throw new SolrException
                    (SERVER_ERROR, "'linestringFieldName' init param must be specified and non-null");
        } else {
            linestringFieldName = obj.toString();
        }

        obj = args.remove("hashFieldName");
        if (null == obj && null == hashFieldName) {
            throw new SolrException
                    (SERVER_ERROR, "'hashFieldName' init param must be specified and non-null");
        } else {
            hashFieldName = obj.toString();
        }

        if (0 < args.size()) {
            throw new SolrException(SERVER_ERROR,
                    "Unexpected init param(s): '" +
                            args.getName(0) + "'");
        }

        super.init(args);
    }

    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        return new LinestringHashUpdateProcessor(this.linestringFieldName, this.hashFieldName, next);
    }


    private class LinestringHashUpdateProcessor
            extends UpdateRequestProcessor {

        final String linestringFieldName;
        final String hashFieldName;

        public LinestringHashUpdateProcessor(final String linestringFieldName,
                                             final String hashFieldName,
                                             final UpdateRequestProcessor next) {
            super(next);
            this.linestringFieldName = linestringFieldName;
            this.hashFieldName = hashFieldName;
        }

        @Override
        public void processAdd(AddUpdateCommand cmd) throws IOException {
            final SolrInputDocument doc = cmd.getSolrInputDocument();

            if (doc.containsKey(linestringFieldName)) {
                if (!doc.containsKey(hashFieldName)) {
                    doc.addField(hashFieldName, calculateHash(doc.getFieldValue(linestringFieldName)));
                }
            }

            super.processAdd(cmd);
        }

        private String calculateHash(Object fieldValue) {
            return LineStringUtils.cacheLineStringGetHash(fieldValue.toString());
        }
    }
}
