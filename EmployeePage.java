package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EmployeePage extends JFrame implements ActionListener, Printable {

    Connection con;
    PreparedStatement pst;
    ResultSet rs;

    JPanel loginPanel, orderPanel;

    JTextField txtUser;
    JPasswordField txtPass;
    JButton btnLogin, btnClear;

    JTextField txtCustomer, txtQuantity;
    JButton btnAddItem, btnGenerateBill, btnLogout;
    JTable menuTable, orderTable;
    DefaultTableModel menuModel, orderModel;
    JLabel lblTotal;
    double totalAmount = 0;

    String billText = ""; // To hold bill for printing

    public EmployeePage() {
        connectDB();
        createLoginPanel();
    }

    private void connectDB() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:xe", "system", "root");
            System.out.println("✅ Connected to Database");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }
    }

    private void createLoginPanel() {
        loginPanel = new JPanel();
        loginPanel.setLayout(null);
        loginPanel.setBackground(new Color(240, 248, 255));

        JLabel lblTitle = new JLabel("Employee Login");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setBounds(120, 20, 200, 30);
        loginPanel.add(lblTitle);

        JLabel lblUser = new JLabel("Username:");
        lblUser.setBounds(70, 90, 100, 25);
        loginPanel.add(lblUser);

        txtUser = new JTextField();
        txtUser.setBounds(160, 90, 150, 25);
        loginPanel.add(txtUser);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setBounds(70, 130, 100, 25);
        loginPanel.add(lblPass);

        txtPass = new JPasswordField();
        txtPass.setBounds(160, 130, 150, 25);
        loginPanel.add(txtPass);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(80, 180, 100, 30);
        btnLogin.addActionListener(this);
        loginPanel.add(btnLogin);

        btnClear = new JButton("Clear");
        btnClear.setBounds(200, 180, 100, 30);
        btnClear.addActionListener(this);
        loginPanel.add(btnClear);

        setTitle("Employee Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(loginPanel);
        setVisible(true);
    }

    private void createOrderPanel() {
        orderPanel = new JPanel();
        orderPanel.setLayout(null);
        orderPanel.setBackground(new Color(255, 250, 240));

        JLabel lblTitle = new JLabel("Restaurant Order System");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setBounds(280, 10, 350, 30);
        orderPanel.add(lblTitle);

        JLabel lblCust = new JLabel("Customer Name:");
        lblCust.setBounds(50, 60, 150, 25);
        orderPanel.add(lblCust);

        txtCustomer = new JTextField();
        txtCustomer.setBounds(180, 60, 200, 25);
        orderPanel.add(txtCustomer);

        JLabel lblMenu = new JLabel("Menu Items");
        lblMenu.setFont(new Font("Arial", Font.BOLD, 16));
        lblMenu.setBounds(70, 100, 150, 25);
        orderPanel.add(lblMenu);

        menuModel = new DefaultTableModel(new String[]{"Item ID", "Item Name", "Category", "Price"}, 0);
        menuTable = new JTable(menuModel);
        JScrollPane menuScroll = new JScrollPane(menuTable);
        menuScroll.setBounds(50, 130, 400, 300);
        orderPanel.add(menuScroll);

        loadMenuFromDB();

        JLabel lblQty = new JLabel("Quantity:");
        lblQty.setBounds(480, 130, 100, 25);
        orderPanel.add(lblQty);

        txtQuantity = new JTextField();
        txtQuantity.setBounds(560, 130, 80, 25);
        orderPanel.add(txtQuantity);

        btnAddItem = new JButton("Add to Order");
        btnAddItem.setBounds(660, 130, 130, 25);
        btnAddItem.addActionListener(this);
        orderPanel.add(btnAddItem);

        orderModel = new DefaultTableModel(new String[]{"Item Name", "Qty", "Price", "Subtotal"}, 0);
        orderTable = new JTable(orderModel);
        JScrollPane orderScroll = new JScrollPane(orderTable);
        orderScroll.setBounds(480, 180, 400, 200);
        orderPanel.add(orderScroll);

        lblTotal = new JLabel("Total: ₹0.00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setBounds(700, 400, 200, 30);
        orderPanel.add(lblTotal);

        btnGenerateBill = new JButton("Generate & Print Bill");
        btnGenerateBill.setBounds(520, 450, 150, 35);
        btnGenerateBill.addActionListener(this);
        orderPanel.add(btnGenerateBill);

        btnLogout = new JButton("Logout");
        btnLogout.setBounds(690, 450, 120, 35);
        btnLogout.addActionListener(this);
        orderPanel.add(btnLogout);

        setTitle("Employee Order Page");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        remove(loginPanel);
        add(orderPanel);
        revalidate();
        repaint();
    }

    private void loadMenuFromDB() {
        try {
            menuModel.setRowCount(0);
            pst = con.prepareStatement("SELECT * FROM MenuTableinfo ORDER BY id");
            rs = pst.executeQuery();
            while (rs.next()) {
                menuModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("itemname"),
                        rs.getString("category"),
                        rs.getDouble("price")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading menu: " + e.getMessage());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnClear) {
            txtUser.setText("");
            txtPass.setText("");
        } else if (e.getSource() == btnLogin) {
            loginAction();
        } else if (e.getSource() == btnAddItem) {
            addItemToOrder();
        } else if (e.getSource() == btnGenerateBill) {
            generateAndPrintBill();
        } else if (e.getSource() == btnLogout) {
            logoutAction();
        }
    }

    private void loginAction() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword()).trim();
        try {
            pst = con.prepareStatement("SELECT * FROM EmployeeinfoTable WHERE username=? AND password=?");
            pst.setString(1, user);
            pst.setString(2, pass);
            rs = pst.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
                createOrderPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void addItemToOrder() {
        int row = menuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item!");
            return;
        }
        if (txtQuantity.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter quantity!");
            return;
        }

        String itemName = menuModel.getValueAt(row, 1).toString();
        double price = Double.parseDouble(menuModel.getValueAt(row, 3).toString());
        int qty = Integer.parseInt(txtQuantity.getText());
        double subtotal = price * qty;

        orderModel.addRow(new Object[]{itemName, qty, price, subtotal});
        totalAmount += subtotal;
        lblTotal.setText("Total: ₹" + totalAmount);
        txtQuantity.setText("");
    }

    private void generateAndPrintBill() {
        String customer = txtCustomer.getText().trim();
        if (customer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter customer name!");
            return;
        }
        if (orderModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No items in the order!");
            return;
        }

        try {
            String restaurantName = "Culinary Crafts";
            String orderDate = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());

            int billID = (int) (Math.random() * 100000);

            double gstRate = 0.05;
            double serviceRate = 0.10;
            double gst = totalAmount * gstRate;
            double service = totalAmount * serviceRate;
            double finalTotal = totalAmount + gst + service;

            // Insert into SalesTable
            for (int i = 0; i < orderModel.getRowCount(); i++) {
                String itemName = orderModel.getValueAt(i, 0).toString();
                int qty = Integer.parseInt(orderModel.getValueAt(i, 1).toString());
                double subtotal = Double.parseDouble(orderModel.getValueAt(i, 3).toString());

                pst = con.prepareStatement("INSERT INTO SalesTable(item_name, quantity, amount, sale_date) VALUES (?,?,?,?)");
                pst.setString(1, itemName);
                pst.setInt(2, qty);
                pst.setDouble(3, subtotal);
                pst.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                pst.executeUpdate();
            }

            // --- Bill Text ---
            StringBuilder bill = new StringBuilder();
            bill.append("\t\t").append(restaurantName).append("\n");
            bill.append("Bill ID: ").append(billID).append("\n");
            bill.append("Customer: ").append(customer).append("\n");
            bill.append("Date/Time: ").append(orderDate).append("\n");
            bill.append("----------------------------------------------------\n");
            bill.append(String.format("%-20s %-5s %-10s\n", "Item", "Qty", "Subtotal"));
            bill.append("----------------------------------------------------\n");
            for (int i = 0; i < orderModel.getRowCount(); i++) {
                String itemName = orderModel.getValueAt(i, 0).toString();
                int qty = Integer.parseInt(orderModel.getValueAt(i, 1).toString());
                double subtotal = Double.parseDouble(orderModel.getValueAt(i, 3).toString());
                bill.append(String.format("%-20s %-5d ₹%-10.2f\n", itemName, qty, subtotal));
            }
            bill.append("----------------------------------------------------\n");
            bill.append(String.format("Subtotal:\t\t₹%.2f\n", totalAmount));
            bill.append(String.format("GST (5%%):\t\t₹%.2f\n", gst));
            bill.append(String.format("Service (10%%):\t₹%.2f\n", service));
            bill.append(String.format("Total:\t\t₹%.2f\n", finalTotal));
            bill.append("----------------------------------------------------\n");
            bill.append("\tThank you for dining with us!\n");

            billText = bill.toString();

            JTextArea ta = new JTextArea(bill.toString());
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Bill", JOptionPane.INFORMATION_MESSAGE);

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(this);
            if (job.printDialog()) job.print();

            // Reset
            orderModel.setRowCount(0);
            totalAmount = 0;
            lblTotal.setText("Total: ₹0.00");
            txtCustomer.setText("");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // Printable interface
    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) {
        if (pageIndex > 0) return NO_SUCH_PAGE;
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        int y = 50;
        for (String line : billText.split("\n")) {
            g.drawString(line, 50, y);
            y += 15;
        }
        return PAGE_EXISTS;
    }

    private void logoutAction() {
        remove(orderPanel);
        createLoginPanel();
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        new EmployeePage();
    }
}
