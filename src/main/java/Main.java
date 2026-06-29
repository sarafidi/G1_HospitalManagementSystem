import util.DataStore;
import view.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        DataStore.getInstance().init();

        // reason: Swing is not thread-safe — all UI must be created on the EDT
        SwingUtilities.invokeLater(() -> new MainFrame());
    }

}