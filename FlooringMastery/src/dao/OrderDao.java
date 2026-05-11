package dao;

import dto.Order;

import java.io.File;
import java.util.List;

public interface OrderDao {

    void addOrder(File file, Order order)
            throws Exception;

    Order getOrder(File file, int orderNumber)
            throws Exception;

    List<Order> getAllOrders(File file)
            throws Exception;

    void editOrder(File file, Order order)
            throws Exception;

    void removeOrder(File file, int orderNumber)
            throws Exception;

    void flushToDisk(File file)
            throws Exception;

    void flushAllToDisk(File ordersDir)
            throws Exception;
}