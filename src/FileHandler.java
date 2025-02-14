import java.io.*;
import java.nio.file.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import javax.swing.*;
import java.awt.GridLayout;

public class FileHandler {
    private static final String VAULT_DIR = "vault_data";
    private static final String PASSWORD_FILE = VAULT_DIR + "/password.txt";
    private static final String HINT_FILE = VAULT_DIR + "/hint.txt";

    public static void setupVault() {
        File vaultDir = new File(VAULT_DIR);
        if (!vaultDir.exists()) vaultDir.mkdir();

        File passwordFile = new File(PASSWORD_FILE);
        if (!passwordFile.exists()) {
            setNewPassword();
        }
    }

    public static boolean verifyPassword(String enteredPassword) {
        try {
            String savedHash = new String(Files.readAllBytes(Paths.get(PASSWORD_FILE))).trim();
            return hashPassword(enteredPassword).equals(savedHash);
        } catch (IOException e) {
            return false;
        }
    }

    public static String getHint() {
        try {
            return new String(Files.readAllBytes(Paths.get(HINT_FILE))).trim();
        } catch (IOException e) {
            return "No hint set.";
        }
    }

    public static void setNewPassword() {
        JPanel panel = new JPanel(new GridLayout(3, 1));
        JPasswordField passwordField = new JPasswordField();
        JTextField hintField = new JTextField();

        panel.add(new JLabel("Enter New Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Enter Password Hint:"));
        panel.add(hintField);

        int option = JOptionPane.showConfirmDialog(null, panel, "Set New Password", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newPassword = new String(passwordField.getPassword());
            String hint = hintField.getText();

            try {
                Files.write(Paths.get(PASSWORD_FILE), hashPassword(newPassword).getBytes());
                Files.write(Paths.get(HINT_FILE), hint.getBytes());
                JOptionPane.showMessageDialog(null, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error saving password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static boolean storeFile(File sourceFile) {
        File encryptedFile = new File(VAULT_DIR, sourceFile.getName() + ".enc");

        try {
            byte[] fileBytes = Files.readAllBytes(sourceFile.toPath());
            byte[] encryptedBytes = encrypt(fileBytes, getKey());

            Files.write(encryptedFile.toPath(), encryptedBytes);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error encrypting file!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static File decryptFile(File encryptedFile) {
        try {
            byte[] encryptedBytes = Files.readAllBytes(encryptedFile.toPath());
            byte[] decryptedBytes = decrypt(encryptedBytes, getKey());

            File tempFile = File.createTempFile("vault_decrypted_", "_" + encryptedFile.getName().replace(".enc", ""));
            Files.write(tempFile.toPath(), decryptedBytes);

            return tempFile;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to decrypt the file. It may be corrupted or tampered with.", "Decryption Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static boolean deleteFile(File file) {
        return file.delete();
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "";
        }
    }

    private static byte[] getKey() throws NoSuchAlgorithmException {
        String password = new String(Base64.getDecoder().decode(hashPassword("defaultKey"))); // Default key
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        return sha.digest(password.getBytes());
    }

    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
}