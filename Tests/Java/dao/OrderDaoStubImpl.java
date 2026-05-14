package dao;

import dto.Order;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderDaoStubImpl implements OrderDao {

    private final Map<String, Map<Integer, Order>> store = new LinkedHashMap<>();

    // key = file name (e.g. "Orders_01022028.txt")
    private Map<Integer, Order> getOrCreate(File file) {
        return store.computeIfAbsent(file.getName(), k -> new LinkedHashMap<>());
    }

    @Override
    public List<Order> getAllOrders(File file) {
        return new ArrayList<>(getOrCreate(file).values());
    }

    @Override
    public Order getOrder(File file, int orderNumber) {
        return getOrCreate(file).get(orderNumber);
    }

    @Override
    public void addOrder(File file, Order order) {
        getOrCreate(file).put(order.getOrderNumber(), order);
    }

    @Override
    public void editOrder(File file, Order order) throws Exception {
        Map<Integer, Order> map = getOrCreate(file);
        if (!map.containsKey(order.getOrderNumber())) {
            throw new Exception("Order #" + order.getOrderNumber() + " not found.");
        }
        map.put(order.getOrderNumber(), order);
    }

    @Override
    public void removeOrder(File file, int orderNumber) throws Exception {
        Map<Integer, Order> map = getOrCreate(file);
        if (!map.containsKey(orderNumber)) {
            throw new Exception("Order #" + orderNumber + " not found.");
        }
        map.remove(orderNumber);
    }

    @Override
    public void flushToDisk(File file) {
    }

    @Override
    public void flushAllToDisk(File ordersDir) {
    }
}
