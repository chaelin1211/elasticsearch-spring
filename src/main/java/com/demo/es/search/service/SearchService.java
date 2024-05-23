package com.demo.es.search.service;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.demo.es.search.Index;
import com.demo.es.search.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final ElasticsearchService elasticsearchService;

    public CommonSearchResponse<ProductResultResponse> search(final String searchWord) {
        SearchResponse<ProductResultResponse> searchResponse;
        final Index PRODUCTS = Index.PRODUCTS;
        final List<CommonFieldRequest> FIELD_OPTIONS = List.of(
                CommonFieldRequest.builder().name("name").highlightOption(
                        CommonFieldRequest.HighlightOption.builder()
                                .noMatchSize(99)
                                .build()
                ).build()
                , CommonFieldRequest.builder().name("desc").highlightOption(
                        CommonFieldRequest.HighlightOption.builder()
                                .fragmentSize(50)
                                .noMatchSize(50)
                                .numberOfFragments(1)
                                .build()
                ).build());

        List<String> recommend = null;
        String fixedSearchWord = "";

        try {
            searchResponse = elasticsearchService.multiMatch(searchWord, PRODUCTS, FIELD_OPTIONS, ProductResultResponse.class);

            // 검색어와 유사한 결과 값 추출 - 추천어

            // 검색 결과가 없으면
            if (searchResponse.hits().hits().isEmpty()) {
                List<String> checkedTypoList = elasticsearchService.phraseSuggest(Index.KO_DICTIONARY, "value.spell", searchWord, 2.0);

                // 1. 자동 오타 검수
                if (checkedTypoList != null && checkedTypoList.size() > 0) {
                    fixedSearchWord = checkedTypoList.getFirst();
                    searchResponse = elasticsearchService.multiMatch(fixedSearchWord, PRODUCTS, FIELD_OPTIONS, ProductResultResponse.class);
                } else {
                    // 2. 오타 검수 결과가 없으면 검색 이력에서 유사 값 추천
                    recommend = elasticsearchService.phraseSuggest(Index.SEARCH_LOGS, "word", searchWord, 2.0);
                }
            }
        } catch (IOException | ElasticsearchException e) {
            log.error(e.getMessage());
            return null;
        }

        List<ProductResultResponse> productResultResponses = new ArrayList<>();
        searchResponse.hits().hits().forEach(hit -> {
            ProductResultResponse result = hit.source();
            if(result == null) return;

            Map<String, List<String>> highlight = hit.highlight();
            Map<String, String> highlightResult = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : highlight.entrySet()) {
                List<String> values = entry.getValue();
                if (!values.isEmpty()) {
                    highlightResult.put(entry.getKey(), values.get(0));
                }
            }

            productResultResponses.add(ProductResultResponse.builder()
                    .name(result.getName())
                    .desc(result.getDesc())
                    .highlight(highlightResult)
                    .build());
        });

        return CommonSearchResponse.<ProductResultResponse>builder()
                .result(productResultResponses)
                .originSearchWord(searchWord)
                .fixedSearchWord(fixedSearchWord)
                .recommendWords(recommend)
                .build();
    }

    public List<SearchLogResponse> getRecommList(final String searchWord) {
        SearchResponse<SearchLogResponse> searchResponse;
        final CommonFieldRequest FIELD_OPTION = CommonFieldRequest.builder()
                .name("word.ngram")
                .build();

        try {
            searchResponse = elasticsearchService.match(searchWord, Index.SEARCH_LOGS, FIELD_OPTION, SearchLogResponse.class);
        } catch (IOException | ElasticsearchException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }

        List<SearchLogResponse> searchLogResponses = new ArrayList<>();
        searchResponse.hits().hits().forEach(hit -> {
            SearchLogResponse result = hit.source();
            if(result == null) return;

            List<String> highlights = hit.highlight().get(FIELD_OPTION.getName());
            String highlight = highlights == null || highlights.isEmpty() ? null : highlights.get(0);

            searchLogResponses.add(SearchLogResponse.builder()
                    .word(result.getWord())
                    .highlight(highlight)
                    .build());
        });

        return searchLogResponses;
    }
}
