package service;

import dao.OrderDao;
import dao.ProductDao;
import dao.TaxesDao;
import dto.Order;
import dto.Product;
import dto.Taxes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FlooringServiceImpl implements FlooringService {

    // --- Constants -----------------------------------------------------------

    private static final BigDecimal MIN_AREA = new BigDecimal("100.00");
    private static final String ORDERS_DIR = "Orders";
    private static final String EXPORT_DIR = "DataExport";
    private static final String EXPORT_FILENAME = "DataExport.txt";
    private static final String EXPORT_HEADER =
            "OrderNumber,CustomerName,State,TaxRate,ProductType," +
                    "Area,CostPerSquareFoot,LaborCostPerSquareFoot," +
                    "MaterialCost,LaborCost,Tax,Total,OrderDate";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMddyyyy");

    // Valid customer name
    private static final String VALID_NAME = "[a-zA-Z0-9 .,]+";

    // DAOs
    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final TaxesDao taxesDao;

    public FlooringServiceImpl(OrderDao orderDao, ProductDao productDao, TaxesDao taxesDao) {
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.taxesDao = taxesDao;
    }


    @Override
    public List<Order> getOrders(LocalDate date) throws FlooringServiceException {
        File file = getOrderFile(date);
        if (!file.exists()) {
            throw new FlooringServiceException(
                    "No orders found for " + date + ". The file does not exist.");
        }
        try {
            List<Order> orders = new ArrayList<>();
            orders = orderDao.getAllOrders(file);
            if (orders.isEmpty()) {
                throw new FlooringServiceException("No orders found for " + date + ".");
            }
            return orders;
        }  catch (Exception e) {
            throw new FlooringServiceException(
                    "Could not read orders for " + date + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Order getOrder(LocalDate date, int orderNumber) throws FlooringServiceException {
        File file = getOrderFile(date);
        try {
            Order order = orderDao.getOrder(file, orderNumber);
            if (order == null) {
                throw new FlooringServiceException("Order #" + orderNumber + " not found for " + date + ".");
            }
            return order;
        } catch (FlooringServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new FlooringServiceException("Could not retrieve order: " + e.getMessage(), e);
        }
    }

    @Override
    public Order previewOrder(LocalDate date, String customerName, String state, String productType, BigDecimal area)
            throws FlooringServiceException {

        // Validate input
        validateFutureDate(date);
        validateCustomerName(customerName);
        Taxes taxes = validateAndGetState(state);
        Product product = validateAndGetProduct(productType);
        validateArea(area);

        // Calculate
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setState(state.toUpperCase().trim());
        order.setTaxRate(taxes.getTaxRate());
        order.setProductType(product.getProductType());
        order.setArea(area);
        order.setCostPerSquareFoot(product.getCostPerSquareFoot());
        order.setLaborCostPerSquareFoot(product.getLaborCostPerSquareFoot());
        calculateCosts(order);
        order.setOrderNumber(getNextOrderNumber(date));

        return order;
    }

    @Override
    public void saveOrder(LocalDate date, Order order) throws FlooringServiceException {
        try {
            File file = getOrderFile(date);
            orderDao.addOrder(file, order);
        } catch (Exception e) {
            throw new FlooringServiceException("Could not save order: " + e.getMessage(), e);
        }
    }

    @Override
    public Order previewEdit(LocalDate date, int orderNumber, String customerName, String state,
                             String productType, BigDecimal area)
            throws FlooringServiceException {

        Order existing = getOrder(date, orderNumber);

        // Fields that were left blank, keep; otherwise change them
        if (customerName != null && !customerName.isBlank()) {
            validateCustomerName(customerName);
            existing.setCustomerName(customerName);
        }

        if (state != null && !state.isBlank()) {
            Taxes taxes = validateAndGetState(state);
            existing.setState(state.toUpperCase().trim());
            existing.setTaxRate(taxes.getTaxRate());
        }

        if (productType != null && !productType.isBlank()) {
            Product product = validateAndGetProduct(productType);
            existing.setProductType(product.getProductType());
            existing.setCostPerSquareFoot(product.getCostPerSquareFoot());
            existing.setLaborCostPerSquareFoot(product.getLaborCostPerSquareFoot());
        }

        if (area != null) {
            validateArea(area);
            existing.setArea(area);
        }

        // Recalculate
        calculateCosts(existing);

        return existing;
    }

    @Override
    public void saveEdit(LocalDate date, Order order) throws FlooringServiceException {
        try {
            File file = getOrderFile(date);
            orderDao.editOrder(file, order);
        } catch (Exception e) {
            throw new FlooringServiceException("Could not save edited order: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeOrder(LocalDate date, int orderNumber) throws FlooringServiceException {
        // Check if it exists
        getOrder(date, orderNumber);
        try {
            File file = getOrderFile(date);
            orderDao.removeOrder(file, orderNumber);
        } catch (Exception e) {
            throw new FlooringServiceException("Could not remove order: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveAllOrders() throws FlooringServiceException {
        File ordersDir = new File(ORDERS_DIR);
        // We only know which dates are loaded via the DAO's memory
        // Ask the DAO to flush each file it knows about.
        try {
            orderDao.flushAllToDisk(ordersDir);
        } catch (Exception e) {
            throw new FlooringServiceException(
                    "Could not save orders to disk: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportAllData() throws FlooringServiceException {
        // First flush in-memory orders to disk so the export file reflects current state
        saveAllOrders();

        File exportFile = new File(EXPORT_DIR, EXPORT_FILENAME);
        File ordersDir = new File(ORDERS_DIR);

        File[] orderFiles = ordersDir.listFiles(
                (dir, name) -> name.startsWith("Orders_") && name.endsWith(".txt"));

        if (orderFiles == null || orderFiles.length == 0) {
            throw new FlooringServiceException("No order files found to export.");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(exportFile))) {
            writer.println(EXPORT_HEADER);
            for (File f : orderFiles) {
                String datePart = f.getName()
                        .replace("Orders_", "")
                        .replace(".txt", "");
                LocalDate orderDate = LocalDate.parse(datePart, DATE_FORMAT);

                List<Order> orders = orderDao.getAllOrders(f);
                for (Order o : orders) {
                    writer.println(o.toString() + "," + orderDate);
                }
            }
        } catch (IOException e) {
            throw new FlooringServiceException(
                    "Export failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FlooringServiceException(
                    "Export failed while reading orders: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Product> getAllProducts() throws FlooringServiceException {
        try {
            return productDao.getAllProducts();
        } catch (Exception e) {
            throw new FlooringServiceException(
                    "Could not load products: " + e.getMessage(), e);
        }
    }

    @Override
    public Taxes getTaxInfo(String stateAbbreviation) throws FlooringServiceException {
        return validateAndGetState(stateAbbreviation);
    }

    // File should be formatted like Orders_06012013.txt
    private File getOrderFile(LocalDate date) {
        File ordersDir = new File(ORDERS_DIR);
        String filename = "Orders_" + date.format(DATE_FORMAT) + ".txt";
        return new File(ordersDir, filename);
    }


    private int getNextOrderNumber(LocalDate date) throws FlooringServiceException {
        File file = getOrderFile(date);
        if (!file.exists()) {
            return 1;
        }
        try {
            List<Order> orders = orderDao.getAllOrders(file);
            return orders.stream()
                    .mapToInt(Order::getOrderNumber)
                    .max()
                    .orElse(0) + 1;
        } catch (Exception e) {
            throw new FlooringServiceException(
                    "Could not determine next order number: " + e.getMessage(), e);
        }
    }


     // MaterialCost = Area * CostPerSquareFoot
     //  LaborCost    = Area * LaborCostPerSquareFoot
     //  Tax          = (MaterialCost + LaborCost) * (TaxRate / 100)
     //  Total        = MaterialCost + LaborCost + Tax

    private void calculateCosts(Order order) {
        BigDecimal area     = order.getArea();
        BigDecimal cpsf     = order.getCostPerSquareFoot();
        BigDecimal lcpsf    = order.getLaborCostPerSquareFoot();
        BigDecimal taxRate  = order.getTaxRate();

        BigDecimal materialCost = area.multiply(cpsf);
        BigDecimal laborCost = area.multiply(lcpsf);
        BigDecimal tax = (materialCost.add(laborCost))
                .multiply(taxRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
        BigDecimal total = materialCost.add(laborCost).add(tax);

        order.setMaterialCost(materialCost);
        order.setLaborCost(laborCost);
        order.setTax(tax);
        order.setTotal(total);
    }


    private void validateFutureDate(LocalDate date) throws FlooringServiceException {
        if (!date.isAfter(LocalDate.now())) {
            throw new FlooringServiceException("Order date must be in the future. Provided: " + date);
        }
    }

    private void validateCustomerName(String name) throws FlooringServiceException {
        if (name == null || name.isBlank()) {
            throw new FlooringServiceException("Customer name may not be blank.");
        }
        if (!name.matches(VALID_NAME)) {
            throw new FlooringServiceException(
                    "Customer name may only contain letters, digits, spaces, " +
                            "periods, and commas. Provided: \"" + name + "\"");
        }
    }

    private Taxes validateAndGetState(String state) throws FlooringServiceException {
        if (state == null || state.isBlank()) {
            throw new FlooringServiceException("State may not be blank.");
        }
        try {
            Taxes taxes = taxesDao.getTaxesInfo(state.toUpperCase().trim());
            if (taxes == null) {
                throw new FlooringServiceException(
                        "State \"" + state + "\" is not available for orders. " +
                                "Check the tax file for supported states.");
            }
            return taxes;
        } catch (FlooringServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new FlooringServiceException(
                    "Could not look up state \"" + state + "\": " + e.getMessage(), e);
        }
    }

    private Product validateAndGetProduct(String productType)
            throws FlooringServiceException {
        if (productType == null || productType.isBlank()) {
            throw new FlooringServiceException("Product type may not be blank.");
        }
        try {
            Product product = productDao.getProductInfo(productType.trim());
            if (product == null) {
                throw new FlooringServiceException(
                        "Product \"" + productType + "\" is not available. " +
                                "Check the products file for supported types.");
            }
            return product;
        } catch (FlooringServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new FlooringServiceException(
                    "Could not look up product \"" + productType + "\": " + e.getMessage(), e);
        }
    }

    private void validateArea(BigDecimal area) throws FlooringServiceException {
        if (area == null || area.compareTo(MIN_AREA) < 0) {
            throw new FlooringServiceException(
                    "Area must be at least " + MIN_AREA + " sq ft. Provided: " + area);
        }
    }
}