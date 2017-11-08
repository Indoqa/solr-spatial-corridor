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
package com.indoqa.solr.spatial.corridor.analyzer;

import com.indoqa.solr.spatial.corridor.LineStringUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.solr.analysis.SolrAnalyzer;

import java.io.IOException;

public class CacheLinestringsAnalyzer extends SolrAnalyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer ts = new CacheLinestringsTokenizer();

        return new TokenStreamComponents(ts);
    }

    private class CacheLinestringsTokenizer extends Tokenizer {
        final StringBuilder stringBuilder = new StringBuilder();
        final char[] buffer = new char[1024];
        final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
        final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

        @Override
        public boolean incrementToken() throws IOException {
            clearAttributes();

            int length;
            stringBuilder.setLength(0);
            while ((length = input.read(buffer)) > 0) {
                stringBuilder.append(buffer, 0, length);
            }
            String linestring = stringBuilder.toString();
            LineStringUtils.parseOrGet(linestring);
            termAtt.setEmpty().append(linestring);
            offsetAtt.setOffset(correctOffset(0),correctOffset(linestring.length()));
            return false;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                stringBuilder.setLength(0);
                stringBuilder.trimToSize();
            }
        }
    }
}
