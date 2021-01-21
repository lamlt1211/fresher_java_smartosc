package com.smartosc.training.service;

import com.smartosc.training.dto.PromotionDTO;
import com.smartosc.training.entity.Product;
import com.smartosc.training.entity.ProductPromotion;
import com.smartosc.training.entity.Promotion;
import com.smartosc.training.repositories.ProductPromotionRepository;
import com.smartosc.training.repositories.ProductRepository;
import com.smartosc.training.repositories.PromotionRepository;
import com.smartosc.training.utils.ConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductPromotionRepository productPromotionRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Create promotion
     *
     * @param promotionDTO
     * @return Promotion has created
     */
    @Transactional
    public PromotionDTO createPromotion(PromotionDTO promotionDTO) {
        Promotion promotion = ConvertUtils.convertPromotionDTOToPromotion(promotionDTO);
        List<Product> products = productRepository.findAllById(promotionDTO.getProductDTOIds());
        ProductPromotion productPromotion = null;
        for (Product product : products) {
            productPromotion = new ProductPromotion();
            productPromotion.setPromotion(promotion);
            productPromotion.setProduct(product);
            productPromotion.setPercent(promotionDTO.getPercent());
            productPromotion.setStartDate(promotionDTO.getStartDate());
            productPromotion.setEndDate(promotionDTO.getEndDate());
            productPromotion.setStatus(1);
            promotion.getProductPromotions().add(productPromotion);
        }
        return ConvertUtils.convertPromotionToPromotionDTO(promotionRepository.save(promotion));
    }

    /**
     * Create and Update promotion
     *
     * @param promotionDTO
     * @param id
     * @return promotion has updated
     */
    @Transactional
    public PromotionDTO createAndUpdatePromotion(PromotionDTO promotionDTO, Integer id) {
        Promotion promotion = null;
        if(id != null) {
            Optional<Promotion> responseData = promotionRepository.findById(id);
            productPromotionRepository.deleteByPromotion_promotionId(id);
            if(!responseData.isPresent()) {
                return null;
            } else {
                promotion = responseData.get();
                promotion.setStatus(promotionDTO.getStatus());
                promotion.setName(promotionDTO.getName());
            }
        } else {
            promotion = ConvertUtils.convertPromotionDTOToPromotion(promotionDTO);
        }
        List<Product> products = productRepository.findAllById(promotionDTO.getProductDTOIds());
        Set<ProductPromotion> newProductPromotionSet = new HashSet<>();
        for (Product product : products) {
            ProductPromotion productPromotion = new ProductPromotion();
            productPromotion.setPromotion(promotion);
            productPromotion.setProduct(product);
            productPromotion.setPercent(promotionDTO.getPercent());
            productPromotion.setStartDate(promotionDTO.getStartDate());
            productPromotion.setEndDate(promotionDTO.getEndDate());
            productPromotion.setStatus(1);
            newProductPromotionSet.add(productPromotion);
        }
        promotion.setProductPromotions(newProductPromotionSet);
        promotionRepository.save(promotion);
        return promotionDTO;
    }

    /**
     * Find All promotion with Pagination
     *
     * @param searchValue
     * @param pageNo
     * @param sizeNo
     * @param sortBy
     * @return Page of promotion
     */
    public Page<PromotionDTO> findAllPromotion(
            String searchValue, Integer pageNo, Integer sizeNo, String sortBy) {
        Pageable pageable = PageRequest.of(
                pageNo, sizeNo, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Promotion> promotions = promotionRepository.findBySearchValue(searchValue, pageable);
        return promotions != null ? promotions.map(ConvertUtils::convertPromotionToPromotionDTO) : null;
    }

    /**
     * Delete promotion (soft delete)
     *
     * @param id
     * @return promotion already deleted (status = 0)
     */
    @Transactional
    public PromotionDTO deletePromotion(Integer id) {
        Promotion responseData = null;
        Optional<Promotion> result = promotionRepository.findById(id);
        if(result.isPresent()) {
            Promotion promotion = result.get();
            promotion.setStatus(0);
            responseData = promotionRepository.save(promotion);
            return ConvertUtils.convertPromotionToPromotionDTO(responseData);
        }  else {
            return null;
        }
    }

    /**
     * Find promotion by id
     *
     * @param id
     * @return promotionDTO
     */
    public PromotionDTO findById(Integer id) {
        Optional<Promotion> result = promotionRepository.findById(id);
        return result.isPresent() ? ConvertUtils.convertPromotionToPromotionDTO(result.get()) : null;
    }

}
