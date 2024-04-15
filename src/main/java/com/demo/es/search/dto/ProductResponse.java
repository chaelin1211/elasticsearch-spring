package com.demo.es.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String name;
    private String desc;
    private Map<String, List<String>> highlight;
}
