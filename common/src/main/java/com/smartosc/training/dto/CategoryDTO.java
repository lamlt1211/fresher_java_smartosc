package com.smartosc.training.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO extends AbstractDTO{
	private int categoryId;
    private String description;
    private String image;
    private String categoryName;

    List<ProductDTO> products;
}
