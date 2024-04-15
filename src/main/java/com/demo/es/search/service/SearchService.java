package com.demo.es.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import com.demo.es.search.dto.ProductResponse;
import com.demo.es.search.dto.SearchLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final ElasticsearchClient elasticsearchClient;

    public List<ProductResponse> getProducts(String searchWord) throws IOException {
        SearchResponse<ProductResponse> searchResponse = elasticsearchClient.search(sb -> sb
                        .index("products")
                        .query(qb -> qb.bool(bb -> bb
                                .should(List.of(
                                    QueryBuilders.match(mq -> mq.field("name").query(searchWord)),
                                    QueryBuilders.match(mq -> mq.field("desc").query(searchWord)))
                                )
                        ))
                        .highlight(hb -> hb.fields(new HashMap<>() {{
                            put("name", HighlightField.of(hf -> hf.type("plain")));
                            put("desc", HighlightField.of(hf -> hf.type("plain")));
                        }}))
                , ProductResponse.class);

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

    public List<SearchLogResponse> getSearchLogList(String searchWord) throws IOException {
        SearchResponse<SearchLogResponse> searchResponse = elasticsearchClient.search(sb -> sb
                        .index("search_log")
                        .query(qb -> qb.match(mb -> mb.field("word").query(searchWord)))
                        .highlight(hb -> hb.fields(new HashMap<>() {{
                            put("word", HighlightField.of(hf -> hf.type("plain")));
                        }}))
                , SearchLogResponse.class);

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
