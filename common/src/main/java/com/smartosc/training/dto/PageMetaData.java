package com.smartosc.training.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageMetaData {
    private int page;
    private int size;
    private int number;
    private int totalPages;
    private long totalElements;
    private boolean last;
    private boolean first;
}