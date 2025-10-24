package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.awt.print.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class AdminPanel extends JFrame implements Printable {

    private JPanel sidebarPanel, contentPanel;
    private JButton btnEmployees, btnMenu, btnSales, btnLogout;
    private JButton btnPrint, btnPDF;
    private JTable employeeTable, menuTable, salesTable;
    private DefaultTableModel employeeModel, menuModel, salesModel;
    private Connection conn;

    public AdminPanel() {
        setTitle("Admin Panel - Culinary Craft");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        connectDB();
        initSidebar();
        initContentPanel();

        loadEmployees();
        loadMenu();
        loadSales();

        setVisible(true);
    }

    // ================= SIDEBAR =================
    private void initSidebar() {
        sidebarPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        sidebarPanel.setPreferredSize(new Dimension(200, 0));
        sidebarPanel.setBackground(new Color(33, 33, 33));

        btnEmployees = new JButton("Employees");
        btnMenu = new JButton("Menu");
        btnSales = new JButton("Sales");
        btnLogout = new JButton("Logout");

        for (JButton b : new JButton[]{btnEmployees, btnMenu, btnSales, btnLogout}) {
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(66, 66, 66));
            b.setFocusPainted(false);
            b.setFont(new Font("SansSerif", Font.BOLD, 14));
            sidebarPanel.add(b);
        }

        add(sidebarPanel, BorderLayout.WEST);

        btnEmployees.addActionListener(e -> showPanel("Employees"));
        btnMenu.addActionListener(e -> showPanel("Menu"));
        btnSales.addActionListener(e -> showPanel("Sales"));
        btnLogout.addActionListener(e -> dispose());
    }

    // ================= CONTENT PANEL =================
    private void initContentPanel() {
        contentPanel = new JPanel(new CardLayout());
        add(contentPanel, BorderLayout.CENTER);

        // ---------- Employee Panel ----------
        JPanel empPanel = new JPanel(new BorderLayout());
        employeeModel = new DefaultTableModel(
                new String[]{"ID", "USERNAME", "PASSWORD", "DOB", "AADHAAR", "ADDRESS", "PHONE", "EMAIL", "ROLE"}, 0
        );
        employeeTable = new JTable(employeeModel);
        empPanel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        JPanel empButtonPanel = new JPanel();
        JButton btnAddEmp = new JButton("Add");
        JButton btnEditEmp = new JButton("Edit");
        JButton btnDeleteEmp = new JButton("Delete");
        empButtonPanel.add(btnAddEmp);
        empButtonPanel.add(btnEditEmp);
        empButtonPanel.add(btnDeleteEmp);
        empPanel.add(empButtonPanel, BorderLayout.SOUTH);

        btnAddEmp.addActionListener(e -> showEmployeeForm(null));
        btnEditEmp.addActionListener(e -> editSelectedEmployee());
        btnDeleteEmp.addActionListener(e -> deleteSelectedEmployee());

        contentPanel.add(empPanel, "Employees");

        // ---------- Menu Panel ----------
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuModel = new DefaultTableModel(new String[]{"ID", "ITEMNAME", "CATEGORY", "PRICE"}, 0);
        menuTable = new JTable(menuModel);
        menuPanel.add(new JScrollPane(menuTable), BorderLayout.CENTER);

        JPanel menuButtonPanel = new JPanel();
        JButton btnAddMenu = new JButton("Add");
        JButton btnEditMenu = new JButton("Edit");
        JButton btnDeleteMenu = new JButton("Delete");
        menuButtonPanel.add(btnAddMenu);
        menuButtonPanel.add(btnEditMenu);
        menuButtonPanel.add(btnDeleteMenu);
        menuPanel.add(menuButtonPanel, BorderLayout.SOUTH);

        btnAddMenu.addActionListener(e -> showMenuForm(null));
        btnEditMenu.addActionListener(e -> editSelectedMenu());
        btnDeleteMenu.addActionListener(e -> deleteSelectedMenu());

        contentPanel.add(menuPanel, "Menu");

        // ---------- Sales Panel ----------
        JPanel salesPanel = new JPanel(new BorderLayout());
        salesModel = new DefaultTableModel(new String[]{"Item Name", "Quantity", "Amount", "Sale Date"}, 0);
        salesTable = new JTable(salesModel);
        salesPanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        btnPrint = new JButton("Print Report");
        btnPDF = new JButton("Export PDF");
        bottomPanel.add(btnPrint);
        bottomPanel.add(btnPDF);
        salesPanel.add(bottomPanel, BorderLayout.SOUTH);

        btnPrint.addActionListener(e -> printSalesReport());
        btnPDF.addActionListener(e -> exportSalesToPDF());

        contentPanel.add(salesPanel, "Sales");
    }

    // ================= DATABASE =================
    private void connectDB() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "system", "root");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }
    }

    private void showPanel(String name) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, name);
    }

    // ================= EMPLOYEE CRUD =================
    private void showEmployeeForm(Object[] data) {
        JTextField username = new JTextField(data != null ? data[1].toString() : "");
        JTextField password = new JTextField(data != null ? data[2].toString() : "");
        JTextField dob = new JTextField(data != null ? data[3].toString() : "");
        JTextField aadhaar = new JTextField(data != null ? data[4].toString() : "");
        JTextField address = new JTextField(data != null ? data[5].toString() : "");
        JTextField phone = new JTextField(data != null ? data[6].toString() : "");
        JTextField email = new JTextField(data != null ? data[7].toString() : "");
        JTextField role = new JTextField(data != null ? data[8].toString() : "");

        Object[] fields = {
                "Username:", username,
                "Password:", password,
                "DOB (yyyy-mm-dd):", dob,
                "Aadhaar:", aadhaar,
                "Address:", address,
                "Phone:", phone,
                "Email:", email,
                "Role:", role
        };

        int result = JOptionPane.showConfirmDialog(this, fields, data == null ? "Add Employee" : "Edit Employee", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                if (data == null) {
                    PreparedStatement ps = conn.prepareStatement("INSERT INTO EmployeeinfoTable (username,password,dob,aadhaar,address,phone,email,role) VALUES (?,?,?,?,?,?,?,?)");
                    ps.setString(1, username.getText());
                    ps.setString(2, password.getText());
                    ps.setString(3, dob.getText());
                    ps.setString(4, aadhaar.getText());
                    ps.setString(5, address.getText());
                    ps.setString(6, phone.getText());
                    ps.setString(7, email.getText());
                    ps.setString(8, role.getText());
                    ps.executeUpdate();
                } else {
                    PreparedStatement ps = conn.prepareStatement("UPDATE EmployeeinfoTable SET username=?,password=?,dob=?,aadhaar=?,address=?,phone=?,email=?,role=? WHERE id=?");
                    ps.setString(1, username.getText());
                    ps.setString(2, password.getText());
                    ps.setString(3, dob.getText());
                    ps.setString(4, aadhaar.getText());
                    ps.setString(5, address.getText());
                    ps.setString(6, phone.getText());
                    ps.setString(7, email.getText());
                    ps.setString(8, role.getText());
                    ps.setInt(9, (int) data[0]);
                    ps.executeUpdate();
                }
                loadEmployees();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void editSelectedEmployee() {
        int row = employeeTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a record to edit.");
            return;
        }
        Object[] data = new Object[employeeModel.getColumnCount()];
        for (int i = 0; i < data.length; i++) data[i] = employeeModel.getValueAt(row, i);
        showEmployeeForm(data);
    }

    private void deleteSelectedEmployee() {
        int row = employeeTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a record to delete.");
            return;
        }
        Object[] data = new Object[employeeModel.getColumnCount()];
        for (int i = 0; i < data.length; i++) data[i] = employeeModel.getValueAt(row, i);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete employee: " + data[1] + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM EmployeeinfoTable WHERE id=?");
                ps.setInt(1, (int) data[0]);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
                loadEmployees();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting employee: " + ex.getMessage());
            }
        }
    }

    // ================= MENU CRUD =================
    private void showMenuForm(Object[] data) {
        JTextField itemname = new JTextField(data != null ? data[1].toString() : "");
        JTextField category = new JTextField(data != null ? data[2].toString() : "");
        JTextField price = new JTextField(data != null ? data[3].toString() : "");

        Object[] fields = {"Item Name:", itemname, "Category:", category, "Price:", price};

        int result = JOptionPane.showConfirmDialog(this, fields, data == null ? "Add Menu Item" : "Edit Menu Item", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                if (data == null) {
                    PreparedStatement ps = conn.prepareStatement("INSERT INTO MenuTableinfo (itemname, category, price) VALUES (?, ?, ?)");
                    ps.setString(1, itemname.getText());
                    ps.setString(2, category.getText());
                    ps.setDouble(3, Double.parseDouble(price.getText()));
                    ps.executeUpdate();
                } else {
                    PreparedStatement ps = conn.prepareStatement("UPDATE MenuTableinfo SET itemname=?, category=?, price=? WHERE id=?");
                    ps.setString(1, itemname.getText());
                    ps.setString(2, category.getText());
                    ps.setDouble(3, Double.parseDouble(price.getText()));
                    ps.setInt(4, (int) data[0]);
                    ps.executeUpdate();
                }
                loadMenu();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void editSelectedMenu() {
        int row = menuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a menu item to edit.");
            return;
        }
        Object[] data = new Object[menuModel.getColumnCount()];
        for (int i = 0; i < data.length; i++) data[i] = menuModel.getValueAt(row, i);
        showMenuForm(data);
    }

    private void deleteSelectedMenu() {
        int row = menuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a menu item to delete.");
            return;
        }
        Object[] data = new Object[menuModel.getColumnCount()];
        for (int i = 0; i < data.length; i++) data[i] = menuModel.getValueAt(row, i);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete menu item: " + data[1] + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM MenuTableinfo WHERE id=?");
                ps.setInt(1, (int) data[0]);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Menu item deleted successfully.");
                loadMenu();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting menu item: " + ex.getMessage());
            }
        }
    }

    // ================= LOAD DATA =================
    private void loadEmployees() {
        try {
            employeeModel.setRowCount(0);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM EmployeeinfoTable ORDER BY id");
            while (rs.next()) {
                employeeModel.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("username"), rs.getString("password"),
                        rs.getDate("dob"), rs.getString("aadhaar"), rs.getString("address"),
                        rs.getString("phone"), rs.getString("email"), rs.getString("role")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadMenu() {
        try {
            menuModel.setRowCount(0);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM MenuTableinfo ORDER BY id");
            while (rs.next()) {
                menuModel.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("itemname"), rs.getString("category"), rs.getDouble("price")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadSales() {
        try {
            salesModel.setRowCount(0);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM SalesTable ORDER BY sale_date DESC");
            while (rs.next()) {
                salesModel.addRow(new Object[]{
                        rs.getString("item_name"), rs.getInt("quantity"), rs.getDouble("amount"), rs.getDate("sale_date")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ================= PRINT SALES REPORT =================
    private void printSalesReport() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(this);
            if (job.printDialog()) job.print();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) {
        if (pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g2 = (Graphics2D) g;
        g2.translate(pf.getImageableX(), pf.getImageableY());
        int y = 20;

        g2.setFont(new Font("Serif", Font.BOLD, 18));
        g2.drawString("Culinary Craft - Sales Report", 180, y);
        y += 20;

        g2.setFont(new Font("Serif", Font.PLAIN, 12));
        String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        g2.drawString("Generated On: " + datetime, 180, y);
        y += 30;

        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.drawString(String.format("%-25s %-10s %-10s %-15s", "Item Name", "Qty", "Amount", "Sale Date"), 50, y);
        y += 15;
        g2.drawLine(50, y, 500, y);
        y += 15;

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        int totalQty = 0;
        double totalAmount = 0.0;

        for (int i = 0; i < salesModel.getRowCount(); i++) {
            String line = String.format("%-25s %-10s %-10s %-15s",
                    salesModel.getValueAt(i, 0),
                    salesModel.getValueAt(i, 1),
                    salesModel.getValueAt(i, 2),
                    salesModel.getValueAt(i, 3));
            g2.drawString(line, 50, y);
            totalQty += Integer.parseInt(salesModel.getValueAt(i, 1).toString());
            totalAmount += Double.parseDouble(salesModel.getValueAt(i, 2).toString());
            y += 15;
        }

        y += 10;
        g2.drawLine(50, y, 500, y);
        y += 15;
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.drawString(String.format("%-25s %-10d %-10.2f", "TOTAL", totalQty, totalAmount), 50, y);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.drawString("Page: " + (pageIndex + 1), 450, (int) pf.getHeight() - 10);

        return PAGE_EXISTS;
    }

    // ================= EXPORT TO PDF =================
    private void exportSalesToPDF() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("SalesReport.pdf"));
        int option = chooser.showSaveDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) return;

        String filePath = chooser.getSelectedFile().getAbsolutePath();
        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(filePath));
            doc.open();

            Paragraph title = new Paragraph("Culinary Craft - Sales Report\n\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Paragraph datePara = new Paragraph("Generated On: " + datetime, FontFactory.getFont(FontFactory.HELVETICA, 12));
            datePara.setAlignment(Element.ALIGN_CENTER);
            doc.add(datePara);
            doc.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(salesModel.getColumnCount());
            table.setWidthPercentage(100);

            int totalQty = 0;
            double totalAmount = 0.0;

            for (int i = 0; i < salesModel.getColumnCount(); i++) {
                PdfPCell cell = new PdfPCell(new Phrase(salesModel.getColumnName(i)));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            for (int i = 0; i < salesModel.getRowCount(); i++) {
                for (int j = 0; j < salesModel.getColumnCount(); j++) {
                    table.addCell(String.valueOf(salesModel.getValueAt(i, j)));
                }
                totalQty += Integer.parseInt(salesModel.getValueAt(i, 1).toString());
                totalAmount += Double.parseDouble(salesModel.getValueAt(i, 2).toString());
            }

            PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL"));
            totalCell.setBackgroundColor(BaseColor.YELLOW);
            table.addCell(totalCell);
            table.addCell(new Phrase(String.valueOf(totalQty)));
            table.addCell(new Phrase(String.format("%.2f", totalAmount)));
            table.addCell(new Phrase(""));

            doc.add(table);
            doc.close();

            JOptionPane.showMessageDialog(this, "PDF saved to: " + filePath);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating PDF: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new AdminPanel();
    }
}
