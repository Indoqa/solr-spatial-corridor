package com.indoqa.solr.spatial.corridor;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

public class CorridorQueryParserPlugin extends QParserPlugin {

    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        return new CorridorQueryParser(qstr, localParams, params, req);
    }

    @Override
    public void init(NamedList args) {

    }

}
