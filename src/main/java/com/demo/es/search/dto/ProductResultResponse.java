package com.demo.es.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResultResponse {
    private String name;

    private String desc;

    private Map<String, String> highlight;
}
