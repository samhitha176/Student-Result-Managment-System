import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;


public class ModernLoginUI extends JFrame {

    JTextField userField;
    JPasswordField passwordField;
    JButton adminBtn, studentBtn, loginBtn;
    String role = "student";

    Connection con;

    public ModernLoginUI() {

        connectDB();

        setTitle("Student Result Management System");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));
        setResizable(true);

        setUndecorated(false);

        // ================= LEFT PANEL =================
        JPanel leftPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background - dark blue to lighter blue
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(15, 30, 70),      // Dark blue at top
                        0, getHeight(), new Color(30, 70, 150)); // Lighter blue at bottom
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Add subtle overlay pattern (optional - very faint)
                g2.setColor(new Color(255, 255, 255, 15));
                for (int i = 0; i < getWidth(); i += 40) {
                    for (int j = 0; j < getHeight(); j += 40) {
                        g2.fillOval(i, j, 3, 3);
                    }
                }
            }
        };

        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel iconLabel = new JLabel("🎓");
        iconLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 80));
        iconLabel.setForeground(Color.WHITE);

        gbc.gridy = 0;
        leftPanel.add(iconLabel, gbc);

        JLabel title = new JLabel("<html><center>Student Result<br>Management System</center></html>");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("segoe ui emoji", Font.BOLD, 34));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 1;
        leftPanel.add(title, gbc);

        JLabel desc = new JLabel(
                "<html><center>A comprehensive platform for managing and<br>visualizing student academic performance.</center></html>");
        desc.setForeground(new Color(255, 255, 255, 220));
        desc.setFont(new Font("segoe ui emoji", Font.PLAIN, 15));

        gbc.gridy = 2;
        leftPanel.add(desc, gbc);

        // ================= RIGHT PANEL =================
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(new Font("segoe ui emoji", Font.BOLD, 30));
        welcome.setForeground(new Color(33, 33, 33));
        welcome.setBounds(70, 100, 300, 40);

        JLabel sub = new JLabel("Sign in to your account to continue");
        sub.setForeground(new Color(120, 120, 120));
        sub.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));
        sub.setBounds(70, 140, 300, 20);

        adminBtn = createCurvedButton("👤 Admin", 20);
        studentBtn = createCurvedButton("👥 Student", 20);

        adminBtn.setBounds(70, 180, 140, 45);
        studentBtn.setBounds(220, 180, 140, 45);

        selectStudent();

        adminBtn.addActionListener(e -> {
            role = "admin";
            selectAdmin();
            updateFieldLabels();
        });

        studentBtn.addActionListener(e -> {
            role = "student";
            selectStudent();
            updateFieldLabels();
        });

        JLabel userLabel = new JLabel("Roll Number");
        userLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 13));
        userLabel.setForeground(new Color(80, 80, 80));
        userLabel.setBounds(70, 250, 300, 20);

        userField = new JTextField();
        userField.setBounds(70, 275, 400, 50);
        userField.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));
        userField.setBorder(new RoundBorder(20, new Color(220, 220, 220)));

        JLabel passLabel = new JLabel("Name (as password)");
        passLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 13));
        passLabel.setForeground(new Color(80, 80, 80));
        passLabel.setBounds(70, 345, 300, 20);

        passwordField = new JPasswordField();
        passwordField.setBounds(70, 370, 400, 50);
        passwordField.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));
        passwordField.setBorder(new RoundBorder(20, new Color(220, 220, 220)));

        JLabel hintLabel = new JLabel("Use your roll number and name to login");
        hintLabel.setFont(new Font("segoe ui emoji", Font.ITALIC, 12));
        hintLabel.setForeground(new Color(25, 118, 210, 180));
        hintLabel.setBounds(70, 425, 400, 20);

        loginBtn = new JButton("Sign In") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(25, 118, 210),
                        getWidth(), 0, new Color(52, 152, 219));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("segoe ui emoji", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                g2.drawString(getText(), (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 3);

                g2.dispose();
            }
        };

        loginBtn.setBounds(70, 470, 400, 55);
        loginBtn.setFocusPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorder(BorderFactory.createEmptyBorder());
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });

        loginBtn.addActionListener(e -> login());

        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 20));
        closeBtn.setForeground(new Color(150, 150, 150));
        closeBtn.setBounds(530, 20, 40, 40);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeBtn.setForeground(Color.RED);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeBtn.setForeground(new Color(150, 150, 150));
            }
        });

        closeBtn.addActionListener(e -> {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException ex) {
            }
            System.exit(0);
        });

        rightPanel.add(welcome);
        rightPanel.add(sub);
        rightPanel.add(adminBtn);
        rightPanel.add(studentBtn);
        rightPanel.add(userLabel);
        rightPanel.add(userField);
        rightPanel.add(passLabel);
        rightPanel.add(passwordField);
        rightPanel.add(hintLabel);
        rightPanel.add(loginBtn);
        rightPanel.add(closeBtn);

        add(leftPanel);
        add(rightPanel);

        setVisible(true);
    }

    class RoundBorder extends AbstractBorder {
        private int radius;
        private Color color;

        public RoundBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(10, 15, 10, 15);
        }
    }

    private JButton createCurvedButton(String text, int radius) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else {
                    g2.setColor(getBackground());
                }

                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                
                // Draw text with emoji icon (already included in text parameter)
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, 
                        (getHeight() + fm.getAscent()) / 2 - 3);

                g2.dispose();
            }
        };

        btn.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(230, 230, 230));
        btn.setForeground(new Color(60, 60, 60));

        return btn;
    }

    private void connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/studentdb?useSSL=false&serverTimezone=UTC";
            con = DriverManager.getConnection(url, "root", "root");
            System.out.println("✅ Database Connected Successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Database Connection Failed!\n" + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void login() {
        String username = userField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill all fields!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (con == null) {
            JOptionPane.showMessageDialog(this,
                    "Database connection not available!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (role.equals("admin")) {
                // Check if users table exists, if not create it
                createUsersTableIfNotExists();

                String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = 'admin'";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, username);
                pst.setString(2, password);

                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this,
                            "✅ Admin Login Successful!\nWelcome " + username,
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    dispose(); // Close login window

                    SwingUtilities.invokeLater(() -> {
                        AdminDashboard dashboard = new AdminDashboard(con);
                        dashboard.setVisible(true);
                    });
                } else {
                    JOptionPane.showMessageDialog(this,
                            "❌ Invalid Admin Credentials!\nDefault Admin - Username: admin, Password: admin123",
                            "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
                rs.close();
                pst.close();

            } else {
                // STUDENT LOGIN - Works for ALL students in database
                String query = "SELECT * FROM students WHERE roll = ? AND name = ?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, username);
                pst.setString(2, password);

                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    String studentName = rs.getString("name");
                    String studentRoll = rs.getString("roll");
                    String dept = rs.getString("dept");

                    JOptionPane.showMessageDialog(this,
                            "✅ Login Successful!\n\nWelcome " + studentName,
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    dispose(); // Close login window

                    // Launch Student Dashboard for ANY student
                    SwingUtilities.invokeLater(() -> {
                        StudentDashboard dashboard = new StudentDashboard(con, studentRoll, studentName, dept);
                        dashboard.setVisible(true);
                    });
                } else {
                    // Show sample credentials for reference
                    String sampleStudents = getSampleStudents();
                    JOptionPane.showMessageDialog(this,
                            "❌ Invalid Student Credentials!\n\nUse your Roll Number and Name.\n\n" + sampleStudents,
                            "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
                rs.close();
                pst.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSampleStudents() {
        StringBuilder samples = new StringBuilder("Sample Students:\n");
        samples.append("────────────────\n");

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT roll, name, dept FROM students LIMIT 5");

            int count = 0;
            while (rs.next()) {
                count++;
                String roll = rs.getString("roll");
                String name = rs.getString("name");
                String dept = rs.getString("dept");
                samples.append("• ").append(roll).append(" / ").append(name).append(" (").append(dept).append(")\n");
            }

            if (count == 0) {
                samples.append("No students found in database!");
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            samples.append("Error loading samples");
        }

        return samples.toString();
    }

    private void createUsersTableIfNotExists() {
        try {
            Statement stmt = con.createStatement();

            // Create users table if it doesn't exist
            String createTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(50) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL)";
            stmt.executeUpdate(createTable);

            // Check if admin user exists
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'");
            rs.next();
            if (rs.getInt(1) == 0) {
                // Insert default admin
                String insertAdmin = "INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'admin')";
                stmt.executeUpdate(insertAdmin);
                System.out.println("✅ Default admin created - Username: admin, Password: admin123");
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void selectAdmin() {
        adminBtn.setBackground(new Color(25, 118, 210));
        adminBtn.setForeground(Color.WHITE);

        studentBtn.setBackground(new Color(230, 230, 230));
        studentBtn.setForeground(new Color(60, 60, 60));
    }

    private void selectStudent() {
        studentBtn.setBackground(new Color(25, 118, 210));
        studentBtn.setForeground(Color.WHITE);

        adminBtn.setBackground(new Color(230, 230, 230));
        adminBtn.setForeground(new Color(60, 60, 60));
    }

    private void updateFieldLabels() {
        Container parent = userField.getParent();
        if (parent instanceof JPanel) {
            Component[] components = parent.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    String text = label.getText();

                    if (role.equals("admin")) {
                        if (text.contains("Roll") || text.contains("Number")) {
                            label.setText("Username");
                        } else if (text.contains("Name") || text.contains("password")) {
                            label.setText("Password");
                        }
                    } else {
                        if (text.contains("Username") || text.contains("Email")) {
                            label.setText("Roll Number");
                        } else if (text.contains("Password")) {
                            label.setText("Name");
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new ModernLoginUI());
    }
}