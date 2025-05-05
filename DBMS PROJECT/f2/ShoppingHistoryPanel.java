package f2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ShoppingHistoryPanel extends JPanel {

    public ShoppingHistoryPanel(int customerId) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Shopping History"));

        // Table for shopping history
        String[] columns = { "Shopping ID", "Date", "City", "State", "Country", "Payment ID", "Method Name" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        // Load shopping history
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String sql = """
                    SELECT s.SHOPPING_ID, s.SHOPPING_DATE, s.CITY, s.STATE, s.COUNTRY,
                           s.PAYMENT_ID, pm.MethodName
                    FROM shopping s
                    JOIN payment p ON s.PAYMENT_ID = p.PaymentID
                    JOIN paymentmethods pm ON p.PaymentMethodID = pm.PaymentMethodID
                    WHERE s.CUSTOMER_ID = ?
                    """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("SHOPPING_ID"),
                        rs.getDate("SHOPPING_DATE"),
                        rs.getString("CITY"),
                        rs.getString("STATE"),
                        rs.getString("COUNTRY"),
                        rs.getInt("PAYMENT_ID"),
                        rs.getString("MethodName")
                };
                model.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load shopping history.");
        }

        // Review Panel
        JPanel reviewPanel = new JPanel(new BorderLayout(5, 5));
        reviewPanel.setBorder(BorderFactory.createTitledBorder("Write Product Review"));

        JTextArea reviewArea = new JTextArea(4, 30);
        JTextField ratingField = new JTextField();
        JButton submitReviewButton = new JButton("Submit Review");

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.add(new JLabel("Rating (1-5):"));
        formPanel.add(ratingField);
        formPanel.add(new JLabel("Review:"));
        formPanel.add(new JScrollPane(reviewArea));

        reviewPanel.add(formPanel, BorderLayout.CENTER);
        reviewPanel.add(submitReviewButton, BorderLayout.SOUTH);

        submitReviewButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a shopping entry.");
                return;
            }

            String feedback = reviewArea.getText().trim();
            if (feedback.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Review cannot be empty.");
                return;
            }

            int rating;
            try {
                rating = Integer.parseInt(ratingField.getText().trim());
                if (rating < 1 || rating > 5) {
                    JOptionPane.showMessageDialog(this, "Rating must be between 1 and 5.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid rating input.");
                return;
            }

            int shoppingId = (int) model.getValueAt(selectedRow, 0);
            int productId = fetchProductIdFromShopping(shoppingId);
            if (productId == -1) {
                JOptionPane.showMessageDialog(this, "No product found for this shopping ID.");
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
                String sql = "INSERT INTO review (PRODUCT_ID, FEEDBACK, RATING, CUSTOMER_ID) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, productId);
                stmt.setString(2, feedback);
                stmt.setInt(3, rating);
                stmt.setInt(4, customerId);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Review submitted!");
                reviewArea.setText("");
                ratingField.setText("");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error submitting review.");
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(reviewPanel, BorderLayout.SOUTH);
    }

    // Helper method to get product ID from shopping ID
    private int fetchProductIdFromShopping(int shoppingId) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String sql = "SELECT PRODUCT_ID FROM shoppingdetails WHERE SHOPPING_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, shoppingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("PRODUCT_ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Not found
    }
}
