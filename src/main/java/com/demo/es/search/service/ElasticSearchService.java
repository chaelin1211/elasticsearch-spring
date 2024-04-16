package com.demo.es.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ElasticSearchService {
    private final ElasticsearchClient elasticsearchClient;

    public <T> SearchResponse<T> simpleMultiSearch(final String SEARCH_WORD, final String INDEX_NAME, final List<String> FIELD_NAMES, final Class<T> T) throws IOException {
        return elasticsearchClient.search(sb -> sb
                        .index(INDEX_NAME)
                        .query(qb -> qb.multiMatch(mb -> mb.query(SEARCH_WORD).fields(FIELD_NAMES)))
                        .highlight(hb -> {
                            FIELD_NAMES.forEach(
                                    field -> hb.fields(field, new HighlightField.Builder().build())
                            );

                            hb.preTags("<strong>")
                                    .postTags("</strong>")
                                    .boundaryChars("");

                            return hb;
                        })
                , T);
    }

    public <T> SearchResponse<T> simpleSingleSearch(final String SEARCH_WORD, final String INDEX_NAME, final String FIELD_NAME, final Class<T> T) throws IOException {
        return elasticsearchClient.search(sb -> sb
                        .index(INDEX_NAME)
                        .query(qb -> qb.match(mb -> mb.field(FIELD_NAME).query(SEARCH_WORD)))
                        .highlight(hb -> hb
                                .fields(FIELD_NAME, new HighlightField.Builder().build())
                                .preTags("<strong>")
                                .postTags("</strong>")
                                .boundaryChars(""))
                , T);
    }
}
