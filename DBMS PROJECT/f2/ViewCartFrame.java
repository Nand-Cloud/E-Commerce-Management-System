package f2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;

public class ViewCartFrame extends JFrame {
    private final HashMap<String, Integer> cart;
    private final int customerId;
    private JPanel cartPanel;
    private JPanel itemsPanel;

    public ViewCartFrame(HashMap<String, Integer> cart, int customerId) {
        this.cart = cart;
        this.customerId = customerId;
        initComponents();
    }

    private void initComponents() {
        setTitle("Your Cart");
        setSize(600, 500);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));

        if (customerId != -1) {
            addCustomerInfoPanel();
        }

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Items"));

        if (cart.isEmpty()) {
            itemsPanel.add(new JLabel("Cart is empty."));
        } else {
            populateCartItems();
        }

        cartPanel.add(itemsPanel);
        add(new JScrollPane(cartPanel), BorderLayout.CENTER);

        JButton checkoutButton = new JButton("Proceed to Checkout");
        checkoutButton.addActionListener(e -> proceedToCheckout());
        add(checkoutButton, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addCustomerInfoPanel() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            JPanel addressPanel = new JPanel(new BorderLayout());
            addressPanel.setBorder(BorderFactory.createTitledBorder("Shipping Address"));

            String addressQuery = "SELECT CITY, STATE, COUNTRY FROM customer WHERE CUSTOMER_ID = ?";
            PreparedStatement addressStmt = conn.prepareStatement(addressQuery);
            addressStmt.setInt(1, customerId);
            ResultSet addressRs = addressStmt.executeQuery();

            JTextArea addressArea = new JTextArea(4, 20);
            addressArea.setEditable(false);
            addressArea.setFont(new Font("Arial", Font.PLAIN, 14));

            if (addressRs.next()) {
                addressArea.setText(
                        "City: " + addressRs.getString("CITY") + "\n" +
                                "State: " + addressRs.getString("STATE") + "\n" +
                                "Country: " + addressRs.getString("COUNTRY"));
            }

            ArrayList<String> mobileNumbers = new ArrayList<>();
            String mobileQuery = "SELECT MOBILE_NO FROM customer_mobile WHERE CUSTOMER_ID = ?";
            PreparedStatement mobileStmt = conn.prepareStatement(mobileQuery);
            mobileStmt.setInt(1, customerId);
            ResultSet mobRs = mobileStmt.executeQuery();

            while (mobRs.next()) {
                mobileNumbers.add(mobRs.getString("MOBILE_NO"));
            }

            if (!mobileNumbers.isEmpty()) {
                addressArea.append("\n\nContact Numbers:\n");
                for (String mobileNo : mobileNumbers) {
                    addressArea.append(mobileNo + "\n");
                }
            }

            addressPanel.add(new JScrollPane(addressArea), BorderLayout.CENTER);
            cartPanel.add(addressPanel);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customer information: " + e.getMessage());
        }
    }

    private void populateCartItems() {
        for (String item : cart.keySet()) {
            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel iconLabel = new JLabel();
            setSmallProductImage(item, iconLabel);

            JLabel nameLabel = new JLabel(item);
            nameLabel.setPreferredSize(new Dimension(120, 20));

            int productId = getProductIdByStyle(item);
            int quantity = cart.get(item);
            double originalPrice = getOriginalPrice(productId);
            double discountedPrice = getDiscountedPrice(productId);
            boolean hasDiscount = discountedPrice < originalPrice;

            JLabel qtyLabel = new JLabel("Qty: " + quantity);
            JLabel priceLabel;

            if (hasDiscount) {
                priceLabel = new JLabel("<html><strike>₹" + originalPrice + "</strike> <font color='green'>₹"
                        + discountedPrice + "</font></html>");
            } else {
                priceLabel = new JLabel("₹" + originalPrice);
            }

            JButton increase = new JButton("+");
            JButton decrease = new JButton("-");
            JButton removeBtn = new JButton("Remove");
            removeBtn.setPreferredSize(new Dimension(90, 25));

            // Remove item
            removeBtn.addActionListener(e -> {
                int currentQty = cart.get(item);
                int stock = getProductStock(productId);
                updateStock(productId, stock + currentQty);

                cart.remove(item);
                itemsPanel.remove(itemPanel);
                itemsPanel.revalidate();
                itemsPanel.repaint();

                if (cart.isEmpty()) {
                    itemsPanel.add(new JLabel("Cart is empty."));
                    itemsPanel.revalidate();
                    itemsPanel.repaint();
                }
            });

            // Increase quantity
            increase.addActionListener(e -> {
                int currentQty = cart.get(item);
                int availableStock = getProductStock(productId);

                if (currentQty < availableStock) {
                    cart.put(item, currentQty + 1);
                    qtyLabel.setText("Qty: " + cart.get(item));
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Cannot add more than available stock (" + availableStock + ") for item: " + item,
                            "Stock Limit Reached", JOptionPane.WARNING_MESSAGE);
                }
            });

            // Decrease quantity
            decrease.addActionListener(e -> {
                int current = cart.get(item);
                if (current > 1) {
                    cart.put(item, current - 1);
                    qtyLabel.setText("Qty: " + cart.get(item));
                }
            });

            itemPanel.add(iconLabel);
            itemPanel.add(nameLabel);
            itemPanel.add(qtyLabel);
            itemPanel.add(priceLabel);
            itemPanel.add(decrease);
            itemPanel.add(increase);
            itemPanel.add(removeBtn);

            itemsPanel.add(itemPanel);
        }
    }

    private void setSmallProductImage(String style, JLabel imageLabel) {
        String imageName = style.toLowerCase().replace(" ", "_");
        String[] extensions = { ".png", ".jpg", ".jpeg" };
        boolean found = false;

        for (String ext : extensions) {
            File file = new File("images/" + imageName + ext);
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                Image scaled = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                found = true;
                break;
            }
        }

        if (!found) {
            ImageIcon fallback = new ImageIcon("images/noimage.png");
            Image fallbackImg = fallback.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(fallbackImg));
        }
    }

    private int getProductIdByStyle(String style) {
        int productId = -1;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String query = "SELECT PRODUCT_ID FROM product WHERE STYLE = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, style);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                productId = rs.getInt("PRODUCT_ID");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return productId;
    }

    private int getProductStock(int productId) {
        int stock = 0;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String query = "SELECT STOCK_QUANTITY FROM product WHERE PRODUCT_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stock = rs.getInt("STOCK_QUANTITY");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return stock;
    }

    private void updateStock(int productId, int newQty) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String update = "UPDATE product SET STOCK_QUANTITY = ? WHERE PRODUCT_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(update);
            stmt.setInt(1, newQty);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating stock: " + e.getMessage());
        }
    }

    private void proceedToCheckout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.", "Checkout", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (customerId == -1) {
            JOptionPane.showMessageDialog(this, "Please login to checkout.", "Checkout",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        new OrderSummaryFrame(cart, customerId); // Should use getDiscountedPrice internally
        dispose();
    }

    private double getOriginalPrice(int productId) {
        double price = 0.0;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String query = "SELECT PRICE FROM product WHERE PRODUCT_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                price = rs.getDouble("PRICE");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return price;
    }

    private double getDiscountedPrice(int productId) {
        double price = getOriginalPrice(productId);
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String discountQuery = "SELECT d.DISCOUNT_PERCENT FROM product p JOIN discount d ON p.discount_id = d.discount_id WHERE p.product_id = ? AND CURDATE() BETWEEN d.start_date AND d.end_date";
            PreparedStatement stmt = conn.prepareStatement(discountQuery);
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double percent = rs.getDouble("DISCOUNT_PERCENT");
                price = price * (1 - percent / 100.0);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return price;
    }

    public static void main(String[] args) {
        HashMap<String, Integer> sampleCart = new HashMap<>();
        sampleCart.put("Casual", 2);
        sampleCart.put("Formal", 1);

        SwingUtilities.invokeLater(() -> new ViewCartFrame(sampleCart, 1));
    }
}
