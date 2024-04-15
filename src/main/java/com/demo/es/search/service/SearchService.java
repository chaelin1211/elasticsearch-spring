package com.demo.es.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
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

    public List<SearchLogResponse> getSearchLogList(String searchWord) throws IOException {
        SearchResponse<SearchLogResponse> searchResponse = elasticsearchClient.search(q -> q
                        .index("search_log")
                        .query(qb -> qb.match(mq->mq.field("word").query(searchWord)))
                        .highlight(hv -> hv.fields(new HashMap<>() {{
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
