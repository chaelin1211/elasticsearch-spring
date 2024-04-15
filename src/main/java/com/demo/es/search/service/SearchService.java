package com.demo.es.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import com.demo.es.search.dto.ProductResponse;
import com.demo.es.search.dto.SearchLogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final ElasticsearchClient elasticsearchClient;

    public List<ProductResponse> getProducts(String searchWord) {
        SearchResponse<ProductResponse> searchResponse;

        try {
            searchResponse = elasticsearchClient.search(sb -> sb
                            .index("products")
                            .query(qb -> qb.multiMatch(mb -> mb.query(searchWord).fields(List.of("name^2", "desc"))))
                            .highlight(hb -> hb.fields(new HashMap<>() {{
                                put("name", HighlightField.of(hf -> hf.type("plain")));
                                put("desc", HighlightField.of(hf -> hf.type("plain")));
                            }}))
                    , ProductResponse.class);
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

    public List<SearchLogResponse> getSearchLogList(String searchWord) {
        SearchResponse<SearchLogResponse> searchResponse;

        try {
            searchResponse = elasticsearchClient.search(sb -> sb
                            .index("search_log")
                            .query(qb -> qb.match(mb -> mb.field("word").query(searchWord)))
                            .highlight(hb -> hb.fields(new HashMap<>() {{
                                put("word", HighlightField.of(hf -> hf.type("plain")));
                            }}))
                    , SearchLogResponse.class);
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
                    .highlight(hit.highlight().get("word"))
                    .build());
        });

        return searchLogResponses;
    }
}
