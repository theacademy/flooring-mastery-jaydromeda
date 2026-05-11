package dao;

import dto.Product;

import java.util.List;

public interface ProductDao {

    Product getProductInfo(String productType)
            throws Exception;

    List<Product> getAllProducts()
            throws Exception;
}