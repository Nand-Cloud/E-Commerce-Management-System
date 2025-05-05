package f2;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductLoader {

    public List<Product> loadProducts(int categoryId) {
        List<Product> products = new ArrayList<>();

        String url = "jdbc:mysql://localhost:3306/dbmspj";
        String user = "root";
        String password = "mysql";

        String query = "SELECT * FROM product WHERE CATEGORY_ID = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("PRODUCT_ID");
                String style = rs.getString("STYLE");
                int stock = rs.getInt("STOCK_QUANTITY");
                double price = rs.getDouble("PRICE");
                int discountId = rs.getInt("discount_id");

                double finalPrice = price;

                // Fetch and apply discount if valid
                if (discountId != 0) {
                    String discountQuery = "SELECT * FROM discount WHERE DISCOUNT_ID = ?";
                    try (PreparedStatement dStmt = conn.prepareStatement(discountQuery)) {
                        dStmt.setInt(1, discountId);
                        ResultSet drs = dStmt.executeQuery();
                        if (drs.next()) {
                            Date start = drs.getDate("START_DATE");
                            Date end = drs.getDate("END_DATE");
                            double percent = drs.getDouble("DISCOUNT_PERCENT");

                            LocalDate today = LocalDate.now();
                            if (today.compareTo(start.toLocalDate()) >= 0 &&
                                    today.compareTo(end.toLocalDate()) <= 0) {
                                finalPrice = price - (price * percent / 100.0);
                            }
                        }
                    }
                }

                Product p = new Product(id, style, stock, price, finalPrice);
                products.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }
}
