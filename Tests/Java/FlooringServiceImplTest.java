
import dao.OrderDaoStubImpl;
import dao.ProductDaoStubImpl;
import dao.TaxesDaoStubImpl;
import dto.Order;
import dto.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.FlooringService;
import service.FlooringServiceException;
import service.FlooringServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlooringServiceImplTest {

    private FlooringService service;

    // A date guaranteed to always be in the future
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusYears(1);
    private static final LocalDate PAST_DATE   = LocalDate.now().minusDays(1);

    @BeforeEach
    void setUp() {
        service = new FlooringServiceImpl(
            new OrderDaoStubImpl(),
            new ProductDaoStubImpl(),
            new TaxesDaoStubImpl()
        );
    }

    @Test
    void previewOrder_assignsOrderNumberOne_whenNoPriorOrders() throws Exception {
        Order order = service.previewOrder(
            FUTURE_DATE, "Grace Hopper", "TX", "Tile", new BigDecimal("150.00"));
        assertEquals(1, order.getOrderNumber());
    }

    @Test
    void previewOrder_doesNotPersist_untilSaveOrderCalled() throws Exception {
        service.previewOrder(FUTURE_DATE, "Test User", "TX", "Wood", new BigDecimal("100.00"));

        // No orders should exist yet because saveOrder was never called
        assertThrows(FlooringServiceException.class,
            () -> service.getOrder(FUTURE_DATE, 1));
    }

    @Test
    void previewOrder_pastDate_throwsException() {
        assertThrows(FlooringServiceException.class, () ->
            service.previewOrder(PAST_DATE, "Ada", "TX", "Carpet", new BigDecimal("200.00")));
    }

    @Test
    void previewOrder_todayDate_throwsException() {
        assertThrows(FlooringServiceException.class, () ->
            service.previewOrder(LocalDate.now(), "Ada", "TX", "Carpet", new BigDecimal("200.00")));
    }

    @Test
    void previewOrder_blankCustomerName_throwsException() {
        assertThrows(FlooringServiceException.class, () ->
            service.previewOrder(FUTURE_DATE, "  ", "TX", "Carpet", new BigDecimal("200.00")));
    }

    @Test
    void previewOrder_invalidCustomerNameChars_throwsException() {
        assertThrows(FlooringServiceException.class, () ->
            service.previewOrder(FUTURE_DATE, "Ada@Lovelace!", "TX", "Carpet", new BigDecimal("200.00")));
    }

    @Test
    void previewOrder_validNameWithPeriodAndComma_succeeds() throws Exception {
        Order order = service.previewOrder(
            FUTURE_DATE, "Acme, Inc.", "TX", "Carpet", new BigDecimal("200.00"));
        assertEquals("Acme, Inc.", order.getCustomerName());
    }

    @Test
    void previewOrder_unsupportedState_throwsException() {
        assertThrows(FlooringServiceException.class, () ->
            service.previewOrder(FUTURE_DATE, "Ada", "ZZ", "Carpet", new BigDecimal("200.00")));
    }

    @Test
    void previewOrder_unknownProduct_throwsException() {
        assertThrows(FlooringServiceException.class, () ->
            service.previewOrder(FUTURE_DATE, "Ada", "TX", "Marble", new BigDecimal("200.00")));
    }

    @Test
    void previewOrder_areaBelowMinimum_throwsException() {
        assertThrows(FlooringServiceException.class, () ->
            service.previewOrder(FUTURE_DATE, "Ada", "TX", "Carpet", new BigDecimal("99.99")));
    }

    @Test
    void previewOrder_areaExactlyAtMinimum_succeeds() throws Exception {
        Order order = service.previewOrder(
            FUTURE_DATE, "Ada", "TX", "Carpet", new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), order.getArea());
    }

    @Test
    void saveOrder_thenGetOrder_returnsCorrectOrder() throws Exception {
        Order preview = service.previewOrder(
            FUTURE_DATE, "Ada Lovelace", "TX", "Carpet", new BigDecimal("200.00"));
        service.saveOrder(FUTURE_DATE, preview);

        Order retrieved = service.getOrder(FUTURE_DATE, preview.getOrderNumber());
        assertEquals("Ada Lovelace", retrieved.getCustomerName());
    }

    @Test
    void getOrder_nonExistentOrder_throwsException() {
        assertThrows(FlooringServiceException.class,
            () -> service.getOrder(FUTURE_DATE, 999));
    }

    @Test
    void getAllProducts_returnsAllStubProducts() throws Exception {
        List<Product> products = service.getAllProducts();
        assertEquals(4, products.size());
    }

    @Test
    void getTaxInfo_validState_returnsTaxInfo() throws Exception {
        assertNotNull(service.getTaxInfo("TX"));
        assertEquals(new BigDecimal("4.45"), service.getTaxInfo("TX").getTaxRate());
    }

    @Test
    void getTaxInfo_invalidState_throwsException() {
        assertThrows(FlooringServiceException.class,
            () -> service.getTaxInfo("ZZ"));
    }

}
