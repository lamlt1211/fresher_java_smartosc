package com.smartosc.training.dto;

import java.util.List;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductDTO extends AbstractDTO {
	private int productId;
    private String productName;
    private String image;
    private String description;
    private double price;
    private int[] categoryIds;
    
    List<CategoryDTO> categories;
    List<PromotionDTO> promotions;
    
    public ProductDTO(ProductDTO productDTO) {
        this.productId = productDTO.productId;
        this.productName = productDTO.productName;
        this.price = productDTO.price;
        this.image = productDTO.image;
        this.description = productDTO.description;
        this.promotions = productDTO.promotions;
    }

    public void setProducts(List<ProductDTO> productDTOS) {
    }
}
