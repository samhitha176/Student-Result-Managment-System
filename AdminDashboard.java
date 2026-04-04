import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.TitledBorder;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.geom.*;

public class AdminDashboard extends JFrame {
    Connection con;
    JPanel mainContent;
    CardLayout cardLayout;
    String adminName = "saikala";
    String adminEmail = "saikala@school.edu";

    // Table models for refresh
    DefaultTableModel studentsTableModel;
    DefaultTableModel marksTableModel;
    JTable studentsTable;
    JTable marksTable;
    JComboBox<String> studentFilterCombo;

    // Panel references for proper refresh
    JPanel dashboardPanel;
    JPanel analyticsPanel;
    JPanel studentsPanel;
    JPanel marksPanel;
    JPanel addStudentPanel;
    JPanel addMarksPanel;
    JPanel revaluationPanel;

    // Revaluation components
    DefaultTableModel revaluationTableModel;
    JTable revaluationTable;
    JLabel pendingRevaluationCount;
    Timer notificationTimer;
    JPanel revaluationStatsPanel;
    
    // Add Marks panel components
    JComboBox<String> addMarksStudentCombo;
    JTextField marksField;
    JComboBox<String> subjectCombo;
    JComboBox<String> semesterCombo;

    public AdminDashboard(Connection existingCon) {
        this.con = existingCon;
        initComponents();
    }

    public AdminDashboard() {
        connectDB();
        initComponents();
    }

    private void connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/studentdb?useSSL=false&serverTimezone=UTC";
            con = DriverManager.getConnection(url, "root", "root");
            System.out.println("✅ Database Connected Successfully!");
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Database Connection Failed!\n" + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        setTitle("Admin Dashboard - Student Result Management System");
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createNotificationsTableIfNotExists();

        // Top Navigation with icons
        JPanel topNav = createTopNav();

        // Main Split
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(1);

        // Sidebar with icons
        JPanel sidebar = createSidebar();

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(new Color(248, 250, 252));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create all panels
        dashboardPanel = createDashboardPanel();
        analyticsPanel = createAnalyticsPanel();
        studentsPanel = createStudentsPanel();
        marksPanel = createMarksPanel();
        addStudentPanel = createAddStudentPanel();
        addMarksPanel = createAddMarksPanel();
        revaluationPanel = createRevaluationPanel();

        // Add component listeners for refresh
        addMarksPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshAddMarksStudentCombo();
            }
        });
        
        marksPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadMarksData();
                loadStudentsToCombo();
            }
        });
        
        studentsPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadStudentsData();
            }
        });

        mainContent.add(dashboardPanel, "dashboard");
        mainContent.add(analyticsPanel, "analytics");
        mainContent.add(studentsPanel, "students");
        mainContent.add(marksPanel, "marks");
        mainContent.add(addStudentPanel, "addstudent");
        mainContent.add(addMarksPanel, "addmarks");
        mainContent.add(revaluationPanel, "revaluation");

        splitPane.setLeftComponent(sidebar);
        splitPane.setRightComponent(mainContent);

        add(topNav, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        cardLayout.show(mainContent, "dashboard");

        startNotificationChecker();

        setVisible(true);
    }

    private void createNotificationsTableIfNotExists() {
        try {
            Statement stmt = con.createStatement();
            String createTable = "CREATE TABLE IF NOT EXISTS notifications (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_roll VARCHAR(20), " +
                    "message TEXT, " +
                    "type VARCHAR(50), " +
                    "is_read BOOLEAN DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (student_roll) REFERENCES students(roll))";
            stmt.executeUpdate(createTable);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createTopNav() {
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setBackground(Color.WHITE);
        topNav.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Left side with enhanced icons
        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftNav.setBackground(Color.WHITE);
        
        JLabel logoLabel = createIconLabel("🏛️", 28);
        JLabel titleLabel = new JLabel("SRMS Admin");
        titleLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 18));
        titleLabel.setForeground(new Color(25, 118, 210));
        
        leftNav.add(logoLabel);
        leftNav.add(titleLabel);

        // Right side with enhanced profile section
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightNav.setBackground(Color.WHITE);
        
        JLabel adminIcon = createIconLabel("👨‍🏫", 24);
        JPanel profilePanel = new JPanel(new GridLayout(2, 1));
        profilePanel.setBackground(Color.WHITE);
        
        JLabel adminNameLabel = new JLabel(adminName);
        adminNameLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));
        
        JLabel adminEmailLabel = new JLabel(adminEmail);
        adminEmailLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
        adminEmailLabel.setForeground(Color.GRAY);
        
        profilePanel.add(adminNameLabel);
        profilePanel.add(adminEmailLabel);

        JButton logoutBtn = createIconButton("🚪 Logout", new Color(25, 118, 210), false);
        logoutBtn.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                ModernLoginUI loginUI = new ModernLoginUI();
                loginUI.setVisible(true);
            });
        });

        rightNav.add(adminIcon);
        rightNav.add(profilePanel);
        rightNav.add(logoutBtn);

        topNav.add(leftNav, BorderLayout.WEST);
        topNav.add(rightNav, BorderLayout.EAST);

        return topNav;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(25, 38, 57));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        String[][] menuItems = {
                { "🏠 Dashboard", "dashboard", "📊" },
                { "📈 Analytics", "analytics", "📊" },
                { "👥 Students", "students", "🎓" },
                { "📝 Marks", "marks", "📋" },
                { "🔄 Revaluation", "revaluation", "✨" }
        };

        for (String[] item : menuItems) {
            JButton menuBtn = createSidebarButton(item[0]);
            menuBtn.addActionListener(e -> {
                cardLayout.show(mainContent, item[1]);

                if (item[1].equals("marks")) {
                    loadMarksData();
                    loadStudentsToCombo();
                    refreshAddMarksStudentCombo();
                }
                if (item[1].equals("students")) {
                    loadStudentsData();
                }
                if (item[1].equals("dashboard")) {
                    refreshDashboard();
                }
                if (item[1].equals("revaluation")) {
                    loadRevaluationRequests();
                }
            });
            sidebar.add(menuBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));
        btn.setForeground(new Color(200, 210, 230));
        btn.setBackground(new Color(25, 38, 57));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setForeground(Color.WHITE);
                btn.setBackground(new Color(35, 50, 70));
                btn.setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setForeground(new Color(200, 210, 230));
                btn.setContentAreaFilled(false);
            }
        });
        return btn;
    }

    private JLabel createIconLabel(String icon, int size) {
        JLabel label = new JLabel(icon);
        label.setFont(new Font("segoe ui emoji Emoji", Font.PLAIN, size));
        return label;
    }

    private JButton createIconButton(String text, Color bgColor, boolean filled) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("segoe ui emoji", Font.BOLD, 12));
        btn.setForeground(filled ? Color.WHITE : bgColor);
        if (filled) {
            btn.setBackground(bgColor);
            btn.setBorderPainted(false);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(bgColor));
        }
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createIconStatCard(String icon, String title, String value, String change, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("segoe ui emoji Emoji", Font.PLAIN, 32));
        iconLabel.setForeground(color);
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel changeLabel = new JLabel(change);
        changeLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
        changeLabel.setForeground(change.contains("+") ? new Color(76, 175, 80) : Color.GRAY);
        changeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(valueLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(changeLabel);

        return card;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 250, 252));
        return panel;
    }

    private void refreshDashboard() {
        try {
            dashboardPanel.removeAll();

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(new Color(248, 250, 252));
            
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            titlePanel.setBackground(new Color(248, 250, 252));
            
            JLabel titleIcon = createIconLabel("🏠", 32);
            JLabel title = new JLabel("Dashboard");
            title.setFont(new Font("segoe ui emoji", Font.BOLD, 28));
            
            titlePanel.add(titleIcon);
            titlePanel.add(title);
            
            JLabel subtitle = new JLabel("Overview of student academic performance");
            subtitle.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));
            subtitle.setForeground(Color.GRAY);
            
            topPanel.add(titlePanel, BorderLayout.NORTH);
            topPanel.add(subtitle, BorderLayout.CENTER);

            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM students");
            rs.next();
            int totalStudents = rs.getInt("count");

            rs = stmt.executeQuery("SELECT AVG(marks) as avg FROM marks");
            rs.next();
            double avgMarks = rs.getDouble("avg");

            rs = stmt.executeQuery("SELECT MAX(marks) as max FROM marks");
            rs.next();
            int topScore = rs.getInt("max");

            rs = stmt.executeQuery("SELECT COUNT(DISTINCT subject) as count FROM marks");
            rs.next();
            int subjects = rs.getInt("count");

            int pendingReval = 0;
            try {
                rs = stmt.executeQuery("SELECT COUNT(*) as count FROM revaluation_requests WHERE status = 'Pending'");
                if (rs.next()) {
                    pendingReval = rs.getInt("count");
                }
            } catch (SQLException e) {
                // Table might not exist yet
            }

            JPanel statsPanel = new JPanel(new GridLayout(1, 5, 15, 0));
            statsPanel.setBackground(new Color(248, 250, 252));
            statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

            statsPanel.add(createIconStatCard("👥", "Total Students", String.valueOf(totalStudents), "+12% this semester", new Color(25, 118, 210)));
            statsPanel.add(createIconStatCard("📊", "Avg. Score", String.format("%.1f%%", avgMarks), "+5% vs last year", new Color(76, 175, 80)));
            statsPanel.add(createIconStatCard("🏆", "Top Scorer", String.valueOf(topScore), "Outstanding performance", new Color(255, 152, 0)));
            statsPanel.add(createIconStatCard("📚", "Subjects", String.valueOf(subjects), "Active courses", new Color(156, 39, 176)));
            statsPanel.add(createIconStatCard("🔄", "Pending Reval", String.valueOf(pendingReval), "Awaiting review", new Color(233, 30, 99)));

            JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
            contentPanel.setBackground(new Color(248, 250, 252));
            contentPanel.add(createSubjectMarksPanel());
            contentPanel.add(createPassFailPanel());

            dashboardPanel.add(topPanel, BorderLayout.NORTH);
            dashboardPanel.add(statsPanel, BorderLayout.CENTER);
            dashboardPanel.add(contentPanel, BorderLayout.SOUTH);

            dashboardPanel.revalidate();
            dashboardPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createSubjectMarksPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleIcon = createIconLabel("📊", 20);
        JLabel title = new JLabel("Average Marks by Subject");
        title.setFont(new Font("segoe ui emoji", Font.BOLD, 16));
        
        titlePanel.add(titleIcon);
        titlePanel.add(title);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(titlePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT subject, AVG(marks) as avg FROM marks GROUP BY subject ORDER BY avg DESC");

            Color[] colors = {
                    new Color(25, 118, 210),
                    new Color(76, 175, 80),
                    new Color(255, 152, 0),
                    new Color(156, 39, 176),
                    new Color(233, 30, 99),
                    new Color(0, 150, 136)
            };

            int i = 0;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String subject = rs.getString("subject");
                int avg = (int) rs.getDouble("avg");
                Color color = colors[i % colors.length];

                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBackground(Color.WHITE);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                leftPanel.setBackground(Color.WHITE);
                
                JLabel iconLabel = createIconLabel(getSubjectIcon(subject), 16);
                JLabel nameLabel = new JLabel(subject);
                nameLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));
                
                leftPanel.add(iconLabel);
                leftPanel.add(nameLabel);

                AnimatedProgressBar bar = new AnimatedProgressBar(0, 100, avg);
                bar.setForeground(color);
                bar.setBackground(new Color(240, 240, 240));
                bar.setPreferredSize(new Dimension(150, 8));
                bar.setBorderPainted(false);
                bar.startAnimation();

                JLabel valueLabel = new JLabel(String.valueOf(avg));
                valueLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));
                valueLabel.setForeground(color);

                JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                rightPanel.setBackground(Color.WHITE);
                rightPanel.add(bar);
                rightPanel.add(valueLabel);

                row.add(leftPanel, BorderLayout.WEST);
                row.add(rightPanel, BorderLayout.EAST);

                panel.add(row);
                panel.add(Box.createRigidArea(new Dimension(0, 10)));
                i++;
            }

            if (!hasData) {
                JLabel noDataLabel = new JLabel("📭 No marks data available");
                noDataLabel.setFont(new Font("segoe ui emoji", Font.ITALIC, 14));
                noDataLabel.setForeground(Color.GRAY);
                noDataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(noDataLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return panel;
    }

    private String getSubjectIcon(String subject) {
        if (subject.contains("Math")) return "🧮";
        if (subject.contains("Phys")) return "⚡";
        if (subject.contains("Chem")) return "🧪";
        if (subject.contains("Bio")) return "🧬";
        if (subject.contains("Computer")) return "💻";
        if (subject.contains("English")) return "📖";
        if (subject.contains("Econ")) return "💰";
        if (subject.contains("Commerce")) return "📈";
        if (subject.contains("Hist")) return "🏛️";
        if (subject.contains("Geo")) return "🌍";
        return "📚";
    }

    class AnimatedProgressBar extends JProgressBar {
        private int targetValue;
        private Timer animationTimer;

        public AnimatedProgressBar(int min, int max, int target) {
            super(min, max);
            this.targetValue = target;
            setValue(0);
            setStringPainted(false);
        }

        public void startAnimation() {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }
            animationTimer = new Timer(20, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (getValue() < targetValue) {
                        setValue(getValue() + 1);
                    } else {
                        animationTimer.stop();
                    }
                    repaint();
                }
            });
            animationTimer.start();
        }
    }

    class AnimatedPieChart extends JPanel {
        private int passPercentage;
        private int currentAngle = 0;
        private Timer animationTimer;
        private int passCount, failCount, total;

        public AnimatedPieChart(int pass, int fail, int total) {
            this.passCount = pass;
            this.failCount = fail;
            this.total = total;
            this.passPercentage = (total > 0) ? (pass * 100 / total) : 0;
            setBackground(Color.WHITE);
            startAnimation();
        }

        public void startAnimation() {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }
            animationTimer = new Timer(20, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int targetAngle = (int) (360.0 * passCount / total);
                    if (currentAngle < targetAngle) {
                        currentAngle += 2;
                    } else {
                        animationTimer.stop();
                    }
                    repaint();
                }
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int diameter = 150;
            int x = (getWidth() - diameter) / 2;
            int y = 10;

            g2.setColor(new Color(25, 118, 210));
            g2.fillArc(x, y, diameter, diameter, 90, -currentAngle);

            g2.setColor(new Color(220, 20, 60));
            g2.fillArc(x, y, diameter, diameter, 90 - currentAngle, -(360 - currentAngle));

            g2.setColor(Color.WHITE);
            g2.fillOval(x + 30, y + 30, diameter - 60, diameter - 60);

            g2.setColor(new Color(60, 60, 60));
            g2.setFont(new Font("segoe ui emoji", Font.BOLD, 16));
            String percentText = passPercentage + "%";
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(percentText);
            int textHeight = fm.getAscent();
            g2.drawString(percentText,
                    x + (diameter - textWidth) / 2,
                    y + (diameter + textHeight) / 2);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 180);
        }
    }

    private JPanel createPassFailPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleIcon = createIconLabel("📈", 20);
        JLabel title = new JLabel("Pass / Fail Rate");
        title.setFont(new Font("segoe ui emoji", Font.BOLD, 16));
        
        titlePanel.add(titleIcon);
        titlePanel.add(title);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(titlePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        try {
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM students");
            rs.next();
            int total = rs.getInt("total");

            rs = stmt.executeQuery("SELECT COUNT(DISTINCT student_roll) as pass FROM marks WHERE marks >= 40");
            rs.next();
            int pass = rs.getInt("pass");
            int fail = total - pass;

            if (total == 0) {
                JLabel noDataLabel = new JLabel("📭 No student data available");
                noDataLabel.setFont(new Font("segoe ui emoji", Font.ITALIC, 14));
                noDataLabel.setForeground(Color.GRAY);
                noDataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(noDataLabel);
                return panel;
            }

            AnimatedPieChart chartPanel = new AnimatedPieChart(pass, fail, total);
            chartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(chartPanel);

            panel.add(Box.createRigidArea(new Dimension(0, 20)));

            JPanel legendPanel = new JPanel(new GridLayout(2, 2, 20, 10));
            legendPanel.setBackground(Color.WHITE);
            legendPanel.setMaximumSize(new Dimension(300, 80));
            legendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel passLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            passLegend.setBackground(Color.WHITE);
            JLabel passIcon = createIconLabel("✅", 16);
            JLabel passText = new JLabel("Pass");
            passText.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));
            passLegend.add(passIcon);
            passLegend.add(passText);

            JLabel passCountLabel = new JLabel(pass + " students");
            passCountLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));
            passCountLabel.setForeground(new Color(25, 118, 210));

            JPanel failLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            failLegend.setBackground(Color.WHITE);
            JLabel failIcon = createIconLabel("❌", 16);
            JLabel failText = new JLabel("Fail");
            failText.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));
            failLegend.add(failIcon);
            failLegend.add(failText);

            JLabel failCountLabel = new JLabel(fail + " students");
            failCountLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));
            failCountLabel.setForeground(new Color(220, 20, 60));

            legendPanel.add(passLegend);
            legendPanel.add(passCountLabel);
            legendPanel.add(failLegend);
            legendPanel.add(failCountLabel);

            panel.add(legendPanel);

            panel.add(Box.createRigidArea(new Dimension(0, 15)));

            JPanel barContainer = new JPanel(new BorderLayout(10, 0));
            barContainer.setBackground(new Color(240, 240, 240));
            barContainer.setPreferredSize(new Dimension(250, 20));
            barContainer.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

            JPanel passBar = new JPanel() {
                private int currentWidth = 0;
                private Timer barTimer;

                {
                    barTimer = new Timer(20, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int targetWidth = (int) (getWidth() * pass / (double) total);
                            if (currentWidth < targetWidth) {
                                currentWidth += 2;
                                repaint();
                            } else {
                                barTimer.stop();
                            }
                        }
                    });
                    barTimer.start();
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(25, 118, 210));
                    g2.fillRect(0, 0, currentWidth, getHeight());
                }
            };
            passBar.setBackground(new Color(240, 240, 240));
            passBar.setPreferredSize(new Dimension(250, 20));

            barContainer.add(passBar, BorderLayout.CENTER);

            JLabel statsIcon = createIconLabel("📊", 20);
            
            JPanel percentTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
            percentTextPanel.setBackground(Color.WHITE);

            JLabel passPercentLabel = new JLabel(String.format("✅ Pass: %.1f%%", (pass * 100.0 / total)));
            passPercentLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 13));
            passPercentLabel.setForeground(new Color(25, 118, 210));

            JLabel failPercentLabel = new JLabel(String.format("❌ Fail: %.1f%%", (fail * 100.0 / total)));
            failPercentLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 13));
            failPercentLabel.setForeground(new Color(220, 20, 60));

            percentTextPanel.add(passPercentLabel);
            percentTextPanel.add(failPercentLabel);

            panel.add(barContainer);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
            panel.add(percentTextPanel);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return panel;
    }

    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleIcon = createIconLabel("📊", 32);
        JLabel title = new JLabel("Analytics Dashboard");
        title.setFont(new Font("segoe ui emoji", Font.BOLD, 24));
        title.setForeground(new Color(25, 118, 210));
        
        titlePanel.add(titleIcon);
        titlePanel.add(title);
        
        panel.add(titlePanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setBorder(null);

        JPanel gradePanel = new JPanel(new BorderLayout());
        gradePanel.setBackground(Color.WHITE);
        gradePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                "📈 Grade Distribution",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("segoe ui emoji", Font.BOLD, 16)));

        String[] grades = { "A+", "A", "B+", "B", "C", "D", "F" };
        int[] counts = { 12, 10, 8, 6, 4, 3, 2 };
        String[] gradeIcons = { "🌟", "⭐", "✨", "📘", "📗", "📙", "❌" };
        Color[] colors = {
                new Color(25, 118, 210),
                new Color(33, 150, 243),
                new Color(76, 175, 80),
                new Color(255, 152, 0),
                new Color(255, 193, 7),
                new Color(156, 39, 176),
                new Color(220, 20, 60)
        };

        JPanel chartPanel = new JPanel(new GridLayout(7, 1, 0, 10));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (int i = 0; i < grades.length; i++) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(Color.WHITE);

            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            leftPanel.setBackground(Color.WHITE);
            
            JLabel iconLabel = createIconLabel(gradeIcons[i], 16);
            JLabel gradeLabel = new JLabel(grades[i]);
            gradeLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));
            
            leftPanel.add(iconLabel);
            leftPanel.add(gradeLabel);

            AnimatedProgressBar bar = new AnimatedProgressBar(0, 15, counts[i]);
            bar.setForeground(colors[i]);
            bar.setBackground(new Color(240, 240, 240));
            bar.setPreferredSize(new Dimension(200, 20));
            bar.setBorderPainted(false);
            bar.setStringPainted(true);
            bar.setString(counts[i] + " students");
            bar.startAnimation();

            JLabel countLabel = new JLabel(String.valueOf(counts[i]));
            countLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));
            countLabel.setForeground(colors[i]);

            row.add(leftPanel, BorderLayout.WEST);
            row.add(bar, BorderLayout.CENTER);
            row.add(countLabel, BorderLayout.EAST);

            chartPanel.add(row);
        }

        JScrollPane gradeScroll = new JScrollPane(chartPanel);
        gradeScroll.setBorder(null);
        gradeScroll.getVerticalScrollBar().setUnitIncrement(16);
        gradePanel.add(gradeScroll, BorderLayout.CENTER);

        JPanel topPerformersPanel = new JPanel(new BorderLayout());
        topPerformersPanel.setBackground(Color.WHITE);
        topPerformersPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                "🏆 Top Performers",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("segoe ui emoji", Font.BOLD, 16)));

        JPanel performersList = new JPanel();
        performersList.setLayout(new BoxLayout(performersList, BoxLayout.Y_AXIS));
        performersList.setBackground(Color.WHITE);
        performersList.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        try {
            String query = "SELECT s.roll, s.name, s.dept, AVG(m.marks) as avg_marks " +
                    "FROM students s JOIN marks m ON s.roll = m.student_roll " +
                    "GROUP BY s.roll, s.name, s.dept " +
                    "ORDER BY avg_marks DESC LIMIT 5";

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            int rank = 1;
            String[] rankIcons = { "🥇", "🥈", "🥉", "🏅", "🎖️" };
            Color[] rankColors = {
                    new Color(255, 215, 0),
                    new Color(192, 192, 192),
                    new Color(205, 127, 50),
                    new Color(100, 100, 100),
                    new Color(100, 100, 100)
            };

            while (rs.next()) {
                String name = rs.getString("name");
                String roll = rs.getString("roll");
                String dept = rs.getString("dept");
                double avg = rs.getDouble("avg_marks");

                JPanel performerCard = new JPanel(new BorderLayout(10, 0));
                performerCard.setBackground(Color.WHITE);
                performerCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                        BorderFactory.createEmptyBorder(15, 10, 15, 10)));
                performerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

                JPanel rankPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                rankPanel.setBackground(Color.WHITE);

                JLabel rankLabel = new JLabel(rankIcons[rank - 1] + " " + rank);
                rankLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 16));
                rankLabel.setForeground(rankColors[rank - 1]);
                rankPanel.add(rankLabel);

                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.setBackground(Color.WHITE);

                JLabel nameLabel = new JLabel(name);
                nameLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));

                JLabel detailLabel = new JLabel(roll + " · " + dept);
                detailLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
                detailLabel.setForeground(Color.GRAY);

                infoPanel.add(nameLabel);
                infoPanel.add(detailLabel);

                JPanel scorePanel = new JPanel(new BorderLayout());
                scorePanel.setBackground(Color.WHITE);

                JLabel scoreLabel = new JLabel(String.format("%.1f%%", avg));
                scoreLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 16));
                scoreLabel.setForeground(new Color(25, 118, 210));

                AnimatedProgressBar miniBar = new AnimatedProgressBar(0, 100, (int) avg);
                miniBar.setForeground(new Color(25, 118, 210));
                miniBar.setBackground(new Color(240, 240, 240));
                miniBar.setPreferredSize(new Dimension(60, 6));
                miniBar.setBorderPainted(false);
                miniBar.startAnimation();

                JPanel scoreWithBar = new JPanel(new BorderLayout(5, 0));
                scoreWithBar.setBackground(Color.WHITE);
                scoreWithBar.add(scoreLabel, BorderLayout.NORTH);
                scoreWithBar.add(miniBar, BorderLayout.SOUTH);

                performerCard.add(rankPanel, BorderLayout.WEST);
                performerCard.add(infoPanel, BorderLayout.CENTER);
                performerCard.add(scoreWithBar, BorderLayout.EAST);

                performersList.add(performerCard);
                rank++;
            }

            if (rank == 1) {
                JLabel noDataLabel = new JLabel("📭 No performance data available");
                noDataLabel.setFont(new Font("segoe ui emoji", Font.ITALIC, 14));
                noDataLabel.setForeground(Color.GRAY);
                noDataLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                performersList.add(noDataLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("⚠️ Error loading performers");
            errorLabel.setForeground(Color.RED);
            performersList.add(errorLabel);
        }

        JScrollPane performersScroll = new JScrollPane(performersList);
        performersScroll.setBorder(null);
        performersScroll.getVerticalScrollBar().setUnitIncrement(16);
        topPerformersPanel.add(performersScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(gradePanel);
        splitPane.setRightComponent(topPerformersPanel);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleIcon = createIconLabel("👥", 28);
        JLabel title = new JLabel("Students Management");
        title.setFont(new Font("segoe ui emoji", Font.BOLD, 20));
        
        titlePanel.add(titleIcon);
        titlePanel.add(title);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton addBtn = createIconButton("➕ Add", new Color(25, 118, 210), true);
        JButton viewBtn = createIconButton("👁️ View Marks", new Color(0, 150, 136), true);
        JButton editBtn = createIconButton("✏️ Edit", new Color(255, 152, 0), true);
        JButton deleteBtn = createIconButton("🗑️ Delete", new Color(220, 20, 60), true);
        JButton refreshBtn = createIconButton("🔄 Refresh", new Color(76, 175, 80), true);

        addBtn.addActionListener(e -> cardLayout.show(mainContent, "addstudent"));
        viewBtn.addActionListener(e -> viewStudentMarks());
        refreshBtn.addActionListener(e -> loadStudentsData());

        buttonPanel.add(addBtn);
        buttonPanel.add(viewBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);

        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(buttonPanel, BorderLayout.EAST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel searchLabel = createIconLabel("🔍", 16);
        JLabel searchTextLabel = new JLabel("Search:");
        searchTextLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));

        JTextField searchField = new JTextField(20);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        JButton searchBtn = createIconButton("Go", new Color(25, 118, 210), true);
        searchBtn.addActionListener(e -> searchStudents(searchField.getText()));
        searchField.addActionListener(e -> searchStudents(searchField.getText()));

        searchPanel.add(searchLabel);
        searchPanel.add(searchTextLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        topBar.add(searchPanel, BorderLayout.SOUTH);

        String[] columns = { "Roll No", "Name", "Department", "Email", "Password" };
        studentsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentsTable = new JTable(studentsTableModel);
        studentsTable.setRowHeight(35);
        studentsTable.setFont(new Font("segoe ui emoji", Font.PLAIN, 13));
        studentsTable.getTableHeader().setFont(new Font("segoe ui emoji", Font.BOLD, 13));
        studentsTable.getTableHeader().setBackground(new Color(248, 250, 252));
        studentsTable.setSelectionBackground(new Color(173, 216, 230));

        viewBtn.setEnabled(false);
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        studentsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean rowSelected = studentsTable.getSelectedRow() != -1;
                viewBtn.setEnabled(rowSelected);
                editBtn.setEnabled(rowSelected);
                deleteBtn.setEnabled(rowSelected);
            }
        });

        editBtn.addActionListener(e -> editSelectedStudent());
        deleteBtn.addActionListener(e -> deleteSelectedStudent());

        JScrollPane scroll = new JScrollPane(studentsTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        loadStudentsData();
        return panel;
    }

    private void searchStudents(String keyword) {
        if (keyword.trim().isEmpty()) {
            loadStudentsData();
            return;
        }

        studentsTableModel.setRowCount(0);
        try {
            String query = "SELECT roll, name, dept, email, password FROM students WHERE " +
                    "roll LIKE ? OR name LIKE ? OR dept LIKE ? OR email LIKE ? ORDER BY roll";
            PreparedStatement pst = con.prepareStatement(query);
            String searchPattern = "%" + keyword + "%";
            pst.setString(1, searchPattern);
            pst.setString(2, searchPattern);
            pst.setString(3, searchPattern);
            pst.setString(4, searchPattern);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                studentsTableModel.addRow(new Object[] {
                        rs.getString("roll"),
                        rs.getString("name"),
                        rs.getString("dept"),
                        rs.getString("email"),
                        rs.getString("password")
                });
            }

            if (studentsTableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No students found matching '" + keyword + "'");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStudentsData() {
        studentsTableModel.setRowCount(0);
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT roll, name, dept, email, password FROM students ORDER BY roll");

            while (rs.next()) {
                studentsTableModel.addRow(new Object[] {
                        rs.getString("roll"),
                        rs.getString("name"),
                        rs.getString("dept"),
                        rs.getString("email"),
                        rs.getString("password")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewStudentMarks() {
        int row = studentsTable.getSelectedRow();
        if (row == -1)
            return;

        String roll = (String) studentsTableModel.getValueAt(row, 0);
        String name = (String) studentsTableModel.getValueAt(row, 1);

        JDialog dialog = new JDialog(this, "Student Marks - " + name + " (" + roll + ")", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        String[] columns = { "Subject", "Marks", "Semester", "Grade", "Status" };
        DefaultTableModel marksModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable marksTable = new JTable(marksModel);
        marksTable.setRowHeight(30);
        marksTable.setFont(new Font("segoe ui emoji", Font.PLAIN, 13));
        marksTable.getTableHeader().setFont(new Font("segoe ui emoji", Font.BOLD, 13));

        try {
            String query = "SELECT subject, marks, semester FROM marks WHERE student_roll = ? ORDER BY semester, subject";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, roll);
            ResultSet rs = pst.executeQuery();

            int totalMarks = 0;
            int subjectCount = 0;

            while (rs.next()) {
                String subject = rs.getString("subject");
                int marks = rs.getInt("marks");
                String semester = rs.getString("semester");

                String grade = calculateGrade(marks);
                String status = marks >= 40 ? "✅ Pass" : "❌ Fail";

                marksModel.addRow(new Object[] { subject, marks, semester, grade, status });

                totalMarks += marks;
                subjectCount++;
            }

            JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 20, 0));
            summaryPanel.setBackground(new Color(248, 250, 252));
            summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            double percentage = subjectCount > 0 ? (double) totalMarks / subjectCount : 0;

            JLabel totalLabel = new JLabel("📚 Subjects: " + subjectCount);
            totalLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));

            JLabel totalMarksLabel = new JLabel("📊 Total: " + totalMarks);
            totalMarksLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));

            JLabel avgLabel = new JLabel(String.format("🎯 Average: %.1f%%", percentage));
            avgLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));
            avgLabel.setForeground(new Color(25, 118, 210));

            boolean allPass = true;
            for (int i = 0; i < marksModel.getRowCount(); i++) {
                if ((int) marksModel.getValueAt(i, 1) < 40) {
                    allPass = false;
                    break;
                }
            }

            JLabel statusLabel = new JLabel(allPass ? "✅ All Pass" : "⚠️ Some Fails");
            statusLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));
            statusLabel.setForeground(allPass ? new Color(76, 175, 80) : new Color(255, 152, 0));

            summaryPanel.add(totalLabel);
            summaryPanel.add(totalMarksLabel);
            summaryPanel.add(avgLabel);
            summaryPanel.add(statusLabel);

            JScrollPane scroll = new JScrollPane(marksTable);
            scroll.setBorder(BorderFactory.createTitledBorder("Marks Details"));

            dialog.add(summaryPanel, BorderLayout.NORTH);
            dialog.add(scroll, BorderLayout.CENTER);

            JButton closeBtn = createIconButton("Close", new Color(25, 118, 210), true);
            closeBtn.addActionListener(e -> dialog.dispose());

            JPanel btnPanel = new JPanel(new FlowLayout());
            btnPanel.setBackground(Color.WHITE);
            btnPanel.add(closeBtn);

            dialog.add(btnPanel, BorderLayout.SOUTH);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading marks: " + e.getMessage());
        }

        dialog.setVisible(true);
    }

    private String calculateGrade(int marks) {
        if (marks >= 90)
            return "A+";
        if (marks >= 80)
            return "A";
        if (marks >= 70)
            return "B+";
        if (marks >= 60)
            return "B";
        if (marks >= 50)
            return "C";
        if (marks >= 40)
            return "D";
        return "F";
    }

    private void editSelectedStudent() {
        int row = studentsTable.getSelectedRow();
        if (row == -1)
            return;

        String roll = (String) studentsTableModel.getValueAt(row, 0);
        String name = (String) studentsTableModel.getValueAt(row, 1);
        String dept = (String) studentsTableModel.getValueAt(row, 2);
        String email = (String) studentsTableModel.getValueAt(row, 3);

        JDialog dialog = new JDialog(this, "Edit Student", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        JTextField rollField = new JTextField(roll);
        rollField.setEditable(false);
        rollField.setBackground(new Color(240, 240, 240));

        JTextField nameField = new JTextField(name);
        JTextField deptField = new JTextField(dept);
        JTextField emailField = new JTextField(email);

        styleField(rollField);
        styleField(nameField);
        styleField(deptField);
        styleField(emailField);

        formPanel.add(new JLabel("🎫 Roll Number"), gbc);
        formPanel.add(rollField, gbc);
        formPanel.add(new JLabel("👤 Full Name"), gbc);
        formPanel.add(nameField, gbc);
        formPanel.add(new JLabel("🏛️ Department"), gbc);
        formPanel.add(deptField, gbc);
        formPanel.add(new JLabel("📧 Email"), gbc);
        formPanel.add(emailField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton saveBtn = createIconButton("💾 Update", new Color(25, 118, 210), true);
        JButton cancelBtn = createIconButton("❌ Cancel", new Color(220, 20, 60), true);

        saveBtn.addActionListener(e -> {
            try {
                String query = "UPDATE students SET name=?, dept=?, email=? WHERE roll=?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, nameField.getText().trim());
                pst.setString(2, deptField.getText().trim());
                pst.setString(3, emailField.getText().trim());
                pst.setString(4, roll);

                int result = pst.executeUpdate();
                if (result > 0) {
                    loadStudentsData();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "✅ Student updated successfully!");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedStudent() {
        int row = studentsTable.getSelectedRow();
        if (row == -1)
            return;

        String roll = (String) studentsTableModel.getValueAt(row, 0);
        String name = (String) studentsTableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete student " + name + " (" + roll + ")?\nThis will also delete all their marks!",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String deleteMarks = "DELETE FROM marks WHERE student_roll=?";
                PreparedStatement pstMarks = con.prepareStatement(deleteMarks);
                pstMarks.setString(1, roll);
                pstMarks.executeUpdate();

                String query = "DELETE FROM students WHERE roll=?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, roll);
                int result = pst.executeUpdate();

                if (result > 0) {
                    loadStudentsData();
                    loadStudentsToCombo();
                    refreshAddMarksStudentCombo();
                    JOptionPane.showMessageDialog(this, "✅ Student deleted successfully!");
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private JPanel createMarksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleIcon = createIconLabel("📝", 28);
        JLabel title = new JLabel("Marks Management");
        title.setFont(new Font("segoe ui emoji", Font.BOLD, 20));
        
        titlePanel.add(titleIcon);
        titlePanel.add(title);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.add(new JLabel("🔍 Filter:"));

        studentFilterCombo = new JComboBox<>();
        studentFilterCombo.addItem("All Students");
        loadStudentsToCombo();

        studentFilterCombo.addActionListener(e -> loadMarksData());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton addBtn = createIconButton("➕ Add", new Color(25, 118, 210), true);
        JButton editBtn = createIconButton("✏️ Edit", new Color(255, 152, 0), true);
        JButton deleteBtn = createIconButton("🗑️ Delete", new Color(220, 20, 60), true);
        JButton refreshBtn = createIconButton("🔄 Refresh", new Color(76, 175, 80), true);

        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        addBtn.addActionListener(e -> {
            refreshAddMarksStudentCombo();
            cardLayout.show(mainContent, "addmarks");
        });

        refreshBtn.addActionListener(e -> {
            loadMarksData();
            loadStudentsToCombo();
            refreshAddMarksStudentCombo();
        });

        filterPanel.add(studentFilterCombo);
        filterPanel.add(addBtn);
        filterPanel.add(editBtn);
        filterPanel.add(deleteBtn);
        filterPanel.add(refreshBtn);

        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(filterPanel, BorderLayout.EAST);

        String[] columns = { "ID", "Roll No", "Name", "Subject", "Marks", "Semester" };
        marksTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        marksTable = new JTable(marksTableModel);
        marksTable.setRowHeight(35);
        marksTable.setFont(new Font("segoe ui emoji", Font.PLAIN, 13));
        marksTable.getTableHeader().setFont(new Font("segoe ui emoji", Font.BOLD, 13));
        marksTable.getTableHeader().setBackground(new Color(248, 250, 252));
        marksTable.setSelectionBackground(new Color(173, 216, 230));

        marksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean rowSelected = marksTable.getSelectedRow() != -1;
                editBtn.setEnabled(rowSelected);
                deleteBtn.setEnabled(rowSelected);
            }
        });

        editBtn.addActionListener(e -> editSelectedMark());
        deleteBtn.addActionListener(e -> deleteSelectedMark());

        JScrollPane scroll = new JScrollPane(marksTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        loadMarksData();
        return panel;
    }

    private void loadStudentsToCombo() {
        studentFilterCombo.removeAllItems();
        studentFilterCombo.addItem("All Students");

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT roll, name FROM students ORDER BY roll");
            while (rs.next()) {
                studentFilterCombo.addItem(rs.getString("roll") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMarksData() {
        marksTableModel.setRowCount(0);
        try {
            String filter = (String) studentFilterCombo.getSelectedItem();
            String query;
            PreparedStatement pst;

            if (filter == null || filter.equals("All Students")) {
                query = "SELECT m.id, s.roll, s.name, m.subject, m.marks, m.semester " +
                        "FROM marks m JOIN students s ON m.student_roll = s.roll ORDER BY s.roll, m.subject";
                pst = con.prepareStatement(query);
            } else {
                String roll = filter.split(" - ")[0];
                query = "SELECT m.id, s.roll, s.name, m.subject, m.marks, m.semester " +
                        "FROM marks m JOIN students s ON m.student_roll = s.roll WHERE s.roll = ? ORDER BY m.subject";
                pst = con.prepareStatement(query);
                pst.setString(1, roll);
            }

            ResultSet rs = pst.executeQuery();
            boolean hasData = false;
            
            while (rs.next()) {
                hasData = true;
                marksTableModel.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getString("roll"),
                        rs.getString("name"),
                        rs.getString("subject"),
                        rs.getInt("marks"),
                        rs.getString("semester")
                });
            }
            
            if (!hasData && filter != null && !filter.equals("All Students")) {
                JOptionPane.showMessageDialog(this, "No marks found for selected student", 
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            
            rs.close();
            pst.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading marks: " + e.getMessage());
        }
    }

    private void editSelectedMark() {
        int row = marksTable.getSelectedRow();
        if (row == -1)
            return;

        int id = (int) marksTableModel.getValueAt(row, 0);
        String roll = (String) marksTableModel.getValueAt(row, 1);
        String subject = (String) marksTableModel.getValueAt(row, 3);
        int marks = (int) marksTableModel.getValueAt(row, 4);
        String semester = (String) marksTableModel.getValueAt(row, 5);

        JDialog dialog = new JDialog(this, "Edit Marks", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        JTextField rollField = new JTextField(roll);
        rollField.setEditable(false);
        rollField.setBackground(new Color(240, 240, 240));

        String[] subjects = { "Mathematics", "Physics", "Chemistry", "Biology", "Computer Science",
                "English", "Economics", "Commerce", "History", "Geography" };
        JComboBox<String> subjectCombo = new JComboBox<>(subjects);
        subjectCombo.setSelectedItem(subject);
        subjectCombo.setBackground(Color.WHITE);

        JTextField marksField = new JTextField(String.valueOf(marks));

        String[] semesters = { "Sem 1", "Sem 2", "Sem 3", "Sem 4", "Sem 5", "Sem 6" };
        JComboBox<String> semesterCombo = new JComboBox<>(semesters);
        semesterCombo.setSelectedItem(semester);
        semesterCombo.setBackground(Color.WHITE);

        styleField(rollField);
        styleField(subjectCombo);
        styleField(marksField);
        styleField(semesterCombo);

        formPanel.add(new JLabel("🎫 Roll Number"), gbc);
        formPanel.add(rollField, gbc);
        formPanel.add(new JLabel("📚 Subject"), gbc);
        formPanel.add(subjectCombo, gbc);
        formPanel.add(new JLabel("📊 Marks"), gbc);
        formPanel.add(marksField, gbc);
        formPanel.add(new JLabel("📅 Semester"), gbc);
        formPanel.add(semesterCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton saveBtn = createIconButton("💾 Update", new Color(25, 118, 210), true);
        JButton cancelBtn = createIconButton("❌ Cancel", new Color(220, 20, 60), true);

        saveBtn.addActionListener(e -> {
            try {
                String newSubject = (String) subjectCombo.getSelectedItem();
                int newMarks = Integer.parseInt(marksField.getText().trim());
                String newSemester = (String) semesterCombo.getSelectedItem();

                String query = "UPDATE marks SET subject=?, marks=?, semester=? WHERE id=?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, newSubject);
                pst.setInt(2, newMarks);
                pst.setString(3, newSemester);
                pst.setInt(4, id);

                int result = pst.executeUpdate();
                if (result > 0) {
                    loadMarksData();
                    refreshDashboard();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "✅ Marks updated successfully!");
                }

            } catch (SQLException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedMark() {
        int row = marksTable.getSelectedRow();
        if (row == -1)
            return;

        int id = (int) marksTableModel.getValueAt(row, 0);
        String roll = (String) marksTableModel.getValueAt(row, 1);
        String subject = (String) marksTableModel.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete marks for " + roll + " - " + subject + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM marks WHERE id=?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setInt(1, id);
                int result = pst.executeUpdate();

                if (result > 0) {
                    loadMarksData();
                    refreshDashboard();
                    JOptionPane.showMessageDialog(this, "✅ Marks deleted successfully!");
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private JPanel createAddStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleIcon = createIconLabel("➕", 32);
        JLabel title = new JLabel("Add New Student");
        title.setFont(new Font("segoe ui emoji", Font.BOLD, 24));
        title.setForeground(new Color(25, 118, 210));
        
        titlePanel.add(titleIcon);
        titlePanel.add(title);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 50, 8, 50);

        JTextField rollField = new JTextField();
        JTextField nameField = new JTextField();

        String[] depts = { "Computer Science", "Science", "Commerce", "Arts", "Engineering" };
        JComboBox<String> deptCombo = new JComboBox<>(depts);
        deptCombo.setBackground(Color.WHITE);

        JTextField emailField = new JTextField();

        styleField(rollField);
        styleField(nameField);
        styleField(deptCombo);
        styleField(emailField);

        formPanel.add(new JLabel("🎫 Roll Number *"), gbc);
        formPanel.add(rollField, gbc);
        formPanel.add(new JLabel("👤 Full Name *"), gbc);
        formPanel.add(nameField, gbc);
        formPanel.add(new JLabel("🏛️ Department"), gbc);
        formPanel.add(deptCombo, gbc);
        formPanel.add(new JLabel("📧 Email"), gbc);
        formPanel.add(emailField, gbc);

        JButton saveBtn = createIconButton("💾 Save Student", new Color(25, 118, 210), true);
        saveBtn.setPreferredSize(new Dimension(200, 45));

        saveBtn.addActionListener(e -> {
            try {
                String roll = rollField.getText().trim();
                String name = nameField.getText().trim();
                String dept = (String) deptCombo.getSelectedItem();
                String email = emailField.getText().trim();

                if (roll.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Roll Number and Name are required!");
                    return;
                }

                String query = "INSERT INTO students (roll, name, dept, email, password) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, roll);
                pst.setString(2, name);
                pst.setString(3, dept);
                pst.setString(4, email.isEmpty() ? name.toLowerCase() + "@school.edu" : email);
                pst.setString(5, name);

                int result = pst.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "✅ Student added successfully!");

                    rollField.setText("");
                    nameField.setText("");
                    emailField.setText("");

                    loadStudentsData();
                    loadStudentsToCombo();
                    refreshAddMarksStudentCombo();
                    refreshDashboard();

                    cardLayout.show(mainContent, "students");
                }

            } catch (SQLException ex) {
                if (ex.getMessage().contains("Duplicate")) {
                    JOptionPane.showMessageDialog(this, "❌ Roll number already exists!");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveBtn);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAddMarksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleIcon = createIconLabel("➕", 32);
        JLabel title = new JLabel("Add Marks");
        title.setFont(new Font("segoe ui emoji", Font.BOLD, 24));
        title.setForeground(new Color(25, 118, 210));
        
        titlePanel.add(titleIcon);
        titlePanel.add(title);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 50, 8, 50);

        addMarksStudentCombo = new JComboBox<>();
        addMarksStudentCombo.addItem("Select Student");
        refreshAddMarksStudentCombo();
        addMarksStudentCombo.setBackground(Color.WHITE);

        String[] subjects = { "Mathematics", "Physics", "Chemistry", "Biology", "Computer Science",
                "English", "Economics", "Commerce", "History", "Geography" };
        subjectCombo = new JComboBox<>(subjects);
        subjectCombo.setBackground(Color.WHITE);

        marksField = new JTextField();
        String[] semesters = { "Sem 1", "Sem 2", "Sem 3", "Sem 4", "Sem 5", "Sem 6" };
        semesterCombo = new JComboBox<>(semesters);
        semesterCombo.setBackground(Color.WHITE);

        styleField(addMarksStudentCombo);
        styleField(subjectCombo);
        styleField(marksField);
        styleField(semesterCombo);

        formPanel.add(new JLabel("👤 Select Student *"), gbc);
        formPanel.add(addMarksStudentCombo, gbc);
        formPanel.add(new JLabel("📚 Subject *"), gbc);
        formPanel.add(subjectCombo, gbc);
        formPanel.add(new JLabel("📊 Marks *"), gbc);
        formPanel.add(marksField, gbc);
        formPanel.add(new JLabel("📅 Semester *"), gbc);
        formPanel.add(semesterCombo, gbc);

        JButton saveBtn = createIconButton("💾 Save Marks", new Color(25, 118, 210), true);
        saveBtn.setPreferredSize(new Dimension(200, 45));

        saveBtn.addActionListener(e -> {
            try {
                String selected = (String) addMarksStudentCombo.getSelectedItem();
                if (selected == null || selected.equals("Select Student")) {
                    JOptionPane.showMessageDialog(this, "Please select a student!");
                    return;
                }

                String roll = selected.split(" - ")[0];
                String subject = (String) subjectCombo.getSelectedItem();
                String marksText = marksField.getText().trim();

                if (marksText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter marks!");
                    return;
                }

                int marks = Integer.parseInt(marksText);
                String semester = (String) semesterCombo.getSelectedItem();

                String checkQuery = "SELECT COUNT(*) FROM marks WHERE student_roll=? AND subject=?";
                PreparedStatement checkStmt = con.prepareStatement(checkQuery);
                checkStmt.setString(1, roll);
                checkStmt.setString(2, subject);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "❌ Marks already exist for this subject!");
                    checkStmt.close();
                    return;
                }
                checkStmt.close();

                String query = "INSERT INTO marks (student_roll, subject, marks, semester) VALUES (?, ?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, roll);
                pst.setString(2, subject);
                pst.setInt(3, marks);
                pst.setString(4, semester);

                int result = pst.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "✅ Marks added successfully!");

                    marksField.setText("");
                    addMarksStudentCombo.setSelectedIndex(0);

                    loadMarksData();
                    loadStudentsToCombo();
                    refreshDashboard();

                    cardLayout.show(mainContent, "marks");
                }
                pst.close();

            } catch (SQLException | NumberFormatException ex) {
                if (ex.getMessage().contains("Duplicate")) {
                    JOptionPane.showMessageDialog(this, "❌ Marks already exist for this subject!");
                } else if (ex instanceof NumberFormatException) {
                    JOptionPane.showMessageDialog(this, "❌ Please enter valid numbers for marks!");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveBtn);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshAddMarksStudentCombo() {
        if (addMarksStudentCombo != null) {
            addMarksStudentCombo.removeAllItems();
            addMarksStudentCombo.addItem("Select Student");
            
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT roll, name FROM students ORDER BY roll");
                while (rs.next()) {
                    addMarksStudentCombo.addItem(rs.getString("roll") + " - " + rs.getString("name"));
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private JPanel createRevaluationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleIcon = createIconLabel("🔄", 32);
        JLabel title = new JLabel("Revaluation Requests");
        title.setFont(new Font("segoe ui emoji", Font.BOLD, 24));
        title.setForeground(new Color(25, 118, 210));

        pendingRevaluationCount = new JLabel("0");
        pendingRevaluationCount.setFont(new Font("segoe ui emoji", Font.BOLD, 12));
        pendingRevaluationCount.setForeground(Color.WHITE);
        pendingRevaluationCount.setBackground(new Color(220, 20, 60));
        pendingRevaluationCount.setOpaque(true);
        pendingRevaluationCount.setHorizontalAlignment(SwingConstants.CENTER);
        pendingRevaluationCount.setPreferredSize(new Dimension(25, 25));
        pendingRevaluationCount.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        titlePanel.add(titleIcon);
        titlePanel.add(title);
        titlePanel.add(Box.createRigidArea(new Dimension(10, 0)));
        titlePanel.add(pendingRevaluationCount);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setBackground(Color.WHITE);

        String[] filterOptions = { "All Requests", "Pending", "Approved", "Rejected", "Completed" };
        JComboBox<String> filterCombo = new JComboBox<>(filterOptions);
        filterCombo.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
        filterCombo.setBackground(Color.WHITE);
        filterCombo.addActionListener(e -> filterRevaluationRequests((String) filterCombo.getSelectedItem()));

        JButton refreshBtn = createIconButton("🔄 Refresh", new Color(76, 175, 80), true);
        refreshBtn.addActionListener(e -> loadRevaluationRequests());

        filterPanel.add(new JLabel("🔍 Filter:"));
        filterPanel.add(filterCombo);
        filterPanel.add(refreshBtn);

        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(filterPanel, BorderLayout.EAST);

        revaluationStatsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        revaluationStatsPanel.setBackground(Color.WHITE);
        revaluationStatsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel northCombined = new JPanel(new BorderLayout());
        northCombined.setBackground(Color.WHITE);
        northCombined.add(topBar, BorderLayout.NORTH);
        northCombined.add(revaluationStatsPanel, BorderLayout.CENTER);
        panel.add(northCombined, BorderLayout.NORTH);

        String[] columns = { "ID", "Date", "Roll No", "Student Name", "Subject",
                "Subject Code", "Amount", "Payment Method", "Status", "Action" };
        revaluationTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8 || column == 9;
            }
        };

        revaluationTable = new JTable(revaluationTableModel);
        revaluationTable.setRowHeight(45);
        revaluationTable.setFont(new Font("segoe ui emoji", Font.PLAIN, 13));
        revaluationTable.getTableHeader().setFont(new Font("segoe ui emoji", Font.BOLD, 13));
        revaluationTable.getTableHeader().setBackground(new Color(248, 250, 252));

        revaluationTable.getColumnModel().getColumn(8).setCellRenderer(new StatusRenderer());
        revaluationTable.getColumnModel().getColumn(8).setCellEditor(new StatusEditor());
        revaluationTable.getColumnModel().getColumn(9).setCellRenderer(new ActionRenderer());
        revaluationTable.getColumnModel().getColumn(9).setCellEditor(new ActionEditor());

        revaluationTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        revaluationTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        revaluationTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        revaluationTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        revaluationTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        revaluationTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        revaluationTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        revaluationTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        revaluationTable.getColumnModel().getColumn(8).setPreferredWidth(100);
        revaluationTable.getColumnModel().getColumn(9).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(revaluationTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        panel.add(scroll, BorderLayout.CENTER);

        loadRevaluationRequests();

        return panel;
    }

    private void loadRevaluationRequests() {
        revaluationTableModel.setRowCount(0);
        int pendingCount = 0;
        int approvedCount = 0;
        int rejectedCount = 0;
        int completedCount = 0;
        double totalRevenue = 0;

        try {
            String query = "SELECT * FROM revaluation_requests ORDER BY " +
                    "CASE status " +
                    "WHEN 'Pending' THEN 1 " +
                    "WHEN 'Approved' THEN 2 " +
                    "WHEN 'Completed' THEN 3 " +
                    "ELSE 4 END, request_date DESC";

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp date = rs.getTimestamp("request_date");
                String roll = rs.getString("student_roll");
                String name = rs.getString("student_name");
                String subject = rs.getString("subject");
                String code = rs.getString("subject_code");
                double amount = rs.getDouble("amount");
                String paymentMethod = rs.getString("payment_method");
                String status = rs.getString("status");

                switch (status) {
                    case "Pending":
                        pendingCount++;
                        break;
                    case "Approved":
                        approvedCount++;
                        break;
                    case "Rejected":
                        rejectedCount++;
                        break;
                    case "Completed":
                        completedCount++;
                        totalRevenue += amount;
                        break;
                }

                String dateStr = new java.text.SimpleDateFormat("dd-MM-yyyy").format(date);

                revaluationTableModel.addRow(new Object[] {
                        id, dateStr, roll, name, subject, code,
                        "₹" + String.format("%.2f", amount), paymentMethod, status, "View"
                });
            }

            updateRevaluationStats(pendingCount, approvedCount, rejectedCount, completedCount, totalRevenue);

            pendingRevaluationCount.setText(String.valueOf(pendingCount));
            if (pendingCount > 0) {
                pendingRevaluationCount.setBackground(new Color(220, 20, 60));
                if (pendingCount > 0) {
                    showRevaluationNotification(pendingCount);
                }
            } else {
                pendingRevaluationCount.setBackground(new Color(76, 175, 80));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading revaluation requests: " + e.getMessage());
        }
    }

    private void updateRevaluationStats(int pending, int approved, int rejected, int completed, double revenue) {
        if (revaluationStatsPanel != null) {
            revaluationStatsPanel.removeAll();

            revaluationStatsPanel.add(createIconStatCard("⏳", "Pending", String.valueOf(pending), "awaiting review", new Color(255, 152, 0)));
            revaluationStatsPanel.add(createIconStatCard("✅", "Approved", String.valueOf(approved), "processed", new Color(76, 175, 80)));
            revaluationStatsPanel.add(createIconStatCard("❌", "Rejected", String.valueOf(rejected), "total", new Color(220, 20, 60)));
            revaluationStatsPanel.add(createIconStatCard("💰", "Revenue", "₹" + String.format("%.0f", revenue), "from completed", new Color(25, 118, 210)));

            revaluationStatsPanel.revalidate();
            revaluationStatsPanel.repaint();
        }
    }

    private void filterRevaluationRequests(String filter) {
        revaluationTableModel.setRowCount(0);

        try {
            String query;
            PreparedStatement pst;

            if (filter.equals("All Requests")) {
                query = "SELECT * FROM revaluation_requests ORDER BY request_date DESC";
                pst = con.prepareStatement(query);
            } else {
                query = "SELECT * FROM revaluation_requests WHERE status = ? ORDER BY request_date DESC";
                pst = con.prepareStatement(query);
                pst.setString(1, filter);
            }

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp date = rs.getTimestamp("request_date");
                String roll = rs.getString("student_roll");
                String name = rs.getString("student_name");
                String subject = rs.getString("subject");
                String code = rs.getString("subject_code");
                double amount = rs.getDouble("amount");
                String paymentMethod = rs.getString("payment_method");
                String status = rs.getString("status");

                String dateStr = new java.text.SimpleDateFormat("dd-MM-yyyy").format(date);

                revaluationTableModel.addRow(new Object[] {
                        id, dateStr, roll, name, subject, code,
                        "₹" + String.format("%.2f", amount), paymentMethod, status, "View"
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateRevaluationStatus(int requestId, String newStatus) {
        try {
            String query = "UPDATE revaluation_requests SET status = ? WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, newStatus);
            pst.setInt(2, requestId);

            int result = pst.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this,
                        "✅ Revaluation request " + newStatus.toLowerCase() + " successfully!",
                        "Status Updated", JOptionPane.INFORMATION_MESSAGE);

                loadRevaluationRequests();
                refreshDashboard();
                notifyStudent(requestId, newStatus);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating status: " + e.getMessage());
        }
    }

    private void viewRevaluationDetails(int requestId) {
        try {
            String query = "SELECT * FROM revaluation_requests WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, requestId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String roll = rs.getString("student_roll");
                String name = rs.getString("student_name");
                String subject = rs.getString("subject");
                String code = rs.getString("subject_code");
                double amount = rs.getDouble("amount");
                String paymentMethod = rs.getString("payment_method");
                String status = rs.getString("status");
                String reason = rs.getString("reason");
                String transactionId = rs.getString("transaction_id");
                Timestamp date = rs.getTimestamp("request_date");

                JDialog dialog = new JDialog(this, "Revaluation Request Details", true);
                dialog.setSize(500, 600);
                dialog.setLocationRelativeTo(this);
                dialog.setLayout(new BorderLayout());

                JPanel detailsPanel = new JPanel();
                detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
                detailsPanel.setBackground(Color.WHITE);
                detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                JLabel header = new JLabel("📄 Revaluation Request #" + requestId);
                header.setFont(new Font("segoe ui emoji", Font.BOLD, 18));
                header.setForeground(new Color(25, 118, 210));
                header.setAlignmentX(Component.LEFT_ALIGNMENT);
                detailsPanel.add(header);
                detailsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

                JPanel gridPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                gridPanel.setBackground(Color.WHITE);
                gridPanel.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        "📋 Request Information",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("segoe ui emoji emoji", Font.BOLD, 14)));

                addDetailRow(gridPanel, "📅 Request Date:", new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date));
                addDetailRow(gridPanel, "🎫 Student Roll:", roll);
                addDetailRow(gridPanel, "👤 Student Name:", name);
                addDetailRow(gridPanel, "📚 Subject:", subject);
                addDetailRow(gridPanel, "🏷️ Subject Code:", code);
                addDetailRow(gridPanel, "💰 Amount:", "₹" + String.format("%.2f", amount));
                addDetailRow(gridPanel, "💳 Payment Method:", paymentMethod);
                addDetailRow(gridPanel, "🧾 Transaction ID:", transactionId);
                addDetailRow(gridPanel, "📊 Current Status:", status);

                detailsPanel.add(gridPanel);
                detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

                if (reason != null && !reason.isEmpty()) {
                    JPanel reasonPanel = new JPanel(new BorderLayout());
                    reasonPanel.setBackground(Color.WHITE);
                    reasonPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(new Color(200, 200, 200)),
                            "💬 Student's Reason",
                            TitledBorder.LEFT, TitledBorder.TOP,
                            new Font("segoe ui emoji", Font.BOLD, 14)));

                    JTextArea reasonArea = new JTextArea(reason);
                    reasonArea.setEditable(false);
                    reasonArea.setLineWrap(true);
                    reasonArea.setWrapStyleWord(true);
                    reasonArea.setFont(new Font("segoe ui emoji", Font.PLAIN, 13));
                    reasonArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    reasonPanel.add(new JScrollPane(reasonArea), BorderLayout.CENTER);
                    detailsPanel.add(reasonPanel);
                }

                JPanel actionPanel = new JPanel(new FlowLayout());
                actionPanel.setBackground(Color.WHITE);

                if (status.equals("Pending")) {
                    JButton approveBtn = createIconButton("✅ Approve", new Color(76, 175, 80), true);
                    approveBtn.addActionListener(e -> {
                        updateRevaluationStatus(requestId, "Approved");
                        dialog.dispose();
                    });

                    JButton rejectBtn = createIconButton("❌ Reject", new Color(220, 20, 60), true);
                    rejectBtn.addActionListener(e -> {
                        updateRevaluationStatus(requestId, "Rejected");
                        dialog.dispose();
                    });

                    actionPanel.add(approveBtn);
                    actionPanel.add(rejectBtn);
                }

                JButton closeBtn = createIconButton("Close", new Color(100, 100, 100), true);
                closeBtn.addActionListener(e -> dialog.dispose());
                actionPanel.add(closeBtn);

                detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                detailsPanel.add(actionPanel);

                dialog.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);
                dialog.setVisible(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("segoe ui emoji", Font.BOLD, 13));
        labelComp.setForeground(new Color(80, 80, 80));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("segoe ui emoji", Font.PLAIN, 13));

        panel.add(labelComp);
        panel.add(valueComp);
    }

    private void notifyStudent(int requestId, String newStatus) {
        try {
            String query = "INSERT INTO notifications (student_roll, message, type) " +
                    "SELECT student_roll, CONCAT('Your revaluation request for ', subject, ' has been ', ?), 'revaluation' " +
                    "FROM revaluation_requests WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, newStatus.toLowerCase());
            pst.setInt(2, requestId);
            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showRevaluationNotification(int count) {
        if (count > 0) {
            JDialog notifDialog = new JDialog(this, false);
            notifDialog.setUndecorated(true);
            notifDialog.setSize(300, 100);
            notifDialog.setLocationRelativeTo(this);

            JPanel notifPanel = new JPanel();
            notifPanel.setBackground(new Color(25, 118, 210));
            notifPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            notifPanel.setLayout(new BorderLayout());

            JLabel notifLabel = new JLabel("🔔 " + count + " new revaluation request" + (count > 1 ? "s" : ""));
            notifLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));
            notifLabel.setForeground(Color.WHITE);
            notifLabel.setHorizontalAlignment(SwingConstants.CENTER);

            notifPanel.add(notifLabel, BorderLayout.CENTER);
            notifDialog.add(notifPanel);

            notifDialog.setVisible(true);

            Timer timer = new Timer(3000, e -> notifDialog.dispose());
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void startNotificationChecker() {
        notificationTimer = new Timer(30000, e -> {
            checkForNewRequests();
        });
        notificationTimer.start();
    }

    private void checkForNewRequests() {
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT COUNT(*) as count FROM revaluation_requests WHERE status = 'Pending'");
            if (rs.next()) {
                int count = rs.getInt("count");
                pendingRevaluationCount.setText(String.valueOf(count));

                if (count > 0) {
                    flashNotificationBadge();
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void flashNotificationBadge() {
        Timer flashTimer = new Timer(500, new ActionListener() {
            boolean isRed = true;
            int count = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (count < 6) {
                    pendingRevaluationCount.setBackground(isRed ? new Color(255, 100, 100) : new Color(220, 20, 60));
                    isRed = !isRed;
                    count++;
                } else {
                    pendingRevaluationCount.setBackground(new Color(220, 20, 60));
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        flashTimer.start();
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value != null) {
                String status = value.toString();
                setHorizontalAlignment(SwingConstants.CENTER);

                switch (status) {
                    case "Pending":
                        setBackground(new Color(255, 243, 224));
                        setForeground(new Color(255, 152, 0));
                        setText("⏳ " + status);
                        break;
                    case "Approved":
                        setBackground(new Color(232, 245, 233));
                        setForeground(new Color(76, 175, 80));
                        setText("✅ " + status);
                        break;
                    case "Rejected":
                        setBackground(new Color(255, 235, 238));
                        setForeground(new Color(220, 20, 60));
                        setText("❌ " + status);
                        break;
                    case "Completed":
                        setBackground(new Color(227, 242, 253));
                        setForeground(new Color(25, 118, 210));
                        setText("✓ " + status);
                        break;
                }
                setFont(new Font("segoe ui emoji", Font.BOLD, 12));
            }

            return c;
        }
    }

    class StatusEditor extends AbstractCellEditor implements TableCellEditor {
        private JComboBox<String> comboBox;
        private String currentStatus;
        private int currentRow;

        public StatusEditor() {
            comboBox = new JComboBox<>(new String[] { "Pending", "Approved", "Rejected", "Completed" });
            comboBox.addActionListener(e -> {
                currentStatus = (String) comboBox.getSelectedItem();
                fireEditingStopped();

                int requestId = (int) revaluationTableModel.getValueAt(currentRow, 0);
                updateRevaluationStatus(requestId, currentStatus);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            currentStatus = value.toString();
            comboBox.setSelectedItem(value);
            return comboBox;
        }

        @Override
        public Object getCellEditorValue() {
            return currentStatus;
        }
    }

    class ActionRenderer extends JButton implements TableCellRenderer {
        public ActionRenderer() {
            setOpaque(true);
            setText("👁️ View");
            setBackground(new Color(25, 118, 210));
            setForeground(Color.BLACK);
            setFont(new Font("segoe ui emoji", Font.BOLD, 12));
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private JButton button;
        private int requestId;

        public ActionEditor() {
            button = new JButton("👁️ View");
            button.setBackground(new Color(25, 118, 210));
            button.setForeground(Color.BLACK);
            button.setFont(new Font("segoe ui emoji", Font.BOLD, 12));
            button.setFocusPainted(false);

            button.addActionListener(e -> {
                fireEditingStopped();
                viewRevaluationDetails(requestId);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.requestId = (int) revaluationTableModel.getValueAt(row, 0);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "👁️ View";
        }
    }

    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("segoe ui emoji", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 35));
        return btn;
    }

    private void styleField(JComponent field) {
        field.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));
        if (field instanceof JTextField) {
            ((JTextField) field).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        } else if (field instanceof JComboBox) {
            ((JComboBox) field).setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            ((JComboBox) field).setBackground(Color.WHITE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new AdminDashboard());
    }
}