package com.demo.es.search;

public enum Index {
    SEARCH_LOG("search_log"),
    KO_DICTIONARY("ko_dictionary"),
    PRODUCTS("products");
    private final String name;

    Index(String name) {
        this.name = name;
    }
}
