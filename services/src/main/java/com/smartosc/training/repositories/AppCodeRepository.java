package com.smartosc.training.repositories;

import com.smartosc.training.entity.AppCode;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


/**
 * fres-parent
 *
 * @author Namtt
 * @created_at 20/04/2020 - 5:58 PM
 * @created_by Namtt
 * @since 20/04/2020
 */

public interface AppCodeRepository  extends JpaRepository<AppCode, Integer> {
    @Query("SELECT a FROM AppCode a  WHERE a.code LIKE %:searchValue% or a.description LIKE %:searchValue% ")
    List<AppCode> findByCodeOrDescription(@Param("searchValue") String searchValue, Pageable pageable);

    AppCode findByCode(String code);

}
