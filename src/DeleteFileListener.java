import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class DeleteFileListener implements ActionListener {
    private final File file;

    public DeleteFileListener(File file) {
        this.file = file;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int confirmation = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete this file?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            if (file.delete()) {
                JOptionPane.showMessageDialog(null, "File deleted successfully.");
                SwingUtilities.invokeLater(() -> VaultGUI.refreshFileGrid());
            } else {
                JOptionPane.showMessageDialog(null, "Failed to delete file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
