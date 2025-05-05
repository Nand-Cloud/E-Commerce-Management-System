package f2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;

public class ProductByCategoryApp extends JFrame {

    private JPanel categoryPanel;
    private JPanel productListPanel;
    private final HashMap<String, Integer> cart = new HashMap<>();
    private final int customerId;

    private ProductLoader productLoader;
    private ProductImageSetter productImageSetter;
    private ProductRatingCalculator productRatingCalculator;

    public ProductByCategoryApp(int customerId) {
        this.customerId = customerId;

        productLoader = new ProductLoader();
        productImageSetter = new ProductImageSetter();
        productRatingCalculator = new ProductRatingCalculator();

        initComponents();
    }

    public ProductByCategoryApp() {
        this(-1);
    }

    private void initComponents() {
        setTitle("Products by Category");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setContentPane(new JLabel(new ImageIcon("i.png")));
        getContentPane().setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        categoryPanel.setOpaque(false);

        String[] categories = { "Formal", "Casual", "Sports", "Denim" };
        int[] categoryIds = { 1, 2, 3, 4 };
        for (int i = 0; i < categories.length; i++) {
            JLabel categoryLabel = new JLabel(categories[i]);
            categoryLabel.setForeground(Color.WHITE);
            categoryLabel.setFont(new Font("Arial", Font.BOLD, 14));
            categoryLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            final int catId = categoryIds[i];

            categoryLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    categoryLabel.setForeground(Color.YELLOW);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    categoryLabel.setForeground(Color.WHITE);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    loadProducts(catId);
                }
            });

            categoryPanel.add(categoryLabel);
        }

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        rightPanel.setOpaque(false);

        ImageIcon profileIcon = new ImageIcon(
                new ImageIcon("images/profile.png").getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JLabel profileIconLabel = new JLabel(profileIcon);
        profileIconLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileIconLabel.setToolTipText("Profile");
        profileIconLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showProfileSection();
            }
        });

        ImageIcon cartIcon = new ImageIcon("images/cart.png");
        Image scaledCart = cartIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        JLabel cartLogo = new JLabel(new ImageIcon(scaledCart));

        JButton viewCart = new JButton("View Cart");
        viewCart.setFocusable(false);
        viewCart.addActionListener(e -> showCart());

        JButton profileButton = new JButton("Profile");
        profileButton.setFocusable(false);
        profileButton.addActionListener(e -> showProfileSection());

        JButton supportButton = new JButton("Support");
        supportButton.setFocusable(false);
        supportButton.addActionListener(e -> openSupportFrame());

        rightPanel.add(profileIconLabel);
        rightPanel.add(cartLogo);
        rightPanel.add(viewCart);
        rightPanel.add(profileButton);
        rightPanel.add(supportButton);

        topPanel.add(categoryPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        productListPanel = new JPanel(new GridLayout(0, 4, 15, 15));
        productListPanel.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(productListPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void openSupportFrame() {
        if (customerId == -1) {
            JOptionPane.showMessageDialog(this, "Please login to use support.", "Support",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new SupportFrame(customerId).setVisible(true);
    }

    private void loadProducts(int categoryId) {
        productListPanel.removeAll();

        List<Product> products = productLoader.loadProducts(categoryId);

        for (Product product : products) {
            int productId = product.getProductId();
            String style = product.getStyle();
            int stock = product.getStock();

            JPanel productPanel = new JPanel(new BorderLayout());
            productPanel.setPreferredSize(new Dimension(50, 50));
            productPanel.setBackground(new Color(255, 255, 255, 190));
            productPanel.setBorder(BorderFactory.createTitledBorder(style));

            JLabel imageLabel = new JLabel();
            productImageSetter.setProductImage(style, imageLabel);

            JLabel stockLabel = new JLabel("Stock: " + stock, SwingConstants.CENTER);
            stockLabel.setFont(new Font("Arial", Font.PLAIN, 12));

            // Price label
            JLabel priceLabel;
            if (product.getDiscountedPrice() < product.getOriginalPrice()) {
                priceLabel = new JLabel(
                        "<html><span style='text-decoration: line-through;'>₹"
                                + String.format("%.2f", product.getOriginalPrice()) +
                                "</span> ₹" + String.format("%.2f", product.getDiscountedPrice()) + "</html>",
                        SwingConstants.CENTER);
            } else {
                priceLabel = new JLabel("₹" + String.format("%.2f", product.getOriginalPrice()), SwingConstants.CENTER);
            }
            priceLabel.setFont(new Font("Arial", Font.PLAIN, 13));

            JButton actionButton = new JButton(cart.containsKey(style) ? "Remove" : "Add");
            actionButton.setFont(new Font("Arial", Font.PLAIN, 11));
            actionButton.setPreferredSize(new Dimension(70, 20));

            actionButton.addActionListener(e -> {
                if (cart.containsKey(style)) {
                    int currentQty = cart.get(style);
                    cart.remove(style);
                    updateStock(productId, stock + currentQty);
                    stockLabel.setText("Stock: " + (stock + currentQty));
                    actionButton.setText("Add");
                    JOptionPane.showMessageDialog(this, "Removed from cart!");
                } else {
                    cart.put(style, 1);
                    updateStock(productId, stock - 1);
                    stockLabel.setText("Stock: " + (stock - 1));
                    actionButton.setText("Remove");
                    JOptionPane.showMessageDialog(this, "Added to cart!");
                }
            });

            double avgRating = productRatingCalculator.getAverageRating(productId);

            JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            ratingPanel.setOpaque(false);
            ImageIcon starIcon = new ImageIcon(
                    new ImageIcon("images/star.png").getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            JLabel starLabel = new JLabel(starIcon);
            JLabel ratingLabel = new JLabel(String.format("%.1f / 5", avgRating));
            ratingLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            ratingPanel.add(starLabel);
            ratingPanel.add(ratingLabel);

            JPanel centerPanel = new JPanel(new GridLayout(4, 1));
            centerPanel.setOpaque(false);
            centerPanel.add(priceLabel);
            centerPanel.add(stockLabel);
            centerPanel.add(ratingPanel);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setOpaque(false);
            buttonPanel.add(actionButton);
            centerPanel.add(buttonPanel);

            productPanel.add(imageLabel, BorderLayout.NORTH);
            productPanel.add(centerPanel, BorderLayout.CENTER);

            productListPanel.add(productPanel);
        }

        productListPanel.revalidate();
        productListPanel.repaint();
    }

    private void updateStock(int productId, int newQty) {
        String url = "jdbc:mysql://localhost:3306/dbmspj";
        String user = "root";
        String password = "mysql";
        String update = "UPDATE product SET STOCK_QUANTITY = ? WHERE PRODUCT_ID = ?";

        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, user, password);
                java.sql.PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setInt(1, newQty);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    private void showCart() {
        new ViewCartFrame(cart, customerId); // Make sure ViewCartFrame also handles discounts
    }

    private void showProfileSection() {
        if (customerId == -1) {
            JOptionPane.showMessageDialog(this, "Please login to access your profile", "Profile",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new ProfileFrame(customerId).setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductByCategoryApp().setVisible(true));
    }
}
