import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;

public class VaultGUI {
    private static JFrame frame;
    private static JPanel fileGridPanel;
    private static JScrollPane scrollPane;
    private static ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileHandler.setupVault();
            showPasswordScreen();
        });
    }

    private static void showPasswordScreen() {
        JFrame passwordFrame = new JFrame("Vault - Enter Password");
        passwordFrame.setSize(420, 280);
        passwordFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        passwordFrame.setLocationRelativeTo(null);
        passwordFrame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 25));

        JLabel titleLabel = new JLabel("Enter Your Vault Password");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        passwordField.setMaximumSize(new Dimension(250, 40));

        JButton enterButton = new JButton("Unlock Vault");
        styleButton(enterButton, new Color(60, 179, 113));
        enterButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hintLabel = new JLabel("Hint: " + FileHandler.getHint());
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        enterButton.addActionListener(e -> {
            String enteredPassword = new String(passwordField.getPassword());
            if (FileHandler.verifyPassword(enteredPassword)) {
                passwordFrame.dispose();
                SwingUtilities.invokeLater(VaultGUI::showVault);
            } else {
                JOptionPane.showMessageDialog(passwordFrame, "Incorrect password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(Box.createVerticalStrut(30));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(enterButton);
        panel.add(Box.createVerticalStrut(15));
        panel.add(hintLabel);
        panel.add(Box.createVerticalStrut(30));

        passwordFrame.add(panel, BorderLayout.CENTER);
        passwordFrame.setVisible(true);
    }

    private static void showVault() {
        frame = new JFrame("Secure Vault");
        frame.setSize(620, 520);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(35, 35, 35));
        topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton addButton = new JButton("+ Add Files");
        styleButton(addButton, new Color(70, 130, 180));
        addButton.addActionListener(e -> openFileChooser());

        topPanel.add(addButton);
        frame.add(topPanel, BorderLayout.NORTH);

        fileGridPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        fileGridPanel.setBackground(new Color(40, 40, 40));
        scrollPane = new JScrollPane(fileGridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        refreshFileGrid();
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter("All Files", "*.*"));

        int returnValue = fileChooser.showOpenDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            executor.execute(() -> {
                for (File file : selectedFiles) {
                    if (FileHandler.storeFile(file)) {
                        SwingUtilities.invokeLater(VaultGUI::refreshFileGrid);
                    }
                }
            });
        }
    }

    public static void refreshFileGrid() {
        fileGridPanel.removeAll();
        File vaultDir = new File("vault_data");
        File[] files = vaultDir.listFiles((dir, name) -> name.endsWith(".enc"));

        if (files != null) {
            for (File file : files) {
                JPanel filePanel = new JPanel();
                filePanel.setLayout(new BorderLayout());
                filePanel.setBackground(new Color(50, 50, 50));
                filePanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

                JLabel fileLabel = new JLabel(file.getName().replace(".enc", ""), SwingConstants.CENTER);
                fileLabel.setForeground(Color.WHITE);

                ImageIcon thumbnail = getThumbnail(file);
                JLabel thumbnailLabel = new JLabel(thumbnail, SwingConstants.CENTER);

                JButton openButton = new JButton("Open");
                styleButton(openButton, new Color(34, 139, 34));
                openButton.addActionListener(new OpenFileListener(file));

                JButton deleteButton = new JButton("Delete");
                styleButton(deleteButton, new Color(178, 34, 34));
                deleteButton.addActionListener(new DeleteFileListener(file));

                JPanel buttonPanel = new JPanel(new FlowLayout());
                buttonPanel.setBackground(new Color(50, 50, 50));
                buttonPanel.add(openButton);
                buttonPanel.add(deleteButton);

                filePanel.add(thumbnailLabel, BorderLayout.CENTER);
                filePanel.add(fileLabel, BorderLayout.NORTH);
                filePanel.add(buttonPanel, BorderLayout.SOUTH);

                fileGridPanel.add(filePanel);
            }
        }

        fileGridPanel.revalidate();
        fileGridPanel.repaint();
    }

    private static ImageIcon getThumbnail(File encryptedFile) {
        try {
            File decryptedFile = FileHandler.decryptFile(encryptedFile);
            BufferedImage img = ImageIO.read(decryptedFile);
            if (img != null) {
                Image scaledImage = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception ignored) {
        }
        return new ImageIcon();
    }

    private static void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}