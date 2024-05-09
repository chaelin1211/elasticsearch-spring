package com.demo.es.search.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommonFieldRequest {
    private String name;

    private HighlightOption highlightOption;

    @Getter
    @Builder
    public static class HighlightOption {
        private int fragmentSize;

        private int noMatchSize;

        private int numberOfFragments;
    }
}
