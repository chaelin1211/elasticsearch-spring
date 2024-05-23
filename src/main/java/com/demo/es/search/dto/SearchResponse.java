package com.demo.es.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultResopnse<T> {
    private String originSearchWord;

    private String fixedSearchWord;

    private List<String> recommendWords;

    private List<T> result;
}
