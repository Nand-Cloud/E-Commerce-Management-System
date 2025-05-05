package f2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.*;
import java.sql.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class OrderSummaryFrame extends JFrame {
    private final HashMap<String, Integer> cart;
    private final int customerId;
    private JComboBox<String> paymentMethodCombo;
    private double totalAmount;

    private String customerCity;
    private String customerState;
    private String customerCountry;

    public OrderSummaryFrame(HashMap<String, Integer> cart, int customerId) {
        this.cart = cart;
        this.customerId = customerId;

        setTitle("Order Summary");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        initUI();

        setVisible(true);
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));

        JLabel userIconLabel = new JLabel(new ImageIcon("user.png"));
        topPanel.add(userIconLabel, BorderLayout.WEST);

        JLabel logoLabel = new JLabel();
        ImageIcon icon = new ImageIcon("n1.png");
        Image scaled = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        logoLabel.setIcon(new ImageIcon(scaled));
        topPanel.add(logoLabel, BorderLayout.EAST);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        JLabel dateLabel = new JLabel("Date: " + timestamp);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextArea customerArea = new JTextArea(5, 40);
        customerArea.setEditable(false);
        loadCustomerInfo(customerArea);

        centerPanel.add(dateLabel);
        centerPanel.add(new JScrollPane(customerArea));
        topPanel.add(centerPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // === Cart Table with Vendor Info ===
        String[] columnNames = { "Product Name", "Quantity", "Price (each)", "Subtotal", "Vendor Info" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable cartTable = new JTable(tableModel);
        totalAmount = 0.0;

        for (String style : cart.keySet()) {
            int quantity = cart.get(style);
            double price = getProductPriceByStyle(style);
            double subtotal = price * quantity;
            totalAmount += subtotal;
            String vendorInfo = getVendorInfoByStyle(style);

            Object[] row = {
                    style,
                    quantity,
                    String.format("₹ %.2f", price),
                    String.format("₹ %.2f", subtotal),
                    vendorInfo
            };
            tableModel.addRow(row);
        }

        JScrollPane tableScroll = new JScrollPane(cartTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Items Summary"));
        add(tableScroll, BorderLayout.CENTER);

        // === Bottom Panel ===
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JLabel totalLabel = new JLabel("Total: ₹ " + String.format("%.2f", totalAmount));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(totalLabel, BorderLayout.WEST);

        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paymentPanel.add(new JLabel("Select Payment Method:"));
        String[] paymentOptions = { "Credit Card", "UPI", "Net Banking", "Cash" };
        paymentMethodCombo = new JComboBox<>(paymentOptions);
        paymentPanel.add(paymentMethodCombo);

        JButton confirmButton = new JButton("Confirm Order");
        confirmButton.addActionListener(e -> confirmOrder());
        paymentPanel.add(confirmButton);

        // === Print to PDF Button ===
        JButton printPdfButton = new JButton("Print to PDF");
        printPdfButton.addActionListener(e -> printFullFrameAsPDF());
        paymentPanel.add(printPdfButton);

        bottomPanel.add(paymentPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCustomerInfo(JTextArea area) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String query = "SELECT FIRST_NAME, MIDDLE_NAME, LAST_NAME, COUNTRY, STATE, CITY, EMAIL, LOYALTY_POINTS FROM customer WHERE CUSTOMER_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("FIRST_NAME") + " " +
                        rs.getString("MIDDLE_NAME") + " " +
                        rs.getString("LAST_NAME");
                customerCity = rs.getString("CITY");
                customerState = rs.getString("STATE");
                customerCountry = rs.getString("COUNTRY");
                String email = rs.getString("EMAIL");
                int loyaltyPoints = rs.getInt("LOYALTY_POINTS");

                area.setText("Customer Name: " + fullName + "\n" +
                        "Location: " + customerCity + ", " + customerState + ", " + customerCountry + "\n" +
                        "Email: " + email + "\n" +
                        "Loyalty Points: " + loyaltyPoints);
            } else {
                area.setText("Customer not found for ID: " + customerId);
            }
        } catch (SQLException e) {
            area.setText("SQL Error loading customer data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double getProductPriceByStyle(String style) {
        double price = 0.0;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String query = "SELECT PRICE FROM product WHERE STYLE = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, style);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                price = rs.getDouble("PRICE");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return price;
    }

    private String getVendorInfoByStyle(String style) {
        String vendorInfo = "Unknown";
        String query = "SELECT v.FIRST_NAME, v.MIDDLE_NAME, v.LAST_NAME, vm.MOBILE_NO " +
                "FROM product p " +
                "JOIN vendor v ON p.VENDOR_ID = v.VENDOR_ID " +
                "JOIN vendor_mobile vm ON v.VENDOR_ID = vm.VENDOR_ID " +
                "WHERE p.STYLE = ? LIMIT 1"; // Only show one mobile number

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql");
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, style);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("FIRST_NAME") + " " +
                        rs.getString("MIDDLE_NAME") + " " +
                        rs.getString("LAST_NAME");
                String mobile = rs.getString("MOBILE_NO");
                vendorInfo = name + " (📱 " + mobile + ")";
            } else {
                System.out.println("No vendor info found for style: " + style);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching vendor info for style: " + style);
            e.printStackTrace();
        }

        return vendorInfo;
    }

    private void confirmOrder() {
        int selectedIndex = paymentMethodCombo.getSelectedIndex();
        int paymentMethodId = selectedIndex + 1;

        String insertPaymentQuery = "INSERT INTO payment (Status, Amount, PaymentDate, PaymentMethodID, CUSTOMER_ID) VALUES (?, ?, CURDATE(), ?, ?)";
        String getPaymentIdQuery = "SELECT PaymentID FROM payment WHERE CUSTOMER_ID = ? ORDER BY PaymentDate DESC LIMIT 1";
        String getProductIdQuery = "SELECT PRODUCT_ID FROM product WHERE STYLE = ?";
        String insertOrderQuery = "INSERT INTO orders (PRODUCT_ID, QUANTITY, CUSTOMER_ID) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(insertPaymentQuery)) {
                stmt.setString(1, "Completed");
                stmt.setDouble(2, totalAmount);
                stmt.setInt(3, paymentMethodId);
                stmt.setInt(4, customerId);
                stmt.executeUpdate();
            }

            int paymentId;
            try (PreparedStatement stmt = conn.prepareStatement(getPaymentIdQuery)) {
                stmt.setInt(1, customerId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    paymentId = rs.getInt("PaymentID");
                } else {
                    throw new SQLException("Failed to retrieve payment ID.");
                }
            }

            try (PreparedStatement getProductStmt = conn.prepareStatement(getProductIdQuery);
                    PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderQuery)) {

                for (String style : cart.keySet()) {
                    int quantity = cart.get(style);
                    getProductStmt.setString(1, style);
                    ResultSet rs = getProductStmt.executeQuery();
                    if (rs.next()) {
                        int productId = rs.getInt("PRODUCT_ID");
                        insertOrderStmt.setInt(1, productId);
                        insertOrderStmt.setInt(2, quantity);
                        insertOrderStmt.setInt(3, customerId);
                        insertOrderStmt.executeUpdate();
                    } else {
                        throw new SQLException("Product not found for style: " + style);
                    }
                }
            }

            callAddShoppingAfterPayment(conn, paymentId);
            conn.commit();

            JOptionPane.showMessageDialog(this, "Order confirmed and payment recorded successfully!");
            dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void callAddShoppingAfterPayment(Connection conn, int paymentId) throws SQLException {
        String procedureCall = "{CALL AddShoppingAfterPayment(?, ?, ?, ?)}";
        try (CallableStatement callStmt = conn.prepareCall(procedureCall)) {
            callStmt.setInt(1, paymentId);
            callStmt.setString(2, customerCity);
            callStmt.setString(3, customerState);
            callStmt.setString(4, customerCountry);
            callStmt.executeUpdate();
        }
    }

    private void printFullFrameAsPDF() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Order Summary Print");

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0)
                return Printable.NO_SUCH_PAGE;

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2d.scale(0.8, 0.8); // Adjust scale if needed
            getContentPane().printAll(g2d);
            return Printable.PAGE_EXISTS;
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Print failed: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        HashMap<String, Integer> sampleCart = new HashMap<>();
        sampleCart.put("Casual", 2);
        sampleCart.put("Formal", 1);
        SwingUtilities.invokeLater(() -> new OrderSummaryFrame(sampleCart, 1));
    }
}
