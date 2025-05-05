package f2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SupportFrame extends JFrame {

    private final int customerId;
    private JTextArea queryTextArea;
    private JTextField searchField;
    private JTextArea resultArea;

    public SupportFrame(int customerId) {
        this.customerId = customerId;
        initComponents();
    }

    private void initComponents() {
        setTitle("Customer Support");
        setSize(600, 550); // Increased height for clarity
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel("Submit a Support Query");
        heading.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(heading, BorderLayout.NORTH);

        // Query input section
        queryTextArea = new JTextArea(5, 40);
        JScrollPane queryScroll = new JScrollPane(queryTextArea);

        JButton submitButton = new JButton("Submit Query");
        submitButton.addActionListener(e -> submitQuery());

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(queryScroll, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.SOUTH);

        // Search section with GridBagLayout for better alignment
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Queries"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(new JLabel("Support ID:"), gbc);

        gbc.gridx = 1;
        searchField = new JTextField(15);
        searchPanel.add(searchField, gbc);

        gbc.gridx = 2;
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchQuery());
        searchPanel.add(searchButton, gbc);

        // Result area
        resultArea = new JTextArea(10, 50);
        resultArea.setEditable(false);
        JScrollPane resultScroll = new JScrollPane(resultArea);

        // Center panel using vertical BoxLayout
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(inputPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(searchPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(resultScroll);

        panel.add(centerPanel, BorderLayout.CENTER);
        setContentPane(panel);
        pack(); // Ensures proper sizing
    }

    private void submitQuery() {
        String queryText = queryTextArea.getText().trim();
        if (queryText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your query before submitting.", "Missing Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO support (QUERY_STATUS, DESCRIPTION) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql");
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, "Open"); // default status
            stmt.setString(2, queryText);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int supportId = generatedKeys.getInt(1);
                    JOptionPane.showMessageDialog(this,
                            "Support query submitted successfully.\nYour Support ID is: " + supportId,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Query submitted, but failed to retrieve Support ID.",
                            "Partial Success", JOptionPane.WARNING_MESSAGE);
                }
                queryTextArea.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit query.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred.", "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchQuery() {
        String input = searchField.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Support ID to search.", "Missing Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int supportId;
        try {
            supportId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Support ID must be a number.", "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "SELECT * FROM support WHERE SUPPORT_ID = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbmspj", "root", "mysql");
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supportId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder results = new StringBuilder();
            if (rs.next()) {
                results.append("Support ID: ").append(rs.getInt("SUPPORT_ID")).append("\n")
                        .append("Status: ").append(rs.getString("QUERY_STATUS")).append("\n")
                        .append("Description: ").append(rs.getString("DESCRIPTION")).append("\n")
                        .append("------------------------\n");
            } else {
                results.append("No support query found with the given ID.");
            }

            resultArea.setText(results.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred.", "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
