package com.demo.es.search;

import lombok.Getter;

@Getter
public enum Index {
    SEARCH_LOGS("search_logs"),
    KO_DICTIONARY("ko_dictionary"),
    PRODUCTS("products");
    private final String name;

    Index(String name) {
        this.name = name;
    }
}
