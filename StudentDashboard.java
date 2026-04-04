import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.TitledBorder;
import java.util.*;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

public class StudentDashboard extends JFrame {
    Connection con;
    String studentRoll;
    String studentName;
    String studentDept;

    // Dashboard components
    JLabel welcomeLabel;
    JLabel avgScoreLabel;
    JLabel highestScoreLabel;
    JLabel subjectsLabel;
    JLabel passedLabel;
    JTable marksTable;
    DefaultTableModel tableModel;

    // Color scheme
    Color primaryColor = new Color(25, 118, 210);
    Color successColor = new Color(76, 175, 80);
    Color warningColor = new Color(255, 152, 0);
    Color dangerColor = new Color(220, 20, 60);
    Color bgColor = new Color(248, 250, 252);

    public StudentDashboard(Connection existingCon, String roll, String name, String dept) {
        this.con = existingCon;
        this.studentRoll = roll;
        this.studentName = name;
        this.studentDept = dept;
        initComponents();
        loadStudentData();
    }

    private void initComponents() {
        setTitle("Student Dashboard - " + studentName);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Navigation
        add(createTopNav(), BorderLayout.NORTH);

        // Main Content with Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);

        // Left Panel - Summary and Bar Chart
        JPanel leftPanel = createLeftPanel();

        // Right Panel - Marks Table
        JPanel rightPanel = createRightPanel();

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createTopNav() {
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setBackground(Color.WHITE);
        topNav.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Left side - Welcome message
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setBackground(Color.WHITE);

        JLabel iconLabel = new JLabel("👋");
        iconLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 28));

        welcomeLabel = new JLabel();
        welcomeLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 22));
        welcomeLabel.setForeground(new Color(33, 33, 33));

        leftPanel.add(iconLabel);
        leftPanel.add(welcomeLabel);

        // Right side - Student info and logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightPanel.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(studentName);
        nameLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 14));

        JLabel rollLabel = new JLabel(studentRoll + " · " + studentDept);
        rollLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
        rollLabel.setForeground(Color.GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(rollLabel);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
        logoutBtn.setForeground(primaryColor);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                ModernLoginUI loginUI = new ModernLoginUI();
                loginUI.setVisible(true);
            });
        });

        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 24));

        rightPanel.add(infoPanel);
        rightPanel.add(logoutBtn);
        rightPanel.add(avatarLabel);

        topNav.add(leftPanel, BorderLayout.WEST);
        topNav.add(rightPanel, BorderLayout.EAST);

        return topNav;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Subtitle
        JLabel subtitle = new JLabel("Here's your academic performance summary");
        subtitle.setFont(new Font("segoe ui emoji", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(subtitle);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Stats Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 15, 15));
        statsGrid.setBackground(Color.WHITE);
        statsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Average Score Card
        JPanel avgCard = createStatCard("📊 Average Score", "0%", primaryColor);
        avgScoreLabel = (JLabel) ((JPanel) avgCard.getComponent(1)).getComponent(0);
        statsGrid.add(avgCard);

        // Highest Score Card
        JPanel highCard = createStatCard("🏆 Highest Score", "0", warningColor);
        highestScoreLabel = (JLabel) ((JPanel) highCard.getComponent(1)).getComponent(0);
        statsGrid.add(highCard);

        // Subjects Card
        JPanel subjCard = createStatCard("📚 Subjects", "0", successColor);
        subjectsLabel = (JLabel) ((JPanel) subjCard.getComponent(1)).getComponent(0);
        statsGrid.add(subjCard);

        // Passed Card
        JPanel passCard = createStatCard("✅ Passed", "0/0", dangerColor);
        passedLabel = (JLabel) ((JPanel) passCard.getComponent(1)).getComponent(0);
        statsGrid.add(passCard);

        panel.add(statsGrid);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Marks by Subject Section (Progress Bars)
        JLabel marksTitle = new JLabel("📊 Subject Performance");
        marksTitle.setFont(new Font("segoe ui emoji", Font.BOLD, 16));
        marksTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(marksTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Subject Marks Bars Container
        JPanel subjectBarsPanel = new JPanel();
        subjectBarsPanel.setLayout(new BoxLayout(subjectBarsPanel, BoxLayout.Y_AXIS));
        subjectBarsPanel.setBackground(Color.WHITE);
        subjectBarsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subjectBarsPanel.setName("subjectBars");
        panel.add(subjectBarsPanel);

        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Performance Comparison Bar Chart
        JLabel chartTitle = new JLabel("📈 Performance Comparison");
        chartTitle.setFont(new Font("segoe ui emoji", Font.BOLD, 16));
        chartTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(chartTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Bar Chart Panel
        JPanel chartPanel = new JPanel() {
            private Map<String, Integer> subjectMarks = new HashMap<>();
            private String[] subjects;
            private int[] marks;
            private int maxMarks = 100;

            public void setData(Map<String, Integer> data) {
                this.subjectMarks = data;
                this.subjects = data.keySet().toArray(new String[0]);
                this.marks = new int[subjects.length];
                for (int i = 0; i < subjects.length; i++) {
                    marks[i] = data.get(subjects[i]);
                }
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (subjects == null || subjects.length == 0)
                    return;

                int w = getWidth();
                int h = getHeight();
                int padding = 40;
                int chartWidth = w - 2 * padding;
                int chartHeight = h - 2 * padding - 30;
                int barWidth = chartWidth / (subjects.length * 2);

                // Draw axes
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawLine(padding, padding, padding, h - padding - 30);
                g2.drawLine(padding, h - padding - 30, w - padding, h - padding - 30);

                // Draw horizontal grid lines
                for (int i = 0; i <= 5; i++) {
                    int y = h - padding - 30 - (i * chartHeight / 5);
                    g2.setColor(new Color(230, 230, 230));
                    g2.drawLine(padding, y, w - padding, y);

                    // Mark percentages
                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font("segoe ui emoji", Font.PLAIN, 10));
                    g2.drawString((i * 20) + "%", padding - 30, y + 3);
                }

                // Draw bars
                Color[] colors = {
                        new Color(25, 118, 210), // Blue
                        new Color(76, 175, 80), // Green
                        new Color(255, 152, 0), // Orange
                        new Color(156, 39, 176), // Purple
                        new Color(233, 30, 99), // Pink
                        new Color(0, 150, 136) // Teal
                };

                for (int i = 0; i < subjects.length; i++) {
                    int x = padding + (i * barWidth * 2) + barWidth / 2;
                    int barHeight = (int) ((double) marks[i] / maxMarks * chartHeight);
                    int y = h - padding - 30 - barHeight;

                    // Draw bar with gradient
                    GradientPaint gradient = new GradientPaint(
                            x, y, colors[i % colors.length],
                            x + barWidth, y + barHeight, new Color(colors[i % colors.length].getRed(),
                                    colors[i % colors.length].getGreen(), colors[i % colors.length].getBlue(), 150));
                    g2.setPaint(gradient);
                    g2.fillRect(x, y, barWidth, barHeight);

                    // Draw bar border
                    g2.setColor(colors[i % colors.length]);
                    g2.drawRect(x, y, barWidth, barHeight);

                    // Draw subject name
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("segoe ui emoji", Font.PLAIN, 11));
                    String subjectShort = subjects[i].length() > 6 ? subjects[i].substring(0, 6) + "..." : subjects[i];

                    // Rotate if too many subjects
                    if (subjects.length > 5) {
                        Graphics2D g2Rot = (Graphics2D) g2.create();
                        g2Rot.rotate(-Math.PI / 4, x + barWidth / 2, h - padding - 15);
                        g2Rot.drawString(subjectShort, x + barWidth / 2 - 20, h - padding - 15);
                        g2Rot.dispose();
                    } else {
                        g2.drawString(subjectShort, x, h - padding - 15);
                    }

                    // Draw marks on top of bar
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("segoe ui emoji", Font.BOLD, 11));
                    g2.drawString(marks[i] + "", x + barWidth / 2 - 8, y - 5);
                }
            }
        };
        chartPanel.setPreferredSize(new Dimension(400, 250));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        chartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartPanel.setName("chartPanel");

        panel.add(chartPanel);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        valuePanel.setBackground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 20));
        valueLabel.setForeground(color);

        valuePanel.add(valueLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel title = new JLabel("📋 Detailed Results");
        title.setFont(new Font("segoe ui emoji", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        // Table
        String[] columns = { "Subject", "Code", "Marks", "Grade", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        marksTable = new JTable(tableModel);
        marksTable.setRowHeight(40);
        marksTable.setFont(new Font("segoe ui emoji", Font.PLAIN, 13));
        marksTable.getTableHeader().setFont(new Font("segoe ui emoji", Font.BOLD, 13));
        marksTable.getTableHeader().setBackground(new Color(248, 250, 252));
        marksTable.setSelectionBackground(new Color(173, 216, 230));

        // Set custom renderer for status column
        marksTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        JScrollPane scrollPane = new JScrollPane(marksTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom status bar with buttons
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        statusBar.setBackground(Color.WHITE);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        // PDF PRINT BUTTON - NEW ADDITION
        JButton printPdfBtn = new JButton("📄 Print / Save as PDF");
        printPdfBtn.setFont(new Font("segoe ui emoji", Font.BOLD, 12));
        printPdfBtn.setForeground(Color.WHITE);
        printPdfBtn.setBackground(new Color(33, 150, 243)); // Blue color
        printPdfBtn.setFocusPainted(false);
        printPdfBtn.setFocusable(true);
        printPdfBtn.setContentAreaFilled(true);
        printPdfBtn.setOpaque(true);
        printPdfBtn.setBorderPainted(false);
        printPdfBtn.setPreferredSize(new Dimension(160, 35));
        printPdfBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect for PDF button
        printPdfBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                printPdfBtn.setBackground(new Color(25, 118, 210));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                printPdfBtn.setBackground(new Color(33, 150, 243));
            }
        });

        printPdfBtn.addActionListener(e -> printMarksheetAsPDF());

        JButton revalBtn = new JButton("🔄 Apply Revaluation");
        revalBtn.setFont(new Font("segoe ui emoji", Font.BOLD, 12));
        revalBtn.setForeground(Color.WHITE);
        revalBtn.setBackground(new Color(255, 152, 0)); // Orange
        revalBtn.setFocusPainted(false);
        revalBtn.setFocusable(true);
        revalBtn.setContentAreaFilled(true);
        revalBtn.setOpaque(true);
        revalBtn.setBorderPainted(false);
        revalBtn.setPreferredSize(new Dimension(160, 35));
        revalBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        revalBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                revalBtn.setBackground(new Color(230, 130, 0)); // Darker orange on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                revalBtn.setBackground(new Color(255, 152, 0)); // Original orange
            }
        });

        revalBtn.addActionListener(e -> applyForRevaluation());

        JButton statusBtn = new JButton("📋 Check Status");
        statusBtn.setFont(new Font("segoe ui emoji", Font.BOLD, 12));
        statusBtn.setForeground(Color.WHITE);
        statusBtn.setBackground(new Color(76, 175, 80)); // Green
        statusBtn.setFocusPainted(false);
        statusBtn.setFocusable(true);
        statusBtn.setContentAreaFilled(true);
        statusBtn.setOpaque(true);
        statusBtn.setBorderPainted(false);
        statusBtn.setPreferredSize(new Dimension(140, 35));
        statusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        statusBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                statusBtn.setBackground(new Color(56, 142, 60)); // Darker green on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                statusBtn.setBackground(new Color(76, 175, 80)); // Original green
            }
        });

        statusBtn.addActionListener(e -> checkRevaluationStatus());

        JLabel searchLabel = new JLabel("🔍 Search");
        searchLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
        searchLabel.setForeground(Color.GRAY);

        JLabel dateLabel = new JLabel(new java.text.SimpleDateFormat("dd-MM-yyyy").format(new java.util.Date()));
        dateLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
        dateLabel.setForeground(Color.GRAY);

        JLabel langLabel = new JLabel("ENG · IN");
        langLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
        langLabel.setForeground(Color.GRAY);

        // Add buttons in order
        statusBar.add(printPdfBtn);
        statusBar.add(revalBtn);
        statusBar.add(statusBtn);
        statusBar.add(searchLabel);
        statusBar.add(dateLabel);
        statusBar.add(langLabel);

        panel.add(statusBar, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== NEW PDF PRINT METHOD ====================
    private void printMarksheetAsPDF() {
        try {
            // Create a panel with the marksheet design
            JPanel printPanel = new JPanel();
            printPanel.setLayout(new BoxLayout(printPanel, BoxLayout.Y_AXIS));
            printPanel.setBackground(Color.WHITE);
            printPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Header
            JLabel titleLabel = new JLabel("STUDENT MARKSHEET");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(new Color(25, 118, 210));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel collegeLabel = new JLabel("SRM INSTITUTE OF SCIENCE AND TECHNOLOGY");
            collegeLabel.setFont(new Font("Arial", Font.BOLD, 14));
            collegeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            printPanel.add(titleLabel);
            printPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            printPanel.add(collegeLabel);
            printPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            
            // Student Info Panel
            JPanel infoPanel = new JPanel(new GridLayout(4, 2, 10, 8));
            infoPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));
            infoPanel.add(new JLabel("Student Name:"));
            infoPanel.add(new JLabel(studentName));
            infoPanel.add(new JLabel("Roll Number:"));
            infoPanel.add(new JLabel(studentRoll));
            infoPanel.add(new JLabel("Department:"));
            infoPanel.add(new JLabel(studentDept));
            infoPanel.add(new JLabel("Issue Date:"));
            infoPanel.add(new JLabel(new java.text.SimpleDateFormat("dd-MM-yyyy").format(new java.util.Date())));
            
            printPanel.add(infoPanel);
            printPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            
            // Create a copy of the table for printing
            JTable printTable = new JTable(tableModel);
            printTable.setRowHeight(28);
            printTable.setFont(new Font("Arial", Font.PLAIN, 11));
            printTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
            
            JScrollPane tableScrollPane = new JScrollPane(printTable);
            tableScrollPane.setPreferredSize(new Dimension(600, 250));
            printPanel.add(tableScrollPane);
            printPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            
            // Summary Panel
            int totalMarks = 0;
            int subjectCount = tableModel.getRowCount();
            int passedCount = 0;
            int highestMarks = 0;
            
            for (int i = 0; i < subjectCount; i++) {
                String marksStr = (String) tableModel.getValueAt(i, 2);
                int marks = Integer.parseInt(marksStr.replace("/100", ""));
                totalMarks += marks;
                if (marks >= 40) passedCount++;
                if (marks > highestMarks) highestMarks = marks;
            }
            
            double average = subjectCount > 0 ? (double) totalMarks / subjectCount : 0;
            String result = passedCount == subjectCount ? "PASSED" : (passedCount >= subjectCount/2 ? "PARTIALLY PASSED" : "FAILED");
            
            JPanel summaryPanel = new JPanel(new GridLayout(5, 2, 10, 8));
            summaryPanel.setBorder(BorderFactory.createTitledBorder("Performance Summary"));
            summaryPanel.setBackground(new Color(240, 248, 255));
            
            summaryPanel.add(new JLabel("Average Score:"));
            summaryPanel.add(new JLabel(String.format("%.2f%%", average)));
            summaryPanel.add(new JLabel("Highest Score:"));
            summaryPanel.add(new JLabel(String.valueOf(highestMarks)));
            summaryPanel.add(new JLabel("Total Subjects:"));
            summaryPanel.add(new JLabel(String.valueOf(subjectCount)));
            summaryPanel.add(new JLabel("Subjects Passed:"));
            summaryPanel.add(new JLabel(passedCount + "/" + subjectCount));
            summaryPanel.add(new JLabel("Overall Result:"));
            JLabel resultLabel = new JLabel(result);
            resultLabel.setForeground(result.equals("PASSED") ? new Color(76, 175, 80) : new Color(220, 20, 60));
            summaryPanel.add(resultLabel);
            
            printPanel.add(summaryPanel);
            printPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            
            // Footer
            JLabel footerLabel = new JLabel("This is a computer-generated marksheet. No signature required.");
            footerLabel.setFont(new Font("Arial", Font.ITALIC, 10));
            footerLabel.setForeground(Color.GRAY);
            footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            printPanel.add(footerLabel);
            
            // Show preview and print
            JScrollPane previewScroll = new JScrollPane(printPanel);
            previewScroll.setPreferredSize(new Dimension(700, 600));
            
            int option = JOptionPane.showConfirmDialog(this, previewScroll, 
                "Marksheet Preview - Click YES to Print/Save as PDF",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                // Print the panel
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                job.setPrintable(new java.awt.print.Printable() {
                    @Override
                    public int print(Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) {
                        if (pageIndex > 0) {
                            return java.awt.print.Printable.NO_SUCH_PAGE;
                        }
                        
                        Graphics2D g2d = (Graphics2D) graphics;
                        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                        
                        double pageWidth = pageFormat.getImageableWidth();
                        double pageHeight = pageFormat.getImageableHeight();
                        double panelWidth = printPanel.getPreferredSize().width;
                        double panelHeight = printPanel.getPreferredSize().height;
                        
                        double scaleX = pageWidth / panelWidth;
                        double scaleY = pageHeight / panelHeight;
                        double scale = Math.min(scaleX, scaleY);
                        
                        if (scale < 1.0) {
                            g2d.scale(scale, scale);
                        }
                        
                        printPanel.print(g2d);
                        return java.awt.print.Printable.PAGE_EXISTS;
                    }
                });
                
                boolean doPrint = job.printDialog();
                if (doPrint) {
                    job.print();
                    JOptionPane.showMessageDialog(this, 
                        "✓ Marksheet sent to printer!\n\n" +
                        "💡 To Save as PDF:\n" +
                        "1. In the print dialog, click 'Print'\n" +
                        "2. Select 'Microsoft Print to PDF' or 'Save as PDF'\n" +
                        "3. Choose location and save!\n\n" +
                        "Your PDF will be created successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    // ==================== END OF PDF PRINT METHOD ====================

    private void loadStudentData() {
        try {
            // Update welcome message
            welcomeLabel.setText("Welcome, " + studentName + "!");

            // Get student marks
            String query = "SELECT subject, marks, semester FROM marks WHERE student_roll = ? ORDER BY subject";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, studentRoll);
            ResultSet rs = pst.executeQuery();

            int totalMarks = 0;
            int subjectCount = 0;
            int highestMarks = 0;
            int passedCount = 0;
            Map<String, Integer> subjectMarksMap = new LinkedHashMap<>();

            // Clear existing table data
            tableModel.setRowCount(0);

            // Subject codes mapping
            Map<String, String> subjectCodes = new HashMap<>();
            subjectCodes.put("Mathematics", "MATH101");
            subjectCodes.put("Physics", "PHYS101");
            subjectCodes.put("Chemistry", "CHEM101");
            subjectCodes.put("Biology", "BIO101");
            subjectCodes.put("Computer Science", "CS101");
            subjectCodes.put("English", "ENG101");
            subjectCodes.put("Economics", "ECO101");
            subjectCodes.put("Commerce", "COM101");

            while (rs.next()) {
                String subject = rs.getString("subject");
                int marks = rs.getInt("marks");
                String semester = rs.getString("semester");

                // Get subject code or generate one
                String code = subjectCodes.getOrDefault(subject,
                        subject.substring(0, Math.min(3, subject.length())).toUpperCase() + "101");

                // Calculate grade
                String grade = calculateGrade(marks);
                String status = marks >= 40 ? "Pass" : "Fail";

                // Add to table
                tableModel.addRow(new Object[] {
                        subject,
                        code,
                        marks + "/100",
                        grade,
                        status
                });

                // Update statistics
                totalMarks += marks;
                subjectCount++;
                highestMarks = Math.max(highestMarks, marks);
                if (marks >= 40)
                    passedCount++;

                // Store for charts
                subjectMarksMap.put(code, marks);
            }

            rs.close();
            pst.close();

            // Update stat labels
            double average = subjectCount > 0 ? (double) totalMarks / subjectCount : 0;
            avgScoreLabel.setText(String.format("%.0f%%", average));
            highestScoreLabel.setText(String.valueOf(highestMarks));
            subjectsLabel.setText(String.valueOf(subjectCount));
            passedLabel.setText(passedCount + "/" + subjectCount);

            // Update subject bars
            updateSubjectBars(subjectMarksMap);

            // Update bar chart
            updateBarChart(subjectMarksMap);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void updateSubjectBars(Map<String, Integer> subjectMarks) {
        JPanel leftPanel = (JPanel) ((JSplitPane) getContentPane().getComponent(1)).getLeftComponent();

        // Find the subject bars panel
        for (Component comp : leftPanel.getComponents()) {
            if (comp instanceof JPanel && comp.getName() != null && comp.getName().equals("subjectBars")) {
                JPanel barsPanel = (JPanel) comp;
                barsPanel.removeAll();

                // Create progress bars for each subject
                Color[] colors = {
                        new Color(25, 118, 210), // Blue
                        new Color(76, 175, 80), // Green
                        new Color(255, 152, 0), // Orange
                        new Color(156, 39, 176), // Purple
                        new Color(233, 30, 99), // Pink
                        new Color(0, 150, 136) // Teal
                };

                int i = 0;
                for (Map.Entry<String, Integer> entry : subjectMarks.entrySet()) {
                    String subject = entry.getKey();
                    int marks = entry.getValue();
                    Color color = colors[i % colors.length];

                    JPanel row = new JPanel(new BorderLayout(10, 0));
                    row.setBackground(Color.WHITE);
                    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

                    JLabel subjectLabel = new JLabel(subject);
                    subjectLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 13));
                    subjectLabel.setPreferredSize(new Dimension(70, 25));

                    AnimatedProgressBar bar = new AnimatedProgressBar(0, 100, marks);
                    bar.setForeground(color);
                    bar.setBackground(new Color(240, 240, 240));
                    bar.setPreferredSize(new Dimension(150, 8));
                    bar.setBorderPainted(false);
                    bar.startAnimation();

                    JLabel marksLabel = new JLabel(marks + "%");
                    marksLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 13));
                    marksLabel.setForeground(color);
                    marksLabel.setPreferredSize(new Dimension(45, 25));

                    row.add(subjectLabel, BorderLayout.WEST);
                    row.add(bar, BorderLayout.CENTER);
                    row.add(marksLabel, BorderLayout.EAST);

                    barsPanel.add(row);
                    barsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                    i++;
                }

                barsPanel.revalidate();
                barsPanel.repaint();
                break;
            }
        }
    }

    private void updateBarChart(Map<String, Integer> subjectMarks) {
        JPanel leftPanel = (JPanel) ((JSplitPane) getContentPane().getComponent(1)).getLeftComponent();

        // Find the chart panel
        for (Component comp : leftPanel.getComponents()) {
            if (comp instanceof JPanel && comp.getName() != null && comp.getName().equals("chartPanel")) {
                JPanel chartPanel = (JPanel) comp;
                // Create a new instance with the data
                JPanel newChartPanel = new JPanel() {
                    private Map<String, Integer> marks = subjectMarks;
                    private String[] subjects = marks.keySet().toArray(new String[0]);
                    private int[] values = new int[subjects.length];

                    {
                        for (int i = 0; i < subjects.length; i++) {
                            values[i] = marks.get(subjects[i]);
                        }
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        if (subjects.length == 0)
                            return;

                        int w = getWidth();
                        int h = getHeight();
                        int padding = 40;
                        int chartWidth = w - 2 * padding;
                        int chartHeight = h - 2 * padding - 30;
                        int barWidth = chartWidth / (subjects.length * 2);

                        // Draw axes
                        g2.setColor(Color.LIGHT_GRAY);
                        g2.drawLine(padding, padding, padding, h - padding - 30);
                        g2.drawLine(padding, h - padding - 30, w - padding, h - padding - 30);

                        // Draw horizontal grid lines
                        for (int i = 0; i <= 5; i++) {
                            int y = h - padding - 30 - (i * chartHeight / 5);
                            g2.setColor(new Color(230, 230, 230));
                            g2.drawLine(padding, y, w - padding, y);

                            // Mark percentages
                            g2.setColor(Color.GRAY);
                            g2.setFont(new Font("segoe ui emoji", Font.PLAIN, 10));
                            g2.drawString((i * 20) + "%", padding - 30, y + 3);
                        }

                        // Draw bars
                        Color[] colors = {
                                new Color(25, 118, 210), // Blue
                                new Color(76, 175, 80), // Green
                                new Color(255, 152, 0), // Orange
                                new Color(156, 39, 176), // Purple
                                new Color(233, 30, 99), // Pink
                                new Color(0, 150, 136) // Teal
                        };

                        for (int i = 0; i < subjects.length; i++) {
                            int x = padding + (i * barWidth * 2) + barWidth / 2;
                            int barHeight = (int) ((double) values[i] / 100 * chartHeight);
                            int y = h - padding - 30 - barHeight;

                            // Draw bar with gradient
                            GradientPaint gradient = new GradientPaint(
                                    x, y, colors[i % colors.length],
                                    x + barWidth, y + barHeight, new Color(colors[i % colors.length].getRed(),
                                            colors[i % colors.length].getGreen(), colors[i % colors.length].getBlue(),
                                            150));
                            g2.setPaint(gradient);
                            g2.fillRect(x, y, barWidth, barHeight);

                            // Draw bar border
                            g2.setColor(colors[i % colors.length]);
                            g2.drawRect(x, y, barWidth, barHeight);

                            // Draw subject name
                            g2.setColor(Color.BLACK);
                            g2.setFont(new Font("segoe ui emoji", Font.PLAIN, 11));
                            String subjectShort = subjects[i].length() > 6 ? subjects[i].substring(0, 6) + "..."
                                    : subjects[i];

                            if (subjects.length > 5) {
                                Graphics2D g2Rot = (Graphics2D) g2.create();
                                g2Rot.rotate(-Math.PI / 4, x + barWidth / 2, h - padding - 15);
                                g2Rot.drawString(subjectShort, x + barWidth / 2 - 20, h - padding - 15);
                                g2Rot.dispose();
                            } else {
                                g2.drawString(subjectShort, x, h - padding - 15);
                            }

                            // Draw marks on top of bar
                            g2.setColor(Color.BLACK);
                            g2.setFont(new Font("segoe ui emoji", Font.BOLD, 11));
                            g2.drawString(values[i] + "", x + barWidth / 2 - 8, y - 5);
                        }
                    }
                };
                newChartPanel.setPreferredSize(new Dimension(400, 250));
                newChartPanel.setBackground(Color.WHITE);
                newChartPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
                newChartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                newChartPanel.setName("chartPanel");

                // Replace the old chart panel
                Container parent = chartPanel.getParent();
                parent.remove(chartPanel);
                parent.add(newChartPanel);
                parent.revalidate();
                parent.repaint();
                break;
            }
        }
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

    // Revaluation System
    private void applyForRevaluation() {
        int selectedRow = marksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a subject to apply for revaluation!",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String subject = (String) tableModel.getValueAt(selectedRow, 0);
        String code = (String) tableModel.getValueAt(selectedRow, 1);
        String marksStr = (String) tableModel.getValueAt(selectedRow, 2);
        String status = (String) tableModel.getValueAt(selectedRow, 4);

        // Show revaluation dialog
        showRevaluationDialog(subject, code, marksStr, status);
    }

    private void showRevaluationDialog(String subject, String code, String marksStr, String status) {
        JDialog dialog = new JDialog(this, "Apply for Revaluation", true);
        dialog.setSize(600, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel headerLabel = new JLabel("📝 Revaluation Application");
        headerLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 20));
        headerLabel.setForeground(primaryColor);
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(headerLabel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Subject info panel
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Subject Details",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("segoe ui emoji", Font.BOLD, 14)));

        infoPanel.add(new JLabel("Subject:"));
        infoPanel.add(new JLabel(subject));
        infoPanel.add(new JLabel("Subject Code:"));
        infoPanel.add(new JLabel(code));
        infoPanel.add(new JLabel("Current Marks:"));
        infoPanel.add(new JLabel(marksStr));
        infoPanel.add(new JLabel("Current Status:"));

        JLabel statusLabel = new JLabel(status);
        statusLabel.setForeground(status.equals("Pass") ? successColor : dangerColor);
        infoPanel.add(statusLabel);

        mainPanel.add(infoPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Revaluation fee panel
        JPanel feePanel = new JPanel(new GridLayout(3, 2, 10, 10));
        feePanel.setBackground(Color.WHITE);
        feePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Payment Details",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("segoe ui emoji", Font.BOLD, 14)));

        double revaluationFee = 846.61;
        double gst = revaluationFee * 0.18;
        double totalFee = revaluationFee + gst;

        feePanel.add(new JLabel("Revaluation Fee:"));
        feePanel.add(new JLabel("₹" + String.format("%.2f", revaluationFee)));
        feePanel.add(new JLabel("GST (18%):"));
        feePanel.add(new JLabel("₹" + String.format("%.2f", gst)));
        feePanel.add(new JLabel("Total Amount:"));

        JLabel totalLabel = new JLabel("₹" + String.format("%.2f", totalFee));
        totalLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 16));
        totalLabel.setForeground(primaryColor);
        feePanel.add(totalLabel);

        mainPanel.add(feePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Payment method panel
        JPanel paymentPanel = new JPanel();
        paymentPanel.setLayout(new BoxLayout(paymentPanel, BoxLayout.Y_AXIS));
        paymentPanel.setBackground(Color.WHITE);
        paymentPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Select Payment Method",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("segoe ui emoji", Font.BOLD, 14)));

        JRadioButton cardRadio = new JRadioButton("💳 Credit/Debit Card");
        JRadioButton upiRadio = new JRadioButton("📱 UPI (Google Pay/PhonePe/PayTM)");
        JRadioButton netbankingRadio = new JRadioButton("🏦 Net Banking");

        cardRadio.setBackground(Color.WHITE);
        upiRadio.setBackground(Color.WHITE);
        netbankingRadio.setBackground(Color.WHITE);

        cardRadio.setFont(new Font("segoe ui emoji", Font.PLAIN, 13));
        upiRadio.setFont(new Font("segoe ui emoji", Font.PLAIN, 13));
        netbankingRadio.setFont(new Font("segoe ui emoji", Font.PLAIN, 13));

        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(cardRadio);
        paymentGroup.add(upiRadio);
        paymentGroup.add(netbankingRadio);

        upiRadio.setSelected(true);

        paymentPanel.add(cardRadio);
        paymentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        paymentPanel.add(upiRadio);
        paymentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        paymentPanel.add(netbankingRadio);

        mainPanel.add(paymentPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // QR Code Panel for UPI
        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setBackground(Color.WHITE);
        qrPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "📱 Scan QR Code to Pay",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("segoe ui emoji", Font.BOLD, 14)));

        // Load QR code image from file
        try {
            // Update this path to your actual QR code image location
            String qrImagePath = "qr.png"; // Place qr.png in your project folder
            File qrFile = new File(qrImagePath);

            if (qrFile.exists()) {
                ImageIcon qrIcon = new ImageIcon(qrImagePath);
                // Scale the image to fit nicely
                Image scaledImage = qrIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                JLabel qrLabel = new JLabel(new ImageIcon(scaledImage));
                qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
                qrPanel.add(qrLabel, BorderLayout.CENTER);

                JPanel upiDetailsPanel = new JPanel(new GridLayout(2, 1));
                upiDetailsPanel.setBackground(Color.WHITE);

                JLabel upiIdLabel = new JLabel("UPI ID: srms@okhdfcbank");
                upiIdLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 12));
                upiIdLabel.setForeground(primaryColor);
                upiIdLabel.setHorizontalAlignment(SwingConstants.CENTER);

                JLabel upiInstructionLabel = new JLabel("Scan this QR code with any UPI app to pay");
                upiInstructionLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 11));
                upiInstructionLabel.setForeground(Color.GRAY);
                upiInstructionLabel.setHorizontalAlignment(SwingConstants.CENTER);

                upiDetailsPanel.add(upiIdLabel);
                upiDetailsPanel.add(upiInstructionLabel);

                qrPanel.add(upiDetailsPanel, BorderLayout.SOUTH);
            } else {
                JLabel errorLabel = new JLabel("⚠️ QR Code image not found. Please place 'qr.png' in project folder.");
                errorLabel.setForeground(dangerColor);
                errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
                qrPanel.add(errorLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("⚠️ Error loading QR code: " + e.getMessage());
            errorLabel.setForeground(dangerColor);
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qrPanel.add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();
        }

        qrPanel.setVisible(true); // Show QR for UPI by default
        mainPanel.add(qrPanel);

        // Add action listeners to show/hide QR based on selection
        upiRadio.addActionListener(e -> qrPanel.setVisible(true));
        cardRadio.addActionListener(e -> qrPanel.setVisible(false));
        netbankingRadio.addActionListener(e -> qrPanel.setVisible(false));

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Reason panel
        JPanel reasonPanel = new JPanel(new BorderLayout());
        reasonPanel.setBackground(Color.WHITE);
        reasonPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Reason for Revaluation (Optional)",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("segoe ui emoji", Font.BOLD, 14)));

        JTextArea reasonArea = new JTextArea(3, 30);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setPreferredSize(new Dimension(400, 60));

        reasonPanel.add(reasonScroll, BorderLayout.CENTER);

        mainPanel.add(reasonPanel);

        // Button panel with improved colors
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);

        // Pay Button - Fixed version
        JButton payBtn = new JButton("✅ Proceed to Pay ₹" + String.format("%.2f", totalFee));
        payBtn.setFont(new Font("segoe ui emoji", Font.BOLD, 14));
        payBtn.setForeground(Color.WHITE);
        payBtn.setBackground(new Color(0, 123, 255));
        payBtn.setFocusPainted(false);
        payBtn.setFocusable(true);
        payBtn.setContentAreaFilled(true);
        payBtn.setOpaque(true);
        payBtn.setBorderPainted(false);
        payBtn.setPreferredSize(new Dimension(240, 50));
        payBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Cancel Button - Fixed version
        JButton cancelBtn = new JButton("❌ Cancel");
        cancelBtn.setFont(new Font("segoe ui emoji", Font.BOLD, 14));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(new Color(220, 53, 69));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setFocusable(true);
        cancelBtn.setContentAreaFilled(true);
        cancelBtn.setOpaque(true);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setPreferredSize(new Dimension(120, 50));
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        payBtn.addActionListener(e -> {
            // Process payment
            String paymentMethod = "UPI";
            if (cardRadio.isSelected())
                paymentMethod = "Card";
            else if (upiRadio.isSelected())
                paymentMethod = "UPI";
            else if (netbankingRadio.isSelected())
                paymentMethod = "Net Banking";

            processRevaluationPayment(dialog, subject, code, totalFee, paymentMethod, reasonArea.getText());
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(payBtn);
        buttonPanel.add(cancelBtn);

        // Add panels to dialog
        dialog.add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void processRevaluationPayment(JDialog dialog, String subject, String code,
            double amount, String paymentMethod, String reason) {
        Random random = new Random(); // Added missing Random declaration

        // If payment method is UPI, show QR code scan instruction
        if (paymentMethod.equals("UPI")) {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Please scan the QR code with your UPI app to complete payment.\n\n" +
                            "UPI ID: srms@okhdfcbank\n" +
                            "Amount: ₹" + String.format("%.2f", amount) + "\n\n" +
                            "Click Yes after making payment, or No to cancel.",
                    "Scan QR Code to Pay",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            // Show payment processing dialog for other methods
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Proceed with payment of ₹" + String.format("%.2f", amount) +
                            " using " + paymentMethod + "?",
                    "Confirm Payment",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Show payment processing dialog
        JDialog processingDialog = new JDialog(dialog, "Processing Payment", true);
        processingDialog.setSize(350, 200);
        processingDialog.setLocationRelativeTo(dialog);
        processingDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JLabel processingLabel = new JLabel("⏳ Processing Payment...");
        processingLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 16));
        processingLabel.setForeground(primaryColor);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(280, 25));
        progressBar.setForeground(successColor);

        JLabel waitLabel = new JLabel("Please wait while we confirm your payment");
        waitLabel.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
        waitLabel.setForeground(Color.GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(15, 10, 10, 10);
        panel.add(processingLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(progressBar, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 15, 10);
        panel.add(waitLabel, gbc);

        processingDialog.add(panel, BorderLayout.CENTER);

        // Simulate payment processing
        Timer timer = new Timer(2500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processingDialog.dispose();

                // Generate transaction ID
                String transactionId = "TXN" + System.currentTimeMillis() +
                        String.format("%04d", random.nextInt(10000));

                // Show success message with receipt
                JPanel receiptPanel = new JPanel();
                receiptPanel.setLayout(new BoxLayout(receiptPanel, BoxLayout.Y_AXIS));
                receiptPanel.setBackground(Color.WHITE);
                receiptPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

                JLabel receiptTitle = new JLabel("🧾 PAYMENT RECEIPT");
                receiptTitle.setFont(new Font("segoe ui emoji", Font.BOLD, 18));
                receiptTitle.setForeground(successColor);
                receiptTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                receiptPanel.add(receiptTitle);

                receiptPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                // Receipt details
                JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 8));
                detailsPanel.setBackground(Color.WHITE);
                detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));

                addReceiptRow(detailsPanel, "Transaction ID:", transactionId);
                addReceiptRow(detailsPanel, "Date:",
                        new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new java.util.Date()));
                addReceiptRow(detailsPanel, "Student Name:", studentName);
                addReceiptRow(detailsPanel, "Roll Number:", studentRoll);
                addReceiptRow(detailsPanel, "Subject:", subject);
                addReceiptRow(detailsPanel, "Subject Code:", code);
                addReceiptRow(detailsPanel, "Payment Method:", paymentMethod);
                addReceiptRow(detailsPanel, "Revaluation Fee:", "₹" + String.format("%.2f", 500.00));
                addReceiptRow(detailsPanel, "GST (18%):", "₹" + String.format("%.2f", 90.00));

                JLabel totalAmountLabel = new JLabel("₹" + String.format("%.2f", amount));
                totalAmountLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 16));
                totalAmountLabel.setForeground(primaryColor);

                detailsPanel.add(new JLabel("TOTAL AMOUNT:"));
                detailsPanel.add(totalAmountLabel);

                receiptPanel.add(detailsPanel);

                receiptPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                JLabel statusLabel = new JLabel("✅ Payment Successful! Your revaluation request has been submitted.");
                statusLabel.setFont(new Font("segoe ui emoji", Font.BOLD, 12));
                statusLabel.setForeground(successColor);
                statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                receiptPanel.add(statusLabel);

                JOptionPane.showMessageDialog(dialog, receiptPanel, "Payment Successful",
                        JOptionPane.INFORMATION_MESSAGE);

                // Save revaluation request to database
                saveRevaluationRequest(subject, code, amount, paymentMethod, reason, transactionId);

                dialog.dispose();
            }
        });
        timer.setRepeats(false);

        processingDialog.setVisible(true);
        timer.start();
    }

    private void saveRevaluationRequest(String subject, String code, double amount,
            String paymentMethod, String reason, String transactionId) {
        try {
            // Check if revaluation table exists, if not create it
            String createTableQuery = "CREATE TABLE IF NOT EXISTS revaluation_requests (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_roll VARCHAR(20) NOT NULL, " +
                    "student_name VARCHAR(100) NOT NULL, " +
                    "subject VARCHAR(100) NOT NULL, " +
                    "subject_code VARCHAR(20), " +
                    "amount DECIMAL(10,2), " +
                    "payment_method VARCHAR(50), " +
                    "reason TEXT, " +
                    "transaction_id VARCHAR(100), " +
                    "status VARCHAR(20) DEFAULT 'Pending', " +
                    "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (student_roll) REFERENCES students(roll))";

            Statement stmt = con.createStatement();
            stmt.executeUpdate(createTableQuery);
            stmt.close();

            // Insert revaluation request
            String insertQuery = "INSERT INTO revaluation_requests " +
                    "(student_roll, student_name, subject, subject_code, amount, payment_method, reason, transaction_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pst = con.prepareStatement(insertQuery);
            pst.setString(1, studentRoll);
            pst.setString(2, studentName);
            pst.setString(3, subject);
            pst.setString(4, code);
            pst.setDouble(5, amount);
            pst.setString(6, paymentMethod);
            pst.setString(7, reason);
            pst.setString(8, transactionId);

            pst.executeUpdate();
            pst.close();

            JOptionPane.showMessageDialog(this,
                    "✅ Revaluation request saved successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving revaluation request: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Add this method to check revaluation status
    private void checkRevaluationStatus() {
        try {
            String query = "SELECT * FROM revaluation_requests WHERE student_roll = ? ORDER BY request_date DESC";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, studentRoll);
            ResultSet rs = pst.executeQuery();

            StringBuilder statusMessage = new StringBuilder();
            statusMessage.append("📋 Your Revaluation Requests\n");
            statusMessage.append("─────────────────────────\n\n");

            boolean hasRequests = false;

            while (rs.next()) {
                hasRequests = true;
                String subject = rs.getString("subject");
                String status = rs.getString("status");
                Timestamp date = rs.getTimestamp("request_date");
                String transactionId = rs.getString("transaction_id");
                double amount = rs.getDouble("amount");

                statusMessage.append("Subject: ").append(subject).append("\n");
                statusMessage.append("Status: ");

                if (status.equals("Pending")) {
                    statusMessage.append("⏳ ").append(status).append("\n");
                } else if (status.equals("Approved")) {
                    statusMessage.append("✅ ").append(status).append("\n");
                } else if (status.equals("Rejected")) {
                    statusMessage.append("❌ ").append(status).append("\n");
                } else if (status.equals("Completed")) {
                    statusMessage.append("✓ ").append(status).append("\n");
                }

                statusMessage.append("Transaction ID: ").append(transactionId).append("\n");
                statusMessage.append("Amount: ₹").append(String.format("%.2f", amount)).append("\n");
                statusMessage.append("Request Date: ").append(date).append("\n");
                statusMessage.append("─────────────────────────\n\n");
            }

            if (!hasRequests) {
                statusMessage.append("You haven't applied for any revaluation yet.");
            }

            JTextArea textArea = new JTextArea(statusMessage.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Revaluation Status", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error checking revaluation status: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addReceiptRow(JPanel panel, String label, String value) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("segoe ui emoji", Font.PLAIN, 12));
        labelComp.setForeground(Color.GRAY);

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("segoe ui emoji", Font.BOLD, 12));
        valueComp.setForeground(Color.BLACK);

        panel.add(labelComp);
        panel.add(valueComp);
    }

    // Animated Progress Bar Class
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

    // Custom cell renderer for status column
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value != null) {
                String status = value.toString();
                if (status.equals("Pass")) {
                    setForeground(new Color(76, 175, 80));
                    setText("✓ " + status);
                } else {
                    setForeground(new Color(220, 20, 60));
                    setText("✗ " + status);
                }
                setFont(new Font("segoe ui emoji", Font.BOLD, 12));
            }

            return c;
        }
    }

    public static void main(String[] args) {
        // This is just for testing - in real usage, this would be launched from login
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection testCon = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/studentdb?useSSL=false&serverTimezone=UTC",
                    "root", "root");
            SwingUtilities
                    .invokeLater(() -> new StudentDashboard(testCon, "STU001", "Alex Thompson", "Computer Science"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}