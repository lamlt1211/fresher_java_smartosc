package com.smartosc.training.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartosc.training.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
	@Query("from Category c where c.name like %?1")
	List<Category> findByName(String name);

	@Query("from Category c where c.name like %?1%")
	Category findByOneName(String name);

	List<Category> findByProducts_Name(String name);

	@Query("SELECT c FROM Category c"
			+ " WHERE (c.name LIKE %:searchValue% OR c.description LIKE %:searchValue%)")
	Page<Category> findBySearchValue(@Param("searchValue") String searchValue, Pageable pageable);
}
