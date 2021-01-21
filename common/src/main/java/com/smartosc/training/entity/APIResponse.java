package com.smartosc.training.entity;

import com.smartosc.training.dto.PageMetaData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class APIResponse<T> {
	private int status;
	private String message;
	private T data;
	private PageMetaData pageMetadata;
}
