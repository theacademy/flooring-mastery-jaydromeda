package dao;

import dto.Order;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OrderDaoImpl implements OrderDao {

    public static final String DELIMITER = ",";
    public static final String HEADER =
            "OrderNumber,CustomerName,State,TaxRate,ProductType," +
                    "Area,CostPerSquareFoot,LaborCostPerSquareFoot," +
                    "MaterialCost,LaborCost,Tax,Total";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMddyyyy");

    /**
     * Master in-memory store.
     * Outer key: order date parsed from the filename.
     * Inner key: order number.
     * A date entry is only added to this map after its file has been loaded.
     */
    private final Map<LocalDate, Map<Integer, Order>> ordersByDate = new LinkedHashMap<>();

    // -------------------------------------------------------------------------
    // Public interface methods
    // -------------------------------------------------------------------------

    @Override
    public List<Order> getAllOrders(File file) throws Exception {
        Map<Integer, Order> ordersForDate = getOrdersForDate(file);
        return new ArrayList<>(ordersForDate.values());
    }

    @Override
    public Order getOrder(File file, int orderNumber) throws Exception {
        Map<Integer, Order> ordersForDate = getOrdersForDate(file);
        return ordersForDate.get(orderNumber); // null if not found; service handles this
    }

    @Override
    public void addOrder(File file, Order order) throws Exception {
        Map<Integer, Order> ordersForDate = getOrdersForDate(file);
        ordersForDate.put(order.getOrderNumber(), order);
        // in-memory only — call flushToDisk() to persist
    }

    @Override
    public void editOrder(File file, Order order) throws Exception {
        Map<Integer, Order> ordersForDate = getOrdersForDate(file);
        if (!ordersForDate.containsKey(order.getOrderNumber())) {
            throw new Exception("Order #" + order.getOrderNumber()
                    + " does not exist in " + file.getName());
        }
        ordersForDate.put(order.getOrderNumber(), order);
        // in-memory only — call flushToDisk() to persist
    }

    @Override
    public void removeOrder(File file, int orderNumber) throws Exception {
        Map<Integer, Order> ordersForDate = getOrdersForDate(file);
        if (!ordersForDate.containsKey(orderNumber)) {
            throw new Exception("Order #" + orderNumber
                    + " does not exist in " + file.getName());
        }
        ordersForDate.remove(orderNumber);
        // in-memory only — call flushToDisk() to persist
    }

    @Override
    public void flushToDisk(File file) throws Exception {
        LocalDate date = parseDateFromFile(file);
        Map<Integer, Order> ordersForDate = ordersByDate.getOrDefault(date, new LinkedHashMap<>());
        writeOrdersToFile(file, ordersForDate);
    }

    @Override
    public void flushAllToDisk(File ordersDir) throws Exception {
        for (Map.Entry<LocalDate, Map<Integer, Order>> entry : ordersByDate.entrySet()) {
            LocalDate date = entry.getKey();
            String filename = "Orders_" + date.format(DATE_FORMAT) + ".txt";
            File file = new File(ordersDir, filename);
            writeOrdersToFile(file, entry.getValue());
        }
    }

    private Map<Integer, Order> getOrdersForDate(File file) throws Exception {
        LocalDate date = parseDateFromFile(file);
        if (!ordersByDate.containsKey(date)) {
            ordersByDate.put(date, loadOrdersFromFile(file));
        }
        return ordersByDate.get(date);
    }


    private LocalDate parseDateFromFile(File file) throws Exception {
        String name = file.getName(); // e.g. "Orders_08212025.txt"
        try {
            String datePart = name.replace("Orders_", "").replace(".txt", "");
            return LocalDate.parse(datePart, DATE_FORMAT);
        } catch (Exception e) {
            throw new Exception("Cannot parse date from filename: " + name, e);
        }
    }

    private Map<Integer, Order> loadOrdersFromFile(File file) throws Exception {
        Map<Integer, Order> map = new LinkedHashMap<>();

        if (!file.exists()) {
            return map;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                if (firstLine) {
                    firstLine = false;
                    // Only skip if this really is the header row
                    if (line.startsWith("OrderNumber")) continue;
                    // Otherwise fall through and parse — file has no header
                }

                Order order = unmarshallOrder(line);
                map.put(order.getOrderNumber(), order);
            }
        } catch (IOException e) {
            throw new Exception("Error reading " + file.getName() + ": " + e.getMessage(), e);
        }

        return map;
    }

    private void writeOrdersToFile(File file, Map<Integer, Order> ordersMap)
            throws Exception {
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(HEADER);
            for (Order order : ordersMap.values()) {
                writer.println(marshallOrder(order));
            }
        } catch (IOException e) {
            throw new Exception("Error writing " + file.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
      Column order (matches spec and Order.toString()):
        0  OrderNumber
        1  CustomerName
        2  State
        3  TaxRate
        4  ProductType
        5  Area
        6  CostPerSquareFoot
        7  LaborCostPerSquareFoot
        8  MaterialCost
        9  LaborCost
        10 Tax
        11 Total
     */
    private Order unmarshallOrder(String orderAsText) throws Exception {
        String[] tokens = orderAsText.split(DELIMITER);

        if (tokens.length != 12) {
            throw new Exception("Malformed order line (expected 12 fields, got "
                    + tokens.length + "): " + orderAsText);
        }

        try {
            Order order = new Order();
            order.setOrderNumber(Integer.parseInt(tokens[0].trim()));
            order.setCustomerName(tokens[1].trim());
            order.setState(tokens[2].trim());
            order.setTaxRate(new BigDecimal(tokens[3].trim()));
            order.setProductType(tokens[4].trim());
            order.setArea(new BigDecimal(tokens[5].trim()));
            order.setCostPerSquareFoot(new BigDecimal(tokens[6].trim()));
            order.setLaborCostPerSquareFoot(new BigDecimal(tokens[7].trim()));
            order.setMaterialCost(new BigDecimal(tokens[8].trim()));
            order.setLaborCost(new BigDecimal(tokens[9].trim()));
            order.setTax(new BigDecimal(tokens[10].trim()));
            order.setTotal(new BigDecimal(tokens[11].trim()));
            return order;
        } catch (NumberFormatException e) {
            throw new Exception("Could not parse numeric field in: " + orderAsText, e);
        }
    }

    private String marshallOrder(Order order) {
        return order.toString();
    }
}