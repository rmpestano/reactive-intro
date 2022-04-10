package com.example.mvc;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Document(indexName = "person")
record Person(@Id String id, String name) {

    @Builder(toBuilder = true)
    Person {
    }
}
