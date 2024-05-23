package com.demo.es.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.PhraseSuggest;
import co.elastic.clients.elasticsearch.core.search.PhraseSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import com.demo.es.search.Index;
import com.demo.es.search.dto.FieldRequest;
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

    public <T> SearchResponse<T> multiMatch(final String searchWord, final Index index, final List<FieldRequest> fieldNames, final Class<T> T) throws IOException {
        return multiMatch(searchWord, index.getName(), fieldNames, T);
    }

    public <T> SearchResponse<T> multiMatch(final String searchWord, final String indexName, final List<FieldRequest> fieldRequest, final Class<T> T) throws IOException {
        return elasticsearchClient.search(sb -> sb
                        .index(indexName)
                        .query(qb -> qb.multiMatch(mb -> mb.query(searchWord).fields(fieldRequest.stream().map(FieldRequest::getName).toList())))
                        .highlight(hb -> {
                            fieldRequest.forEach(
                                    field -> hb.fields(field.getName(), new HighlightField.Builder()
                                            .fragmentSize(field.getHighlightOption().getFragmentSize())
                                            .numberOfFragments(field.getHighlightOption().getNumberOfFragments())
                                            .noMatchSize(field.getHighlightOption().getNoMatchSize())
                                            .build())
                            );

                            hb.preTags("<strong>")
                                    .postTags("</strong>")
                                    .boundaryChars("");

                            return hb;
                        })
                , T);
    }

    public <T> SearchResponse<T> match(final String searchWord, final Index index, final FieldRequest fieldRequest, final Class<T> T) throws IOException {
        return match(searchWord, index.getName(), fieldRequest, T);
    }

    public <T> SearchResponse<T> match(final String searchWord, final String indexName, final FieldRequest fieldRequest, final Class<T> T) throws IOException {
        String fieldName = fieldRequest.getName();
        FieldRequest.HighlightOption highlightOption = fieldRequest.getHighlightOption();
        HighlightField highlightField = highlightOption != null
                ? new HighlightField.Builder().noMatchSize(highlightOption.getNoMatchSize())
                    .fragmentSize(highlightOption.getFragmentSize())
                    .numberOfFragments(highlightOption.getNumberOfFragments())
                    .build()
                : new HighlightField.Builder().build();
        return elasticsearchClient.search(sb -> sb
                        .index(indexName)
                        .query(qb -> qb.match(mb -> mb.field(fieldName).query(searchWord)))
                        .highlight(hb -> hb
                                .fields(fieldName, highlightField)
                                .preTags("<strong>")
                                .postTags("</strong>")
                                .boundaryChars(""))
                , T);
    }

    public List<String> phraseSuggest(Index index, String fieldName, String searchWord, Double maxError) {
        return phraseSuggest(index.getName(), fieldName, searchWord, maxError);
    }

    public List<String> phraseSuggest(final String indexName, final String fieldName, final String searchWord, final Double maxError) {
        final String SUGGESTER_KEY = "phrase_suggest";
        SearchResponse<PhraseSuggest> searchResponse;
        try {
            searchResponse = elasticsearchClient.search(sb -> sb
                    .index(indexName)
                    .suggest(s -> s
                            .suggesters(SUGGESTER_KEY, fsb -> fsb
                                    .text(searchWord)
                                    .phrase(ps -> ps
                                            .field(fieldName)
                                            .maxErrors(maxError))
                            )), PhraseSuggest.class);
        } catch (IOException | ElasticsearchException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }

        return getSuggestWords(searchResponse, SUGGESTER_KEY);
    }

    private <T> List<String> getSuggestWords(SearchResponse<T> searchResponse, final String suggesterKey) {
        List<Suggestion<T>> suggestList = searchResponse.suggest().get(suggesterKey);
        if (suggestList != null && !suggestList.isEmpty()) {
            Suggestion<T> phraseSuggestSuggestion = suggestList.getFirst();
            List<PhraseSuggestOption> options = phraseSuggestSuggestion != null ? phraseSuggestSuggestion.phrase().options() : null;

            return options != null && !options.isEmpty() ? options.stream().map(PhraseSuggestOption::text).toList() : Collections.emptyList();
        } else {
            return Collections.emptyList();
        }
    }
}
