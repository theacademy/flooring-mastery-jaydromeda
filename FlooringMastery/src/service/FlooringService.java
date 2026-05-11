package service;

import dto.Order;
import dto.Product;
import dto.Taxes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FlooringService {


    List<Order> getOrders(LocalDate date) throws FlooringServiceException;

    Order getOrder(LocalDate date, int orderNumber) throws FlooringServiceException;

    Order previewOrder(LocalDate date, String customerName, String state,
                       String productType, BigDecimal area)
            throws FlooringServiceException;

    void saveOrder(LocalDate date, Order order) throws FlooringServiceException;

    Order previewEdit(LocalDate date, int orderNumber,
                      String customerName, String state,
                      String productType, BigDecimal area)
            throws FlooringServiceException;


    void saveEdit(LocalDate date, Order order) throws FlooringServiceException;

    void removeOrder(LocalDate date, int orderNumber) throws FlooringServiceException;

    void saveAllOrders() throws FlooringServiceException;

    void exportAllData() throws FlooringServiceException;

    List<Product> getAllProducts() throws FlooringServiceException;

    Taxes getTaxInfo(String stateAbbreviation) throws FlooringServiceException;
}