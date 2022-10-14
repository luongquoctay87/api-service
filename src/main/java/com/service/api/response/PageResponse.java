package com.service.api.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageResponse {
    private long pageNo;
    private long pageSize;
    private long total;
    private List<?> items;
}
