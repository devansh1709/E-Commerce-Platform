package com.cfs.Ecomm.repo;

import com.cfs.Ecomm.model.Product;
import com.cfs.Ecomm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
