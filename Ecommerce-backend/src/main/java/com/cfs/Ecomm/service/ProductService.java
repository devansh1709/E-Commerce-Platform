package com.cfs.Ecomm.service;

import com.cfs.Ecomm.exception.ResourceNotFoundException;
import com.cfs.Ecomm.model.Product;
import com.cfs.Ecomm.repo.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts(){

        return productRepository.findAll();
    }

    @Cacheable(value = "product", key = "#id")
    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public Product updateProduct(Long id, Product product) {

        Product existing = getProductById(id);

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setCategory(product.getCategory());
        existing.setImageUrl(product.getImageUrl());

        return productRepository.save(existing);
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public Product addProduct(Product product){

        return productRepository.save(product);
    }

    public List<Product> getProductsByCategory(String category) {

        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String keyword) {

        return productRepository.findByNameContainingIgnoreCase(keyword);
    }


    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void deleteProduct(Long id) {

        Product product = getProductById(id);

        productRepository.delete(product);
    }


}
