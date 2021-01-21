package com.smartosc.training.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductFormDTO {
	private int productId;
    private String productName;
    @JsonIgnore
    private String image;
    private String description;
    private double price;
    private int[] categoryIds;
    private String imageURL;
	private MultipartFile imageFile = null;
    
    List<CategoryDTO> categories;
}
