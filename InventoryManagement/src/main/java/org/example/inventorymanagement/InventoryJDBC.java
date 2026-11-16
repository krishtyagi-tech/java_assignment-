
package org.example.inventorymanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class InventoryJDBC {


    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/retail_db";
        String username = "root";
        String password = "----";

        try (Connection con = DriverManager.getConnection(url, username, password);
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Database Connected Successfully!");

            int choice;

            do {
                System.out.println("\n===== INVENTORY MANAGEMENT =====");
                System.out.println("1. Insert Products");
                System.out.println("2. Retrieve Products by Category");
                System.out.println("3. Update Product Stock");
                System.out.println("4. Search and Update Product Price");
                System.out.println("5. Display Inventory Value and Delete Product");
                System.out.println("6. Exit");

                System.out.print("\nEnter your choice: ");
                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {

                    case 1:
                        insertProducts(con);
                        break;

                    case 2:
                        System.out.print("Enter category: ");
                        String category = sc.nextLine();
                        retrieveProducts(con, category);
                        break;

                    case 3:
                        updateStock(con, sc);
                        break;

                    case 4:
                        updateProductPrice(con, sc);
                        break;

                    case 5:
                        inventoryValueAndDelete(con, sc);
                        break;

                    case 6:
                        System.out.println("Thank you for using Inventory Management!");
                        break;

                    default:
                        System.out.println("Invalid choice! Try again.");
                }

            } while (choice != 6);

        } catch (SQLException e) {
            System.out.println("Database Connection Failed!");
            e.printStackTrace();
        }
    }


    // =====================================
    // (A) INSERT PRODUCT RECORDS
    // =====================================

    public static void insertProducts(Connection con) {

        String query = "INSERT INTO Product VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(query)) {

            Object[][] products = {
                    {101, "Laptop", "Electronics", 10, 55000.0},
                    {102, "Mouse", "Electronics", 15, 500.0},
                    {103, "Chair", "Furniture", 25, 2500.0}
            };

            for (Object[] p : products) {

                ps.setInt(1, (int) p[0]);
                ps.setString(2, (String) p[1]);
                ps.setString(3, (String) p[2]);
                ps.setInt(4, (int) p[3]);
                ps.setDouble(5, (double) p[4]);

                try {

                    ps.executeUpdate();

                    System.out.println("Product inserted: " + p[1]);

                } catch (SQLException e) {

                    if (e.getErrorCode() == 1062) {

                        System.out.println("Duplicate Product ID: " + p[0]);

                    } else {
                        throw e;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =====================================
    // (B) CONDITIONAL RETRIEVAL AND SORTING
    // =====================================

    public static void retrieveProducts(Connection con, String category) {

        String query = "SELECT * FROM Product " +
                "WHERE category = ? AND quantity < 20 " +
                "ORDER BY price DESC";

        try (PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, category);

            try (ResultSet rs = ps.executeQuery()) {

                System.out.println("\n--- Products Found ---");

                boolean found = false;

                while (rs.next()) {

                    found = true;

                    System.out.println("Product ID: " +
                            rs.getInt("product_id"));

                    System.out.println("Name: " +
                            rs.getString("product_name"));

                    System.out.println("Category: " +
                            rs.getString("category"));

                    System.out.println("Quantity: " +
                            rs.getInt("quantity"));

                    System.out.println("Price: " +
                            rs.getDouble("price"));

                    System.out.println("----------------------");
                }

                if (!found) {
                    System.out.println("No matching products found!");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void updateStock(Connection con, Scanner sc) {

        System.out.print("\nEnter Product ID: ");
        int id = sc.nextInt();

        System.out.print("Enter quantity to add: ");
        int quantity = sc.nextInt();

        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero!");
            return;
        }

        String query = "UPDATE Product SET quantity = quantity + ? WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, quantity);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Stock updated successfully!");
            } else {
                System.out.println("Product does not exist!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateProductPrice(Connection con, Scanner sc) {

        System.out.print("\nEnter Product ID: ");
        int id = sc.nextInt();

        String searchQuery = "SELECT * FROM Product WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(searchQuery)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    System.out.println("\nProduct Found!");
                    System.out.println("Name: " + rs.getString("product_name"));
                    System.out.println("Current Price: " + rs.getDouble("price"));

                    System.out.print("Enter New Price: ");
                    double newPrice = sc.nextDouble();

                    if (newPrice <= 0) {
                        System.out.println("Price must be greater than zero!");
                        return;
                    }

                    String updateQuery =
                            "UPDATE Product SET price = ? WHERE product_id = ?";

                    try (PreparedStatement updatePS =
                                 con.prepareStatement(updateQuery)) {

                        updatePS.setDouble(1, newPrice);
                        updatePS.setInt(2, id);

                        updatePS.executeUpdate();

                        System.out.println("\nPrice Updated Successfully!");

                        // Display updated details
                        try (PreparedStatement displayPS =
                                     con.prepareStatement(searchQuery)) {

                            displayPS.setInt(1, id);

                            try (ResultSet updatedRS = displayPS.executeQuery()) {

                                if (updatedRS.next()) {

                                    System.out.println("\n--- Updated Product Details ---");
                                    System.out.println("ID: " +
                                            updatedRS.getInt("product_id"));
                                    System.out.println("Name: " +
                                            updatedRS.getString("product_name"));
                                    System.out.println("Category: " +
                                            updatedRS.getString("category"));
                                    System.out.println("Quantity: " +
                                            updatedRS.getInt("quantity"));
                                    System.out.println("Price: " +
                                            updatedRS.getDouble("price"));
                                }
                            }
                        }
                    }

                } else {
                    System.out.println("Product does not exist!");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void inventoryValueAndDelete(Connection con, Scanner sc) {

        // Part 1: Calculate total inventory value

        String sumQuery = "SELECT SUM(quantity * price) AS total FROM Product";

        try (PreparedStatement ps = con.prepareStatement(sumQuery);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println("\nTotal Inventory Value: "
                        + rs.getDouble("total"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Part 2: Delete product only if quantity is zero

        System.out.print("\nEnter Product ID to delete: ");
        int id = sc.nextInt();

        String deleteQuery =
                "DELETE FROM Product WHERE product_id = ? AND quantity = 0";

        try (PreparedStatement ps = con.prepareStatement(deleteQuery)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println(
                        "Product cannot be deleted! It does not exist or quantity is not zero."
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
