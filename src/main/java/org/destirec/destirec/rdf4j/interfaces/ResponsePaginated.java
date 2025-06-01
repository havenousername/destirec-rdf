package org.destirec.destirec.rdf4j.interfaces;

public record ResponsePaginated<T>(T data, Pagination pagination) {
    public record Pagination(int page, long total, int offset) {}
}