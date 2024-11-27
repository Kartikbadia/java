import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LibraryBookPurchaseGUI {

    private static final String[] books = {
            "Bhagavad Gita",
            "Ramayana",
            "Mahabharata",
            "Garud Puran",
            "Shiv Puran",
            "Ram Charit Manas"
    };

    private static final double[] prices = {150.0, 250.0, 350.0, 300.0, 200.0, 180.0};
    private static final double simplePackingCharge = 20.0;
    private static final double giftPackingCharge = 50.0;

    private static final String dbUrl = "jdbc:mysql://localhost:3306/LibraryDB";
    private static final String dbUser = "root";
    private static final String dbPassword = "password";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryBookPurchaseGUI().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Library Book Purchase");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("Welcome to the Library!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(welcomeLabel);

        JLabel bookLabel = new JLabel("Books Available:");
        bookLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bookLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(bookLabel);

        JComboBox<String> bookDropdown = new JComboBox<>(books);
        bookDropdown.setMaximumSize(new Dimension(300, 30));
        panel.add(bookDropdown);

        JLabel actionLabel = new JLabel("Choose an action:");
        actionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(actionLabel);

        JRadioButton purchaseRadio = new JRadioButton("Purchase");
        JRadioButton rentRadio = new JRadioButton("Rent");
        ButtonGroup actionGroup = new ButtonGroup();
        actionGroup.add(purchaseRadio);
        actionGroup.add(rentRadio);
        purchaseRadio.setSelected(true);
        panel.add(purchaseRadio);
        panel.add(rentRadio);

        JLabel packingLabel = new JLabel("Choose Packing Type:");
        packingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(packingLabel);

        JRadioButton simplePackingRadio = new JRadioButton("Simple Packing (₹20)");
        JRadioButton giftPackingRadio = new JRadioButton("Gift Packing (₹50)");
        ButtonGroup packingGroup = new ButtonGroup();
        packingGroup.add(simplePackingRadio);
        packingGroup.add(giftPackingRadio);
        simplePackingRadio.setSelected(true);
        panel.add(simplePackingRadio);
        panel.add(giftPackingRadio);

        JLabel paymentLabel = new JLabel("Choose Payment Method:");
        paymentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(paymentLabel);

        JRadioButton cashRadio = new JRadioButton("Cash");
        JRadioButton cardRadio = new JRadioButton("Card");
        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(cashRadio);
        paymentGroup.add(cardRadio);
        cashRadio.setSelected(true);
        panel.add(cashRadio);
        panel.add(cardRadio);

        JLabel nameLabel = new JLabel("Enter your name:");
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(nameLabel);

        JTextField nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(300, 30));
        panel.add(nameField);

        JButton submitButton = new JButton("Submit");
        panel.add(submitButton);

        JTextArea resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        panel.add(scrollPane);

        frame.add(panel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                String book = (String) bookDropdown.getSelectedItem();
                int bookIndex = bookDropdown.getSelectedIndex();
                boolean isPurchase = purchaseRadio.isSelected();
                boolean isSimplePacking = simplePackingRadio.isSelected();
                String paymentMethod = cashRadio.isSelected() ? "Cash" : "Card";
                String userName = nameField.getText().trim();

                if (userName.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter your name.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double bookPrice = prices[bookIndex];
                double totalCost;

                if (isPurchase) {
                    double packingCharge = isSimplePacking ? simplePackingCharge : giftPackingCharge;
                    totalCost = bookPrice + packingCharge;
                } else {
                    totalCost = bookPrice * 0.10;
                }

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO transactions (user_name, book_name, payment_mode, total_amount) VALUES (?, ?, ?, ?)")) {
                    pstmt.setString(1, userName);
                    pstmt.setString(2, book);
                    pstmt.setString(3, paymentMethod);
                    pstmt.setDouble(4, totalCost);
                    pstmt.executeUpdate();
                }

                resultArea.append(String.format("Transaction Successful:\nUser: %s\nBook: %s\nPayment: %s\nTotal: ₹%.2f\n\n",
                        userName, book, paymentMethod, totalCost));

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
