import dao.OrderDao;
import dao.OrderDaoImpl;
import dto.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class OrderDaoImplTest {

    @TempDir
    Path tempDir;

    private OrderDao dao;
    private File orderFile;

    @BeforeEach
    void setUp() {
        dao = new OrderDaoImpl();
        orderFile = tempDir.resolve("Orders_01022028.txt").toFile();
    }

    private Order makeOrder(int number, String name) {
        Order o = new Order();
        o.setOrderNumber(number);
        o.setCustomerName(name);
        o.setState("TX");
        o.setTaxRate(new BigDecimal("4.45"));
        o.setProductType("Carpet");
        o.setArea(new BigDecimal("200.00"));
        o.setCostPerSquareFoot(new BigDecimal("2.25"));
        o.setLaborCostPerSquareFoot(new BigDecimal("2.10"));
        o.setMaterialCost(new BigDecimal("450.00"));
        o.setLaborCost(new BigDecimal("420.00"));
        o.setTax(new BigDecimal("38.72"));
        o.setTotal(new BigDecimal("908.72"));
        return o;
    }

    @Test
    void addOrder_toEmptyFile_addsToMemory() throws Exception {
        dao.addOrder(orderFile, makeOrder(1, "Ada Lovelace"));

        List<Order> orders = dao.getAllOrders(orderFile);
        assertEquals(1, orders.size());
        assertEquals("Ada Lovelace", orders.get(0).getCustomerName());
    }

    @Test
    void addOrder_multipleOrders_allRetrievable() throws Exception {
        dao.addOrder(orderFile, makeOrder(1, "Ada Lovelace"));
        dao.addOrder(orderFile, makeOrder(2, "Grace Hopper"));
        dao.addOrder(orderFile, makeOrder(3, "Margaret Hamilton"));

        List<Order> orders = dao.getAllOrders(orderFile);
        assertEquals(3, orders.size());
    }

    @Test
    void removeOrder_existingOrder_removesFromMemory() throws Exception {
        dao.addOrder(orderFile, makeOrder(1, "Ada Lovelace"));
        dao.removeOrder(orderFile, 1);

        assertNull(dao.getOrder(orderFile, 1));
    }

}
