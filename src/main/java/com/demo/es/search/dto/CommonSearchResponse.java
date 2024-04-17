package com.demo.es.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CommonSearchResponse {
    private String originSearchWord;

    private String fixedSearchWord;

    private List<String> recommendWords;
}
