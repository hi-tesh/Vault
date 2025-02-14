import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class OpenFileListener implements ActionListener {
    private final File encryptedFile;

    public OpenFileListener(File encryptedFile) {
        this.encryptedFile = encryptedFile;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            File decryptedFile = FileHandler.decryptFile(encryptedFile);
            if (decryptedFile != null && decryptedFile.exists()) {
                Desktop.getDesktop().open(decryptedFile);
            } else {
                throw new IOException("Decrypted file does not exist.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
