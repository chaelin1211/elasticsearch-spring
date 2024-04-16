package com.demo.es.search.service;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.demo.es.search.dto.ProductResponse;
import com.demo.es.search.dto.SearchLogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final ElasticSearchService elasticSearchService;

    public List<ProductResponse> search(final String SEARCH_WORD) {
        SearchResponse<ProductResponse> searchResponse;
        final List<String> FIELD_NAMES = List.of("name", "desc");

        try {
            searchResponse = elasticSearchService.simpleMultiSearch(SEARCH_WORD, "products", FIELD_NAMES, ProductResponse.class);
        } catch (IOException | ElasticsearchException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }

        List<ProductResponse> productResponses = new ArrayList<>();
        searchResponse.hits().hits().forEach(hit -> {
            ProductResponse result = hit.source();
            assert result != null;
            productResponses.add(ProductResponse.builder()
                    .name(result.getName())
                    .desc(result.getDesc())
                    .highlight(hit.highlight())
                    .build());
        });

        return productResponses;
    }

    public List<SearchLogResponse> getRecommList(final String SEARCH_WORD) {
        SearchResponse<SearchLogResponse> searchResponse;
        final String FIELD_NAME = "word.ngram";

        try {
            searchResponse = elasticSearchService.simpleSingleSearch(SEARCH_WORD, "search_log", FIELD_NAME, SearchLogResponse.class);
        } catch (IOException | ElasticsearchException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }

        List<SearchLogResponse> searchLogResponses = new ArrayList<>();
        searchResponse.hits().hits().forEach(hit -> {
            SearchLogResponse result = hit.source();
            assert result != null;
            searchLogResponses.add(SearchLogResponse.builder()
                    .word(result.getWord())
                    .highlight(hit.highlight().get(FIELD_NAME))
                    .build());
        });

        return searchLogResponses;
    }
}
