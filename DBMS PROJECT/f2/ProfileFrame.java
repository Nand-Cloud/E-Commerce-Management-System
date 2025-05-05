package f2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

public class ProfileFrame extends JFrame {
    private final int customerId;

    public ProfileFrame(int customerId) {
        this.customerId = customerId;
        initProfileFrame();
    }

    private void initProfileFrame() {
        setTitle("My Profile");
        setSize(500, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        ImageIcon topRightIcon = new ImageIcon(new ImageIcon("images/user.png")
                .getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
        JLabel topRightIconLabel = new JLabel(topRightIcon);
        topRightIconLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        headerPanel.add(topRightIconLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));

        JPanel userInfoPanel = new JPanel(new BorderLayout(10, 10));
        userInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ImageIcon profileIcon = new ImageIcon(new ImageIcon("images/profile.png").getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
        JLabel profileImage = new JLabel(profileIcon);
        profileImage.setHorizontalAlignment(SwingConstants.CENTER);
        userInfoPanel.add(profileImage, BorderLayout.WEST);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            JPanel userDetailsPanel = new JPanel(new GridLayout(0, 1, 5, 5));

            String userQuery = "SELECT CUSTOMER_ID, FIRST_NAME, LAST_NAME, EMAIL, LOYALTY_POINTS FROM customer WHERE CUSTOMER_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(userQuery);
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("FIRST_NAME") + " " + rs.getString("LAST_NAME");
                String email = rs.getString("EMAIL");
                int loyaltyPoints = rs.getInt("LOYALTY_POINTS");

                JLabel nameLabel = new JLabel("Name: " + fullName);
                nameLabel.setFont(new Font("Arial", Font.BOLD, 16));

                JLabel emailLabel = new JLabel("Email: " + email);
                JLabel pointsLabel = new JLabel("Loyalty Points: " + loyaltyPoints);

                userDetailsPanel.add(nameLabel);
                userDetailsPanel.add(pointsLabel);

                JPanel emailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                emailPanel.add(emailLabel);

                JLabel editEmailIcon = new JLabel(new ImageIcon(new ImageIcon("images/edit.png").getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
                editEmailIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
                editEmailIcon.setToolTipText("Edit Email");

                editEmailIcon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        String newEmail = JOptionPane.showInputDialog(ProfileFrame.this, "Enter new email:", email);
                        if (newEmail != null && !newEmail.trim().isEmpty()) {
                            updateCustomerEmail(newEmail);
                            emailLabel.setText("Email: " + newEmail);
                        }
                    }
                });

                emailPanel.add(editEmailIcon);
                userDetailsPanel.add(emailPanel);
            } else {
                JOptionPane.showMessageDialog(this, "Customer not found.", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }

            userInfoPanel.add(userDetailsPanel, BorderLayout.CENTER);
            profilePanel.add(userInfoPanel);

            JPanel mobilePanel = new JPanel(new BorderLayout());
            mobilePanel.setBorder(BorderFactory.createTitledBorder("Contact Numbers"));

            JPanel numbersPanel = new JPanel();
            numbersPanel.setLayout(new BoxLayout(numbersPanel, BoxLayout.Y_AXIS));

            String mobileQuery = "SELECT MOBILE_NO FROM customer_mobile WHERE CUSTOMER_ID = ?";
            PreparedStatement mobileStmt = conn.prepareStatement(mobileQuery);
            mobileStmt.setInt(1, customerId);
            ResultSet mobRs = mobileStmt.executeQuery();

            while (mobRs.next()) {
                String mobileNo = mobRs.getString("MOBILE_NO");
                JPanel numberRow = createMobileNumberRow(numbersPanel, mobileNo);
                numbersPanel.add(numberRow);
            }

            JButton addNewNumberBtn = new JButton("Add New Number");
            addNewNumberBtn.addActionListener(e -> {
                String newNumber = JOptionPane.showInputDialog(ProfileFrame.this, "Enter new mobile number:");
                if (newNumber != null && !newNumber.trim().isEmpty()) {
                    addMobileNumber(newNumber);
                    JPanel numberRow = createMobileNumberRow(numbersPanel, newNumber);
                    numbersPanel.add(numberRow);
                    numbersPanel.revalidate();
                    numbersPanel.repaint();
                }
            });

            mobilePanel.add(new JScrollPane(numbersPanel), BorderLayout.CENTER);
            mobilePanel.add(addNewNumberBtn, BorderLayout.SOUTH);
            profilePanel.add(mobilePanel);

            JPanel addressPanel = new JPanel(new BorderLayout());
            addressPanel.setBorder(BorderFactory.createTitledBorder("Address"));

            String addressQuery = "SELECT CITY, STATE, COUNTRY FROM customer WHERE CUSTOMER_ID = ?";
            PreparedStatement addressStmt = conn.prepareStatement(addressQuery);
            addressStmt.setInt(1, customerId);
            ResultSet addressRs = addressStmt.executeQuery();

            if (addressRs.next()) {
                JPanel addressFields = new JPanel(new GridLayout(3, 2, 5, 5));

                JLabel cityLabel = new JLabel("City:");
                JTextField cityField = new JTextField(addressRs.getString("CITY"), 20);

                JLabel stateLabel = new JLabel("State:");
                JTextField stateField = new JTextField(addressRs.getString("STATE"), 20);

                JLabel countryLabel = new JLabel("Country:");
                JTextField countryField = new JTextField(addressRs.getString("COUNTRY"), 20);

                addressFields.add(cityLabel);
                addressFields.add(cityField);
                addressFields.add(stateLabel);
                addressFields.add(stateField);
                addressFields.add(countryLabel);
                addressFields.add(countryField);

                JButton updateAddressBtn = new JButton("Update Address");
                updateAddressBtn.addActionListener(e -> {
                    updateCustomerAddress(cityField.getText(), stateField.getText(), countryField.getText());
                    JOptionPane.showMessageDialog(ProfileFrame.this, "Address updated successfully!");
                });

                addressPanel.add(addressFields, BorderLayout.CENTER);
                addressPanel.add(updateAddressBtn, BorderLayout.SOUTH);
            }

            profilePanel.add(addressPanel);

            // ✅ Add Transaction Button at the end
            JButton transactionButton = new JButton("Transaction History");
            transactionButton.addActionListener(e -> new TransactionHistoryFrame(customerId));
            profilePanel.add(transactionButton);

            ShoppingHistoryPanel shoppingHistoryPanel = new ShoppingHistoryPanel(customerId);
            profilePanel.add(shoppingHistoryPanel);

            

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage());
        }

        add(new JScrollPane(profilePanel), BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createMobileNumberRow(JPanel parentPanel, String mobileNo) {
        JPanel numberRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel mobileLabel = new JLabel(mobileNo);

        JLabel editIcon = new JLabel(new ImageIcon(new ImageIcon("images/edit.png").getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        editIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editIcon.setToolTipText("Edit Number");

        JLabel deleteIcon = new JLabel(new ImageIcon(new ImageIcon("images/delete.png").getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        deleteIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteIcon.setToolTipText("Delete Number");

        final String currentNumber = mobileNo;

        editIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String newNumber = JOptionPane.showInputDialog(ProfileFrame.this, "Enter new mobile number:", currentNumber);
                if (newNumber != null && !newNumber.trim().isEmpty()) {
                    updateMobileNumber(currentNumber, newNumber);
                    mobileLabel.setText(newNumber);
                }
            }
        });

        deleteIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int confirm = JOptionPane.showConfirmDialog(ProfileFrame.this,
                        "Are you sure you want to delete this number?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    deleteMobileNumber(currentNumber);
                    parentPanel.remove(numberRow);
                    parentPanel.revalidate();
                    parentPanel.repaint();
                }
            }
        });

        numberRow.add(mobileLabel);
        numberRow.add(editIcon);
        numberRow.add(deleteIcon);

        return numberRow;
    }

    private void updateCustomerEmail(String newEmail) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String query = "UPDATE customer SET EMAIL = ? WHERE CUSTOMER_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, newEmail);
            stmt.setInt(2, customerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating email: " + e.getMessage());
        }
    }

    private void updateMobileNumber(String oldNumber, String newNumber) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String query = "UPDATE customer_mobile SET MOBILE_NO = ? WHERE CUSTOMER_ID = ? AND MOBILE_NO = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, newNumber);
            stmt.setInt(2, customerId);
            stmt.setString(3, oldNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating mobile number: " + e.getMessage());
        }
    }

    private void deleteMobileNumber(String number) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String query = "DELETE FROM customer_mobile WHERE CUSTOMER_ID = ? AND MOBILE_NO = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, customerId);
            stmt.setString(2, number);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting mobile number: " + e.getMessage());
        }
    }

    private void addMobileNumber(String number) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String query = "INSERT INTO customer_mobile (CUSTOMER_ID, MOBILE_NO) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, customerId);
            stmt.setString(2, number);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding mobile number: " + e.getMessage());
        }
    }

    private void updateCustomerAddress(String city, String state, String country) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql")) {
            String query = "UPDATE customer SET CITY = ?, STATE = ?, COUNTRY = ? WHERE CUSTOMER_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, city);
            stmt.setString(2, state);
            stmt.setString(3, country);
            stmt.setInt(4, customerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating address: " + e.getMessage());
        }
    }
}
