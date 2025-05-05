package f2;

import java.sql.*;

public class ProductRatingCalculator {
    private String url = "jdbc:mysql://localhost:3306/dbmspj";
    private String user = "root";
    private String password = "mysql";

    public double getAverageRating(int productId) {
        double avgRating = 0.0;
        String query = "SELECT AVG(RATING) FROM review WHERE PRODUCT_ID = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                avgRating = rs.getDouble(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return avgRating;
    }
}