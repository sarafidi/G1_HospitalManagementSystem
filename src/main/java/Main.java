import util.DataStore;
import view.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // TODO: initialise DataStore — must happen before UI starts
        // hint: DataStore.getInstance().init()
        DataStore.getInstance().init();

        // TODO: launch UI on the Event Dispatch Thread
        // hint: SwingUtilities.invokeLater(() -> new MainFrame())
        // reason: Swing is not thread-safe — all UI must be created on the EDT
        SwingUtilities.invokeLater(() -> new MainFrame());
    }

}