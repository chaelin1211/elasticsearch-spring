package com.demo.es.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Getter
@SuperBuilder
public class ProductResponse extends CommonSearchResponse {
    private List<ProductInfo> productInfos;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private String name;
        private String desc;
        private Map<String, List<String>> highlight;
    }
}
