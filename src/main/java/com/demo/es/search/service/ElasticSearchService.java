package com.demo.es.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.PhraseSuggest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticSearchService {
    private final ElasticsearchClient elasticsearchClient;

    public String checkTypo(final String SEARCH_WORD) {
        final String INDEX_NAME = "ko_dictionary";
        SearchResponse<PhraseSuggest> searchResponse;
        final String SUGGESTER_KEY = "fix_typo";

        try {
            searchResponse = elasticsearchClient.search(sb -> sb
                    .index(INDEX_NAME)
                    .suggest(s -> s
                            .suggesters(SUGGESTER_KEY, fsb -> fsb
                                    .text(SEARCH_WORD)
                                    .phrase(ps -> ps
                                            .field("value.spell")
                                            .maxErrors(2.0))
                            )), PhraseSuggest.class);
        } catch (IOException | ElasticsearchException e) {
            log.error(e.getMessage());
            return null;
        }

        try {
            return searchResponse.suggest().get(SUGGESTER_KEY).getFirst().phrase().options().getFirst().text();
        } catch (NoSuchElementException | NullPointerException e) {
            log.error(e.getMessage());
            return null;
        }
    }

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
