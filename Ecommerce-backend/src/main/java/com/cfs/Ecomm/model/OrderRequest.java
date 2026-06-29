package com.cfs.Ecomm.model;

import jakarta.persistence.Entity;

import java.util.Map;


public class OrderRequest {

    private Map<Long, Integer> productQuantities;

    public Map<Long, Integer> getProductQuantities() {
        return productQuantities;
    }

    public void setProductQuantities(Map<Long, Integer> productQuantities) {
        this.productQuantities = productQuantities;
    }

}
