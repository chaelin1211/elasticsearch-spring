package com.demo.es.search.service;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.demo.es.search.dto.CommonSearchResponse;
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
    private final ElasticsearchService elasticsearchService;

    public CommonSearchResponse search(final String SEARCH_WORD) {
        SearchResponse<ProductResponse.Result> searchResponse;
        final List<String> FIELD_NAMES = List.of("name", "desc");
        final String INDEX_NAME = "products";
        List<String> recommend = null;
        String fixedSearchWord = "";

        try {
            searchResponse = elasticsearchService.multiMatch(SEARCH_WORD, products.name(), FIELD_NAMES, ProductResponse.Result.class);

            // 검색어와 유사한 결과 값 추출 - 추천어

            // 검색 결과가 없으면
            if (searchResponse.hits().hits().isEmpty()) {
                List<String> checkedTypoList = elasticsearchService.phraseSuggest(Index.KO_DICTIONARY, "value.spell", SEARCH_WORD, 2.0);

                // 1. 자동 오타 검수
                if(checkedTypoList != null && checkedTypoList.size() > 0) {
                    fixedSearchWord = checkedTypoList.getFirst();
                    searchResponse = elasticsearchService.multiMatch(fixedSearchWord, products, FIELD_NAMES, ProductResponse.Result.class);
                } else {
                // 2. 오타 검수 결과가 없으면 검색 이력에서 유사 값 추천
                    recommend = elasticsearchService.phraseSuggest(Index.SEARCH_LOG, "word", SEARCH_WORD, 2.0);
                }
            }
        } catch (IOException | ElasticsearchException e) {
            log.error(e.getMessage());
            return null;
        }

        List<ProductResponse.Result> productResponses = new ArrayList<>();
        searchResponse.hits().hits().forEach(hit -> {
            ProductResponse.Result result = hit.source();
            assert result != null;
            productResponses.add(ProductResponse.Result.builder()
                    .name(result.getName())
                    .desc(result.getDesc())
                    .highlight(hit.highlight())
                    .build());
        });

        return ProductResponse.builder()
                .result(productResponses)
                .originSearchWord(SEARCH_WORD)
                .fixedSearchWord(fixedSearchWord)
                .recommendWords(recommend)
                .build();
    }

    public List<SearchLogResponse> getRecommList(final String SEARCH_WORD) {
        SearchResponse<SearchLogResponse> searchResponse;
        final String FIELD_NAME = "word.ngram";

        try {
            searchResponse = elasticsearchService.match(SEARCH_WORD, Index.SEARCH_LOG, FIELD_NAME, SearchLogResponse.class);
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
