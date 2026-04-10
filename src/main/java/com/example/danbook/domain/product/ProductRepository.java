package com.example.danbook.domain.product;

import com.example.danbook.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySellerOrderByCreatedAtDesc(User seller);

    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR p.category = :category) " +
           "ORDER BY p.createdAt DESC")
    List<Product> searchProducts(@Param("keyword") String keyword, @Param("category") Category category);
}
