package controller;

import dto.Order;
import dto.Product;
import service.FlooringService;
import service.FlooringServiceException;
import ui.FlooringView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FlooringController {

    private FlooringView view;
    private FlooringService service;

    public FlooringController(FlooringService service, FlooringView view) {
        this.service = service;
        this.view = view;
    }

    public void run() {
        boolean keepGoing = true;
        int menuSelection = 0;
        try {
            while (keepGoing) {

                menuSelection = getMenuSelection();

                switch (menuSelection) {
                    case 1:
                        displayOrders();
                        break;
                    case 2:
                        addOrder();
                        break;
                    case 3:
                        editOrder();
                        break;
                    case 4:
                        removeOrder();
                        break;
                    case 5:
                        exportData();
                        break;
                    case 6:
                        keepGoing = false;
                        break;
                    default:
                        unknownCommand();
                }
            }
            exitMessage();
        } catch (FlooringServiceException e) {
            view.displayErrorMessage(e.getMessage());
        }
    }

    private int getMenuSelection() {
        return view.printMenuAndGetSelection();
    }

    private void displayOrders() throws FlooringServiceException {
        view.displayOrdersBanner();
        LocalDate date = view.getOrderDate();
        List<Order> orders = service.getOrders(date);
        view.displayOrderList(orders);
    }

    private void addOrder() throws FlooringServiceException {
        view.displayAddOrderBanner();
        boolean hasErrors = false;
        do {
            try {
                LocalDate date = view.getOrderDate();
                String customerName = view.getCustomerName();
                String state = view.getState();
                List<Product> products = service.getAllProducts();
                String productType = view.getProductType(products);
                BigDecimal area = view.getArea();

                // Validate and calculate but doesn't save
                Order preview = service.previewOrder(date, customerName, state, productType, area);
                view.displayOrder(preview);
                boolean confirmed = view.getPlaceOrderConfirmation();
                if (confirmed) {
                    service.saveOrder(date, preview);
                    view.displayAddSuccessBanner();
                } else {
                    view.displayOrderNotPlaced();
                }
                hasErrors = false;
            } catch (FlooringServiceException e) {
                hasErrors = true;
                view.displayErrorMessage(e.getMessage());
            }
        } while (hasErrors);
    }

    private void editOrder() throws FlooringServiceException {
        view.displayEditOrderBanner();
        boolean hasErrors = false;
        do {
            try {
                LocalDate date = view.getOrderDate();
                int orderNumber = view.getOrderNumber();

                // Load existing order so the view can show current values as prompts
                Order existing = service.getOrder(date, orderNumber);

                String customerName = view.getEditCustomerName(existing.getCustomerName());
                String state        = view.getEditState(existing.getState());
                List<Product> products = service.getAllProducts();
                String productType  = view.getEditProductType(existing.getProductType(), products);
                BigDecimal area     = view.getEditArea(existing.getArea());

                // Validate and recalculate
                Order preview = service.previewEdit(date, orderNumber,
                        customerName, state, productType, area);
                view.displayOrder(preview);
                boolean confirmed = view.getSaveEditConfirmation();
                if (confirmed) {
                    service.saveEdit(date, preview);
                    view.displayEditSuccessBanner();
                } else {
                    view.displayEditCancelled();
                }
                hasErrors = false;
            } catch (FlooringServiceException e) {
                hasErrors = true;
                view.displayErrorMessage(e.getMessage());
            }
        } while (hasErrors);
    }

    private void removeOrder() throws FlooringServiceException {
        view.displayRemoveOrderBanner();
        LocalDate date = view.getOrderDate();
        int orderNumber = view.getOrderNumber();
        Order order = service.getOrder(date, orderNumber);
        view.displayOrder(order);
        boolean confirmed = view.getRemoveOrderConfirmation();
        if (confirmed) {
            service.removeOrder(date, orderNumber);
            view.displayRemoveSuccessBanner();
        } else {
            view.displayRemoveCancelled();
        }
    }

    private void exportData() throws FlooringServiceException {
        view.displayExportBanner();
        service.exportAllData();
        view.displayExportSuccessBanner();
    }

    private void unknownCommand() {
        view.displayUnknownCommandBanner();
    }

    private void exitMessage() {
        view.displayExitBanner();
    }
}