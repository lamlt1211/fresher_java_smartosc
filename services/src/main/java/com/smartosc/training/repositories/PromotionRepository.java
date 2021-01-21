package com.smartosc.training.repositories;

import com.smartosc.training.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    @Query("SELECT DISTINCT pro FROM Promotion pro LEFT JOIN pro.productPromotions pp LEFT JOIN pp.product"
            + " WHERE pro.name LIKE %:searchValue%")
    Page<Promotion> findBySearchValue(@Param("searchValue") String searchValue, Pageable pageable);
}
