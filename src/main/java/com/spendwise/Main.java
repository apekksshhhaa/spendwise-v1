package com.spendwise;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.io.FileWriter;

public class Main {
    private static double budgetLimit = 5000.00;
    
    // --- 🖋️ TYPOGRAPHY ---
    private static final String FONT_NAME = "Consolas";
    private static final Font BOLD_20 = new Font(FONT_NAME, Font.BOLD, 20);
    private static final Font BOLD_18 = new Font(FONT_NAME, Font.BOLD, 18);
    private static final Font PLAIN_17 = new Font(FONT_NAME, Font.PLAIN, 17);

    // --- 🎨 SYSTEM COLORS ---
    private static final Color MUTED_PINK_BG = new Color(255, 240, 245);
    private static final Color SIDEBAR_PINK = new Color(230, 180, 185);
    private static final Color ROSE_PINK = new Color(244, 194, 194);
    
    // --- 🖋️ FONT COLORS ---
    private static final Color BLOOD_RED = new Color(138, 3, 3);
    private static final Color LEMON_YELLOW = new Color(200, 180, 0); 
    private static final Color LEAF_GREEN = new Color(34, 139, 34);
    private static final Color MAROON = new Color(128, 0, 0);
    private static final Color DARK_BLUE = new Color(0, 0, 139);

    public static void main(String[] args) {
        initLocalStorage();
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 25);
        } catch (Exception ex) { ex.printStackTrace(); }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SpendWise - Final Edition");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLayout(new BorderLayout());

            // --- SIDEBAR ---
            JPanel sidebar = new JPanel(new GridLayout(10, 1, 0, 15));
            sidebar.setBackground(SIDEBAR_PINK);
            sidebar.setBorder(BorderFactory.createEmptyBorder(30, 15, 30, 15));
            sidebar.setPreferredSize(new Dimension(250, 0));

            // --- MAIN AREA ---
            JPanel mainArea = new JPanel(new BorderLayout());
            mainArea.setBackground(MUTED_PINK_BG);

            // Header Section
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
            
            JLabel title = new JLabel("[#] EXPENDITURE LOG");
            title.setFont(BOLD_20);
            header.add(title, BorderLayout.WEST);

            JLabel totalLabel = new JLabel("TOTAL: 0.00");
            totalLabel.setFont(BOLD_20);
            header.add(totalLabel, BorderLayout.EAST);

            // Table with Category Text Color
            String[] columns = {"ID", "ITEM", "AMOUNT", "CATEGORY"};
            DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
            JTable table = new JTable(tableModel);
            table.setRowHeight(45);
            table.setFont(PLAIN_17);
            table.getTableHeader().setFont(BOLD_18);
            table.setShowGrid(false);
            
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                    Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                    String category = t.getValueAt(row, 3).toString();
                    c.setBackground(Color.WHITE);
                    if (isSelected) c.setBackground(new Color(230, 230, 250));

                    switch (category) {
                        case "Food": c.setForeground(MAROON); break;
                        case "Shopping": c.setForeground(new Color(218, 165, 32)); break;
                        case "Travel": c.setForeground(DARK_BLUE); break;
                        case "Bills": c.setForeground(Color.BLACK); break;
                        default: c.setForeground(Color.GRAY); break;
                    }
                    return c;
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

            // Bottom Buttons
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setOpaque(false);
            JButton addBtn = new JButton("[+] ADD NEW");
            JButton deleteBtn = new JButton("[X] DELETE");
            addBtn.setFont(BOLD_18); deleteBtn.setFont(BOLD_18);
            bottomPanel.add(addBtn); bottomPanel.add(deleteBtn);

            // --- REFRESH LOGIC ---
            Runnable refreshTable = () -> {
                tableModel.setRowCount(0);
                double total = 0;
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:spendwise.db")) {
                    ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM expenses");
                    while (rs.next()) {
                        double amt = rs.getDouble("amount");
                        total += amt;
                        tableModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), amt, rs.getString(4)});
                    }
                    totalLabel.setText("TOTAL: " + String.format("%.2f", total));
                    if (total > budgetLimit) totalLabel.setForeground(BLOOD_RED);
                    else if (total > budgetLimit * 0.7) totalLabel.setForeground(LEMON_YELLOW);
                    else totalLabel.setForeground(LEAF_GREEN);
                } catch (Exception ex) { ex.printStackTrace(); }
            };

            // Sidebar Buttons
            String[] labels = {"HOME", "STATS", "BUDGET", "EXPORT"};
            for (String label : labels) {
                JButton btn = new JButton("[#] " + label);
                btn.setFont(BOLD_18); btn.setBackground(ROSE_PINK);
                btn.addActionListener(e -> {
                    if (label.equals("STATS")) showAnalytics(frame);
                    else if (label.equals("BUDGET")) {
                        String in = JOptionPane.showInputDialog(frame, "Set Limit:", budgetLimit);
                        if (in != null) { try { budgetLimit = Double.parseDouble(in); refreshTable.run(); } catch(Exception ex){} }
                    } else if (label.equals("EXPORT")) exportData(frame);
                    refreshTable.run();
                });
                sidebar.add(btn);
            }

            addBtn.addActionListener(e -> showAddDialog(frame, refreshTable));
            deleteBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row != -1) {
                    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:spendwise.db")) {
                        PreparedStatement ps = conn.prepareStatement("DELETE FROM expenses WHERE id=?");
                        ps.setInt(1, (int) tableModel.getValueAt(row, 0));
                        ps.executeUpdate(); refreshTable.run();
                    } catch (Exception ex) {}
                }
            });

            mainArea.add(header, BorderLayout.NORTH);
            mainArea.add(scrollPane, BorderLayout.CENTER);
            mainArea.add(bottomPanel, BorderLayout.SOUTH);
            frame.add(sidebar, BorderLayout.WEST);
            frame.add(mainArea, BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            refreshTable.run();
        });
    }

    // --- 📊 WORKING STATS LOGIC ---
    private static void showAnalytics(JFrame frame) {
        Map<String, Double> data = new HashMap<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:spendwise.db");
             ResultSet rs = conn.createStatement().executeQuery("SELECT category, SUM(amount) FROM expenses GROUP BY category")) {
            while (rs.next()) data.put(rs.getString(1), rs.getDouble(2));
            
            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No data to display!");
                return;
            }

            JDialog d = new JDialog(frame, "Expenditure Stats", true);
            d.setSize(450, 550);
            d.add(new PieChartPanel(data));
            d.setLocationRelativeTo(frame);
            d.setVisible(true);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private static void exportData(JFrame frame) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(chooser.getSelectedFile().getAbsolutePath() + ".csv");
                 Connection conn = DriverManager.getConnection("jdbc:sqlite:spendwise.db")) {
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM expenses");
                fw.write("ID,ITEM,AMOUNT,CATEGORY\n");
                while (rs.next()) fw.write(rs.getInt(1)+","+rs.getString(2)+","+rs.getDouble(3)+","+rs.getString(4)+"\n");
                JOptionPane.showMessageDialog(frame, "Data Exported Successfully.");
            } catch (Exception ex) {}
        }
    }

    private static void showAddDialog(JFrame frame, Runnable refreshTable) {
        JDialog d = new JDialog(frame, "ADD", true);
        d.setLayout(new GridLayout(4, 2, 10, 10)); d.setSize(350, 250);
        JTextField nF = new JTextField(); JTextField aF = new JTextField();
        JComboBox<String> cB = new JComboBox<>(new String[]{"Food", "Shopping", "Travel", "Bills"});
        d.add(new JLabel(" ITEM:")); d.add(nF);
        d.add(new JLabel(" PRICE:")); d.add(aF);
        d.add(new JLabel(" TAG:")); d.add(cB);
        JButton s = new JButton("SAVE");
        s.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:spendwise.db")) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO expenses(name,amount,category) VALUES(?,?,?)");
                ps.setString(1, nF.getText()); ps.setDouble(2, Double.parseDouble(aF.getText()));
                ps.setString(3, (String)cB.getSelectedItem()); ps.executeUpdate();
                refreshTable.run(); d.dispose();
            } catch (Exception ex) {}
        });
        d.add(new JLabel("")); d.add(s); d.setLocationRelativeTo(frame); d.setVisible(true);
    }

    private static void initLocalStorage() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:spendwise.db")) {
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS expenses (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, amount REAL, category TEXT)");
        } catch (Exception e) {}
    }
}

// --- 🥧 PIE CHART COMPONENT ---
class PieChartPanel extends JPanel {
    private final Map<String, Double> data;
    public PieChartPanel(Map<String, Double> data) { 
        this.data = data; 
        setBackground(new Color(255, 245, 250)); 
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double total = data.values().stream().mapToDouble(d -> d).sum();
        double cur = 0;
        Color[] colors = {new Color(128, 0, 0), new Color(218, 165, 32), new Color(0, 0, 139), Color.BLACK};
        int i = 0;
        for (Map.Entry<String, Double> e : data.entrySet()) {
            int arc = (int) (e.getValue() * 360 / total);
            g2.setColor(colors[i % 4]);
            g2.fillArc(100, 50, 220, 220, (int)(cur * 360 / total), arc);
            g2.fillRect(60, 330 + (i * 30), 15, 15);
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Consolas", Font.BOLD, 15));
            g2.drawString(e.getKey().toUpperCase() + ": " + String.format("%.2f", e.getValue()), 90, 342 + (i * 30));
            cur += e.getValue(); i++;
        }
    }
}