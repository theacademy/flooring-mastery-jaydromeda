package ui;

import dto.Order;
import dto.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class FlooringView {

    private UserIO io;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMddyyyy");

    public FlooringView(UserIO io) {
        this.io = io;
    }

    public int printMenuAndGetSelection() {
        io.print("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
        io.print("* <<Flooring Program>>");
        io.print("* 1. Display Orders");
        io.print("* 2. Add an Order");
        io.print("* 3. Edit an Order");
        io.print("* 4. Remove an Order");
        io.print("* 5. Export All Data");
        io.print("* 6. Quit");
        io.print("*");
        io.print("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
        return io.readInt("Please select from the above choices.", 1, 6);
    }

    public void displayOrdersBanner() {
        io.print("=== Display Orders ===");
    }

    public void displayAddOrderBanner() {
        io.print("=== Add an Order ===");
    }

    public void displayEditOrderBanner() {
        io.print("=== Edit an Order ===");
    }

    public void displayRemoveOrderBanner() {
        io.print("=== Remove an Order ===");
    }

    public void displayExportBanner() {
        io.print("=== Export All Data ===");
    }

    public void displayUnknownCommandBanner() {
        io.print("Unknown Command!!!");
    }

    public void displayExitBanner() {
        io.print("Good Bye!!!");
    }

    public void displayAddSuccessBanner() {
        io.readString("Order successfully placed. Please hit enter to continue.");
    }

    public void displayOrderNotPlaced() {
        io.readString("Order was not placed. Please hit enter to continue.");
    }

    public void displayEditSuccessBanner() {
        io.readString("Order successfully updated. Please hit enter to continue.");
    }

    public void displayEditCancelled() {
        io.readString("Edit cancelled. No changes were saved. Please hit enter to continue.");
    }

    public void displayRemoveSuccessBanner() {
        io.print("Order successfully removed.");
    }

    public void displayRemoveCancelled() {
        io.readString("Remove cancelled. Order was not deleted. Please hit enter to continue.");
    }

    public void displayExportSuccessBanner() {
        io.readString("All data exported successfully. Please hit enter to continue.");
    }

    public void displayErrorMessage(String errorMsg) {
        io.print("=== ERROR ===");
        io.print(errorMsg);
    }

    public LocalDate getOrderDate() {
        while (true) {
            String input = io.readString("Please enter the order date (MM/DD/YYYY or MMDDYYYY):").trim();
            try {
                    return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                io.print("Invalid date. Please enter as MMDDYYYY (e.g. 06012013).");
            }
        }
    }

    public int getOrderNumber() {
        return io.readInt("Please enter the order number:");
    }

    public String getCustomerName() {
        return io.readString("Please enter the customer name:");
    }

    public String getState() {
        return io.readString("Please enter the state abbreviation (TX, CA, KY, WA):");
    }

    public String getProductType(List<Product> products) {
        io.print("Available products:");
        for (Product p : products) {
            io.print(String.format("  %-12s | Cost/sqft: $%-6s | Labor/sqft: $%s",
                    p.getProductType(),
                    p.getCostPerSquareFoot(),
                    p.getLaborCostPerSquareFoot()));
        }
        return io.readString("Please enter the product type:");
    }

    public BigDecimal getArea() {
        return io.readBigDecimal("Please enter the area (minimum 100 sq ft):");
    }

    public boolean getPlaceOrderConfirmation() {
        String answer = io.readString("Place this order? (Y/N):");
        return answer.trim().equalsIgnoreCase("Y");
    }

    public String getEditCustomerName(String current) {
        String input = io.readString("Customer name (" + current + "):");
        return input.isBlank() ? null : input;
    }

    public String getEditState(String current) {
        String input = io.readString("State (" + current + "):");
        return input.isBlank() ? null : input;
    }

    public String getEditProductType(String current, List<Product> products) {
        io.print("Available products:");
        for (Product p : products) {
            io.print(String.format("  %-12s | Cost/sqft: $%-6s | Labor/sqft: $%s",
                    p.getProductType(),
                    p.getCostPerSquareFoot(),
                    p.getLaborCostPerSquareFoot()));
        }
        String input = io.readString("Product type (" + current + "):");
        return input.isBlank() ? null : input;
    }

    public BigDecimal getEditArea(BigDecimal current) {
        String input = io.readString("Area (" + current + "):");
        return input.isBlank() ? null : new BigDecimal(input.trim());
    }

    public boolean getSaveEditConfirmation() {
        String answer = io.readString("Save changes? (Y/N):");
        return answer.trim().equalsIgnoreCase("Y");
    }

    public boolean getRemoveOrderConfirmation() {
        String answer = io.readString("Are you sure you want to remove this order? (Y/N):");
        return answer.trim().equalsIgnoreCase("Y");
    }

    public void displayOrderList(List<Order> orders) {
        io.print("--- Orders ---");
        for (Order o : orders) {
            io.print(String.format("#%d | %s | %s | %s | Area: %s sqft | Total: $%s",
                    o.getOrderNumber(),
                    o.getCustomerName(),
                    o.getState(),
                    o.getProductType(),
                    o.getArea(),
                    o.getTotal()));
        }
        io.readString("Please hit enter to continue.");
    }

    public void displayOrder(Order order) {
        if (order == null) {
            io.print("No such order.");
            return;
        }
        io.print("--- Order Summary ---");
        io.print("Order #:         " + order.getOrderNumber());
        io.print("Customer:        " + order.getCustomerName());
        io.print("State:           " + order.getState());
        io.print("Tax Rate:        " + order.getTaxRate() + "%");
        io.print("Product:         " + order.getProductType());
        io.print("Area:            " + order.getArea() + " sqft");
        io.print("Cost/sqft:       $" + order.getCostPerSquareFoot());
        io.print("Labor/sqft:      $" + order.getLaborCostPerSquareFoot());
        io.print("Material Cost:   $" + order.getMaterialCost());
        io.print("Labor Cost:      $" + order.getLaborCost());
        io.print("Tax:             $" + order.getTax());
        io.print("Total:           $" + order.getTotal());
        io.print("---------------------");
    }
}