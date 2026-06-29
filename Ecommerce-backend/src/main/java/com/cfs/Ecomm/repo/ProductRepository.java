package com.cfs.Ecomm.repo;

import com.cfs.Ecomm.model.Product;
import com.cfs.Ecomm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    List<Product> findByNameContainingIgnoreCase(String keyword);

}
