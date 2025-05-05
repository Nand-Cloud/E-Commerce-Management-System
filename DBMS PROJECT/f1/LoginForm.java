import f2.ProductByCategoryApp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/dbmspj";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "mysql";
    private static final String AUTH_QUERY = "SELECT * FROM security_confi WHERE email = ? AND pass = ?";

    public LoginForm() {
        setTitle("Login Form");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel background = new JLabel(new ImageIcon("back1.jpg"));
        background.setBounds(0, 0, 1000, 700);
        background.setLayout(null);
        add(background);

        ImageIcon logoIcon = new ImageIcon("n1.png");
        Image logoImg = logoIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        logoIcon = new ImageIcon(logoImg);
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setBounds(100, 10, 200, 200);
        background.add(logoLabel);

        JLabel welcomeLabel = new JLabel("WELCOME", SwingConstants.CENTER);
        welcomeLabel.setBounds(100, 220, 200, 30);
        welcomeLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.RED);
        background.add(welcomeLabel);

        JLabel loginLabel = new JLabel("Login", SwingConstants.CENTER);
        loginLabel.setBounds(150, 260, 100, 25);
        loginLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        loginLabel.setForeground(Color.BLUE);
        background.add(loginLabel);

        JTextField emailField = new JTextField();
        emailField.setBounds(100, 300, 200, 30);
        background.add(emailField);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(100, 340, 200, 30);
        background.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(100, 380, 200, 30);
        loginButton.setBackground(Color.PINK);
        background.add(loginButton);

        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                        PreparedStatement ps = connection.prepareStatement(AUTH_QUERY)) {

                    ps.setString(1, email);
                    ps.setString(2, password);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        int customerId = rs.getInt("customer_id"); // Important
                        JOptionPane.showMessageDialog(this, "Login Successful!");
                        dispose(); // Close login form
                        new ProductByCategoryApp(customerId).setVisible(true); // pass customer_id
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid Email or Password", "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Exception",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new LoginForm();
    }
}
