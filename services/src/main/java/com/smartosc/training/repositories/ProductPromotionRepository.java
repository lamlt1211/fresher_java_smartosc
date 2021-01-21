package com.smartosc.training.repositories;

import com.smartosc.training.entity.ProductPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPromotionRepository extends JpaRepository<ProductPromotion, Integer> {

    void deleteByPromotion_promotionId(Integer id);
}
