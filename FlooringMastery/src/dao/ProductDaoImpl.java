package dao;

import dto.Product;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductDaoImpl implements ProductDao {

    private static final String PRODUCTS_FILE = "Data/Products.txt";
    private static final String DELIMITER = ",";

    private Map<String, Product> productsCache = null;

    @Override
    public Product getProductInfo(String productType) throws Exception {
        Map<String, Product> cache = getCache();
        Product product = cache.get(productType.toLowerCase().trim());
        return product; // null if not found — service layer handles the null check
    }

    @Override
    public List<Product> getAllProducts() throws Exception {
        return new ArrayList<>(getCache().values());
    }

    private Map<String, Product> getCache() throws Exception {
        if (productsCache == null) {
            productsCache = loadProductsFromFile();
        }
        return productsCache;
    }

    /**
      Expected column order:
        0  ProductType
        1  CostPerSquareFoot
        2  LaborCostPerSquareFoot
     */
    private Map<String, Product> loadProductsFromFile() throws Exception {
        Map<String, Product> map = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(PRODUCTS_FILE))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (line.startsWith("ProductType")) continue;
                    // no header — fall through and parse
                }
                if (line.isBlank()) {
                    continue;
                }
                Product product = unmarshallProduct(line);
                map.put(product.getProductType().toLowerCase(), product);
            }
        } catch (IOException e) {
            throw new Exception("Error reading products file: " + e.getMessage(), e);
        }

        return map;
    }


     // Parses a single comma-delimited line into a Product.

    private Product unmarshallProduct(String line) throws Exception {
        String[] tokens = line.split(DELIMITER);

        if (tokens.length != 3) {
            throw new Exception("Malformed product line (expected 3 fields, got "
                    + tokens.length + "): " + line);
        }

        try {
            Product product = new Product();
            product.setProductType(tokens[0].trim());
            product.setCostPerSquareFoot(new BigDecimal(tokens[1].trim()));
            product.setLaborCostPerSquareFoot(new BigDecimal(tokens[2].trim()));
            return product;
        } catch (NumberFormatException e) {
            throw new Exception("Could not parse numeric field in product line: "
                    + line, e);
        }
    }
}