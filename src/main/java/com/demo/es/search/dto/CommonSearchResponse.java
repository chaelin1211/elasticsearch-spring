package com.demo.es.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonSearchResponse {
    private String originSearchWord;

    private String fixedSearchWord;

    private String recommendWord;
}
