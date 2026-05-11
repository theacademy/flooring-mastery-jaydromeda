package dao;

import dto.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductDaoStubImpl implements ProductDao {

    private final Map<String, Product> products = new LinkedHashMap<>();

    public ProductDaoStubImpl() {
        products.put("carpet",   makeProduct("Carpet",   "2.25", "2.10"));
        products.put("tile",     makeProduct("Tile",     "3.50", "4.15"));
        products.put("laminate", makeProduct("Laminate", "1.75", "2.10"));
        products.put("wood",     makeProduct("Wood",     "5.15", "4.75"));
    }

    @Override
    public Product getProductInfo(String productType) {
        return products.get(productType.toLowerCase().trim());
    }

    @Override
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    private Product makeProduct(String type, String cost, String labor) {
        Product p = new Product();
        p.setProductType(type);
        p.setCostPerSquareFoot(new BigDecimal(cost));
        p.setLaborCostPerSquareFoot(new BigDecimal(labor));
        return p;
    }
}
