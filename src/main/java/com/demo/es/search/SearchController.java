package com.demo.es.search;

import com.demo.es.search.dto.ProductResponse;
import com.demo.es.search.dto.SearchLogResponse;
import com.demo.es.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping()
    public List<ProductResponse> getProducts(@RequestParam String searchWord) throws IOException {
        return searchService.search(searchWord);
    }

    @GetMapping("/completion")
    public List<SearchLogResponse> getSearchLogList(@RequestParam String searchWord) throws IOException {
        return searchService.getRecommList(searchWord);
    }
}
