package com.demo.es.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.PhraseSuggest;
import co.elastic.clients.elasticsearch.core.search.PhraseSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import com.demo.es.search.Index;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchService {
    private final ElasticsearchClient elasticsearchClient;

    public <T> SearchResponse<T> multiMatch(final String SEARCH_WORD, final Index INDEX, final List<String> FIELD_NAMES, final Class<T> T) throws IOException {
        return multiMatch(SEARCH_WORD, INDEX.name(), FIELD_NAMES, T);
    }

    public <T> SearchResponse<T> multiMatch(final String SEARCH_WORD, final String INDEX_NAME, final List<String> FIELD_NAMES, final Class<T> T) throws IOException {
        return elasticsearchClient.search(sb -> sb
                        .index(INDEX_NAME)
                        .query(qb -> qb.multiMatch(mb -> mb.query(SEARCH_WORD).fields(FIELD_NAMES)))
                        .highlight(hb -> {
                            FIELD_NAMES.forEach(
                                    field -> hb.fields(field, new HighlightField.Builder()
                                            .build())
                            );

                            hb.preTags("<strong>")
                                    .postTags("</strong>")
                                    .boundaryChars("");

                            return hb;
                        })
                , T);
    }

    public <T> SearchResponse<T> match(final String SEARCH_WORD, final Index INDEX, final String FIELD_NAME, final Class<T> T) throws IOException {
        return match(SEARCH_WORD, INDEX.name(), FIELD_NAME, T);
    }

    public <T> SearchResponse<T> match(final String SEARCH_WORD, final String INDEX_NAME, final String FIELD_NAME, final Class<T> T) throws IOException {
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

    public List<String> phraseSuggest(Index INDEX, String FIELD_NAME, String SEARCH_WORD, Double maxError) {
        return phraseSuggest(INDEX.name(), FIELD_NAME, SEARCH_WORD, maxError);
    }

    public List<String> phraseSuggest(String INDEX_NAME, String FIELD_NAME, String SEARCH_WORD, Double maxError) {
        final String SUGGESTER_KEY = "phrase_suggest";
        SearchResponse<PhraseSuggest> searchResponse;
        try {
            searchResponse = elasticsearchClient.search(sb -> sb
                    .index(INDEX_NAME)
                    .suggest(s -> s
                            .suggesters(SUGGESTER_KEY, fsb -> fsb
                                    .text(SEARCH_WORD)
                                    .phrase(ps -> ps
                                            .field(FIELD_NAME)
                                            .maxErrors(maxError))
                            )), PhraseSuggest.class);
        } catch (IOException | ElasticsearchException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }

        return getSuggestWords(searchResponse, SUGGESTER_KEY);
    }

    public <T> List<String> getSuggestWords(SearchResponse<T> searchResponse, final String SUGGESTER_KEY) {
        List<Suggestion<T>> suggestList = searchResponse.suggest().get(SUGGESTER_KEY);
        if(suggestList != null && !suggestList.isEmpty()) {
            Suggestion<T> phraseSuggestSuggestion = suggestList.getFirst();
            List<PhraseSuggestOption> options = phraseSuggestSuggestion != null ? phraseSuggestSuggestion.phrase().options() : null;

            return options != null && !options.isEmpty() ? options.stream().map(PhraseSuggestOption::text).toList() : Collections.emptyList();
        } else {
            return Collections.emptyList();
        }
    }
}
