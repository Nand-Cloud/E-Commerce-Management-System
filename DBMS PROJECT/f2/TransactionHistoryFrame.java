package f2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TransactionHistoryFrame extends JFrame {

    public TransactionHistoryFrame(int customerId) {
        setTitle("Transaction History");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = {"PaymentID", "Status", "Amount", "PaymentDate", "PaymentMethodID"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String query = "SELECT PaymentID, Status, Amount, PaymentDate, PaymentMethodID FROM payment WHERE CUSTOMER_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("PaymentID"),
                        rs.getString("Status"),
                        rs.getDouble("Amount"),
                        rs.getDate("PaymentDate"),
                        rs.getInt("PaymentMethodID")
                };
                tableModel.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading transaction history: " + e.getMessage());
        }

        add(new JScrollPane(table));
        setVisible(true);
    }
}
