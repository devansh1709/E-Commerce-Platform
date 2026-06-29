package com.cfs.Ecomm.service;

import com.cfs.Ecomm.exception.ResourceNotFoundException;
import com.cfs.Ecomm.model.Product;
import com.cfs.Ecomm.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    public List<Product> getAllProducts(){

        return productRepository.findAll();
    }

    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product product) {

        Product existing = getProductById(id);

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setCategory(product.getCategory());
        existing.setImageUrl(product.getImageUrl());

        return productRepository.save(existing);
    }

    public Product addProduct(Product product){

        return productRepository.save(product);
    }

    public List<Product> getProductsByCategory(String category) {

        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String keyword) {

        return productRepository.findByNameContainingIgnoreCase(keyword);
    }


    public void deleteProduct(Long id) {

        Product product = getProductById(id);

        productRepository.delete(product);
    }


}
