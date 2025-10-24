package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Homepage extends JFrame implements ActionListener {
    ImageIcon img;
    JLabel backgroundLabel, titleLabel;
    JButton adminButton, employeeButton;

    public Homepage() {
        // Load background image
        img = new ImageIcon("C:\\Users\\gayat\\Downloads\\Restimg7.jpeg");

        // Scale the image to fit frame
        Image scaledImg = img.getImage().getScaledInstance(1835, 900, Image.SCALE_SMOOTH);
        img = new ImageIcon(scaledImg);

        backgroundLabel = new JLabel(img);
        backgroundLabel.setBounds(0, 0, 1835, 900);
        backgroundLabel.setLayout(null);

        // Title
        titleLabel = new JLabel("Culinary Crafts", JLabel.CENTER);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 60));
        titleLabel.setForeground(new Color(255, 244, 230));

        // Buttons
        adminButton = new JButton("Admin");
        employeeButton = new JButton("Employee");

        adminButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        employeeButton.setFont(new Font("SansSerif", Font.BOLD, 18));

        // Aesthetic colors
        Color warmBrown = new Color(121, 85, 72);  // elegant earthy brown
        Color golden = new Color(255, 193, 7);     // rich gold accent
        Color hoverBrown = new Color(101, 67, 56);
        Color hoverGold = new Color(255, 160, 0);

        styleButton(adminButton, warmBrown);
        styleButton(employeeButton, golden);

        // Hover effects
        adminButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { adminButton.setBackground(hoverBrown); }
            public void mouseExited(MouseEvent e) { adminButton.setBackground(warmBrown); }
        });

        employeeButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { employeeButton.setBackground(hoverGold); }
            public void mouseExited(MouseEvent e) { employeeButton.setBackground(golden); }
        });

        // Add components
        backgroundLabel.add(titleLabel);
        backgroundLabel.add(adminButton);
        backgroundLabel.add(employeeButton);
        add(backgroundLabel);

        // Center elements dynamically
        centerComponents();

        // Action listeners
        adminButton.addActionListener(this);
        employeeButton.addActionListener(this);

        // Frame setup
        setTitle("Culinary Crafts");
        setSize(1835, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // ✨ Center all components visually
    private void centerComponents() {
        int frameWidth = 1835;
        int frameHeight = 900;

        int titleWidth = 700;
        int titleHeight = 80;
        int buttonWidth = 220;
        int buttonHeight = 50;
        int spacing = 20;

        // Center X
        int centerX = (frameWidth - buttonWidth) / 2;

        // Center Y for buttons
        int centerY = (frameHeight / 2) - 50;

        // Title position above buttons
        titleLabel.setBounds((frameWidth - titleWidth) / 2, centerY - 150, titleWidth, titleHeight);

        adminButton.setBounds(centerX, centerY, buttonWidth, buttonHeight);
        employeeButton.setBounds(centerX, centerY + buttonHeight + spacing, buttonWidth, buttonHeight);
    }

    // ✨ Button styling
    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(new RoundedBorder(30));
    }

    // Rounded border class
    static class RoundedBorder implements javax.swing.border.Border {
        private int radius;
        RoundedBorder(int radius) { this.radius = radius; }
        public Insets getBorderInsets(Component c) { return new Insets(radius + 1, radius + 1, radius + 2, radius); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource().equals(adminButton)) {
            AdminPanel a = new AdminPanel();
            a.setTitle("Admin Panel");
            a.setSize(1000, 700);
            a.setVisible(true);
        }
        if (ae.getSource().equals(employeeButton)) {
            EmployeePage e = new EmployeePage();
            e.setTitle("Employee Login Page");
            e.setSize(1000, 700);
            e.setVisible(true);
        }
    }

    public static void main(String[] args) {
        new Homepage();
    }
}
