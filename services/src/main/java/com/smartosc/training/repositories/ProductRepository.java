package com.smartosc.training.repositories;

import com.smartosc.training.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("from Product p where p.name like %?1")
    List<Product> findByNameProduct(String nameProduct);

    @Query("from Product p where p.name like %?1%")
    Product findBy1NameProduct(String nameProduct);

    @Query("FROM Product p WHERE p.name LIKE %:searchValue% OR p.description LIKE %:searchValue%")
    Page<Product> searchByNameAndDes(@Param("searchValue") String searchValue, Pageable pageable);

    List<Product> findByCategories_CategoryId(Integer categoryId);

    List<Product> findByCategories_Name(String name);

    @Query(value = "SELECT p FROM Product p INNER JOIN p.categories c WHERE c.categoryId = :categoryId")
    Page<Product> getAllProductBycategory(@Param("categoryId") Integer categoryId, Pageable pageable);

    @Query("from Product p where p.name like %?1")
    Product findByOneName(String name);

    @Query(value = "SELECT p1 FROM Product p1 INNER JOIN ProductPromotion p3 ON p1.productId = p3.product.productId " +
            "INNER JOIN Promotion p2 ON p2.promotionId = p3.promotion.promotionId group by p1.productId")
    List<Product> getTop8NewestPromotedProducts(Pageable pageable);

    @Query(value = "select product.* from product\n" +
            "                                left join product_promotion on product.product_id = product_promotion.product_id\n" +
            "                                left join promotion on product_promotion.promotion_id = promotion.promotion_id\n" +
            "                                inner join orderdetail on product.product_id = orderdetail.product_id\n" +
            "                                inner join orders on orderdetail.orders_id = orders.order_id\n" +
            "                                WHERE orders.status = 2 GROUP BY product.name ORDER BY COUNT(product.name) DESC LIMIT 8", nativeQuery = true)
    List<Product> findHotProducts();


    @Query(value = "SELECT p1 FROM Product p1 INNER JOIN ProductPromotion p3 ON p1.productId = p3.product.productId " +
            "INNER JOIN Promotion p2 ON p2.promotionId = p3.promotion.promotionId WHERE p2.status = 1" +
            "GROUP BY p1.productId order by p1.updatedAt DESC ")
    Page<Product> getAllProductAndPromotion(Pageable pageable);
}
