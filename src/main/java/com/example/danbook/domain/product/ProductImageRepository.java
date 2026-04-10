package com.example.danbook.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct(Product product);
    Optional<ProductImage> findFirstByProductOrderByIdAsc(Product product);

    @Transactional
    void deleteByProduct(Product product);
}
