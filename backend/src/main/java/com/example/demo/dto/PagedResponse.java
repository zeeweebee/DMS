package com.example.demo.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PagedResponse<T> {

    private final List<T> content;
    private final int page;
    private final int pageSize;
    private final long totalCount;
    private final int totalPages;

    public PagedResponse(List<T> content, int page, int pageSize, long totalCount) {
        this.content = content;
        this.page = page;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
    }
}
